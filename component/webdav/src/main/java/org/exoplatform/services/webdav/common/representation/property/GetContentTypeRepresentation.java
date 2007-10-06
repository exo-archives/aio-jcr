/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.property;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.WebDavStatus;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class GetContentTypeRepresentation extends WebDavPropertyRepresentation {
  
  private static Log log = ExoLogger.getLogger("jcr.GetContentTypeRepresentation");
  
  public static final String TAGNAME = "getcontenttype";
  
  private String contentType = ""; 
  
  public GetContentTypeRepresentation() {
    log.info("construct...");
  }

  public void read(WebDavService webdavService, Node node) throws RepositoryException {    
    Node contentNode = node.getNode(DavConst.NodeTypes.JCR_CONTENT);
    if (!contentNode.hasProperty(DavConst.NodeTypes.JCR_MIMETYPE)) {        
      return;
    }      

    contentType = contentNode.getProperty(DavConst.NodeTypes.JCR_MIMETYPE).getString();      
    status = WebDavStatus.OK;
  }

  @Override
  public String getTagName() {
    return TAGNAME;
  }

  @Override
  protected void writeContent(XMLStreamWriter xmlWriter) throws XMLStreamException {
    xmlWriter.writeCharacters(contentType);
  }

}
