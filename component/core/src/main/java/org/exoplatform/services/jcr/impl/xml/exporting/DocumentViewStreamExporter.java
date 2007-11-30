/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.services.jcr.impl.xml.exporting;

import java.io.IOException;
import java.util.List;

import javax.jcr.NamespaceException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.util.StringConverter;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class DocumentViewStreamExporter extends StreamExporter {

  public DocumentViewStreamExporter(XMLStreamWriter writer,
      SessionImpl session,
      ItemDataConsumer dataManager,
      boolean skipBinary,
      boolean noRecurse) throws NamespaceException, RepositoryException {
    super(writer, session, dataManager, skipBinary, noRecurse);
  }

  @Override
  protected void entering(NodeData node, int level) throws RepositoryException {
    try {
      if (!node.getQPath().getName().equals(Constants.JCR_XMLTEXT)) {
        List<NodeData> nodes = dataManager.getChildNodesData(node);
        if (nodes.size() > 0) {
          writer.writeStartElement("", getExportName(node, true), "");
        } else {
          writer.writeEmptyElement("", getExportName(node, true), "");
        }
      }
      if (level == 0) {
        startPrefixMapping();

      }

    } catch (XMLStreamException e) {
      throw new RepositoryException(e);
    }
  }

  @Override
  protected void entering(PropertyData property, int level) throws RepositoryException {
    InternalQName propName = property.getQPath().getName();
    try {
      if (propName.equals(Constants.JCR_XMLCHARACTERS)) {
        writer.writeCharacters(new String(property.getValues().get(0).getAsByteArray(),
            Constants.DEFAULT_ENCODING));
      } else {

        //
        ItemData parentNodeData = session.getTransientNodesManager().getItemData(property
            .getParentIdentifier());
        if (parentNodeData.getQPath().getName().equals(Constants.JCR_XMLTEXT)) {
          return;
        }
        String strValue = "";

        for (ValueData valueData : property.getValues()) {
          String strVal = getValueAsStringForExport(valueData, property.getType());
          if (strVal == "") {
            continue;
          }
          strValue += MULTI_VALUE_DELIMITER
              + (property.getType() == PropertyType.BINARY ? strVal : StringConverter
                  .normalizeString(strVal, true));
        }

        writer.writeAttribute(getExportName(property, true), strValue.length() > 0 ? strValue
            .substring(1) : strValue);

      }
    } catch (IllegalStateException e) {
      throw new RepositoryException(e);
    } catch (XMLStreamException e) {
      throw new RepositoryException(e);
    } catch (IOException e) {
      throw new RepositoryException(e);
    }

  }

  @Override
  protected void leaving(NodeData node, int level) throws RepositoryException {

    try {
      if (!node.getQPath().getName().equals(Constants.JCR_XMLTEXT)) {
        List<NodeData> nodes = dataManager.getChildNodesData(node);
        if (nodes.size() > 0) {
          writer.writeEndElement();
        } 

        

      }
    } catch (XMLStreamException e) {
      throw new RepositoryException(e);
    }
  }

  @Override
  protected void leaving(PropertyData property, int level) throws RepositoryException {
  }
}
