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
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

/**
 * Created by The eXo Platform SAS.
 * 
 * @version $Id$
 */
public class TestSameNameSiblingDelete extends BaseUsecasesTest {

  /**
   * Test Order on Delete.
   *
   * @throws Exception
   */
  public void testOrderOnDelete() throws Exception {
    Node testRoot = root.addNode("testSameNameSiblingDelete");
    Node subNode_1 = testRoot.addNode("resource", "nt:resource");
    subNode_1.setProperty("jcr:data", "data 1");
    subNode_1.setProperty("jcr:mimeType", "text/html");
    subNode_1.setProperty("jcr:lastModified", Calendar.getInstance());
    Node subNode_2 = testRoot.addNode("resource", "nt:resource");
    subNode_2.setProperty("jcr:data", "data 2");
    subNode_2.setProperty("jcr:mimeType", "text/plain");
    subNode_2.setProperty("jcr:lastModified", Calendar.getInstance());

    session.save();
    
    // usecase - /testSameNameSiblingDelete/node[2] will be UPDATED to /testSameNameSiblingDelete/node[1] 
    subNode_1.remove();
    root.save();
    
    String sqlQuery = "SELECT * FROM nt:resource WHERE jcr:mimeType='text/plain'";
    QueryManager manager = session.getWorkspace().getQueryManager();
    Query query = manager.createQuery(sqlQuery, Query.SQL);

    QueryResult queryResult = query.execute();
    NodeIterator iterator = queryResult.getNodes();
    assertTrue("Node expected ",  iterator.getSize() == 1);
    Node node = iterator.nextNode();
    assertEquals("Wrong id ", subNode_2.getUUID(), node.getUUID());
    assertEquals("Wrong path ", subNode_2.getPath(), node.getPath());
  }

}
