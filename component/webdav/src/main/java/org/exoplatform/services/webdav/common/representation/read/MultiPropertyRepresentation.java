/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.read;

import java.util.ArrayList;
import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.BadRequestException;
import org.exoplatform.services.webdav.common.representation.ResponseRepresentation;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class MultiPropertyRepresentation implements ResponseRepresentation {
  
  private static Log log = ExoLogger.getLogger("jcr.MultiPropertyRepresentation");
  
  public static final String XML_MULTISTATUS = "multistatus";
  
  public static final int MODE_LIST = 1;
  
  public static final int MODE_ALLPROP = 2;
  
  public static final int MODE_PROPNAMES = 3;
  
  protected String prefix;
  
  protected Node node;
  
  protected int maxLevel;

  protected WebDavService webDavService;
  
  protected int selectMode = MODE_LIST;
  
  protected HashMap<String, ArrayList<String>> properties;
  
  public MultiPropertyRepresentation(WebDavService webDavService, HashMap<String, ArrayList<String>> properties, int selectMode) {
    log.info("construct...");
    this.webDavService = webDavService;
    this.properties = properties;
    this.selectMode = selectMode;
  }

  public void init(String prefix, Node node, int maxLevel) throws RepositoryException {
    
    log.info("\r\n----------------------------------\r\n");
    log.info("init with server prefix: " + prefix);
    log.info("\r\n----------------------------------\r\n");
    
    this.prefix = prefix;
    this.node = node;
    this.maxLevel = maxLevel;
    
    log.info("Prefix: " + prefix);
    log.info("Node: " + node);
    log.info("MaxLevel: " + maxLevel);
  }

  public void write(XMLStreamWriter xmlStreamWriter) throws Exception {
    xmlStreamWriter.writeStartElement("D", XML_MULTISTATUS, "DAV:");

    xmlStreamWriter.writeNamespace("D", "DAV:");
    
    AbstractItemVisitor visitor = null;

    switch (selectMode) {
      case MODE_LIST:
        visitor = new PropListItemVisitor(webDavService, true, maxLevel, prefix, properties, xmlStreamWriter);
        break;
        
      case MODE_ALLPROP:
        visitor = new AllPropItemVisitor(webDavService, true, maxLevel, prefix, properties, xmlStreamWriter);
        break;
        
      case MODE_PROPNAMES:
        visitor = new PropNamesItemVisitor(webDavService, true, maxLevel, prefix, properties, xmlStreamWriter);
        break;
    }
    
    if (visitor == null) {
      throw new BadRequestException();
    }
    
    node.accept(visitor);
    
    xmlStreamWriter.writeEndElement();
  }

}
