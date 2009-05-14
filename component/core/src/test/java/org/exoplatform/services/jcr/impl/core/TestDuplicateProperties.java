/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.impl.core;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;

import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.core.CredentialsImpl;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="work.visor.ck@gmail.com">Dmytro Katayev</a>
 *
 * May 14, 2009  
 */
public class TestDuplicateProperties extends JcrImplBaseTest {
  
  // Reproduces the issue described in http://jira.exoplatform.org/browse/JCR-953 
  public void testDuplicateProperties() throws Exception {
    
    String nodeName = "testDuplNod";
    String propName = "jcr:mimeType";
    
    root.addNode(nodeName);
    root.save();
    
    Session session1 = repository.login(new CredentialsImpl("exo1", "exo1".toCharArray()));
    Session session2 = repository.login(new CredentialsImpl("exo2", "exo2".toCharArray()));
    
    Node node1 = session1.getRootNode().getNode(nodeName);
    node1.setProperty(propName, "text/xml");
    
    Node node2 = session2.getRootNode().getNode(nodeName);
    node2.setProperty(propName, "text/html");
    node2.save();
    
    PropertyIterator iter = node1.getProperties();
    int propCount = 0;
    while(iter.hasNext()){
      Property prop = (Property) iter.next();
      if(prop.getName().equals(propName)){
        propCount++;
      }
    }
    assertEquals(1, propCount);
  }

}
