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
package org.exoplatform.services.jcr.aws.storage.sdb;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;
import javax.jcr.PropertyType;

import org.exoplatform.services.jcr.aws.storage.sdb.SDBConstants;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.util.SIDGenerator;

import com.amazonaws.sdb.model.Attribute;
import com.amazonaws.sdb.model.GetAttributesResponse;
import com.amazonaws.sdb.model.GetAttributesResult;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date: 13.10.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: SDBWorkspaceStorageConnectionConstraintsTest.java 21434 2008-10-15 21:57:24Z
 *          pnedonosko $
 */
public class SDBWorkspaceStorageConnectionConstraintsTest extends SDBWorkspaceTestBase {

  /**
   * Test if add node will fails on save without parent in Repository.
   * 
   * @throws Exception
   *           - error
   */
  public void testParentNotFound() throws Exception {

    try {
      sdbConn.add(testRoot);
      sdbConn.commit();
    } catch (InvalidItemStateException e) {
      if (e.getMessage().indexOf("parent not found") < 0) {
        LOG.error("add Node error", e);
        fail(e.getMessage());
      }
    }

    // check
    GetAttributesResponse resp = readItem(sdbClient, SDB_DOMAIN_NAME, testRoot.getIdentifier());

    if (resp.isSetGetAttributesResult()) {
      GetAttributesResult res = resp.getGetAttributesResult();
      assertTrue("Node should not be saved", res.getAttribute().size() <= 0);
    } else
      fail("Not a result");
  }

  /**
   * Test if save of Item will fails on Workspace containing a newer version of the Item.
   * 
   * @throws Exception
   *           - SDB error
   */
  public void nontestInvalidItemState() throws Exception {

    // TODO check InvalidItemState logic in connection

    sdbConn.add(jcrRoot);
    sdbConn.add(testRoot);
    sdbConn.commit();

    NodeData updated2 = new TransientNodeData(testRoot.getQPath(),
                                              testRoot.getIdentifier(),
                                              2,
                                              Constants.NT_FILE,
                                              new InternalQName[] { Constants.MIX_VERSIONABLE },
                                              1,
                                              testRoot.getParentIdentifier(),
                                              null);

    NodeData updated3 = new TransientNodeData(testRoot.getQPath(),
                                              testRoot.getIdentifier(),
                                              3,
                                              Constants.NT_UNSTRUCTURED,
                                              new InternalQName[] {},
                                              1,
                                              testRoot.getParentIdentifier(),
                                              null);
    sdbConn.update(updated3);
    sdbConn.commit();

    Thread.sleep(2000);

    // run for fail
    try {
      sdbConn.update(updated2);
      sdbConn.commit();
      fail("InvalidItemStateException should be thrown");
    } catch (InvalidItemStateException e) {
      // ok
    }

    // check
    GetAttributesResponse resp = readItem(sdbClient, SDB_DOMAIN_NAME, updated3.getIdentifier());

    if (resp.isSetGetAttributesResult()) {
      GetAttributesResult res = resp.getGetAttributesResult();
      String id = null;
      String idata = null;
      for (Attribute attr : res.getAttribute()) {
        if (attr.getName().equals(SDBConstants.ID))
          id = attr.getValue();
        else if (attr.getName().equals(SDBConstants.IDATA))
          idata = attr.getValue();
      }

      assertEquals("Id doesn't match", updated3.getIdentifier(), id);
      assertTrue("Persistent version should be 3", idata.startsWith("3"
          + SDBConstants.IDATA_DELIMITER));

      assertTrue("Not a nt:file", idata.indexOf(Constants.NT_FILE.getAsString()) == -1);
      assertTrue("Not a mix:versionable",
                 idata.indexOf(Constants.MIX_VERSIONABLE.getAsString()) == -1);

      assertTrue("This is a nt:unstructured node",
                 idata.indexOf(Constants.NT_UNSTRUCTURED.getAsString()) > -1);
    } else
      fail("Not a result");
  }

  /**
   * Test if nonexisted Item delete will fails on save.
   * 
   * @throws Exception
   *           - if error
   */
  public void testItemNotFoundOnDelete() throws Exception {

    // prepare
    sdbConn.add(jcrRoot);
    sdbConn.commit();

    try {
      sdbConn.delete(testRoot);
      sdbConn.commit();
      fail("InvalidItemStateException should be thrown");
    } catch (InvalidItemStateException e) {
      // ok
    }

    // check
    GetAttributesResponse resp = readItem(sdbClient, SDB_DOMAIN_NAME, testRoot.getIdentifier());

    if (resp.isSetGetAttributesResult()) {
      GetAttributesResult res = resp.getGetAttributesResult();
      assertTrue("Node should not be saved", res.getAttribute().size() <= 0);
    } else
      fail("Not a result");
  }

  /**
   * Test if nonexisted Item delete will fails on save.
   * 
   * @throws Exception
   *           - if error
   */
  public void testItemNotFoundOnUpdate() throws Exception {

    // prepare
    sdbConn.add(jcrRoot);
    sdbConn.add(testRoot); // parent
    sdbConn.add(testMultivaluedProperty); // anu stuff
    sdbConn.commit();

    // should fail
    try {
      sdbConn.update(testProperty);
      sdbConn.commit();
      fail("InvalidItemStateException should be thrown");
    } catch (InvalidItemStateException e) {
      // ok
    }

    // check
    GetAttributesResponse resp = readItem(sdbClient, SDB_DOMAIN_NAME, testProperty.getIdentifier());

    if (resp.isSetGetAttributesResult()) {
      GetAttributesResult res = resp.getGetAttributesResult();
      assertTrue("Property should not be saved", res.getAttribute().size() <= 0);
    } else
      fail("Not a result");
  }

  /**
   * Test if add of Item already stored in Repository will fails. Check by ID and by parent ID and
   * Name.
   * 
   * @throws Exception
   *           - if error
   */
  public void testItemExists() throws Exception {

    // prepare
    sdbConn.add(jcrRoot);
    sdbConn.add(testRoot); // parent
    sdbConn.add(testProperty); // property
    sdbConn.commit();

    // Thread.sleep(5000);

    // should fail
    String newpId = null;
    try {
      // try property with same name but diff Id
      TransientPropertyData newp = new TransientPropertyData(testProperty.getQPath(),
                                                             newpId = SIDGenerator.generate(), // new
                                                                                               // ID
                                                                                               // !
                                                             1,
                                                             PropertyType.DATE,
                                                             testProperty.getParentIdentifier(),
                                                             false);
      List<ValueData> values = new ArrayList<ValueData>(1);
      values.add(new TransientValueData(Calendar.getInstance()));
      newp.setValues(values);

      sdbConn.add(newp);
      sdbConn.commit();
      fail("ItemExistsException should be thrown");
    } catch (ItemExistsException e) {
      // ok
    }

    // check
    GetAttributesResponse resp = readItem(sdbClient, SDB_DOMAIN_NAME, newpId);

    if (resp.isSetGetAttributesResult()) {
      GetAttributesResult res = resp.getGetAttributesResult();
      assertTrue("Property should not be saved", res.getAttribute().size() <= 0);
    } else
      fail("Not a result");

    try {
      // try property with same Id but diff Name
      TransientPropertyData newp = new TransientPropertyData(testMultivaluedProperty.getQPath(),
                                                             testProperty.getIdentifier(), // ID of
                                                             // testProperty
                                                             1,
                                                             PropertyType.DATE,
                                                             testProperty.getParentIdentifier(),
                                                             false);
      List<ValueData> values = new ArrayList<ValueData>(1);
      values.add(new TransientValueData(Calendar.getInstance()));
      newp.setValues(values);

      sdbConn.add(newp);
      sdbConn.commit();
      fail("ItemExistsException should be thrown");
    } catch (ItemExistsException e) {
      // ok
    }

    // check
    resp = readItem(sdbClient, SDB_DOMAIN_NAME, testProperty.getIdentifier());

    if (resp.isSetGetAttributesResult()) {
      GetAttributesResult res = resp.getGetAttributesResult();
      String name = null;
      String idata = null;
      for (Attribute attr : res.getAttribute()) {
        if (attr.getName().equals(SDBConstants.NAME))
          name = attr.getValue();
        if (attr.getName().equals(SDBConstants.IDATA))
          idata = attr.getValue();
      }

      assertEquals("Property should has new name",
                   testProperty.getQPath().getEntries()[testProperty.getQPath().getEntries().length - 1].getAsString(true),
                   name);

      assertEquals("Property type should be DATE", "1" + SDBConstants.IDATA_DELIMITER
          + String.valueOf(PropertyType.STRING) + SDBConstants.IDATA_DELIMITER + "false", idata);
    } else
      fail("Not a result");
  }

}
