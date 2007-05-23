/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.property.WebDavProperty;
import org.exoplatform.services.webdav.common.response.DavStatus;
import org.exoplatform.services.webdav.common.response.MultiStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: DavUtil.java 12899 2007-02-20 15:13:30Z gavrikvetal $
 */

public class DavUtil {
  
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
      document = GetDocumentFromInputStream(request.getInputStream());
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
  
  public static void sendMultistatus(HttpServletResponse response, MultiStatus multistatus) throws Exception {
    Document responseDocument = getDomDocument();

    Element multistatusElement = multistatus.toXml(responseDocument);    
    byte []xmlBytes = getSerializedDom(multistatusElement);
    
    response.setStatus(DavStatus.MULTISTATUS);      
    response.addHeader(DavConst.Headers.CONTENTTYPE, "text/xml;charset=UTF-8 ");
    response.addHeader(DavConst.Headers.CONTENTLENGTH, String.format("%s", xmlBytes.length));
    
    response.getOutputStream().write(xmlBytes, 0, xmlBytes.length);
  }
  
  public static void sendSingleProperty(HttpServletResponse response, WebDavProperty property) throws Exception {
    Document responseDocument = getDomDocument();
    
    Element propElement = responseDocument.createElementNS(DavConst.DAV_NAMESPACE, DavConst.DAV_PREFIX + DavProperty.PROP);
    responseDocument.appendChild(propElement);

    property.serialize(responseDocument, propElement);
    
    byte []xmlBytes = getSerializedDom(propElement);
    
    response.setStatus(DavStatus.OK);
    response.addHeader(DavConst.Headers.CONTENTTYPE, "text/xml;charset=UTF-8 ");
    response.addHeader(DavConst.Headers.CONTENTLENGTH, String.format("%s", xmlBytes.length));

    response.getOutputStream().write(xmlBytes, 0, xmlBytes.length);
  }
    
}
