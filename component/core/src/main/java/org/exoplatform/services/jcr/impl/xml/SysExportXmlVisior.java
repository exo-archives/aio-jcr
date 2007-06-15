/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.xml;

import java.util.List;

import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.core.ExtendedPropertyType;
import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version
 */
public class SysExportXmlVisior extends ExportXmlVisitor {


  private final String    SV_NAMESPACE_URI;



  /**
   * @param handler
   * @param session
   * @param dataManager
   * @param noRecurse
   * @throws NamespaceException
   * @throws RepositoryException
   */
  public SysExportXmlVisior(ContentHandler handler, SessionImpl session, ItemDataConsumer dataManager,
      boolean skipBinary, boolean noRecurse) throws NamespaceException, RepositoryException {
    super(handler, session,dataManager, skipBinary, noRecurse);

    if (handler == null)
      throw new java.lang.IllegalArgumentException("ContentHandler can't be null");
    if (session == null)
      throw new java.lang.IllegalArgumentException("session can't be null");

    SV_NAMESPACE_URI = session.getNamespaceURI("sv");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.dataflow.ItemDataTraversingVisitor#entering(org.exoplatform.services.jcr.datamodel.PropertyData,
   *      int)
   */
  @Override
  protected void entering(PropertyData property, int level) throws RepositoryException {
    try {
      // set name and type of property
      AttributesImpl atts = new AttributesImpl();
      atts.addAttribute(SV_NAMESPACE_URI, "name", "sv:name", "CDATA", locationFactory
          .createJCRName(property.getQPath().getName()).getAsString());
      atts.addAttribute(SV_NAMESPACE_URI, "type", "sv:type", "CDATA", ExtendedPropertyType
          .nameFromValue(property.getType()));

      contentHandler.startElement(SV_NAMESPACE_URI, "property", "sv:property", atts);
      List<ValueData> values = property.getValues();
      for (ValueData valueData : values) {
        contentHandler.startElement(SV_NAMESPACE_URI, "value", "sv:value", new AttributesImpl());

        writeValueData(valueData,property.getType());

        contentHandler.endElement(SV_NAMESPACE_URI, "value", "sv:value");
      }
    } catch (Exception e) {
      throw new RepositoryException("Can't export value to string: " + e.getMessage(), e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.dataflow.ItemDataTraversingVisitor#entering(org.exoplatform.services.jcr.datamodel.NodeData,
   *      int)
   */
  @Override
  protected void entering(NodeData node, int level) throws RepositoryException {
    try {
      // set name of node as sv:name attribute
      AttributesImpl atts = new AttributesImpl();
      atts.addAttribute(SV_NAMESPACE_URI, "name", "sv:name", "CDATA", locationFactory
          .createJCRName(node.getQPath().getName()).getAsString());

      contentHandler.startElement(SV_NAMESPACE_URI, "node", "sv:node", atts);
    } catch (SAXException e) {
      throw new RepositoryException(e);
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.dataflow.ItemDataTraversingVisitor#leaving(org.exoplatform.services.jcr.datamodel.PropertyData,
   *      int)
   */
  @Override
  protected void leaving(PropertyData property, int level) throws RepositoryException {
    try {
      contentHandler.endElement(SV_NAMESPACE_URI, "property", "sv:property");
    } catch (SAXException e) {
      throw new RepositoryException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.dataflow.ItemDataTraversingVisitor#leaving(org.exoplatform.services.jcr.datamodel.NodeData,
   *      int)
   */
  @Override
  protected void leaving(NodeData node, int level) throws RepositoryException {

    try {
      contentHandler.endElement(SV_NAMESPACE_URI, "node", "sv:node");
    } catch (SAXException e) {
      throw new RepositoryException(e);
    }

  }

  /**
   * Return the current content handler
   */
  public ContentHandler getContentHandler() {
    return contentHandler;
  }

}
