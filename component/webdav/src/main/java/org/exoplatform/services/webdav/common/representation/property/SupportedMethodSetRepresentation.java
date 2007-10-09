/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.property;

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.WebDavStatus;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class SupportedMethodSetRepresentation extends WebDavPropertyRepresentation {
  
  public static final String TAGNAME = "supported-method-set";
  
  public static final String XML_SUPPORTEDMETHOD = "supported-method";
  
  public static final String XML_NAME = "name";

  private static Log log = ExoLogger.getLogger("jcr.SupportedMethodSetRepresentation");
  
  private WebDavService webDavService;
  
  private ArrayList<String> supportedCommands = new ArrayList<String>();
  
  public SupportedMethodSetRepresentation(WebDavService webDavService) {
    log.info("construct...");
    this.webDavService = webDavService;
  }

  public void read(Node node) throws RepositoryException {
    supportedCommands = webDavService.getAvailableCommands();
    status = WebDavStatus.OK;
  }

  @Override
  public String getTagName() {
    return TAGNAME;
  }

  @Override
  protected void writeContent(XMLStreamWriter xmlWriter) throws XMLStreamException {    
    for (int i = 0; i < supportedCommands.size(); i++) {
      String commandName = supportedCommands.get(i);      
      xmlWriter.writeStartElement("DAV:", XML_SUPPORTEDMETHOD);      
      xmlWriter.writeAttribute(XML_NAME, commandName);      
      xmlWriter.writeEndElement();
    }    
    
  }

}
