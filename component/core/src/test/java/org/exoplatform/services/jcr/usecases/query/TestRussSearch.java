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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

/**
 * Created by The eXo Platform SAS
 * Author : Sergey Karpenko <sergey.karpenko@exoplatform.com.ua>
 * @version $Id: $
 */

public class TestRussSearch extends BaseUsecasesTest {
  
  private static String[] input;
    
  public void testQuery() throws Exception {
    
    File file = new File("src/test/resources/russ.txt");
    assertTrue("/test/resources/ArabicUTF8.txt not found", file.exists());

    FileInputStream is = new FileInputStream(file);

    //Create nodes
    Session session = repository.getSystemSession(repository.getSystemWorkspaceName());
    
    Node n = session.getRootNode().addNode("test_node","nt:file");
    Node content = n.addNode("jcr:content","nt:resource");
    content.setProperty("jcr:data", is);
    content.setProperty("jcr:encoding", "cp1251");
    content.setProperty("jcr:lastModified", Calendar.getInstance());
    content.setProperty("jcr:mimeType", "text/plain");    
    session.save();
    
    String sqlQuery = "select * from nt:resource where CONTAINS(., '\u0442\u0435\u0441\u0442')"; 

    QueryManager manager = session.getWorkspace().getQueryManager();
    Query query = manager.createQuery(sqlQuery,Query.SQL) ;
    QueryResult queryResult = query.execute() ;
    NodeIterator iter = queryResult.getNodes() ;
      
    assertEquals(1, iter.getSize());
    
    assertEquals("jcr:content",iter.nextNode().getName());
  }
  }
