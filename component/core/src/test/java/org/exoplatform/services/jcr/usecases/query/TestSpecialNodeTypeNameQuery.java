/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

/**
 * Created by The eXo Platform SAS Author : Do Ngoc Anh anh.do@exoplatform.com
 * anhdn86@gmail.com Dec 8, 2008
 */
public class TestSpecialNodeTypeNameQuery extends BaseUsecasesTest {

	public void testOnSpecialNodeType() throws Exception {
		Session session = repository.getSystemSession(repository
				.getSystemWorkspaceName());
		Node root = session.getRootNode();
		Node test = root.addNode("test", "nt:unstructured");
		Node articleNode = test.addNode("article", "exo:article");
		articleNode.setProperty("exo:title", "a article");
		articleNode.setProperty("exo:text", " text");
		articleNode.setProperty("exo:summary", " summary");
		session.save();
		
		String sqlQuery = "select * from exo:rss-enable where jcr:path like '/test/%'";
		QueryManager manager = session.getWorkspace().getQueryManager();
		Query query = manager.createQuery(sqlQuery, Query.SQL);
		QueryResult result = query.execute();		
		assertEquals(1, result.getNodes().getSize());
		
		test.remove();
		session.save();
	}

	public void testCombainationSearch() throws Exception {
		Session session = repository.getSystemSession(repository
				.getSystemWorkspaceName());
		Node root = session.getRootNode();
		Node test = root.addNode("test", "nt:unstructured");
		Node articleNode = test.addNode("article", "exo:article");
		articleNode.setProperty("exo:title", "exo");
		articleNode.setProperty("exo:text", "sea");
		articleNode.setProperty("exo:summary", "summary");
		session.save();
		
		String sqlQuery = "select * from nt:base where jcr:primaryType='exo:article' OR jcr:mixinTypes='exo:rss-enable' AND contains(., 'exo sea')";
		QueryManager manager = session.getWorkspace().getQueryManager();
		Query query = manager.createQuery(sqlQuery, Query.SQL);
		QueryResult result = query.execute();
		NodeIterator iterator = result.getNodes();
		assertEquals(1, iterator.getSize());
		
		test.remove();
		session.save();
	}

	public void testSearchPath() throws Exception {
    Node r = root.addNode("Contribution").addNode("A-Publier");
    Node test = r.addNode("test", "nt:unstructured");
    test.addMixin("exo:type_commentaire");
    Node articleNode = test.addNode("article", "exo:article");
    articleNode.setProperty("exo:title", "exo");
    articleNode.setProperty("exo:text", "sea");
    articleNode.setProperty("exo:summary", "summary");
    session.save();
    
    String sqlQuery = "select * from exo:type_commentaire where jcr:path like '/Contribution/Archives/%' OR jcr:path like '/Contribution/A-Publier/%' ";//AND jcr:path like '/Contribution/A-Valider/%'";
    QueryManager manager = session.getWorkspace().getQueryManager();
    Query query = manager.createQuery(sqlQuery, Query.SQL);
    QueryResult result = query.execute();
    NodeIterator iterator = result.getNodes();
    assertEquals(1, iterator.getSize());
    
    test.remove();
    session.save();
  }

	

}



