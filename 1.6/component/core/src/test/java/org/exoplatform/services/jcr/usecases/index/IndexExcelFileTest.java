/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.usecases.index;

import java.util.Calendar;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.nodetype.NodeType;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Property;

import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.impl.core.value.BinaryValue;
import org.exoplatform.services.jcr.impl.core.value.StringValue;
import org.exoplatform.services.jcr.impl.core.query.lucene.NodeIndexer;
import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

import org.exoplatform.services.document.DocumentReaderService;
import org.exoplatform.container.StandaloneContainer;

/**
 * Created by The eXo Platform SARL.
 * @author <a href="mailto:zagrebin_v@mail.ru">Victor Zagrebin</a>
 * @version $Id: IndexExcelFileTest.java 12841 2007-02-16 08:58:38Z peterit $
 *
 * The test for indexing an excel .xls file
 * which contained within jcr:data property
 */

public class IndexExcelFileTest extends BaseUsecasesTest {

  /**
   * The test for indexing an excel .xls file
   * @throws Exception
   */
  public void testIndexTextPlainFile() throws Exception {
    InputStream is = new FileInputStream("src/test/resources/index/test_index.xls");
    this.assertNotNull("Can not create an input stream from file for indexing",is);
    int size = is.available();
    byte b[] = new byte[size];
    is.read(b);
    is.close();

    Node cmr = root.addNode("cmr").addNode("categories").addNode("cmr");
    Node cool = cmr.addNode("cool","nt:file");
    Node contentNode = cool.addNode("jcr:content", "nt:resource");
    contentNode.setProperty("jcr:encoding", "UTF-8");
    contentNode.setProperty("jcr:data", session.getValueFactory().createValue(new String(b)));
    contentNode.setProperty("jcr:mimeType", "application/excel");
    contentNode.setProperty("jcr:lastModified", session.getValueFactory().createValue(Calendar.getInstance()));
    this.assertNotNull("Can not create a cmr node for indexing",cmr);
    this.assertNotNull("Can not create a cool node for indexing",cool);
    this.assertNotNull("Can not create a contentNode node for indexing",contentNode);

    Node football = cmr.addNode("sports").addNode("football");
    Node news = cmr.addNode("news");
    Node uefa = football.addNode("uefa");
    Node champions_league = football.addNode("champions-league");
    Node economy = news.addNode("economy");
    Node world = news.addNode("world");
    this.assertNotNull("Can not create a football node for indexing",football);
    this.assertNotNull("Can not create a news node for indexing",news);
    this.assertNotNull("Can not create an uefa node for indexing",uefa);
    this.assertNotNull("Can not create a champions_league node for indexing",champions_league);
    this.assertNotNull("Can not create an economy node for indexing",economy);
    this.assertNotNull("Can not create a world node for indexing",world);

    session.save();

    Query q;
    String xpath = "//*[jcr:contains(., 'excel file content')]";
    System.out.println("------------------ QUERY START -----------------------");
    System.out.println(xpath);
    System.out.println("------------------ QUERY END-----------------------");
    q = session.getWorkspace().getQueryManager().createQuery(xpath, Query.XPATH);
    this.assertNotNull("Can not create a query for indexing",q);
    QueryResult result = q.execute();
    System.out.println("Information for query");

    for (NodeIterator it = result.getNodes(); it.hasNext();)
    {
       Node next = it.nextNode();
       System.out.println("Node name: "+next.getName());
       System.out.println("Node type: "+next.getPrimaryNodeType().getName());
       if(next.getPrimaryNodeType().getName().equals("nt:resource"))
       {
          if(next.hasProperty("jcr:data"))
          {
             String mimeType = "" ;
             if(next.hasProperty("jcr:mimeType"))
             {
                mimeType = next.getProperty("jcr:mimeType").getString() ;
             }
             is = next.getProperty("jcr:data").getStream() ;
             StandaloneContainer scontainer = StandaloneContainer.getInstance();
             DocumentReaderService service_ =
                 (DocumentReaderService) scontainer.getComponentInstanceOfType(
                          DocumentReaderService.class);
             this.assertNotNull("Can not create service_ a for indexing",world);
             String found_text = service_.getContentAsText(mimeType, is);
             this.assertNotNull("Can not create found_text for indexing",world);
             is.close();
             System.out.println("------------------ SEARCH TEXT RESULTS START-----------------------");
             System.out.println(found_text);
             System.out.println("------------------ SEARCH TEXT RESULTS END-----------------------");
          }
       }
    }
    //fail("QUERY TEST");
  }
}
