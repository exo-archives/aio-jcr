/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.property;

import javax.jcr.Node;
import javax.jcr.Property;
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

public class GetContentLengthRepresentation extends WebDavPropertyRepresentation {
  
  public static final String TAGNAME = "getcontentlength";
  
  private long contentLength;
  
  public void read(Node node) {    
    try {
      if (node.isNodeType(DavConst.NodeTypes.NT_VERSION)) {
        node = node.getNode(DavConst.NodeTypes.JCR_FROZENNODE);
      }

      Node dataNode = node.getNode(DavConst.NodeTypes.JCR_CONTENT);
      Property dataProperty = dataNode.getProperty(DavConst.NodeTypes.JCR_DATA);      
      contentLength = dataProperty.getLength();
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
    xmlWriter.writeCharacters("" + contentLength);
  }

}
