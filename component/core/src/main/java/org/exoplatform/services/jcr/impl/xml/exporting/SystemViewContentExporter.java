/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.xml.exporting;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.jcr.NamespaceException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.ws.commons.util.Base64;
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
public class SystemViewContentExporter extends HandlingContentExporter {

  /**
   * @param handler
   * @param session
   * @param dataManager
   * @param noRecurse
   * @throws NamespaceException
   * @throws RepositoryException
   */
  public SystemViewContentExporter(ContentHandler handler,
      SessionImpl session,
      ItemDataConsumer dataManager,
      boolean skipBinary,
      boolean noRecurse) throws NamespaceException, RepositoryException {
    super(handler, session, dataManager, skipBinary, noRecurse);

  }

  /**
   * Return the current content handler
   */
  public ContentHandler getContentHandler() {
    return contentHandler;
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
      atts.addAttribute(SV_NAMESPACE_URI, "name", "sv:name", "CDATA", getExportName(node, false));

      contentHandler.startElement(SV_NAMESPACE_URI, "node", "sv:node", atts);
    } catch (SAXException e) {
      throw new RepositoryException(e);
    }

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
      atts.addAttribute(SV_NAMESPACE_URI,
          "name",
          "sv:name",
          "CDATA",
          getExportName(property, false));
      atts.addAttribute(SV_NAMESPACE_URI, "type", "sv:type", "CDATA", ExtendedPropertyType
          .nameFromValue(property.getType()));

      contentHandler.startElement(SV_NAMESPACE_URI, "property", "sv:property", atts);

      List<ValueData> values = property.getValues();
      for (ValueData valueData : values) {

        contentHandler.startElement(SV_NAMESPACE_URI, "value", "sv:value", new AttributesImpl());

        writeValueData(valueData, property.getType());
        contentHandler.endElement(SV_NAMESPACE_URI, "value", "sv:value");
      }
    } catch (SAXException e) {
      throw new RepositoryException("Can't export value to string: " + e.getMessage(), e);
    } catch (IllegalStateException e) {
      throw new RepositoryException("Can't export value to string: " + e.getMessage(), e);
    } catch (IOException e) {
      throw new RepositoryException("Can't export value to string: " + e.getMessage(), e);
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

  protected void writeValueData(ValueData data, int type) throws RepositoryException,
      IllegalStateException,
      IOException,
      SAXException {
    if (PropertyType.BINARY == type) {
      if (!isSkipBinary()) {
        if (data.getLength() < 3 * 1024 * 3) {
          String charValue = getValueAsStringForExport(data, type);
          contentHandler.characters(charValue.toCharArray(), 0, charValue.length());
        } else {
          InputStream is = data.getAsStream();
          byte[] buffer = new byte[3 * 1024 * 3];
          int len;
          while ((len = is.read(buffer)) > 0) {
            char[] charbuf1 = Base64.encode(buffer, 0, len, 0, "").toCharArray();
            contentHandler.characters(charbuf1, 0, charbuf1.length);
          }
        }
      }
    } else {
      String charValue = getValueAsStringForExport(data, type);

      // charValue = StringConverter.normalizeString(charValue,false);
      contentHandler.characters(charValue.toCharArray(), 0, charValue.length());
    }
  }
}
