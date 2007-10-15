/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.command.web.fckeditor;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: FCKConnectorXMLOutput.java 5800 2006-05-28 18:03:31Z geaz $
 */

public class FCKConnectorXMLOutput {
  
  protected Element rootElement;
  
  protected void initRootElement ( 
      String commandStr, String typeStr,  String currentPath, 
      String currentUrl ) throws ParserConfigurationException {
    
    Document doc = null;
    //try {
    DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    doc=builder.newDocument();
    //} catch (ParserConfigurationException pce) {
    //  pce.printStackTrace();
    //}
    
    rootElement = doc.createElement("Connector");
    doc.appendChild(rootElement);
    rootElement.setAttribute("command",commandStr);
    rootElement.setAttribute("resourceType",typeStr);
    
    Element myEl = doc.createElement("CurrentFolder");
    myEl.setAttribute("path",currentPath);
    myEl.setAttribute("url", currentUrl);
    rootElement.appendChild(myEl);
    
    //return root;
    
  }

  protected void outRootElement(HttpServletResponse response) throws Exception {
    response.setContentType("text/xml; charset=UTF-8");
    response.setHeader("Cache-Control","no-cache");
    PrintWriter out = response.getWriter();

    rootElement.normalize();
    TransformerFactory tFactory = TransformerFactory.newInstance();
    Transformer transformer = tFactory.newTransformer();
    DOMSource source = new DOMSource(rootElement.getOwnerDocument());

    StreamResult result = new StreamResult(out);
    transformer.transform(source, result);
    out.flush();
    out.close();
  }

}
