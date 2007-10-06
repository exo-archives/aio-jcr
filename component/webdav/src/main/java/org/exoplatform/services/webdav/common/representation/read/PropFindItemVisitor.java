/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.read;

import java.util.ArrayList;
import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.representation.HrefRepresentation;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public abstract class PropFindItemVisitor extends AbstractItemVisitor {
  
  private static Log log = ExoLogger.getLogger("jcr.PropFindItemVisitor");
  
  public static final String XML_RESPONSE = "response";
  
  public static final String XML_PROPSTAT = "propstat";
  
  public static final String XML_PROP = "prop";
  
  protected String rootHref;

  protected HashMap<String, ArrayList<String>> properties;
  
  protected XMLStreamWriter xmlWriter;
  
  protected WebDavService webDavService;

  public PropFindItemVisitor(WebDavService webdavService, boolean breadthFirst, int maxLevel, String rootHref, HashMap<String, ArrayList<String>> properties, XMLStreamWriter xmlWriter) {
    super(breadthFirst, maxLevel);
    
    this.webDavService = webdavService;
    this.rootHref = rootHref;
    this.properties = properties;
    this.xmlWriter = xmlWriter;
  }

  @Override
  protected void entering(Node node, int level) throws RepositoryException {
    if (node.isNodeType("nt:resource")) {
      return;
    }
    
    try {
      xmlWriter.writeStartElement("D", XML_RESPONSE, "DAV:");
      
      String responseHref = rootHref + node.getPath();
      
      HrefRepresentation href = new HrefRepresentation(responseHref);
      href.write(xmlWriter);
      
      fillContent(node, xmlWriter);
      
      xmlWriter.writeEndElement();
    } catch (Exception exc) {
      //log.info("Unhandled exception. " + exc.getMessage(), exc);
      throw new RepositoryException("Can't reply user."); 
    }
  }
  
  protected abstract void fillContent(Node node, XMLStreamWriter xmlWriter) throws RepositoryException, XMLStreamException;
  
}
