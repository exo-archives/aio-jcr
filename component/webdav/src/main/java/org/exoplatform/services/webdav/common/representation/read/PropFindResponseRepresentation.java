/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
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
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.representation.HrefRepresentation;
import org.exoplatform.services.webdav.common.representation.WebDavNameSpaceContext;
import org.exoplatform.services.webdav.common.representation.XmlResponseRepresentation;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public abstract class PropFindResponseRepresentation extends XmlResponseRepresentation {
  
  public static final String XML_MULTISTATUS = "multistatus";
  
  public static final String XML_RESPONSE = "response";

  protected String defaultHref;
  
  protected String href;
  
  protected Node node;
  
  protected int depth;  
  
  protected WebDavService webDavService;
  
  public PropFindResponseRepresentation(WebDavService webDavService, String defaultHref, Node node, int depth) throws RepositoryException {
    super(new WebDavNameSpaceContext((ManageableRepository)node.getSession().getRepository()));
    this.webDavService = webDavService;
    this.defaultHref = defaultHref;
    this.node = node;
    this.depth = depth;
  }

  protected void write(XMLStreamWriter xmlStreamWriter) throws XMLStreamException, RepositoryException {
    xmlStreamWriter.writeStartElement("D", XML_MULTISTATUS, "DAV:");

    xmlStreamWriter.writeNamespace("D", "DAV:");
    
    xmlStreamWriter.writeAttribute("xmlns:b", "urn:uuid:c2f41010-65b3-11d1-a29f-00aa00c14882/");
    
    listRecursive(xmlStreamWriter, node, depth);
    
    xmlStreamWriter.writeEndElement();    
  }
  
  protected void listRecursive(XMLStreamWriter xmlStreamWriter, Node curNode, int curDepth) throws XMLStreamException, RepositoryException {
    xmlStreamWriter.writeStartElement("D", XML_RESPONSE, "DAV:");
    
    href = defaultHref + curNode.getPath();
    
    new HrefRepresentation(href).write(xmlStreamWriter);
    
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
