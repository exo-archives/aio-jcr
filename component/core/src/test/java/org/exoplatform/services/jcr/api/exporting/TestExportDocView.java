/*
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail. 
 */

package org.exoplatform.services.jcr.api.exporting;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.jcr.impl.core.value.BinaryValue;
import org.exoplatform.services.jcr.impl.core.value.StringValue;
import org.xml.sax.SAXException;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: TestExportDocView.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class TestExportDocView extends JcrAPIBaseTest {

  //private XMLQueryingService xmlQueryingService;

  public void initRepository() throws RepositoryException {

    Node root = session.getRootNode();
    Node file = root.addNode("childNode", "nt:folder").addNode("childNode2", "nt:file");

    Node contentNode = file.addNode("jcr:content", "nt:resource");
    try {
      contentNode.setProperty("jcr:data", new BinaryValue("this is the content"));
      contentNode.setProperty("jcr:mimeType", new StringValue("text/html"));
    } catch (IOException e) {
      throw new RepositoryException(e);
    }
    contentNode.setProperty("jcr:lastModified", session.getValueFactory().createValue(Calendar.getInstance()));
    log.debug(">> save childNode START");
    session.save();
    log.debug(">> save childNode END");

    //xmlQueryingService = (XMLQueryingService) container.getComponentInstanceOfType(XMLQueryingService.class);
  }

  public void tearDown() throws Exception {
    log.debug(">> get rootNode on TD START");

  	Node root = session.getRootNode();
    log.debug(">> get childNode on TD START");
    //session.getItem("/childNode");
    root.getNode("childNode").remove();
    log.debug(">> get childNode on TD END ");

    session.save();
    
    super.tearDown();
  }

  public void testWithOutputStream() throws RepositoryException, IOException
    //InvalidSourceException,InvalidStatementException, QueryRunTimeException, UniFormTransformationException 
    {
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();

//    System.out.println(">>"+session.getItem("/childNode/jcr:created"));
    log.debug(">> get /childNode >>"+session.getItem("/childNode"));
    Node node1 = (Node)session.getItem("/childNode");
    log.debug(">> END get /childNode >>"+session.getItem("/childNode"));

    //System.out.println(">>"+node1.getProperty("jcr:created").getDate());
    //+" "+props.nextProperty().getString()
    PropertyIterator props = node1.getProperties();
    while(props.hasNext())
      System.out.println("props >>"+props.nextProperty().getPath());


    session.exportDocumentView("/childNode", out, false, false);
    byte[] bArray = out.toByteArray();
//    System.out.println(""+new String(bArray));
    
    //SimpleStatementHelper sHelper = xmlQueryingService.createStatementHelper();
    //XMLDataManager dManager = xmlQueryingService.createXMLDataManager();
    //XMLQuery query = xmlQueryingService.createQuery();
    
    // [PN] 19.07.06 There are problem with XMLQueryingService work
//    String sba = new String(bArray).trim();
//    log.info("bArray: [" + sba + "]");
//    sba = sba.substring("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".length()); // !!! Otherwise XMLQueryingService don't work
//    
//    query.setInputStream(new ByteArrayInputStream(sba.getBytes()));
//    query.prepare(sHelper.select("//childNode"));
//    query.execute();
//    NodeList nodes = dManager.toFragment(query.getResult()).getAsNodeList();
//    assertEquals(1, nodes.getLength());
//    query.prepare(sHelper.select("//*[name()='childNode2']"));
//    query.execute();
//    nodes = dManager.toFragment(query.getResult()).getAsNodeList();
//    assertEquals(1, nodes.getLength());
// 
//    query.prepare(sHelper.select("//jcr:content"));
//    query.execute();
//    nodes = dManager.toFragment(query.getResult()).getAsNodeList();
//    assertEquals(1, nodes.getLength());
//    for (int i = 0; i < nodes.getLength(); i++) {
//      Element node = (Element) nodes.item(i);
//      String value = node.getAttribute("jcr:data");
//      if (!(new String(Base64.encodeBase64("this is the content".getBytes())).
//          equals(value) )) {
//        fail("incorrect property value");
//      }
//    }
//
//    out = new ByteArrayOutputStream();
//    session.exportDocumentView("/childNode", out, true, false);
//    bArray = out.toByteArray();
//    query.setInputStream(new ByteArrayInputStream(bArray));
//    query.prepare(sHelper.select("//jcr:content"));
//    query.execute();
//    nodes = dManager.toFragment(query.getResult()).getAsNodeList();
//    assertEquals(1, nodes.getLength());
//
//    out = new ByteArrayOutputStream();
//    session.exportDocumentView("/childNode", out, true, true);
//    bArray = out.toByteArray();
//    query.setInputStream(new ByteArrayInputStream(bArray));
//    query.prepare(sHelper.select("childNode"));
//    query.execute();
//    nodes = dManager.toFragment(query.getResult()).getAsNodeList();
//    assertEquals(1, nodes.getLength());
//    for (int i = 0; i < nodes.getLength(); i++) {
//      Element node = (Element) nodes.item(i);
//      //for(int j=0; j<node.getAttributes().getLength(); j++)
//      //  System.out.println("NODE >> "+node.getAttributes().item(j).getNodeName());
//
//      assertTrue(node.getAttributes().getLength()>=7);
//    }
//    query.prepare(sHelper.select("//*[name()='childNode2']"));
//    query.execute();
//    nodes = dManager.toFragment(query.getResult()).getAsNodeList();
//    assertEquals(0, nodes.getLength());
  }

  public void testWithContentHandler() throws RepositoryException, SAXException {

    MockContentHandler mock = new MockContentHandler();

    mock = new MockContentHandler();
    session.exportDocumentView("/childNode", mock, false, true);
    assertEquals(1, mock.docElement);
  }


//  public void testEmpty() {
//  }
}
