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

package org.exoplatform.services.jcr.impl.core.query;

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;

import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;

import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.query.lucene.FieldNames;

/**
 * Created by The eXo Platform SAS
 * Author : Sergey Karpenko <sergey.karpenko@exoplatform.com.ua>
 * @version $Id: $
 */

public class TestDateSearch extends BaseQueryTest {
  
  public static String fileName = "testDateSearch";
 
  public void testSearchDate()throws Exception {
    File file = new File("src/test/resources/test.xls");
    assertTrue("/test/resources/ArabicUTF8.txt not found",file.exists());
    
    FileInputStream fis = new FileInputStream(file);
    
    NodeImpl node = (NodeImpl)root.addNode(fileName,"nt:file");
    NodeImpl cont = (NodeImpl)node.addNode("jcr:content","nt:resource");
    cont.setProperty("jcr:mimeType", "application/excel");
    cont.setProperty("jcr:lastModified", Calendar.getInstance());
    
    cont.setProperty("jcr:data", fis);
    root.save();
    
    String word = "2005-10-02".toLowerCase();//"ronaldo";//-10-06T00:00:00.000+0300
    
    //Check is node indexed
    Document doc = getDocument(cont.getInternalIdentifier(), false);
    assertNotNull("Node is not indexed",doc);
    System.out.println("its doc "+ doc);
    
    
    IndexReader reader = defaultSearchIndex.getIndexReader(false);
    IndexSearcher is = new IndexSearcher(reader);
    TermQuery query = new TermQuery(new Term(FieldNames.FULLTEXT, word )); 
    Hits result = is.search(query);
    assertEquals(1,result.length());
    
    QueryManager qman = this.workspace.getQueryManager();
    
    Query q = qman.createQuery("SELECT * FROM nt:resource "
        + " WHERE  CONTAINS(., '" + word + "')", Query.SQL); 
    QueryResult res = q.execute();
    assertEquals(1,res.getNodes().getSize());
  }

}
