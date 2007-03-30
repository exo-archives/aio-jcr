/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.usecases.index;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.exoplatform.services.jcr.impl.core.value.BinaryValue;
import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

/**
 * Created by The eXo Platform SARL.
 * 
 * @author <a href="mailto:zagrebin_v@mail.ru">Victor Zagrebin</a>
 * @version $Id: SlowListTest.java 12841 2007-02-16 08:58:38Z peterit $ The
 *          test for indexing an excel .xls file which contained within jcr:data
 *          property
 */

public class SlowListTest extends BaseUsecasesTest {

  /**
   * The test for indexing an excel .xls file
   * 
   * @throws Exception
   */
  public void testIndexTextPlainFile() throws Exception {
    // variables for the execution time
    long start, end;
    InputStream is = new FileInputStream("src/test/resources/index/test_index.xls");
    this.assertNotNull("Can not create an input stream from file for indexing", is);
    int size = is.available();
    byte b[] = new byte[size];
    is.read(b);
    is.close();
    Node test = root.addNode("cms2").addNode("test");
    start = System.currentTimeMillis(); // to get the time of start
    this.assertNotNull("Can not create a test node for indexing", test);
    for (int i = 0; i < 111; i++) {
      String name = new String("nnn-" + i);
      Node cool = test.addNode(name, "nt:file");
      Node contentNode = cool.addNode("jcr:content", "nt:resource");
      contentNode.setProperty("jcr:encoding", "UTF-8");
      contentNode.setProperty("jcr:data", session.getValueFactory().createValue(new String(b)));
      contentNode.setProperty("jcr:mimeType", "application/excel");
      contentNode.setProperty("jcr:lastModified", session.getValueFactory().createValue(
          Calendar.getInstance()));
      this.assertNotNull("Can not create a cool node for indexing", cool);
      this.assertNotNull("Can not create a contentNode node for indexing", contentNode);
    }
    end = System.currentTimeMillis();
    System.out.println("The time of the adding of 111 nodes: " + ((end - start) / 1000) + " sec");
    session.save();
    session.save();
    start = System.currentTimeMillis();
    Query q;
    String xpath = "/jcr:root/cms2/test//*";

    q = session.getWorkspace().getQueryManager().createQuery(xpath, Query.XPATH);
    this.assertNotNull("Can not create a query for indexing", q);
    QueryResult result = q.execute();
    end = System.currentTimeMillis();

    System.out.println(xpath);
    System.out.println("The time of query execution : " + ((end - start) / 1000) + " sec");

    System.out.println("----------- Information for query");

    start = System.currentTimeMillis();
    for (NodeIterator it = result.getNodes(); it.hasNext();) {
      Node next = it.nextNode();
      // System.out.println("Node name: "+next.getName());
    }
    end = System.currentTimeMillis();
    System.out.println("The time of getNodes() call 1 : " + ((end - start) / 1000) + " sec");

    start = System.currentTimeMillis();
    for (NodeIterator it = result.getNodes(); it.hasNext();) {
      Node next = it.nextNode();
      // System.out.println("Node name: "+next.getName());
    }
    end = System.currentTimeMillis();
    System.out.println("The time of getNodes() call 2 : " + ((end - start) / 1000) + " sec");

    // -------------------------------------------------------------------------------------
    Node n2 = test.addNode("fff");
    session.save();
    result = q.execute();
    start = System.currentTimeMillis();
    for (NodeIterator it = result.getNodes(); it.hasNext();) {
      Node next = it.nextNode();
      // System.out.println("Node name: "+next.getName());
    }
    end = System.currentTimeMillis();
    System.out.println("The time of getNodes() call 3 : " + ((end - start) / 1000) + " sec");

    start = System.currentTimeMillis();
    for (NodeIterator it = result.getNodes(); it.hasNext();) {
      Node next = it.nextNode();
      // System.out.println("Node name: "+next.getName());
    }
    end = System.currentTimeMillis();
    System.out.println("The time of getNodes() call 4 : " + ((end - start) / 1000) + " sec");

    // [PN] 21.07.06 hasn't fails that no fail
    //fail("QUERY TEST"); // Only for the view of intermediate results
  }
}
