/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.chain.web.servlet.ServletWebContext;
import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.orgservice.common.resource.RestResource;
import org.exoplatform.services.orgservice.common.resource.RootResource;
import org.exoplatform.services.orgservice.common.template.RestTemplate;
import org.exoplatform.services.orgservice.common.template.Template;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class RestCommandContext extends ServletWebContext {
  
  private static Log log = ExoLogger.getLogger("jcr.RestCommandContext");
  
  private RestService restService;
  
  public RestCommandContext(
      ServletContext context, 
      HttpServletRequest request, 
      HttpServletResponse response,
      RestService restService) {
    super(context, request, response);
    log.info("public RestCommandContext()");
    
    this.restService = restService;
  }
  
  protected Document getDocument(InputStream inputStream) throws Exception {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    byte []buff = new byte[2048];
    while (true) {
      int readed = inputStream.read(buff);
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
    return builder.parse(inStream);    
  }  
  
  public Template getRequestTemplate() throws Exception {
    String contentLength = getRequest().getHeader(HttpHeader.CONTENTLENGTH);
    if (contentLength == null || "0".equals(contentLength)) {
      log.info("NO REQUEST DOCUMENT FOUND!!!");
      return null;
    }
    
    InputStream httpInputStream = getRequest().getInputStream();    
    log.info("REQUEST INPUT STREAM: " + httpInputStream);
    
    Document requestDocument = getDocument(httpInputStream);    
    log.info("REQUEST DOCUMENT: " + requestDocument);
    
    Node documentNode = requestDocument.getChildNodes().item(0);
    
    log.info("DOCUMENT NAMESPACE: " + documentNode.getNamespaceURI());
    
    if (documentNode.getNamespaceURI().equals(RestTemplate.EXO_HREF)) {
      log.info(">>>>>>>>>>>>>>>>>>>>> EXO document present!!!!!!!!!");
      
      String templateName = documentNode.getLocalName();
      log.info("TEMPLATE NAME: " + templateName);
      
      for (int i = 0; i < RequestTemplates.TEMPLATES.length; i++) {
        if (templateName.equals(RequestTemplates.TEMPLATES[i][0])) {
          
          Template requestTemplate = (Template)Class.forName(RequestTemplates.TEMPLATES[i][1]).newInstance();
          if (requestTemplate.parse(documentNode)) {
            return requestTemplate;
            
          } else {
            log.info("TEMPLATE CAN'T BE INITIALIZED!!!!!");
            return null;
          }
          
        }
      }
      
    } else {
      log.info(">>>>>>>>>>>>>>>>>>>>> NO EXO document present!!!!!!!!!");
    }
    
    return null;
  }
  
  public void replyTemplate(RestTemplate template) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();    
    Document responseDocument = builder.newDocument();

    Element templateElement = template.serialize(responseDocument);
    responseDocument.appendChild(templateElement);
    
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    
    DOMSource source = new DOMSource(templateElement.getOwnerDocument());
    
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    StreamResult resultStream = new StreamResult(outStream);
    
    transformer.transform(source, resultStream);
    
    byte []xmlBytes = outStream.toByteArray();    
    
    response.setStatus(200);      
    response.addHeader(HttpHeader.CONTENTTYPE, "text/xml;charset=UTF-8 ");
    response.addHeader(HttpHeader.CONTENTLENGTH, String.format("%s", xmlBytes.length));
    
    response.getOutputStream().write(xmlBytes, 0, xmlBytes.length);
  }
  
  public OrganizationService getOrganizationService() {
    return restService.getOrganizationService();
  }
  
  public RestResource getResource() throws Exception {
    String resourcePath = getRequest().getPathInfo();
    log.info("RESOURCE PATH INFO: [" + resourcePath + "]");
    
    String []pathes = resourcePath.split("/");
    ArrayList<String> splittedPath = new ArrayList<String>();
    for (int i = 0; i < pathes.length; i++) {
      if (!"".equals(pathes[i])) {
        splittedPath.add(pathes[i]);
      }
    }
    
    RestResource resource = new RootResource(this, "/");
    resource = resource.findChildResource(splittedPath);
        
    return resource;
  }
  
}

