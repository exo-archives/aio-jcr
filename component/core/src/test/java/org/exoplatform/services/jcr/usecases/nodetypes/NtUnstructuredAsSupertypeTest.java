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
package org.exoplatform.services.jcr.usecases.nodetypes;

import javax.jcr.Node;

import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

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
