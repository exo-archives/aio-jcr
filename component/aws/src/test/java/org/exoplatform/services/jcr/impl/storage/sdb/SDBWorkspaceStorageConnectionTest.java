/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.impl.storage.sdb;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.ItemExistsException;
import javax.jcr.RepositoryException;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.config.AccessManagerEntry;
import org.exoplatform.services.jcr.config.CacheEntry;
import org.exoplatform.services.jcr.config.ContainerEntry;
import org.exoplatform.services.jcr.config.LockManagerEntry;
import org.exoplatform.services.jcr.config.QueryHandlerEntry;
import org.exoplatform.services.jcr.config.SimpleParameterEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.dataflow.persistent.PersistedNodeData;
import org.exoplatform.services.jcr.dataflow.persistent.PersistedPropertyData;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.persistent.ByteArrayPersistedValueData;
import org.exoplatform.services.jcr.impl.storage.value.StandaloneStoragePluginProvider;
import org.exoplatform.services.jcr.storage.value.ValueStoragePluginProvider;
import org.exoplatform.services.jcr.util.SIDGenerator;
import org.exoplatform.services.log.ExoLogger;

import com.amazonaws.sdb.AmazonSimpleDB;
import com.amazonaws.sdb.AmazonSimpleDBClient;
import com.amazonaws.sdb.AmazonSimpleDBConfig;
import com.amazonaws.sdb.AmazonSimpleDBException;
import com.amazonaws.sdb.model.Attribute;
import com.amazonaws.sdb.model.DeleteAttributesRequest;
import com.amazonaws.sdb.model.DeleteDomainRequest;
import com.amazonaws.sdb.model.GetAttributesRequest;
import com.amazonaws.sdb.model.GetAttributesResponse;
import com.amazonaws.sdb.model.GetAttributesResult;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 13.10.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: SDBWorkspaceStorageConnectionTest.java 111 2008-11-11 11:11:11Z pnedonosko $
 */
public class SDBWorkspaceStorageConnectionTest extends TestCase {

  /**
   * Test logger.
   */
  protected static final Log            LOG                      = ExoLogger.getLogger("jcr.JCRAWSTest");

  /**
   * Default workspace container name.
   */
  private static final String           WORKSPACE_CONTAINER_NAME = "ws-aws-test";

  /**
   * Max buff size.
   */
  private static final int              MAX_BUFF_SIZE            = 200 * 1024;

  /**
   * SDB domain name.
   */
  private static final String           SDB_DOMAIN_NAME          = "exo-aws-test-ws";

  /**
   * JCR SDB storage version.
   */
  private static final String           TEST_STORAGE_VERSION     = "1.0-test";

  /**
   * SimpleDB client (Amazon stuff). For checks.
   */
  private AmazonSimpleDB                sdbClient;

  /**
   * eXo JCR SimpleDB connection.
   */
  private SDBWorkspaceStorageConnection sdbConn;

  /**
   * jcrRoot.
   */
  private NodeData                      jcrRoot;

  /**
   * testRoot.
   */
  private NodeData                      testRoot;

  /**
   * testProperty.
   */
  private PersistedPropertyData         testProperty;

  /**
   * Make dummy WorkspaceEntry.
   * 
   * @return WorkspaceEntry
   */
  protected WorkspaceEntry makeWorkspaceEntry() {

    WorkspaceEntry wse = new WorkspaceEntry();
    wse.setName(WORKSPACE_CONTAINER_NAME);
    wse.setUniqueName(WORKSPACE_CONTAINER_NAME);

    wse.setAccessManager(new AccessManagerEntry());
    wse.setCache(new CacheEntry());

    ArrayList<SimpleParameterEntry> contParams = new ArrayList<SimpleParameterEntry>();
    // contParams.add(new SimpleParameterEntry("max-buffer-size", "200k"));
    // contParams.add(new SimpleParameterEntry("swap-directory", "target/temp/swap/ws"));
    // contParams.add(new SimpleParameterEntry("aws-access-key", "xxx"));
    // contParams.add(new SimpleParameterEntry("aws-access-secret-key", "xxx"));
    // contParams.add(new SimpleParameterEntry("domain-name", "exo-aws-test-ws"));

    ContainerEntry conte = new ContainerEntry("dummyType", contParams);
    conte.setValueStorages(null);

    wse.setContainer(conte);

    wse.setLockManager(new LockManagerEntry());

    wse.setQueryHandler(new QueryHandlerEntry());

    return wse;
  }

  /**
   * Read SDB Item.
   * 
   * @param service
   *          SDB service
   * @param domainName
   *          SDB domain name
   * @param item
   *          item name
   * @return GetAttributesResponse
   * @throws AmazonSimpleDBException
   *           in case of SDB error
   */
  protected GetAttributesResponse readItem(AmazonSimpleDB service, String domainName, String item) throws AmazonSimpleDBException {
    GetAttributesRequest request = new GetAttributesRequest().withDomainName(domainName)
                                                             .withItemName(item);

    return service.getAttributes(request);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void setUp() throws Exception {

    String myAccessKey = System.getProperty("exo.aws.access.key");
    String mySecretKey = System.getProperty("exo.aws.secret.key");

    if (myAccessKey == null || myAccessKey.length() <= 0)
      fail("Access key required.");

    if (mySecretKey == null || mySecretKey.length() <= 0)
      fail("Secret key required.");

    // Amazon SDB client
    try {
      AmazonSimpleDBConfig config = new AmazonSimpleDBConfig();
      config.setSignatureVersion("0");

      sdbClient = new AmazonSimpleDBClient(myAccessKey, mySecretKey, config);
    } catch (Throwable e) {
      LOG.error("setup error", e);
      fail(e.getMessage());
    }

    // eXo JCR SDB Container
    ValueStoragePluginProvider vsp = new StandaloneStoragePluginProvider(makeWorkspaceEntry());
    sdbConn = new SDBWorkspaceStorageConnection(myAccessKey,
                                                mySecretKey,
                                                SDB_DOMAIN_NAME,
                                                MAX_BUFF_SIZE,
                                                vsp);

    try {
      String userVersion = sdbConn.initStorage(WORKSPACE_CONTAINER_NAME, TEST_STORAGE_VERSION);

      assertEquals("Storage versions should be same", TEST_STORAGE_VERSION, userVersion);

    } catch (RepositoryException e) {
      LOG.error("init error", e);
      fail(e.getMessage());
    }

    // test items

    AccessControlList acl;
    jcrRoot = new PersistedNodeData(Constants.ROOT_UUID,
                                    QPath.parse("[]:1"),
                                    Constants.ROOT_PARENT_UUID,
                                    1,
                                    1,
                                    Constants.NT_UNSTRUCTURED,
                                    new InternalQName[] {},
                                    acl = new AccessControlList());

    testRoot = new PersistedNodeData(SIDGenerator.generate(),
                                     QPath.makeChildPath(jcrRoot.getQPath(),
                                                         QPathEntry.parse("[]sdbTestRoot:1")),
                                     jcrRoot.getIdentifier(),
                                     1,
                                     1,
                                     Constants.NT_FILE,
                                     new InternalQName[] { Constants.MIX_REFERENCEABLE },
                                     acl);

    testProperty = new PersistedPropertyData(SIDGenerator.generate(),
                                             QPath.makeChildPath(testRoot.getQPath(),
                                                                 QPathEntry.parse("[]sdbTestProperty:1")),
                                             testRoot.getIdentifier(),
                                             1,
                                             1,
                                             false);
    List<ValueData> values = new ArrayList<ValueData>(1);
    values.add(new ByteArrayPersistedValueData("1234567890qwerty".getBytes(), 1));
    testProperty.setValues(values);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void tearDown() throws Exception {
    try {
      DeleteDomainRequest request = new DeleteDomainRequest(SDB_DOMAIN_NAME);
      //DeleteAttributesRequest request = new DeleteAttributesRequest().withDomainName(SDB_DOMAIN_NAME).withItemName(jcrRoot.getIdentifier());
      sdbClient.deleteDomain(request);
    } catch (Throwable e) {
      LOG.error("teardown error", e);
      fail(e.getMessage());
    }

    super.tearDown();
  }

  /**
   * Test InitStorage procedure.
   * 
   * @throws AmazonSimpleDBException
   *           - SDB error
   * 
   */
  public void testInitStorage() throws AmazonSimpleDBException {

    // check
    GetAttributesResponse resp = readItem(sdbClient,
                                          SDB_DOMAIN_NAME,
                                          SDBWorkspaceStorageConnection.STORAGE_VERSION_ID);

    if (resp.isSetGetAttributesResult()) {
      GetAttributesResult res = resp.getGetAttributesResult();
      String sdbVersion = null;
      String sdbContainer = null;
      for (Attribute attr : res.getAttribute()) {
        if (attr.getName().equals(SDBWorkspaceStorageConnection.STORAGE_VERSION))
          sdbVersion = attr.getValue();
        else if (attr.getName().equals(SDBWorkspaceStorageConnection.STORAGE_CONTAINER_NAME))
          sdbContainer = attr.getValue();
      }

      assertEquals("Storage version should match", TEST_STORAGE_VERSION, sdbVersion);
      assertEquals("Storage name should match", WORKSPACE_CONTAINER_NAME, sdbContainer);
    } else
      fail("Not initialized");
  }

  /**
   * Test add item.
   * 
   * @throws AmazonSimpleDBException
   *           - SDB error
   */
  public void testAddNode() throws AmazonSimpleDBException {

    try {
      sdbConn.add(jcrRoot);
      sdbConn.commit();
    } catch (ItemExistsException e) {
      LOG.error("add Node error", e);
      fail(e.getMessage());
    } catch (RepositoryException e) {
      LOG.error("add Node error", e);
      fail(e.getMessage());
    }

    // check
    GetAttributesResponse resp = readItem(sdbClient, SDB_DOMAIN_NAME, jcrRoot.getIdentifier());

    if (resp.isSetGetAttributesResult()) {
      GetAttributesResult res = resp.getGetAttributesResult();
      String id = null;
      String pid = null;
      String name = null;
      String iclass = null;
      String idata = null;
      for (Attribute attr : res.getAttribute()) {
        if (attr.getName().equals(SDBConstants.ID))
          id = attr.getValue();
        else if (attr.getName().equals(SDBConstants.PID))
          pid = attr.getValue();
        else if (attr.getName().equals(SDBConstants.NAME))
          name = attr.getValue();
        else if (attr.getName().equals(SDBConstants.ICLASS))
          iclass = attr.getValue();
        else if (attr.getName().equals(SDBConstants.IDATA))
          idata = attr.getValue();
      }

      assertEquals("Id doesn't match", jcrRoot.getIdentifier(), id);
      assertEquals("Parent id doesn't match", jcrRoot.getParentIdentifier(), pid);
      assertEquals("Name doesn't match", jcrRoot.getQPath().getEntries()[jcrRoot.getQPath().getEntries().length - 1].getAsString(true), name);
    } else
      fail("Not initialized");
  }
}
