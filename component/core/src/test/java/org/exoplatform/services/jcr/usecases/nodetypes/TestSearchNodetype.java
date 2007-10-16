/**
 * Copyright 2001-2007 The eXo Platform SASL   All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */
package org.exoplatform.services.jcr.usecases.nodetypes;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

/**
 * Created by The eXo Platform SASL        .
 * @author <a href="volodymyr.krasnikov@exoplatform.com.ua">Volodymyr Krasnikov</a>
 * @version $Id: TestSearchNodetype.java 17:40:24
 */

public class TestSearchNodetype extends BaseUsecasesTest {
	
	public void testCreateNodetype() throws Exception {

		log.info("!!Adding node with test nodetype");

		Node rootNode = session.getRootNode();
		Node queryNode = rootNode.addNode("queryNode", "nt:unstructured");
		Node someNode = queryNode.addNode("smth", "nt:unstructured");

		if (!someNode.canAddMixin("rma:record"))
			throw new RepositoryException("Cannot add mixin node");
		else {
			someNode.addMixin("rma:record");
			someNode.setProperty("rma:recordIdentifier", "testIdentificator");
			someNode
					.setProperty("rma:originatingOrganization", "testProperty2");
		}

		someNode.addNode("Test1");
		someNode.addNode("Test2");
		
		session.save();
		
		String sqlQuery = "SELECT * FROM rma:record WHERE jcr:path LIKE '/queryNode/smth' ";
		QueryManager manager = session.getWorkspace().getQueryManager();
		Query query = manager.createQuery(sqlQuery, Query.SQL);

		QueryResult queryResult = query.execute();
		NodeIterator iter = queryResult.getNodes();
		
		Node target = null;
		assertTrue( iter.getSize() == 1 );	// check target node for existanse
		assertNotNull( target = iter.nextNode() );
		
		target.getNode("Test1");
		target.getNode("Test2");
				
				
	}
	
	public void testCreateNodetypeWithLogout() throws Exception{

		log.info("!!Adding node with test nodetype - USE LOGOUT/LOGIN");

		Node rootNode = session.getRootNode();
		Node queryNode = rootNode.addNode("queryNode", "nt:unstructured");
		Node someNode = queryNode.addNode("smth", "nt:unstructured");

		if (!someNode.canAddMixin("rma:record"))
			throw new RepositoryException("Cannot add mixin node");
		else {
			someNode.addMixin("rma:record");
			someNode.setProperty("rma:recordIdentifier", "testIdentificator");
			someNode
					.setProperty("rma:originatingOrganization", "testProperty2");
		}

		someNode.addNode("Test1");
		someNode.addNode("Test2");
		
		session.save();
		
		String sqlQuery = "SELECT * FROM rma:record WHERE jcr:path LIKE '/queryNode/smth' ";
		QueryManager manager = session.getWorkspace().getQueryManager();
		Query query = manager.createQuery(sqlQuery, Query.SQL);

		QueryResult queryResult = query.execute();
		NodeIterator iter = queryResult.getNodes();
		
		Node target = null;
		assertTrue( iter.getSize() == 1 );	// check target node for existanse
		assertNotNull( target = iter.nextNode() );
		
		Node testNode1 = target.getNode("Test1");
		Node testNode2 = target.getNode("Test2");
		
		session.logout();
		
		// new login
		session = (SessionImpl) repository.login(credentials, "ws");
		
		Query query2 = manager.createQuery(sqlQuery, Query.SQL);
		QueryResult queryResult2 = query2.execute();
		NodeIterator iter2 = queryResult2.getNodes();
		
		assertTrue( iter2.getSize() == 1 ); // check target node for existanse
		assertNotNull( target = iter2.nextNode() );
		
		testNode1 = target.getNode("Test1");
		testNode2 = target.getNode("Test2");
	
	}
	
	public void testCreateNodetypeWithPreQueryManader() throws Exception{
		log.info("!!Adding node with test nodetype - USE LOGOUT/LOGIN");
		
		String sqlQuery = "SELECT * FROM rma:record WHERE jcr:path LIKE '/queryNode/smth' ";
		QueryManager manager = session.getWorkspace().getQueryManager();
		Query query = manager.createQuery(sqlQuery, Query.SQL);

		Node rootNode = session.getRootNode();
		Node queryNode = rootNode.addNode("queryNode", "nt:unstructured");
		Node someNode = queryNode.addNode("smth", "nt:unstructured");

		if (!someNode.canAddMixin("rma:record"))
			throw new RepositoryException("Cannot add mixin node");
		else {
			someNode.addMixin("rma:record");
			someNode.setProperty("rma:recordIdentifier", "testIdentificator");
			someNode
					.setProperty("rma:originatingOrganization", "testProperty2");
		}

		someNode.addNode("Test1");
		someNode.addNode("Test2");
		
		session.save();
		
		QueryResult queryResult = query.execute();
		NodeIterator iter = queryResult.getNodes();
		
		Node target = null;
		assertTrue( iter.getSize() == 1 );	// check target node for existanse
		assertNotNull( target = iter.nextNode() );
		
		Node testNode1 = target.getNode("Test1");
		Node testNode2 = target.getNode("Test2");
		
		session.logout();
		
		// new login
		session = (SessionImpl) repository.login(credentials, "ws");
		
		Query query2 = manager.createQuery(sqlQuery, Query.SQL);
		QueryResult queryResult2 = query2.execute();
		NodeIterator iter2 = queryResult2.getNodes();
		
		assertTrue( iter2.getSize() == 1 ); // check target node for existanse
		assertNotNull( target = iter2.nextNode() );
		
		testNode1 = target.getNode("Test1");
		testNode2 = target.getNode("Test2");

	}
}
