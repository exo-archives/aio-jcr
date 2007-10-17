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

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.representation.HrefRepresentation;
import org.exoplatform.services.webdav.common.representation.property.WebDavPropertyRepresentation;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class PredecessorSetRepresentation extends WebDavPropertyRepresentation {
  
  public static final String TAGNAME = "predecessor-set";
  
  private String href;
  
  private ArrayList<String> predecessorHrefs = new ArrayList<String>();
  
  public PredecessorSetRepresentation(String href) {
    this.href = href;
  }

  public void read(Node node) {    
    if (!(node instanceof Version)) {
      return;
    }
    
    try {
      Version []predecessors = ((Version)node).getPredecessors();
      while (predecessors.length > 0) {
        if (DavConst.NodeTypes.JCR_ROOTVERSION.equals(predecessors[0].getName())) {
          break;
        }
        
        predecessorHrefs.add(href + "?VERSIONID=" + predecessors[0].getName());
        predecessors = predecessors[0].getPredecessors();
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
    for (int i = 0; i < predecessorHrefs.size(); i++) {
      String predecessorHref = predecessorHrefs.get(i);
      new HrefRepresentation(predecessorHref).write(xmlWriter);
    }
  }

}
