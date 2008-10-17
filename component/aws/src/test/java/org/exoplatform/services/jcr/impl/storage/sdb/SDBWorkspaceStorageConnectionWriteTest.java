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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.ws.commons.util.Base64;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.datamodel.IllegalNameException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;

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
public class SDBWorkspaceStorageConnectionWriteTest extends SDBWorkspaceTestBase {

  /**
   * Test add Node. Test if Node storage metadata (persisted version, order number, nodetypes, ACL)
   * stored well.
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
      assertEquals("Item class should be Node ", "1", iclass);
      assertEquals("Name doesn't match",
                   jcrRoot.getQPath().getEntries()[jcrRoot.getQPath().getEntries().length - 1].getAsString(true),
                   name);

      // get IData metas
      String[] idms = idata.split(SDBConstants.IDATA_DELIMITER_REGEXP);
      assertEquals("Node IData has wrong size ", 8, idms.length);
      try {
        assertEquals("Persisted version should match ", "1", idms[0]);
        assertEquals("Order number should match ", "1", idms[1]);

        InternalQName primary = InternalQName.parse(idms[2]);
        assertEquals("nt:unstructured expected.", Constants.NT_UNSTRUCTURED, primary);

        // ACL permissions
        List<AccessControlEntry> perms = jcrRoot.getACL().getPermissionEntries();

        assertEquals("Permission should match ", perms.get(0).getAsString(), idms[3].substring(2));
        assertEquals("Permission should match ", perms.get(1).getAsString(), idms[4].substring(2));
        assertEquals("Permission should match ", perms.get(2).getAsString(), idms[5].substring(2));
        assertEquals("Permission should match ", perms.get(3).getAsString(), idms[6].substring(2));

        // ACL owner
        assertEquals("Owner should match ", jcrRoot.getACL().getOwner(), idms[7].substring(2));

      } catch (IndexOutOfBoundsException e) {
        fail("IData value is wrong " + e.getMessage());
      } catch (IllegalNameException e) {
        fail(e.getMessage());
      }
    } else
      fail("Not initialized");
  }

  /**
   * Test if Node storage metadata (persisted version, order number, nodetypes) stored well.
   * 
   * @throws AmazonSimpleDBException
   *           - SDB error
   */
  public void testAddNodeWithMixin() throws AmazonSimpleDBException {

    try {
      sdbConn.add(jcrRoot); // parent
      sdbConn.add(testRoot);
      sdbConn.commit();
    } catch (ItemExistsException e) {
      LOG.error("add Node error", e);
      fail(e.getMessage());
    } catch (RepositoryException e) {
      LOG.error("add Node error", e);
      fail(e.getMessage());
    }

    // check
    GetAttributesResponse resp = readItem(sdbClient, SDB_DOMAIN_NAME, testRoot.getIdentifier());

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

      assertEquals("Id doesn't match", testRoot.getIdentifier(), id);
      assertEquals("Parent id doesn't match", testRoot.getParentIdentifier(), pid);
      assertEquals("Item class should be Node ", "1", iclass);
      assertEquals("Name doesn't match",
                   testRoot.getQPath().getEntries()[testRoot.getQPath().getEntries().length - 1].getAsString(true),
                   name);

      // get IData metas
      String[] idms = idata.split(SDBConstants.IDATA_DELIMITER_REGEXP);
      assertEquals("Node IData has wrong size ", 4, idms.length);
      try {
        assertEquals("Persisted version should match ", "1", idms[0]);
        assertEquals("Order number should match ", "1", idms[1]);

        InternalQName primary = InternalQName.parse(idms[2]);
        assertEquals("nt:file expected.", Constants.NT_FILE, primary);

        InternalQName mixin = InternalQName.parse(idms[3].substring(2));
        assertEquals("mix:referenceable mixin expected.", Constants.MIX_REFERENCEABLE, mixin);
      } catch (IndexOutOfBoundsException e) {
        fail("IData value is wrong " + e.getMessage());
      } catch (IllegalNameException e) {
        fail(e.getMessage());
      }
    } else
      fail("Not initialized");
  }

  /**
   * Test add of Property. Test if storage metadata (persisted version, property type, multivalue
   * status) stored well.
   * 
   * @throws AmazonSimpleDBException
   *           SDB error
   * @throws IOException
   *           if I/O error
   * @throws IllegalStateException
   *           not matter here
   */
  public void testAddProperty() throws AmazonSimpleDBException, IllegalStateException, IOException {

    try {
      sdbConn.add(jcrRoot); // root
      sdbConn.add(testRoot); // parent
      sdbConn.add(testProperty); // property
      sdbConn.commit();
    } catch (ItemExistsException e) {
      LOG.error("add Property error", e);
      fail(e.getMessage());
    } catch (RepositoryException e) {
      LOG.error("add Property error", e);
      fail(e.getMessage());
    }

    // check
    GetAttributesResponse resp = readItem(sdbClient, SDB_DOMAIN_NAME, testProperty.getIdentifier());

    if (resp.isSetGetAttributesResult()) {
      GetAttributesResult res = resp.getGetAttributesResult();
      String id = null;
      String pid = null;
      String name = null;
      String iclass = null;
      String idata = null;
      String data = null;
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
        else if (attr.getName().equals(SDBConstants.DATA))
          data = attr.getValue();
      }

      assertEquals("Id doesn't match", testProperty.getIdentifier(), id);
      assertEquals("Parent id doesn't match", testProperty.getParentIdentifier(), pid);
      assertEquals("Item class should be Property ", "2", iclass);
      assertEquals("Name doesn't match",
                   testProperty.getQPath().getEntries()[testProperty.getQPath().getEntries().length - 1].getAsString(true),
                   name);

      // get IData metas
      String[] idms = idata.split(SDBConstants.IDATA_DELIMITER_REGEXP);
      assertEquals("Property IData has wrong size ", 3, idms.length);
      try {
        assertEquals("Property persisted version should match ", "1", idms[0]);
        assertEquals("Property type should match ", String.valueOf(PropertyType.STRING), idms[1]);
        assertEquals("Property multivalued status should match ", "false", idms[2]);

        // check data
        assertEquals("Property value ", SDBConstants.VALUEPREFIX_DATA
            + new String(testProperty.getValues().get(0).getAsByteArray(),
                         Constants.DEFAULT_ENCODING), data);
      } catch (IndexOutOfBoundsException e) {
        fail("IData value is wrong " + e.getMessage());
      }
    } else
      fail("Not initialized");
  }

  /**
   * Test add of Binary Property. Test if storage metadata (persisted version, property type,
   * multivalue status) stored well.
   * 
   * @throws AmazonSimpleDBException
   *           SDB error
   * @throws IOException
   *           if I/O error
   * @throws IllegalStateException
   *           not matter here
   */
  public void testAddBinaryProperty() throws AmazonSimpleDBException,
                                     IllegalStateException,
                                     IOException {

    try {
      sdbConn.add(jcrRoot); // root
      sdbConn.add(testRoot); // parent

      sdbConn.add(testProperty); // any stuff

      sdbConn.add(testBinaryProperty); // property
      sdbConn.commit();
    } catch (ItemExistsException e) {
      LOG.error("add Property error", e);
      fail(e.getMessage());
    } catch (RepositoryException e) {
      LOG.error("add Property error", e);
      fail(e.getMessage());
    }

    // check
    GetAttributesResponse resp = readItem(sdbClient,
                                          SDB_DOMAIN_NAME,
                                          testBinaryProperty.getIdentifier());

    if (resp.isSetGetAttributesResult()) {
      GetAttributesResult res = resp.getGetAttributesResult();
      String id = null;
      String pid = null;
      String name = null;
      String iclass = null;
      String idata = null;
      String data = null;
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
        else if (attr.getName().equals(SDBConstants.DATA))
          data = attr.getValue();
      }

      assertEquals("Id doesn't match", testBinaryProperty.getIdentifier(), id);
      assertEquals("Parent id doesn't match", testBinaryProperty.getParentIdentifier(), pid);
      assertEquals("Item class should be Property ", "2", iclass);
      assertEquals("Name doesn't match",
                   testBinaryProperty.getQPath().getEntries()[testBinaryProperty.getQPath()
                                                                                .getEntries().length - 1].getAsString(true),
                   name);

      // get IData metas
      String[] idms = idata.split(SDBConstants.IDATA_DELIMITER_REGEXP);
      assertEquals("Property IData has wrong size ", 3, idms.length);
      try {
        assertEquals("Property persisted version should match ", "1", idms[0]);
        assertEquals("Property type should match ", String.valueOf(PropertyType.BINARY), idms[1]);
        assertEquals("Property multivalued status should match ", "false", idms[2]);

        // check data
        byte[] vdata = testBinaryProperty.getValues().get(0).getAsByteArray();
        assertEquals("Property value ", SDBConstants.VALUEPREFIX_DATA
            + Base64.encode(vdata,
                            0,
                            vdata.length,
                            SDBConstants.SDB_ATTRIBUTE_VALUE_MAXLENGTH,
                            "\n"), data);
      } catch (IndexOutOfBoundsException e) {
        fail("IData value is wrong " + e.getMessage());
      }
    } else
      fail("Not initialized");
  }

  /**
   * Test add of Multivalued Property. Test if storage metadata (persisted version, property type,
   * multivalue status) stored well.
   * 
   * @throws AmazonSimpleDBException
   *           SDB error
   * @throws IOException
   *           if I/O error
   * @throws IllegalStateException
   *           not matter here
   */
  public void testAddMultivaluedProperty() throws AmazonSimpleDBException,
                                          IllegalStateException,
                                          IOException {

    try {
      sdbConn.add(jcrRoot); // root
      sdbConn.add(testRoot); // parent

      sdbConn.add(testProperty); // any stuff
      sdbConn.add(testBinaryProperty); // any stuff

      sdbConn.add(testMultivaluedProperty); // property
      sdbConn.commit();
    } catch (ItemExistsException e) {
      LOG.error("add Property error", e);
      fail(e.getMessage());
    } catch (RepositoryException e) {
      LOG.error("add Property error", e);
      fail(e.getMessage());
    }

    // check
    GetAttributesResponse resp = readItem(sdbClient,
                                          SDB_DOMAIN_NAME,
                                          testMultivaluedProperty.getIdentifier());

    if (resp.isSetGetAttributesResult()) {
      GetAttributesResult res = resp.getGetAttributesResult();
      String id = null;
      String pid = null;
      String name = null;
      String iclass = null;
      String idata = null;
      List<String> data = new ArrayList<String>();
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
        else if (attr.getName().equals(SDBConstants.DATA))
          data.add(attr.getValue());
      }

      assertEquals("Id doesn't match", testMultivaluedProperty.getIdentifier(), id);
      assertEquals("Parent id doesn't match", testMultivaluedProperty.getParentIdentifier(), pid);
      assertEquals("Item class should be Property ", "2", iclass);
      assertEquals("Name doesn't match",
                   testMultivaluedProperty.getQPath().getEntries()[testMultivaluedProperty.getQPath()
                                                                                          .getEntries().length - 1].getAsString(true),
                   name);

      // get IData metas
      String[] idms = idata.split(SDBConstants.IDATA_DELIMITER_REGEXP);
      try {
        assertEquals("Property IData has wrong size ", 3, idms.length);
        assertEquals("Property persisted version should match ", "1", idms[0]);
        assertEquals("Property type should match ", String.valueOf(PropertyType.STRING), idms[1]);
        assertEquals("Property multivalued status should match ", "true", idms[2]);

        // check data
        for (int i = 0; i < data.size(); i++) {
          String value = data.get(i);

          assertEquals("Property value ",
                       new String(testMultivaluedProperty.getValues().get(i).getAsByteArray(),
                                  Constants.DEFAULT_ENCODING),
                       value.substring(SDBConstants.VALUEPREFIX_MULTIVALUED_LENGTH));
        }
      } catch (IndexOutOfBoundsException e) {
        fail("IData value is wrong " + e.getMessage());
      }
    } else
      fail("Not initialized");
  }

  /**
   * Test update Node (orderNum and mixin changes). Test if Node storage metadata (persisted
   * version, order number, nodetypes, ACL) updated well.
   * 
   * @throws AmazonSimpleDBException
   *           - SDB error
   * @throws RepositoryException
   * @throws ItemExistsException
   */
  public void testUpdateNode() throws AmazonSimpleDBException,
                              ItemExistsException,
                              RepositoryException {

    // prepare
    sdbConn.add(jcrRoot);
    sdbConn.add(testRoot);
    sdbConn.commit();

    // update
    NodeData updated = null;
    try {
      updated = new TransientNodeData(testRoot.getQPath(),
                                      testRoot.getIdentifier(),
                                      2,
                                      Constants.NT_FILE,
                                      new InternalQName[] { Constants.MIX_REFERENCEABLE,
                                          Constants.MIX_VERSIONABLE },
                                      2,
                                      testRoot.getParentIdentifier(),
                                      null);

      sdbConn.update(updated);
      sdbConn.commit();
    } catch (ItemExistsException e) {
      LOG.error("update Node error", e);
      fail(e.getMessage());
    } catch (RepositoryException e) {
      LOG.error("update Node error", e);
      fail(e.getMessage());
    }

    // check
    GetAttributesResponse resp = readItem(sdbClient, SDB_DOMAIN_NAME, updated.getIdentifier());

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

      assertEquals("Id doesn't match", updated.getIdentifier(), id);
      assertEquals("Parent id doesn't match", updated.getParentIdentifier(), pid);
      assertEquals("Item class should be Node ", "1", iclass);
      assertEquals("Name doesn't match",
                   updated.getQPath().getEntries()[updated.getQPath().getEntries().length - 1].getAsString(true),
                   name);

      // get IData metas
      String[] idms = idata.split(SDBConstants.IDATA_DELIMITER_REGEXP);
      assertEquals("Node IData has wrong size ", 5, idms.length);
      try {
        assertEquals("Persisted version should match ", "2", idms[0]);
        assertEquals("Order number should match ", "2", idms[1]);

        InternalQName primary = InternalQName.parse(idms[2]);
        assertEquals("nt:file expected.", Constants.NT_FILE, primary);

        assertEquals("mix:referenceable mixin expected.",
                     Constants.MIX_REFERENCEABLE,
                     InternalQName.parse(idms[3].substring(2)));
        assertEquals("mix:versionable mixin expected.",
                     Constants.MIX_VERSIONABLE,
                     InternalQName.parse(idms[4].substring(2)));
      } catch (IndexOutOfBoundsException e) {
        fail("IData value is wrong " + e.getMessage());
      } catch (IllegalNameException e) {
        fail(e.getMessage());
      }
    } else
      fail("Not initialized");
  }

  /**
   * Test update Property (orderNum and mixin changes). Test if Property storage metadata (persisted
   * version, order number, type) updated well.
   * 
   * @throws Exception
   *           - error
   */
  public void testUpdateProperty() throws Exception {

    // prepare
    sdbConn.add(jcrRoot);
    sdbConn.add(testRoot);
    sdbConn.add(testProperty);
    sdbConn.commit();

    // update
    TransientValueData newValue = new TransientValueData(Calendar.getInstance());
    TransientPropertyData updated = null;
    try {
      updated = new TransientPropertyData(testProperty.getQPath(),
                                          testProperty.getIdentifier(),
                                          2,
                                          PropertyType.DATE,
                                          testProperty.getParentIdentifier(),
                                          false);
      List<ValueData> values = new ArrayList<ValueData>(1);
      values.add(newValue);
      updated.setValues(values);

      sdbConn.update(updated);
      sdbConn.commit();
    } catch (ItemExistsException e) {
      LOG.error("update Property error", e);
      fail(e.getMessage());
    } catch (RepositoryException e) {
      LOG.error("update Property error", e);
      fail(e.getMessage());
    }

    // check
    GetAttributesResponse resp = readItem(sdbClient, SDB_DOMAIN_NAME, updated.getIdentifier());

    if (resp.isSetGetAttributesResult()) {
      GetAttributesResult res = resp.getGetAttributesResult();
      String id = null;
      String pid = null;
      String name = null;
      String iclass = null;
      String idata = null;
      String data = null;
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
        else if (attr.getName().equals(SDBConstants.DATA))
          data = attr.getValue();
      }

      assertEquals("Id doesn't match", updated.getIdentifier(), id);
      assertEquals("Parent id doesn't match", updated.getParentIdentifier(), pid);
      assertEquals("Item class should be Property ", "2", iclass);
      assertEquals("Name doesn't match",
                   updated.getQPath().getEntries()[updated.getQPath().getEntries().length - 1].getAsString(true),
                   name);

      // get IData metas
      String[] idms = idata.split(SDBConstants.IDATA_DELIMITER_REGEXP);
      assertEquals("Property IData has wrong size ", 3, idms.length);
      try {
        assertEquals("Property persisted version should match ", "2", idms[0]);
        assertEquals("Property type should match ", String.valueOf(PropertyType.DATE), idms[1]);
        assertEquals("Property multivalued status should match ", "false", idms[2]);

        // check data
        assertEquals("Property value ",
                     SDBConstants.VALUEPREFIX_DATA
                         + new String(updated.getValues().get(0).getAsByteArray(),
                                      Constants.DEFAULT_ENCODING),
                     data);
      } catch (IndexOutOfBoundsException e) {
        fail("IData value is wrong " + e.getMessage());
      }
    } else
      fail("Not initialized");
  }

  /**
   * Test update of Binary Property. Test if storage metadata (persisted version, property type,
   * multivalue status) stored well.
   * 
   * @throws Exception
   *           error
   */
  public void testUpdateBinaryProperty() throws Exception {

    // prepare
    sdbConn.add(jcrRoot); // root
    sdbConn.add(testRoot); // parent

    sdbConn.add(testProperty); // any stuff

    sdbConn.add(testBinaryProperty); // property
    sdbConn.commit();

    // update
    TransientValueData newValue = new TransientValueData(getClass().getClassLoader()
                                                                   .getResourceAsStream("html/small-page.html"));
    TransientPropertyData updated = null;
    try {
      updated = new TransientPropertyData(testBinaryProperty.getQPath(),
                                          testBinaryProperty.getIdentifier(),
                                          2,
                                          PropertyType.BINARY,
                                          testBinaryProperty.getParentIdentifier(),
                                          false);
      List<ValueData> values = new ArrayList<ValueData>(1);
      values.add(newValue);
      updated.setValues(values);

      sdbConn.update(updated);
      sdbConn.commit();
    } catch (ItemExistsException e) {
      LOG.error("update Property error", e);
      fail(e.getMessage());
    } catch (RepositoryException e) {
      LOG.error("update Property error", e);
      fail(e.getMessage());
    }

    // check
    GetAttributesResponse resp = readItem(sdbClient, SDB_DOMAIN_NAME, updated.getIdentifier());

    if (resp.isSetGetAttributesResult()) {
      GetAttributesResult res = resp.getGetAttributesResult();
      String id = null;
      String pid = null;
      String name = null;
      String iclass = null;
      String idata = null;
      String data = null;
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
        else if (attr.getName().equals(SDBConstants.DATA))
          data = attr.getValue();
      }

      assertEquals("Id doesn't match", updated.getIdentifier(), id);
      assertEquals("Parent id doesn't match", updated.getParentIdentifier(), pid);
      assertEquals("Item class should be Property ", "2", iclass);
      assertEquals("Name doesn't match",
                   updated.getQPath().getEntries()[updated.getQPath().getEntries().length - 1].getAsString(true),
                   name);

      // get IData metas
      String[] idms = idata.split(SDBConstants.IDATA_DELIMITER_REGEXP);
      assertEquals("Property IData has wrong size ", 3, idms.length);
      try {
        assertEquals("Property persisted version should match ", "2", idms[0]);
        assertEquals("Property type should match ", String.valueOf(PropertyType.BINARY), idms[1]);
        assertEquals("Property multivalued status should match ", "false", idms[2]);

        // check data
        byte[] vdata = updated.getValues().get(0).getAsByteArray();
        assertEquals("Property value ", SDBConstants.VALUEPREFIX_DATA
            + Base64.encode(vdata,
                            0,
                            vdata.length,
                            SDBConstants.SDB_ATTRIBUTE_VALUE_MAXLENGTH,
                            "\n"), data);
      } catch (IndexOutOfBoundsException e) {
        fail("IData value is wrong " + e.getMessage());
      }
    } else
      fail("Not initialized");
  }

  /**
   * Test update of Multivalued Property. Test if storage metadata (persisted version, property
   * type, multivalue status) stored well.
   * 
   * @throws Exception
   *           error
   */
  public void testUpdateMultivaluedProperty() throws Exception {

    // prepare
    sdbConn.add(jcrRoot); // root
    sdbConn.add(testRoot); // parent

    sdbConn.add(testProperty); // any stuff
    sdbConn.add(testBinaryProperty); // any stuff

    sdbConn.add(testMultivaluedProperty); // property
    sdbConn.commit();

    // update
    TransientPropertyData updated = null;
    try {
      updated = new TransientPropertyData(testMultivaluedProperty.getQPath(),
                                          testMultivaluedProperty.getIdentifier(),
                                          2,
                                          PropertyType.STRING,
                                          testMultivaluedProperty.getParentIdentifier(),
                                          true);

      final int valuesCount = 50;
      List<ValueData> values = new ArrayList<ValueData>(valuesCount);
      for (int i = 1; i <= valuesCount; i++) {
        values.add(new TransientValueData("This is a text value #" + i));
      }
      updated.setValues(values);

      sdbConn.update(updated); // property
      sdbConn.commit();
    } catch (ItemExistsException e) {
      LOG.error("add Property error", e);
      fail(e.getMessage());
    } catch (RepositoryException e) {
      LOG.error("add Property error", e);
      fail(e.getMessage());
    }

    // check
    GetAttributesResponse resp = readItem(sdbClient, SDB_DOMAIN_NAME, updated.getIdentifier());

    if (resp.isSetGetAttributesResult()) {
      GetAttributesResult res = resp.getGetAttributesResult();
      String id = null;
      String pid = null;
      String name = null;
      String iclass = null;
      String idata = null;
      List<String> data = new ArrayList<String>();
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
        else if (attr.getName().equals(SDBConstants.DATA))
          data.add(attr.getValue());
      }

      assertEquals("Id doesn't match", updated.getIdentifier(), id);
      assertEquals("Parent id doesn't match", updated.getParentIdentifier(), pid);
      assertEquals("Item class should be Property ", "2", iclass);
      assertEquals("Name doesn't match",
                   updated.getQPath().getEntries()[updated.getQPath().getEntries().length - 1].getAsString(true),
                   name);

      // get IData metas
      String[] idms = idata.split(SDBConstants.IDATA_DELIMITER_REGEXP);
      assertEquals("Property IData has wrong size ", 3, idms.length);
      try {
        assertEquals("Property persisted version should match ", "2", idms[0]);
        assertEquals("Property type should match ", String.valueOf(PropertyType.STRING), idms[1]);
        assertEquals("Property multivalued status should match ", "true", idms[2]);

        // check data
        for (int i = 0; i < data.size(); i++) {
          String value = data.get(i);

          assertEquals("Property value ",
                       new String(updated.getValues().get(i).getAsByteArray(),
                                  Constants.DEFAULT_ENCODING),
                       value.substring(SDBConstants.VALUEPREFIX_MULTIVALUED_LENGTH));
        }
      } catch (IndexOutOfBoundsException e) {
        fail("IData value is wrong " + e.getMessage());
      }
    } else
      fail("Not initialized");
  }

  /**
   * Test delete of Node.
   * 
   * @throws Exception
   *           - SDB error
   */
  public void testDeleteNode() throws Exception {

    // prepare
    sdbConn.add(jcrRoot);
    sdbConn.add(testRoot);
    sdbConn.commit();

    // delete
    TransientNodeData deleted = new TransientNodeData(testRoot.getQPath(),
                                                      testRoot.getIdentifier(),
                                                      2,
                                                      Constants.NT_FILE,
                                                      new InternalQName[] { Constants.MIX_REFERENCEABLE },
                                                      2,
                                                      testRoot.getParentIdentifier(),
                                                      null);
    try {
      sdbConn.delete(deleted);
      sdbConn.commit();
    } catch (ItemExistsException e) {
      LOG.error("delete Node error", e);
      fail(e.getMessage());
    } catch (RepositoryException e) {
      LOG.error("delete Node error", e);
      fail(e.getMessage());
    }

    // check
    GetAttributesResponse resp = readItem(sdbClient, SDB_DOMAIN_NAME, deleted.getIdentifier());

    if (resp.isSetGetAttributesResult()) {
      GetAttributesResult res = resp.getGetAttributesResult();
      String id = null;
      for (Attribute attr : res.getAttribute()) {
        if (attr.getName().equals(SDBConstants.ID))
          id = attr.getValue();
      }
      assertTrue("Node should be deleted", SDBConstants.ITEM_DELETED_ID.equals(id));
    } else
      fail("Not initialized");
  }

  /**
   * Test delete of Multivalued Property.
   * 
   * @throws Exception
   *           error
   */
  public void testDeleteProperty() throws Exception {

    // prepare
    sdbConn.add(jcrRoot); // root
    sdbConn.add(testRoot); // parent

    sdbConn.add(testProperty);
    sdbConn.add(testBinaryProperty);
    sdbConn.add(testMultivaluedProperty);
    sdbConn.commit();

    // delete
    TransientPropertyData deleted = new TransientPropertyData(testMultivaluedProperty.getQPath(),
                                                              testMultivaluedProperty.getIdentifier(),
                                                              2,
                                                              PropertyType.STRING,
                                                              testMultivaluedProperty.getParentIdentifier(),
                                                              true);
    try {
      sdbConn.delete(deleted);
      sdbConn.commit();
    } catch (ItemExistsException e) {
      LOG.error("delete Property error", e);
      fail(e.getMessage());
    } catch (RepositoryException e) {
      LOG.error("delete Property error", e);
      fail(e.getMessage());
    }

    // check
    GetAttributesResponse resp = readItem(sdbClient, SDB_DOMAIN_NAME, deleted.getIdentifier());

    if (resp.isSetGetAttributesResult()) {
      GetAttributesResult res = resp.getGetAttributesResult();
      String id = null;
      for (Attribute attr : res.getAttribute()) {
        if (attr.getName().equals(SDBConstants.ID))
          id = attr.getValue();
      }
      assertTrue("Property should be deleted", SDBConstants.ITEM_DELETED_ID.equals(id));
    } else
      fail("Not initialized");

    resp = readItem(sdbClient, SDB_DOMAIN_NAME, testBinaryProperty.getIdentifier());

    if (resp.isSetGetAttributesResult()) {
      GetAttributesResult res = resp.getGetAttributesResult();
      String id = null;
      for (Attribute attr : res.getAttribute()) {
        if (attr.getName().equals(SDBConstants.ID))
          id = attr.getValue();
      }
      assertTrue("Property should exists", testBinaryProperty.getIdentifier().equals(id));
    } else
      fail("Not initialized");
  }

  /**
   * Test if rollback works well.
   * 
   * @throws Exception
   *           error
   */
  public void testRollback() throws Exception {

    // prepare
    sdbConn.add(jcrRoot); // root
    sdbConn.commit();

    // test - add without parent
    try {
      sdbConn.add(testNode); // Node with parent jcrRoot
      sdbConn.add(testNodeProperty); // Property with parent testNode

      sdbConn.add(testProperty); // Property without parent (testRoot), should cause an error

      sdbConn.commit();
    } catch (InvalidItemStateException e) {
      // ok, rollback all changes in last commit
      sdbConn.rollback();
    }

    // check
    GetAttributesResponse resp = readItem(sdbClient, SDB_DOMAIN_NAME, testNode.getIdentifier());

    if (resp.isSetGetAttributesResult()) {
      GetAttributesResult res = resp.getGetAttributesResult();
      assertTrue("Node should not exists", res.getAttribute().size() <= 0);
    } else
      fail("Not initialized");

    resp = readItem(sdbClient, SDB_DOMAIN_NAME, testProperty.getIdentifier());

    if (resp.isSetGetAttributesResult()) {
      GetAttributesResult res = resp.getGetAttributesResult();
      assertTrue("Property should not exists", res.getAttribute().size() <= 0);
    } else
      fail("Not initialized");
  }

}
