/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.usecases.query;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

/**
 * Created by The eXo Platform SARL
 * Author : Anh Nguyen
 *          ntuananh.vn@gmail.com
 * Nov 15, 2007  
 */

public class TestI18NSQLQuery extends BaseUsecasesTest {
  
  private static String[] input;
    
	public void testI18NQueryPath() throws Exception {
	  
	  //Create nodes
	  Session session = repository.getSystemSession(repository.getSystemWorkspaceName());
    
    input = readInputText("testi18n.txt").split("\n");
    Node rootNode = session.getRootNode();
    
    for (int i = 0; i < input.length; i++) {
      String content = input[i];
      rootNode.addNode(content, "nt:unstructured");      
    }
    rootNode.save();
        
    
		//Do Query by jcr:path
    for (int i = 0; i < input.length; i++) {    
  	  String searchInput = input[i] ;
  	  
  	  String sqlQuery = "select * from nt:unstructured where jcr:path like '/"+ searchInput +"' " ;

  	  QueryManager manager = session.getWorkspace().getQueryManager() ;
      Query query = manager.createQuery(sqlQuery,Query.SQL) ;
      QueryResult queryResult = query.execute() ;
      NodeIterator iter = queryResult.getNodes() ;
      
      assertEquals(1, iter.getSize());
    }
    
	}
	
	public void testI18NQueryProperty() throws Exception {

	  //We have problem with unicode chars, in Vietnamese or French, the result alway empty
	  
     
    //Create nodes
    Session session = repository.getSystemSession(repository.getSystemWorkspaceName());
    
    Node rootNode = session.getRootNode();    
    for (int i = 0; i < input.length; i++) {
      String content = input[i];
      
      Node childNode = rootNode.addNode(String.valueOf(i), "nt:unstructured");
      childNode.setProperty("exo:testi18n", content);
      
    }
    rootNode.save();
    
    
    //Do Query by properties
    for (int i = 0; i < input.length; i++) {

      String searchInput = input[i];
      
      String sqlQuery = "select * from nt:unstructured where exo:testi18n like '"+ searchInput +"' " ;
      
      QueryManager manager = session.getWorkspace().getQueryManager() ;
      Query query = manager.createQuery(sqlQuery,Query.SQL) ;
      QueryResult queryResult = query.execute() ;
      
      NodeIterator iter = queryResult.getNodes() ;
      assertEquals(1, iter.getSize());
    }
    
  }
	
	private static String readInputText(String fileName) {

    try {
      InputStream is = TestI18NSQLQuery.class.getResourceAsStream(fileName);
      ByteArrayOutputStream output = new ByteArrayOutputStream();

      int r = is.available();
      byte[] bs = new byte[r];
      while (r > 0) {
        r = is.read(bs);
        if (r > 0) {
          output.write(bs, 0, r);
        }
        r = is.available();
      }
      is.close();
      return output.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
