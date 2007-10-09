/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.xml.exporting;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.jcr.NamespaceException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.ws.commons.util.Base64;
import org.exoplatform.services.jcr.core.ExtendedPropertyType;
import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.core.SessionImpl;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class SystemViewStreamExporter extends StreamExporter {

  /**
   * @param writer
   * @param session
   * @param dataManager
   * @param maxLevel
   * @throws RepositoryException
   * @throws NamespaceException
   */
  public SystemViewStreamExporter(XMLStreamWriter writer,
      SessionImpl session,
      ItemDataConsumer dataManager,
      boolean skipBinary,
      boolean noRecurse) throws NamespaceException, RepositoryException {
    super(writer, session, dataManager, skipBinary, noRecurse);
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
      writer.writeStartElement("sv", "node", SV_NAMESPACE_URI);
      if (level == 0) {
        startPrefixMapping();
      }


      writer.writeAttribute("sv", SV_NAMESPACE_URI, "name", getExportName(node, false));
    } catch (XMLStreamException e) {
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
      writer.writeStartElement("sv", "property", SV_NAMESPACE_URI);
      
      writer.writeAttribute("sv", SV_NAMESPACE_URI, "name", getExportName(property, false));

      writer.writeAttribute("sv", SV_NAMESPACE_URI, "type", ExtendedPropertyType
          .nameFromValue(property.getType()));

      List<ValueData> values = property.getValues();
      for (ValueData valueData : values) {

        writer.writeStartElement("sv", "value", SV_NAMESPACE_URI);

        writeValueData(valueData, property.getType());

        writer.writeEndElement();
      }
    } catch (XMLStreamException e) {
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
      writer.writeEndElement();
    } catch (XMLStreamException e) {
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
      writer.writeEndElement();
    } catch (XMLStreamException e) {
      throw new RepositoryException(e);
    }

  }

  protected void writeValueData(ValueData data, int type) throws RepositoryException,
      IllegalStateException,
      XMLStreamException,
      IOException {

    if (PropertyType.BINARY == type) {
      if (!isSkipBinary()) {
        if (data.getLength() < 3 * 1024 * 3) {
          String charValue = getValueAsStringForExport(data, type);
          writer.writeCharacters(charValue.toCharArray(), 0, charValue.length());
        } else {
          InputStream is = data.getAsStream();
          byte[] buffer = new byte[3 * 1024 * 3];
          int len;
          while ((len = is.read(buffer)) > 0) {
            char[] charbuf1 = Base64.encode(buffer, 0, len, 0, "").toCharArray();
            writer.writeCharacters(charbuf1, 0, charbuf1.length);
          }
        }
      }
    } else {
      String charValue = getValueAsStringForExport(data, type);
      // charValue = StringConverter.normalizeString(charValue,f);
      writer.writeCharacters(charValue.toCharArray(), 0, charValue.length());
    }

  }
}
