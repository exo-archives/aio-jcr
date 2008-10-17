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

import javax.jcr.ItemExistsException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.util.SIDGenerator;

import com.amazonaws.sdb.AmazonSimpleDBException;
import com.amazonaws.sdb.model.Attribute;
import com.amazonaws.sdb.model.GetAttributesResponse;
import com.amazonaws.sdb.model.GetAttributesResult;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 13.10.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class SDBWorkspaceDataContainerTest extends SDBWorkspaceTestBase {

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
   * Test of container delete cleaner.
   * 
   * @throws Exception
   *           error
   */
  public void testContainerCleaner() throws Exception {

    // prepare
    sdbConn.add(jcrRoot); // root
    sdbConn.add(testRoot); // parent

    sdbConn.add(testProperty);

    final int nodesCount = 10;
    final String[] ids = new String[nodesCount];
    for (int i = 0; i < nodesCount; i++) {
      sdbConn.add(new TransientNodeData(QPath.makeChildPath(jcrRoot.getQPath(),
                                                            QPathEntry.parse("[]node" + i + ":1")),
                                        ids[i] = SIDGenerator.generate(),
                                        1,
                                        Constants.NT_UNSTRUCTURED,
                                        new InternalQName[] { Constants.MIX_REFERENCEABLE },
                                        1,
                                        jcrRoot.getIdentifier(),
                                        jcrRoot.getACL()));
    }

    sdbConn.commit();

    // delete
    TransientPropertyData deletedProp = new TransientPropertyData(testProperty.getQPath(),
                                                                  testProperty.getIdentifier(),
                                                                  2,
                                                                  PropertyType.STRING,
                                                                  testProperty.getParentIdentifier(),
                                                                  true);
    TransientNodeData deletedNode = new TransientNodeData(testRoot.getQPath(),
                                                          testRoot.getIdentifier(),
                                                          2,
                                                          Constants.NT_FILE,
                                                          new InternalQName[] { Constants.MIX_REFERENCEABLE },
                                                          2,
                                                          testRoot.getParentIdentifier(),
                                                          testRoot.getACL());
    try {
      sdbConn.delete(deletedProp);
      sdbConn.delete(deletedNode);

      for (int i = 0; i < nodesCount; i++) {
        sdbConn.delete(new TransientNodeData(QPath.makeChildPath(jcrRoot.getQPath(),
                                                              QPathEntry.parse("[]node" + i + ":1")),
                                          ids[i],
                                          2,
                                          Constants.NT_UNSTRUCTURED,
                                          new InternalQName[] { Constants.MIX_REFERENCEABLE },
                                          1,
                                          jcrRoot.getIdentifier(),
                                          jcrRoot.getACL()));
      }
      sdbConn.commit();
    } catch (ItemExistsException e) {
      LOG.error("delete Property error", e);
      fail(e.getMessage());
    } catch (RepositoryException e) {
      LOG.error("delete Property error", e);
      fail(e.getMessage());
    }

    // cleaner
    final int timeoutPerItem = 700;
    final int timeoutCleaner = 1000;
    StorageCleaner cleaner = new StorageCleaner("test", sdbConn, timeoutCleaner);
    try {
      cleaner.start();
  
      Thread.sleep(timeoutCleaner + (timeoutPerItem * nodesCount)); // wait for SDB too here
  
      // check
      GetAttributesResponse resp = readItem(sdbClient, SDB_DOMAIN_NAME, deletedProp.getIdentifier());
      //readItem(sdbClient, SDB_DOMAIN_NAME, jcrRoot.getIdentifier()).getGetAttributesResult().getAttribute()
      if (resp.isSetGetAttributesResult()) {
        GetAttributesResult res = resp.getGetAttributesResult();
        assertTrue("Property should be actually deleted", res.getAttribute().size() == 0);
      } else
        fail("Not initialized");
  
      resp = readItem(sdbClient, SDB_DOMAIN_NAME, deletedNode.getIdentifier());
  
      if (resp.isSetGetAttributesResult()) {
        GetAttributesResult res = resp.getGetAttributesResult();
        assertTrue("Node should be actually deleted", res.getAttribute().size() == 0);
      } else
        fail("Not initialized");
      
    } finally {
      cleaner.cancel();
    }
  }
}
