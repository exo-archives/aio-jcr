/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.restclient.command;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.exoplatform.frameworks.httpclient.HttpClient;
import org.exoplatform.frameworks.httpclient.HttpHeader;
import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.restclient.RestCommands;
import org.exoplatform.frameworks.restclient.RestContext;
import org.exoplatform.frameworks.restclient.common.template.RestTemplate;
import org.exoplatform.frameworks.restclient.common.template.Template;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class RestCommand {
  
  protected String commandName = RestCommands.REST;
  
  protected HttpClient httpClient;
  
  protected RestContext context;
  protected String srcPath = "/";
  
  protected RestTemplate restTemplate;
  
  protected int status = -1;
  
  public RestCommand(RestContext context, RestTemplate restTemplate) throws Exception {
    this.context = context;
    this.restTemplate = restTemplate;
    
    httpClient = new HttpClient(context.getHost(), context.getPort());
    httpClient.setRequestHeader(HttpHeader.CONNECTION, HttpHeader.TE);
    httpClient.setRequestHeader(HttpHeader.TE, "trailers, deflate, gzip, compress");
    //httpClient.setRequestHeader(Const.HttpHeaders.DEPTH, "1");
    httpClient.setRequestHeader(HttpHeader.TRANSLATE, "f");
    httpClient.setRequestHeader(HttpHeader.ACCEPTENCODING, "deflate, gzip, x-gzip, compress, x-compress");
    //next string influence for the search by content, should be commented
    httpClient.setRequestHeader(HttpHeader.CONTENTTYPE, "text/xml");    
  }

  public void setSrcPath(String srcPath) {
    this.srcPath = srcPath;
  }
  
  public int getStatus() {
    return status;
  }
  
  public byte []getResponseBytes() {
    return httpClient.getResponseBytes();
  }
  
  public Template getResponseTemplate() throws Exception {
    Template template = null;
    
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);        
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document responseDocument = builder.parse(new ByteArrayInputStream(getResponseBytes()));
    
    Node documentNode = responseDocument.getChildNodes().item(0);
    Log.info("NODE: " + documentNode);
    
    Log.info("NODE NAME: " + documentNode.getNodeName());
    
    return template; 
  }
  
  public int execute() throws Exception {
    byte []requestBytes = new byte[0];
    
    if (restTemplate != null) {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document xmlDocument = builder.newDocument();
      
      Element rootElement = restTemplate.serialize(xmlDocument);
      xmlDocument.appendChild(rootElement);
      
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(rootElement.getOwnerDocument());
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      StreamResult resultStream = new StreamResult(outStream);
      transformer.transform(source, resultStream);
      
      requestBytes = outStream.toByteArray();
    }
    
    Log.info("REQUEST: \r\n" + new String(requestBytes));
    
    httpClient.setRequestBody(requestBytes);
    
    httpClient.setHttpCommand(commandName);
    
    String path = context.getServlet() + srcPath;
    Log.info("PATH: [" + path + "]");    
    
    httpClient.setRequestPath(path);
    
    httpClient.conect();

    status = httpClient.execute();    
      
    Log.info("public int execute() throws Exception");
    
    return status;
  }

}

