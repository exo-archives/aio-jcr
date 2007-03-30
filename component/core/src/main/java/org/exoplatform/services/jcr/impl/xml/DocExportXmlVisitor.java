/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.xml;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;
import org.exoplatform.services.jcr.dataflow.ItemDataTraversingVisitor;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.util.ISO9075;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version
 */
public class DocExportXmlVisitor extends ExportXmlVisitor {
  private StringBuffer buffer;
  protected String encoding;
  private AttributesImpl currentAttr  = null;; 
  public DocExportXmlVisitor(ContentHandler handler, SessionImpl session, ItemDataConsumer dataManager,
      boolean skipBinary, boolean noRecurse) {
    super(handler, session, dataManager, skipBinary, noRecurse);
   
    // this.writer = writer;
    
  }

  @Override
  protected void entering(PropertyData property, int level) throws RepositoryException {
    // TODO Auto-generated method stub
    InternalQName name = property.getQPath().getName();
    currentAttr.addAttribute(name.getNamespace(),name.getName(),name.getAsString(),"CDATA",locationFactory
        .createJCRName(property.getQPath().getName()).getAsString());
  }

  @Override
  protected void entering(NodeData node, int level) throws RepositoryException {
//    String name = node.getQPath().getName().getAsString();
//    if (name.length() == 0) // root node
//      name = "jcr:root";
    
        
    InternalQName internalNodeName = ISO9075.encode(node.getQPath().getName());

    String nodeName = ((SessionImpl) session).getLocationFactory().createJCRName(internalNodeName)
        .getAsString();

    if (nodeName.length() <= 0)
      nodeName = "jcr:root";
    
    // set name of node as sv:name attribute
    AttributesImpl atts = new AttributesImpl();
    atts.addAttribute(Constants.JCR_URI, "name", "sv:name", "CDATA", locationFactory
        .createJCRName(node.getQPath().getName()).getAsString());

    
//    contentHandler.startElement(node.getQPath().getName().getNamespace(),
//        node.getQPath().getName(), elemName, attrs);
  }

  @Override
  protected void leaving(PropertyData property, int level) throws RepositoryException {
    // TODO Auto-generated method stub

  }

  @Override
  protected void leaving(NodeData node, int level) throws RepositoryException {
    // TODO Auto-generated method stub

  }

}
