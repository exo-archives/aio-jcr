/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.property;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavStatus;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class ChildCountRepresentation extends WebDavPropertyRepresentation {
  
  public static final String TAGNAME = "childcount";
  
  private int childCount = 0; 
  
  public void read(Node node) {
    try {
      if (!node.isNodeType(DavConst.NodeTypes.NT_FILE)) {
        NodeIterator nodeIter = node.getNodes();
        while (nodeIter.hasNext()) {
          nodeIter.next();
          childCount++;
        }
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
    if (status != WebDavStatus.OK) {
      return;
    }
    xmlWriter.writeCharacters("" + childCount);
  }
  
}
