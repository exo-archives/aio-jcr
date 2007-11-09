/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.property;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.webdav.WebDavStatus;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class ParentNameRepresentation extends WebDavPropertyRepresentation {
  
  public static final String TAGNAME = "parentname";
  
  private String parentName = "";
  
  @Override
  public String getTagName() {
    return TAGNAME;
  }

  @Override
  protected void writeContent(XMLStreamWriter xmlWriter) throws XMLStreamException {
    xmlWriter.writeCharacters(parentName);
  }

  public void read(Node node) {
    try {
      if (node.getDepth() == 0) {
        return;
      }
      
      if (node.getDepth() > 1) {
        parentName = node.getParent().getName();
      } else {
        parentName = node.getSession().getWorkspace().getName();
      }

      status = WebDavStatus.OK;      
    } catch (RepositoryException exc) {
    }
  }

}
