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
package org.exoplatform.services.jcr.usecases;

import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.Node;

import org.exoplatform.services.jcr.impl.core.NodeImpl;



/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 
 *
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a> 
 * @version $Id: TestAddAndRead.java 111 2008-11-11 11:11:11Z serg $
 */
public class TestAddAndRead extends BaseUsecasesTest {

  
  public void testFirst() throws Exception {
    
    InputStream fis = TestAddAndRead.class.getResourceAsStream("/Java_2.pdf");
    
    Node node = root.addNode("testNode","nt:file");
    NodeImpl cont = (NodeImpl) node.addNode("jcr:content", "nt:resource");
    cont.setProperty("jcr:mimeType", "text/plain");
    cont.setProperty("jcr:lastModified", Calendar.getInstance());
    cont.setProperty("jcr:encoding", "UTF-8");

    cont.setProperty("jcr:data", fis);
    root.save();
    
    InputStream is = node.getNode("jcr:content").getProperty("jcr:data").getStream();
    
    node.remove();
    root.save();
    
    int i = 0;
    
    Thread.sleep(100000);
  }
  
  
}
