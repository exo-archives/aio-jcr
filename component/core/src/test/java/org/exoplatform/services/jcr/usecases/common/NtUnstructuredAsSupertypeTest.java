/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.usecases;

import javax.jcr.Node;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: NtUnstructuredAsSupertypeTest.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class NtUnstructuredAsSupertypeTest extends BaseUsecasesTest {

  public void testMultiValue() throws Exception {
    Node rootNode = session.getRootNode();
    Node tNode = rootNode.addNode("testNode","exojcrtest:sub1");
    tNode.setProperty("multi",new String[]{"v1","v2"});
    tNode.setProperty("multi",new String[]{"v1"});
    rootNode.save();

    
  }
  public void testSingleValue() throws Exception {
    Node rootNode = session.getRootNode();
    Node tNode = rootNode.addNode("testNode","exojcrtest:sub1");
    tNode.setProperty("single","v1");
    rootNode.save();
  }
  public void testSingleandMultiValue() throws Exception {
    Node rootNode = session.getRootNode();
    Node tNode = rootNode.addNode("testNode","exojcrtest:sub1");
    tNode.setProperty("single","v1");
    tNode.setProperty("multi",new String[]{"v1","v2"});
    tNode.setProperty("multi",new String[]{"v1"});
    rootNode.save();
  }
  
}
