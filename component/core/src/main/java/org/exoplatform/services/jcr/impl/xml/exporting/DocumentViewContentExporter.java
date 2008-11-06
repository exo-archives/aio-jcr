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

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;
import org.exoplatform.services.jcr.impl.util.ISO9075;
import org.exoplatform.services.jcr.impl.util.StringConverter;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version
 */
public class DocumentViewContentExporter extends HandlingContentExporter {
  private AttributesImpl currentAttr = null;

  protected String       encoding;           ;

  public DocumentViewContentExporter(ContentHandler handler,
                                     SessionImpl session,
                                     ItemDataConsumer dataManager,
                                     ValueFactoryImpl systemValueFactory,
                                     boolean skipBinary,
                                     boolean noRecurse) throws NamespaceException,
      RepositoryException {
    super(handler, session, dataManager, systemValueFactory, skipBinary, noRecurse);
  }

  @Override
  protected void entering(NodeData node, int level) throws RepositoryException {

    try {
      if (node.getQPath().getName().equals(Constants.JCR_XMLTEXT)) {
        // jcr:xmlcharacters
        List<PropertyData> nodeData = session.getTransientNodesManager()
                                             .getChildPropertiesData(node);
        String strValue = "";
        for (PropertyData propertyData : nodeData) {
          if (propertyData.getQPath().getName().equals(Constants.JCR_XMLCHARACTERS)) {
            strValue = new String(propertyData.getValues().get(0).getAsByteArray(),
                                  Constants.DEFAULT_ENCODING);
          }
        }
        contentHandler.characters(strValue.toCharArray(), 0, strValue.length());
      } else {
        List<PropertyData> nodeData = session.getTransientNodesManager()
                                             .getChildPropertiesData(node);
        currentAttr = new AttributesImpl();
        for (PropertyData property : nodeData) {

          // JCRName name2 = session.getLocationFactory().createJCRName(internalPropName);
          String strValue = "";

          for (ValueData valueData : property.getValues()) {
            String strVal = getValueAsStringForExport(valueData, property.getType());
            if (strVal == "") {
              continue;
            }

            strValue += MULTI_VALUE_DELIMITER
                + (property.getType() == PropertyType.BINARY ? strVal
                                                            : StringConverter.normalizeString(strVal,
                                                                                              true));

          }
          // encode node name
          InternalQName internalPropName = ISO9075.encode(property.getQPath().getName());

          currentAttr.addAttribute(internalPropName.getNamespace(),
                                   internalPropName.getName(),
                                   getExportName(property, true),
                                   "CDATA",
                                   strValue != "" ? strValue.substring(1) : strValue);
        }

        if (Constants.ROOT_PATH.equals(node.getQPath()))
          contentHandler.startElement(Constants.NS_JCR_URI,
                                      Constants.NS_JCR_PREFIX,
                                      JCR_ROOT,
                                      currentAttr);

        else
          contentHandler.startElement(node.getQPath().getName().getNamespace(),
                                      node.getQPath().getName().getName(),
                                      getExportName(node, true),
                                      currentAttr);

      }
    } catch (SAXException e) {
      throw new RepositoryException(e);
    } catch (IllegalStateException e) {
      throw new RepositoryException(e);
    } catch (IOException e) {
      throw new RepositoryException(e);
    }
  }

  @Override
  protected void entering(PropertyData property, int level) throws RepositoryException {

  }

  @Override
  protected void leaving(NodeData node, int level) throws RepositoryException {

    try {
      if (!node.getQPath().getName().equals(Constants.JCR_XMLTEXT)) {

        if (Constants.ROOT_PATH.equals(node.getQPath()))
          contentHandler.endElement(Constants.NS_JCR_URI, Constants.NS_JCR_PREFIX, JCR_ROOT);

        else
          contentHandler.endElement(node.getQPath().getName().getNamespace(),
                                    node.getQPath().getName().getName(),
                                    getExportName(node, true));
      }
    } catch (SAXException e) {
      throw new RepositoryException(e);
    }
  }

  @Override
  protected void leaving(PropertyData property, int level) throws RepositoryException {
  }
}
