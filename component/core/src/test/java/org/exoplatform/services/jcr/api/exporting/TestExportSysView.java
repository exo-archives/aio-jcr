/*
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail.
 */
package org.exoplatform.services.jcr.api.exporting;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.jcr.impl.core.value.StringValue;
import org.exoplatform.services.xml.querying.InvalidSourceException;
import org.exoplatform.services.xml.querying.InvalidStatementException;
import org.exoplatform.services.xml.querying.QueryRunTimeException;
import org.exoplatform.services.xml.querying.UniFormTransformationException;
import org.exoplatform.services.xml.querying.XMLQuery;
import org.exoplatform.services.xml.querying.XMLQueryingService;
import org.exoplatform.services.xml.querying.helper.SimpleStatementHelper;
import org.exoplatform.services.xml.querying.helper.XMLDataManager;
import org.xml.sax.SAXException;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: TestExportSysView.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class TestExportSysView extends JcrAPIBaseTest {

  private XMLQueryingService xmlQueryingService;

  public void initRepository() throws RepositoryException {
    Node root = session.getRootNode();
    Node file = root.addNode("childNode", "nt:folder").addNode("childNode2", "nt:file");

    Node contentNode = file.addNode("jcr:content", "nt:resource");
    contentNode.setProperty("jcr:data", session.getValueFactory().createValue("this is the content", PropertyType.BINARY));
    contentNode.setProperty("jcr:mimeType", session.getValueFactory().createValue("text/html"));
    contentNode.setProperty("jcr:lastModified", session.getValueFactory().createValue(Calendar.getInstance()));

    session.save();

    xmlQueryingService = (XMLQueryingService) container.getComponentInstanceOfType(XMLQueryingService.class);
  }

  public void tearDown() throws Exception {
    Node root = session.getRootNode();
    root.getNode("childNode").remove();
    session.save();
    
    super.tearDown();
  }

  public void testWithOutputStream() throws RepositoryException, IOException,
      InvalidSourceException, InvalidStatementException, QueryRunTimeException, UniFormTransformationException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    session.exportSystemView("/childNode", out, false, false);
    byte[] bArray = out.toByteArray();

    log.debug("testWithOutputStream export result ["+new String(bArray)+"]");
//    System.out.println(""+new String(bArray));

    SimpleStatementHelper sHelper = xmlQueryingService.createStatementHelper();
    XMLDataManager dManager = xmlQueryingService.createXMLDataManager();
    XMLQuery query = xmlQueryingService.createQuery();
/*    
    query.setInputStream(new ByteArrayInputStream(bArray));
    query.prepare(sHelper.select("//childNode/sv:node"));
    query.execute();
    NodeList nodes = dManager.toFragment(query.getResult()).getAsNodeList();

    String[] names = {"sv:root", "childNode", "jcr:content", "childNode2"};
    for (int i = 0; i < nodes.getLength(); i++) {
      Element node = (Element) nodes.item(i);
      String name = node.getAttribute("sv:name");
      assertTrue(ArrayUtils.contains(names, name));
    }

    query.prepare(sHelper.select("//sv:property[@sv:name='jcr:mimeType']"));
    query.execute();
    nodes = dManager.toFragment(query.getResult()).getAsNodeList();
    assertEquals(1, nodes.getLength());
    for (int i = 0; i < nodes.getLength(); i++) {
      Element node = (Element) nodes.item(i);
      String type = node.getAttribute("sv:type");
      if ("sv:binary".equals(type)) {
        assertEquals(new String(Base64.encodeBase64("this is the content".getBytes())),
            node.getFirstChild().getNodeValue());
      } else if ("sv:string".equals(type)) {
        assertEquals("text/html", node.getFirstChild().getNodeValue());
      } else {
//        fail("incorrect property type");
      }
    }
*/    
    out = new ByteArrayOutputStream();
    session.exportSystemView("/childNode", out, true, true);
    bArray = out.toByteArray();
    log.debug("testWithOutputStream export result ["+new String(bArray)+"]");

/*    
    bArray = out.toByteArray();
    query.setInputStream(new ByteArrayInputStream(bArray));
    query.prepare(sHelper.select("//sv:property"));
    query.execute();
    nodes = dManager.toFragment(query.getResult()).getAsNodeList();
    assertEquals(2, nodes.getLength());
    query.prepare(sHelper.select("//sv:node"));
    query.execute();
    nodes = dManager.toFragment(query.getResult()).getAsNodeList();
    assertEquals(1, nodes.getLength());
    for (int i = 0; i < nodes.getLength(); i++) {
      Element node = (Element) nodes.item(i);
      assertEquals("childNode", node.getAttribute("sv:name"));
    }
*/
  }

  public void testWithContentHandler() throws RepositoryException, SAXException {
    MockContentHandler mock = new MockContentHandler();
    mock = new MockContentHandler();
    session.exportSystemView("/childNode", mock, false, true);
    assertEquals(1, mock.nodes);
    assertEquals(2, mock.properties);
  }

}
