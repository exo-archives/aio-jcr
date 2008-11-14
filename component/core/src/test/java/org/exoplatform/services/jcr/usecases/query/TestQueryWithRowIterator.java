/***************************************************************************
 * Copyright 2001-2008 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.usecases.query;

import javax.jcr.Node;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

/**
 * Created by The eXo Platform SARL
 * Author : Nhu Dinh Thuan
 *          nhudinhthuan@exoplatform.com
 * Nov 14, 2008  
 */
public class TestQueryWithRowIterator extends BaseUsecasesTest {
  private String s1 = "Của ông đây";
  public void testExcerpt() throws Exception {
    System.out.println("\n\n----------Test Search with Row Iterator");
    Node node1 = root.addNode("ông", "exo:article");
    node1.setProperty("exo:title", "abc");
    node1.setProperty("exo:text", s1);

    Node node2 = root.addNode("Node2", "exo:article");
    node2.setProperty("exo:title", "Node2");
    node2.setProperty("exo:text", "Tai vi 1 nguoi");

    Node node3 = root.addNode("Node3", "exo:article");
    node3.setProperty("exo:title", "Node3");
    node3.setProperty("exo:text", "Ho ho ha ha");

    session.save();
    
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    Query q1 = queryManager.createQuery(
        "select * from nt:base where jcr:path like '/ông'", Query.SQL);
    QueryResult result1 = q1.execute();
    for (RowIterator it = result1.getRows(); it.hasNext();) {
      Row row = it.nextRow();
      String jcrPath = row.getValue("jcr:path").getString();
      try {
        Node node = (Node)session.getItem(jcrPath);
        assertNotNull(node);
      } catch (Exception e) {
        e.printStackTrace();
        fail();
      }
    }
  }
}
