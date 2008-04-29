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
package org.exoplatform.services.jcr.ext.metadata;

import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.BaseStandaloneTest;

public class MetaDataActionTest extends BaseStandaloneTest {

  /**
                 <value>
                    <object type="org.exoplatform.services.jcr.impl.ext.action.ActionConfiguration">
                      <field  name="eventTypes"><string>addProperty,changeProperty</string></field>
                      <field  name="path"><string>/MetaDataActionTest/testAddContent</string></field>
                      <field  name="isDeep"><boolean>true</boolean></field>
                      <field  name="parentNodeType"><string>nt:resource</string></field>
                      <field  name="actionClassName"><string>org.exoplatform.services.jcr.ext.metadata.AddMetadataAction</string></field>
                    </object>
                  </value>

   * @throws Exception
   */
  public void testAddContent() throws Exception {

    
    InputStream is = MetaDataActionTest.class.getResourceAsStream("/test_index.xls");
    Node rootNode = session.getRootNode().addNode("MetaDataActionTest");
    session.save();
    Node contentNode = rootNode.addNode("testAddContent", "nt:resource");
    //contentNode.setProperty("jcr:encoding", "UTF-8");
    contentNode.setProperty("jcr:data", is);
    contentNode.setProperty("jcr:mimeType", "application/excel");
    contentNode.setProperty("jcr:lastModified", Calendar.getInstance());
    root.save();

    Node testNode = repository.getSystemSession().getRootNode().getNode("MetaDataActionTest/testAddContent");
    assertTrue(testNode.hasProperty("dc:creator"));
    assertTrue(testNode.hasProperty("dc:date"));
    assertTrue(testNode.hasProperty("dc:contributor"));
  }

  /**
   * Prerequisites:
                  <value>
                    <object type="org.exoplatform.services.jcr.impl.ext.action.ActionConfiguration">
                      <field  name="eventTypes"><string>addNode</string></field>
                      <field  name="path"><string>/MetaDataActionTest/setmetadata</string></field>
                      <field  name="isDeep"><boolean>false</boolean></field>
                      <field  name="actionClassName"><string>org.exoplatform.services.jcr.ext.metadata.SetDCMetadataAction</string></field>
                    </object>
                  </value>

   *
   * @throws Exception
   */
  public void testSetMetaData() throws Exception {
    Node rootNode = session.getRootNode().addNode("MetaDataActionTest");
    session.save();
    Node contentNode = rootNode.addNode("testSetMetaData");
    rootNode.save();
    assertTrue(contentNode.hasProperty("dc:creator"));
    assertTrue(contentNode.hasProperty("dc:date"));
    assertEquals(session.getUserID(), contentNode.getProperty("dc:creator").getValues()[0].getString());
  }
  
  public void testDontSetMetaData() throws Exception {
    Node rootNode = session.getRootNode().addNode("MetaDataActionTest");
    session.save();
    Node contentNode = rootNode.addNode("testDontSetMetaData");
    contentNode.setProperty("prop", "prop 1");
    rootNode.save();
    assertFalse(contentNode.hasProperty("dc:creator"));
    assertFalse(contentNode.hasProperty("dc:date"));
    assertFalse(contentNode.hasProperty("dc:creator"));
  }
  
  public void testDontSetMetaDataNtFile() throws Exception {
    Node rootNode = session.getRootNode().addNode("MetaDataActionTest");
    session.save();
    Node node = rootNode.addNode("testDontSetMetaDataNtFile", "nt:file");
    Node contentNode = node.addNode("jcr:content", "nt:unstructured");
    contentNode.setProperty("jcr:data", MetaDataActionTest.class.getResourceAsStream("/test_index.xls"));
    contentNode.setProperty("jcr:mimeType", "application/vnd.ms-excel");
    contentNode.setProperty("jcr:lastModified", Calendar.getInstance());
    // dc:elementset properties SHOULD NOT be setted automatically
    rootNode.save();
    
    assertFalse(contentNode.hasProperty("dc:creator"));
    assertFalse(contentNode.hasProperty("dc:date"));
    assertFalse(contentNode.hasProperty("dc:creator"));
  }
  
  public void testDontSetMetaDataAnywhere() throws Exception {
    Node rootNode = session.getRootNode().addNode("MetaDataActionTest");
    session.save();
    Node contentNode = session.getRootNode().addNode("testDontSetMetaDataAnywhere");
    contentNode.setProperty("prop", "prop 1");
    rootNode.save();
    assertFalse(contentNode.hasProperty("dc:creator"));
    assertFalse(contentNode.hasProperty("dc:date"));
    assertFalse(contentNode.hasProperty("dc:creator"));
  }
}
