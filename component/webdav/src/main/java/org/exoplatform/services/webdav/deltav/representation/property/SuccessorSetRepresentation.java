/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.representation.property;

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.representation.HrefRepresentation;
import org.exoplatform.services.webdav.common.representation.property.WebDavPropertyRepresentation;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class SuccessorSetRepresentation extends WebDavPropertyRepresentation {
  
  public static final String TAGNAME = "successor-set";
  
  private String href;
  
  private ArrayList<String> successorHrefs = new ArrayList<String>();
  
  public SuccessorSetRepresentation(String href) {
    this.href = href;   
  }

  public void read(Node node) {    
    if (!(node instanceof Version)) {
      return;
    }
    
    try {      
      Version []successors = ((Version)node).getSuccessors();

      while (successors.length > 0) {
        successorHrefs.add(href + "?VERSIONID=" + successors[0].getName());
        successors = successors[0].getSuccessors();
      }      
      status = WebDavStatus.OK;
    } catch (RepositoryException exc) {
    }
    
  }

  @Override
  public String getTagName() {
    return TAGNAME;
  }

  @Override
  protected void writeContent(XMLStreamWriter xmlWriter) throws XMLStreamException {
    for (int i = 0; i < successorHrefs.size(); i++) {
      String successorHref = successorHrefs.get(i);
      new HrefRepresentation(successorHref).write(xmlWriter);
    }
  }

}
