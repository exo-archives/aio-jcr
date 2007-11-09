/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.DavConst;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class DavUtil {
  
  private static Log log = ExoLogger.getLogger("jcr.DavUtil");
  
  public static Node getChildNode(Node node, String childName) {
    NodeList nodes = node.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node curNode = nodes.item(i);
      if (curNode.getLocalName() != null && curNode.getLocalName().equals(childName)) {
        return curNode;
      }
    }
    return null;
  }    
    
  public static Document GetDocumentFromInputStream(InputStream in) throws Exception {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    byte []buff = new byte[2048];
    while (true) {
      int readed = in.read(buff);
      if (readed < 0) {
        break;
      }
      outStream.write(buff, 0, readed);
    }
    
    byte []datas = outStream.toByteArray();    
    ByteArrayInputStream inStream = new ByteArrayInputStream(datas);
    
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);        
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(inStream);
    return document;
  }
    
  public static Document GetDocumentFromRequest(HttpServletRequest request) throws Exception {
    Document document = null;
    String contentLength = request.getHeader(DavConst.Headers.CONTENTLENGTH);        
    if (contentLength != null && !contentLength.equals("0")) {
      
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      InputStream inS = request.getInputStream();
      
      byte []buffer = new byte[1024];
      while (true) {
        int readed = inS.read(buffer);
        if (readed < 0) {
          break;
        }
        outStream.write(buffer, 0, readed);
      }
      
      String inXML = new String(outStream.toByteArray());
      log.info("- XML ------------------------------------");
      log.info(inXML);
      log.info("------------------------------------------");
      
      InputStream ins2 = new ByteArrayInputStream(outStream.toByteArray());
            
      document = GetDocumentFromInputStream(ins2);
      //document = GetDocumentFromInputStream(request.getInputStream());
    }      
    return document;
  }    

  public static Document getDomDocument() throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();    
    return builder.newDocument();
  }
  
  public static byte []getSerializedDom(Element element) throws Exception {
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    
    DOMSource source = new DOMSource(element.getOwnerDocument());
    
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    StreamResult resultStream = new StreamResult(outStream);
    
    transformer.transform(source, resultStream);
    
    return outStream.toByteArray();
  }
  
  public static void tuneSession(Session session, ArrayList<String> lockTokens) {
    String []sessionTokens = session.getLockTokens();
    
    ArrayList<String> sessionTokensList = new ArrayList<String>();
    for (int i = 0; i < sessionTokens.length; i++) {
      sessionTokensList.add(sessionTokens[i]);
    }
    
    for (int i = 0; i < lockTokens.size(); i++) {
      String token = lockTokens.get(i);
      if (!sessionTokensList.contains(token)) {
        session.addLockToken(token);
      }      
    }
  }
    
}
