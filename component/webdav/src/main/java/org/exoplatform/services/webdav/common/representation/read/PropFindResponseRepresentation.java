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

import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.representation.HrefRepresentation;
import org.exoplatform.services.webdav.common.representation.WebDavNameSpaceContext;
import org.exoplatform.services.webdav.common.representation.XmlResponseRepresentation;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public abstract class PropFindResponseRepresentation extends XmlResponseRepresentation {
  
  public static final String XML_MULTISTATUS = "multistatus";
  
  public static final String XML_RESPONSE = "response";
  
  protected String href;
  
  protected Node node;
  
  protected int depth;  
  
  public PropFindResponseRepresentation(String href, Node node, int depth) throws RepositoryException {
    super(new WebDavNameSpaceContext((ManageableRepository)node.getSession().getRepository()));
    this.href = href;
    this.node = node;
    this.depth = depth;
  }

  protected void write(XMLStreamWriter xmlStreamWriter) throws XMLStreamException, RepositoryException {
    xmlStreamWriter.writeStartElement("D", XML_MULTISTATUS, "DAV:");

    xmlStreamWriter.writeNamespace("D", "DAV:");
    
    listRecursive(xmlStreamWriter, node, depth);
    
    xmlStreamWriter.writeEndElement();    
  }
  
  protected void listRecursive(XMLStreamWriter xmlStreamWriter, Node curNode, int curDepth) throws XMLStreamException, RepositoryException {
    xmlStreamWriter.writeStartElement("D", XML_RESPONSE, "DAV:");
    
    String responseHref = href + curNode.getPath();
    
    new HrefRepresentation(responseHref).write(xmlStreamWriter);
    
    writeResponseContent(xmlStreamWriter, curNode);
    
    xmlStreamWriter.writeEndElement();
    
    if (curDepth == 0 || curNode.isNodeType(DavConst.NodeTypes.NT_FILE)) {
      return;
    }
    
    NodeIterator nodeIter = curNode.getNodes();
    while (nodeIter.hasNext()) {
      Node childNode = nodeIter.nextNode();
      listRecursive(xmlStreamWriter, childNode, curDepth - 1);
    }
  }
  
  protected abstract void writeResponseContent(XMLStreamWriter xmlStreamWriter, Node node) throws XMLStreamException, RepositoryException;

}
