/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.ext.owner;

import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 * Prerequisites:
                  <value>
                    <object type="org.exoplatform.services.jcr.impl.ext.action.ActionConfiguration">
                      <field  name="eventTypes"><string>addNode</string></field>
                      <field  name="path"><string>/test</string></field>
                      <field  name="isDeep"><boolean>true</boolean></field>
                      <field  name="actionClassName"><string>org.exoplatform.services.jcr.ext.owner.AddOwneableAction</string></field>
                    </object>
                  </value>

 * 
 */
public class AddOwnerTest extends BaseStandaloneTest{
  
  public void testIfOwnerAdd() throws Exception {
    ExtendedNode node = (ExtendedNode)session.getRootNode().addNode("test");
    assertTrue(node.isNodeType("exo:owneable"));
    assertEquals(session.getUserID(), node.getProperty("exo:owner").getString());
  }
  
  public void testIfNotOwnerAdd() throws Exception {
    ExtendedNode node = (ExtendedNode)session.getRootNode().addNode("test2");
    assertFalse(node.isNodeType("exo:owneable"));
  }

}
