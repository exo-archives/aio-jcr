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

package org.exoplatform.services.jcr.api.importing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PropertyType;

import org.exoplatform.services.jcr.JcrAPIBaseTest;

/**
 * Created by The eXo Platform SAS Author : Peter Nedonosko
 * peter.nedonosko@exoplatform.com.ua 25.09.2006
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: TestSysView.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class TestImportSysView extends JcrAPIBaseTest {

  static public final String    SOURCE_NAME = "source node";

  static protected final String BIN_STRING  = "222222222222222222<=Any binary=>22222222222222222222";

  private Node                  sysview;

  private File                  xmlContent;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    sysview = session.getRootNode().addNode("test sysview", "nt:unstructured");

    Node ref = sysview.addNode(SOURCE_NAME, "nt:file");
    Node content = ref.addNode("jcr:content", "nt:unstructured");
    content.setProperty("anyDate", Calendar.getInstance());
    content.setProperty("anyString", "11111111111111<=Any string=>11111111111111111");
    content.setProperty("anyNumb", 123.321d);

    content.setProperty("anyBinary", BIN_STRING, PropertyType.BINARY);

    content.addNode("anyNode1").setProperty("_some_double", 1234.4321d);
    content.addNode("anyNode2").setProperty("_some_long", 123456789L);

    session.save();

    if (ref.canAddMixin("mix:referenceable")) {
      ref.addMixin("mix:referenceable");
      ref.save();
    } else {
      fail("Can't add mixin mix:referenceable");
    }

    // export

    File tmp = File.createTempFile("__exojcr_TestSysView__", ".tmp");

    OutputStream xmlOut = new FileOutputStream(tmp);
    sysview.getSession().exportSystemView(ref.getPath(), xmlOut, false, false);
    xmlOut.close();

    xmlContent = tmp;
  }

  public void testExportUuid_IMPORT_UUID_COLLISION_REMOVE_EXISTING() throws Exception {
    Node source = sysview.getNode(SOURCE_NAME);
    String uuid = source.getProperty("jcr:uuid").getString();

    Node importTarget = sysview.addNode("import target");
    sysview.save();

    sysview.getSession().importXML(importTarget.getPath(),
                                   new FileInputStream(xmlContent),
                                   ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);

    sysview.save();

    // check uuid

    assertFalse("A node must is not exists " + source.getPath(), sysview.hasNode(SOURCE_NAME));

    String importedUuid = importTarget.getNode(SOURCE_NAME).getProperty("jcr:uuid").getString();
    assertTrue("Uuids must be same. " + uuid + " = " + importedUuid, uuid.equals(importedUuid));

    // try one more (for same-name sibling nodes test), mus replace before
    // imported node
    sysview.getSession().importXML(importTarget.getPath(),
                                   new FileInputStream(xmlContent),
                                   ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);

    sysview.save();

    // check sns...

    assertFalse("Same-name sibling node must is not exists. ", importTarget.hasNode(SOURCE_NAME
        + "[2]"));
    assertTrue(importTarget.hasNode(SOURCE_NAME));
    String importedSNSUuid = importTarget.getNode(SOURCE_NAME).getProperty("jcr:uuid").getString();
    assertTrue("Uuids must be same. " + uuid + " = " + importedSNSUuid,
               uuid.equals(importedSNSUuid));
  }

  public void testExportUuid_IMPORT_UUID_COLLISION_REPLACE_EXISTING() throws Exception {
    Node source = sysview.getNode(SOURCE_NAME);

    source.getNode("jcr:content").setProperty("New property 1, boolean", false);
    source.getNode("jcr:content").setProperty("New property 2, string", "STRING 1");

    String uuid = source.getProperty("jcr:uuid").getString();

    Node importTarget = sysview.addNode("import target");
    sysview.save();

    sysview.getSession().importXML(importTarget.getPath(),
                                   new FileInputStream(xmlContent),
                                   ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);

    sysview.save();

    // check...

    Node target = sysview.getNode(SOURCE_NAME);

    String importedUuid = target.getProperty("jcr:uuid").getString();
    assertTrue("Uuids must be same. " + uuid + " = " + importedUuid, uuid.equals(importedUuid));

    assertFalse("A imported node must has no property 'New property 1, boolean' "
        + target.getPath(), target.hasProperty("jcr:content/New property 1, boolean"));
    assertFalse("A imported node must has no property 'New property 2, string' " + target.getPath(),
                target.hasProperty("jcr:content/New property 2, string"));

    // create one more same-name sibling node
    sysview.getSession().importXML(importTarget.getPath(),
                                   new FileInputStream(xmlContent),
                                   ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);

    sysview.save();

    // check sns...

    target = sysview.getNode(SOURCE_NAME);
    String importedSNSUuid = target.getProperty("jcr:uuid").getString();
    assertTrue("Uuids must be same. " + uuid + " = " + importedSNSUuid,
               uuid.equals(importedSNSUuid));

    assertTrue("Uuid of SNS replaced node must be different. " + importedSNSUuid + " != "
        + importedSNSUuid, importedSNSUuid.equals(importedSNSUuid));

    assertFalse("A imported node must has no property 'New property 1, boolean' "
        + target.getPath(), target.hasProperty("jcr:content/New property 1, boolean"));
    assertFalse("A imported node must has no property 'New property 2, string' " + target.getPath(),
                target.hasProperty("jcr:content/New property 2, string"));
  }

  public void testExportUuid_IMPORT_UUID_COLLISION_THROW() throws Exception {
    Node source = sysview.getNode(SOURCE_NAME);
    String uuid = source.getProperty("jcr:uuid").getString();

    Node importTarget = sysview.addNode("import target");
    sysview.save();

    try {
      sysview.getSession().importXML(importTarget.getPath(),
                                     new FileInputStream(xmlContent),
                                     ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);

      fail("An exception ItemExistsException must be throwed. Node with same uuid already exists");
    } catch (ItemExistsException e) {
      // ok
    }

    // one more time...:)
    try {
      sysview.getSession().importXML(importTarget.getPath(),
                                     new FileInputStream(xmlContent),
                                     ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
      fail("An exception ItemExistsException must be throwed. Node with same uuid already exists. TEST CYCLE2");
    } catch (ItemExistsException e) {
      // ok
    }
  }

  public void testExportUuid_IMPORT_UUID_CREATE_NEW() throws Exception {
    String uuid = sysview.getNode(SOURCE_NAME).getProperty("jcr:uuid").getString();

    Node importTarget = sysview.addNode("import target");
    sysview.save();

    sysview.getSession().importXML(importTarget.getPath(),
                                   new FileInputStream(xmlContent),
                                   ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);

    sysview.save();

    // check uuid

    String importedUuid = importTarget.getNode(SOURCE_NAME).getProperty("jcr:uuid").getString();
    assertFalse("Uuids must be different. " + uuid + " != " + importedUuid,
                uuid.equals(importedUuid));

    // create one more same-name sibling node
    sysview.getSession().importXML(importTarget.getPath(),
                                   new FileInputStream(xmlContent),
                                   ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);

    sysview.save();

    // check sns...
    String importedSNSUuid = importTarget.getNode(SOURCE_NAME + "[2]")
                                         .getProperty("jcr:uuid")
                                         .getString();
    assertFalse("Uuids must be different. " + uuid + " != " + importedSNSUuid,
                uuid.equals(importedSNSUuid));
    assertFalse("Uuids must be different. " + importedSNSUuid + " != " + importedUuid,
                importedSNSUuid.equals(importedUuid));

    // ...temp check
    InputStream anyBinary = importTarget.getNode(SOURCE_NAME + "[2]/jcr:content")
                                        .getProperty("anyBinary")
                                        .getStream();
    assertEquals("Stream length must be same", BIN_STRING.length(), anyBinary.available());
    assertEquals("Stream content must be same", BIN_STRING, importTarget.getNode(SOURCE_NAME
        + "[2]/jcr:content").getProperty("anyBinary").getString());
  }

  @Override
  protected void tearDown() throws Exception {

    if (xmlContent != null)
      xmlContent.delete();

    super.tearDown();
  }
}
