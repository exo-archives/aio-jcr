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
import org.exoplatform.services.webdav.WebDavStatus;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class IsCollectionRepresentation extends WebDavPropertyRepresentation {
  
  private static Log log = ExoLogger.getLogger("jcr.IsCollectionRepresentation");
  
  public static final String TAGNAME = "iscollection";
  
  private boolean isCollection = true;
  
  public IsCollectionRepresentation() {
    log.info("construct...");
  }

  public void read(Node node) throws RepositoryException {
    status = WebDavStatus.OK;

    if (node.isNodeType(DavConst.NodeTypes.NT_FILE)) {
      isCollection = false;        
    }
  }

  @Override
  public String getTagName() {
    return TAGNAME;
  }

  @Override
  protected void writeContent(XMLStreamWriter xmlWriter) throws XMLStreamException {
    xmlWriter.writeCharacters((isCollection ? "1" : "0"));
  }

}
