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

package org.exoplatform.services.jcr.usecases.query;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

/**
 * Created by The eXo Platform SAS
 * Author : Sergey Karpenko <sergey.karpenko@exoplatform.com.ua>
 * @version $Id: $
 */

public class TestQuerySameNamePath extends BaseUsecasesTest{
  
  
  public void testQueryPathElemIndex() throws Exception {
      String nodeName = "testNodeHi";
      try {
        Node folder = root.addNode("Departments");

        Node node = folder.addNode(nodeName, "nt:file");
        node.addMixin("mix:lockable");
        NodeImpl cont = (NodeImpl) node.addNode("jcr:content", "nt:resource");
        cont.setProperty("jcr:mimeType", "text/plain");
        cont.setProperty("jcr:lastModified", Calendar.getInstance());
        cont.setProperty("jcr:data", "test text");
        

        Node snode = folder.addNode(nodeName, "nt:file");
        NodeImpl scont = (NodeImpl) snode.addNode("jcr:content", "nt:resource");
        scont.setProperty("jcr:mimeType", "text/plain");
        scont.setProperty("jcr:lastModified", Calendar.getInstance());
        scont.setProperty("jcr:data", "test text second");
        
        Node thirdnode = folder.addNode(nodeName, "nt:file");
        NodeImpl thirdcont = (NodeImpl) thirdnode.addNode("jcr:content", "nt:resource");
        thirdcont.setProperty("jcr:mimeType", "text/plain");
        thirdcont.setProperty("jcr:lastModified", Calendar.getInstance());
        thirdcont.setProperty("jcr:data", "test text second");
        
        root.save();
        
        String sql = "SELECT *  FROM nt:file WHERE jcr:path = '/Departments/testNodeHi[3]'";
        
        QueryManager manager = session.getWorkspace().getQueryManager();
        Query query = manager.createQuery(sql, Query.SQL);

        QueryResult r = query.execute();
        NodeIterator it = r.getNodes();

        assertEquals(1, it.getSize());
        Node n = it.nextNode();
        
        assertTrue(n.isSame(thirdnode));
        
        
      } catch (InvalidQueryException e) {
        // e.printStackTrace();
        fail(e.getMessage());
      } catch (RepositoryException e) {
        // e.printStackTrace();
        fail(e.getMessage());
      }
    }

}
