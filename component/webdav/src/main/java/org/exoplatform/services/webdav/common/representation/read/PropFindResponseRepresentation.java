/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.read;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.representation.HrefRepresentation;
import org.exoplatform.services.webdav.common.representation.ResponseRepresentation;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public abstract class PropFindResponseRepresentation implements ResponseRepresentation {
  
  public static final String XML_MULTISTATUS = "multistatus";
  
  public static final String XML_RESPONSE = "response";
  
  private static Log log = ExoLogger.getLogger("jcr.PropFindResponseRepresentation");
  
  protected String href;
  
  protected Node node;
  
  protected int maxLevel;  
  
  public PropFindResponseRepresentation() {
    log.info("construct..");
  }

  public void init(String href, Node node, int maxLevel) throws RepositoryException {
    this.href = href;
    this.node = node;
    this.maxLevel = maxLevel;
  }

  public void write(XMLStreamWriter xmlStreamWriter) throws XMLStreamException, RepositoryException {
    log.info("try writing response");

    xmlStreamWriter.writeStartElement("D", XML_MULTISTATUS, "DAV:");

    xmlStreamWriter.writeNamespace("D", "DAV:");
    
    listRecursive(xmlStreamWriter, node, maxLevel);
    
    xmlStreamWriter.writeEndElement();    
  }
  
  protected void listRecursive(XMLStreamWriter xmlStreamWriter, Node node, int depth) throws XMLStreamException, RepositoryException {
    log.info("NODE >> " + node.getPath());
    
    xmlStreamWriter.writeStartElement("D", XML_RESPONSE, "DAV:");
    
    String responseHref = href + node.getPath();
    
    new HrefRepresentation(responseHref).write(xmlStreamWriter);
    
    writeResponseContent(xmlStreamWriter, node);
    
    xmlStreamWriter.writeEndElement();
    
    if (depth == 0 || node.isNodeType(DavConst.NodeTypes.NT_FILE)) {
      return;
    }
    
    NodeIterator nodeIter = node.getNodes();
    while (nodeIter.hasNext()) {
      Node childNode = nodeIter.nextNode();
      listRecursive(xmlStreamWriter, childNode, depth - 1);
    }
  }
  
  protected abstract void writeResponseContent(XMLStreamWriter xmlStreamWriter, Node node) throws XMLStreamException, RepositoryException;

}
