/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.ext.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.BaseStandaloneTest;

public class MetaDataActionTest extends BaseStandaloneTest {

  private Node rootNode = null;

  public void setUp() throws Exception {
    super.setUp();

    if(session.getRootNode().hasNode("MetaDataActionTest"))
      rootNode = session.getRootNode().getNode("MetaDataActionTest");
    else
      rootNode = session.getRootNode().addNode("MetaDataActionTest");
  }


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
//    ActionConfiguration ac = new ActionConfiguration(
//        "org.exoplatform.services.jcr.ext.metadata.AddMetadataAction",
//        "addProperty,changeProperty", "/test,/exo:test1", true,
//        null, "nt:resource", null);
//    List actionsList = new ArrayList();
//    ActionsConfig actions = new ActionsConfig();
//    actions.setActions(actionsList);
//    actionsList.add(ac);
//    InitParams params = new InitParams();
//    ObjectParameter op = new ObjectParameter();
//    op.setObject(actions);
//    op.setName("actions");
//    params.addParameter(op);
//
//    AddActionsPlugin aap = new AddActionsPlugin(params);
//    SessionActionCatalog catalog = (SessionActionCatalog)
//      container.getComponentInstanceOfType(SessionActionCatalog.class);
//    catalog.clear();
//    catalog.addPlugin(aap);
    
    String path = "src/test/resources/test_index.xls";
    if (!new File(path).exists()){
      path = "component/ext/" + path;
    }
    InputStream is = new FileInputStream(path);

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
  public void testAutoAddMetadata() throws Exception {
    Node contentNode = rootNode.addNode("setmetadata");
    rootNode.save();
    assertTrue(contentNode.hasProperty("dc:creator"));
    assertTrue(contentNode.hasProperty("dc:date"));
    assertEquals(session.getUserID(), contentNode.getProperty("dc:creator").getValues()[0].getString());
//    Calendar date = contentNode.getProperty("dc:date").getValues()[0].getDate();
//    contentNode.setProperty("dc:subject", new String[]{"subject"});
//    assertFalse(date.equals(contentNode.getProperty("dc:date").getValues()[0].getDate()));
  }
}
