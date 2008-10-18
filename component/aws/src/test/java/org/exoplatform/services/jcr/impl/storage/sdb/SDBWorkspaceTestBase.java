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

import javax.jcr.PropertyType;
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
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.storage.value.StandaloneStoragePluginProvider;
import org.exoplatform.services.jcr.storage.value.ValueStoragePluginProvider;
import org.exoplatform.services.jcr.util.SIDGenerator;
import org.exoplatform.services.log.ExoLogger;

import com.amazonaws.sdb.AmazonSimpleDB;
import com.amazonaws.sdb.AmazonSimpleDBClient;
import com.amazonaws.sdb.AmazonSimpleDBConfig;
import com.amazonaws.sdb.AmazonSimpleDBException;
import com.amazonaws.sdb.model.DeleteDomainRequest;
import com.amazonaws.sdb.model.GetAttributesRequest;
import com.amazonaws.sdb.model.GetAttributesResponse;
import com.amazonaws.sdb.model.ListDomainsRequest;
import com.amazonaws.sdb.model.ListDomainsResponse;
import com.amazonaws.sdb.model.ListDomainsResult;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 13.10.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
abstract class SDBWorkspaceTestBase extends TestCase {

  /**
   * Test logger.
   */
  protected static final Log              LOG                      = ExoLogger.getLogger("jcr.JCRAWSTest");

  /**
   * Default workspace container name.
   */
  protected static final String           WORKSPACE_CONTAINER_NAME = "ws-test";

  /**
   * Max buff size.
   */
  protected static final int              MAX_BUFF_SIZE            = 200 * 1024;

  /**
   * SDB domain name.
   */
  protected static final String           SDB_DOMAIN_NAME          = WORKSPACE_CONTAINER_NAME
                                                                       + "-domain";

  /**
   * JCR SDB storage version.
   */
  protected static final String           TEST_STORAGE_VERSION     = "1.0-test";

  /**
   * SimpleDB client (Amazon stuff). For checks.
   */
  protected AmazonSimpleDB                sdbClient;

  /**
   * eXo JCR SimpleDB connection.
   */
  protected SDBWorkspaceStorageConnection sdbConn;

  /**
   * jcrRoot.
   */
  protected NodeData                      jcrRoot;

  /**
   * testNode.
   */
  protected NodeData                      testNode;

  /**
   * testNodeProperty.
   */
  protected PropertyData                  testNodeProperty;

  /**
   * testRoot.
   */
  protected NodeData                      testRoot;

  /**
   * testProperty.
   */
  protected PropertyData                  testProperty;

  /**
   * test Property multivalued.
   */
  protected PropertyData                  testMultivaluedProperty;

  /**
   * test binary Property.
   */
  protected PropertyData                  testBinaryProperty;

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

      // check if test donain exists
      String nextToken = null;
      do {
        ListDomainsResponse resp = sdbClient.listDomains(new ListDomainsRequest(20, nextToken));
        if (resp.isSetListDomainsResult()) {
          ListDomainsResult res = resp.getListDomainsResult();
          List<String> domainNamesList = res.getDomainName();
          if (domainNamesList.contains(SDB_DOMAIN_NAME)) {
            // delete domain
            sdbClient.deleteDomain(new DeleteDomainRequest(SDB_DOMAIN_NAME));
            Thread.sleep(SDBWorkspaceStorageConnection.SDB_OPERATION_TIMEOUT);
            break;
          }

          nextToken = res.getNextToken();
        }
      } while (nextToken != null);
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
    jcrRoot = new TransientNodeData(QPath.parse("[]:1"),
                                    Constants.ROOT_UUID,
                                    1,
                                    Constants.NT_UNSTRUCTURED,
                                    new InternalQName[] {},
                                    1,
                                    null,
                                    new AccessControlList());

    testNode = new TransientNodeData(QPath.makeChildPath(jcrRoot.getQPath(),
                                                         QPathEntry.parse("[]sdbTestNode:1")),
                                     SIDGenerator.generate(),
                                     1,
                                     Constants.NT_FOLDER,
                                     new InternalQName[] {},
                                     1,
                                     jcrRoot.getIdentifier(),
                                     null);

    TransientPropertyData testNodeProperty = new TransientPropertyData(QPath.makeChildPath(testNode.getQPath(),
                                                                                           QPathEntry.parse("[]sdbTestNode#Property:1")),
                                                                       SIDGenerator.generate(),
                                                                       1,
                                                                       PropertyType.STRING,
                                                                       testNode.getIdentifier(),
                                                                       false);
    List<ValueData> values = new ArrayList<ValueData>(1);
    values.add(new TransientValueData("This is a text property of testNode."));
    testNodeProperty.setValues(values);
    this.testNodeProperty = testNodeProperty;

    testRoot = new TransientNodeData(QPath.makeChildPath(jcrRoot.getQPath(),
                                                         QPathEntry.parse("[]sdbTestRoot:1")),
                                     SIDGenerator.generate(),
                                     1,
                                     Constants.NT_FILE,
                                     new InternalQName[] { Constants.MIX_REFERENCEABLE },
                                     1,
                                     jcrRoot.getIdentifier(),
                                     null);

    TransientPropertyData testProperty = new TransientPropertyData(QPath.makeChildPath(testRoot.getQPath(),
                                                                                       QPathEntry.parse("[]sdbTestProperty:1")),
                                                                   SIDGenerator.generate(),
                                                                   1,
                                                                   PropertyType.STRING,
                                                                   testRoot.getIdentifier(),
                                                                   false);
    values = new ArrayList<ValueData>(1);
    values.add(new TransientValueData("This is a text property. Have a nice day, SDB."));
    testProperty.setValues(values);
    this.testProperty = testProperty;

    TransientPropertyData testBinaryProperty = new TransientPropertyData(QPath.makeChildPath(testRoot.getQPath(),
                                                                                             QPathEntry.parse("[]sdbTest@Binary Property:1")),
                                                                         SIDGenerator.generate(),
                                                                         1,
                                                                         PropertyType.BINARY,
                                                                         testRoot.getIdentifier(),
                                                                         false);

    values = new ArrayList<ValueData>(1);
    values.add(new TransientValueData(getClass().getClassLoader()
                                                .getResourceAsStream("images/button-sample.gif")));
    testBinaryProperty.setValues(values);
    this.testBinaryProperty = testBinaryProperty;

    TransientPropertyData testMultivaluedProperty = new TransientPropertyData(QPath.makeChildPath(testRoot.getQPath(),
                                                                                                  QPathEntry.parse("[]sdbTestProperty multivalued:1")),
                                                                              SIDGenerator.generate(),
                                                                              1,
                                                                              PropertyType.STRING,
                                                                              testRoot.getIdentifier(),
                                                                              true);
    values = new ArrayList<ValueData>(5);
    values.add(new TransientValueData("This is a text value #1"));
    values.add(new TransientValueData("This is a text value #2"));
    values.add(new TransientValueData("This is a text value #3"));
    values.add(new TransientValueData("This is a text value #4"));
    values.add(new TransientValueData("This is a text value #5"));
    testMultivaluedProperty.setValues(values);
    this.testMultivaluedProperty = testMultivaluedProperty;

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void tearDown() throws Exception {
    try {
      sdbClient.deleteDomain(new DeleteDomainRequest(SDB_DOMAIN_NAME));

      // wait for SDB
      Thread.sleep(SDBWorkspaceStorageConnection.SDB_OPERATION_TIMEOUT);
    } catch (Throwable e) {
      LOG.error("teardown error", e);
      fail(e.getMessage());
    }

    super.tearDown();
  }

}
