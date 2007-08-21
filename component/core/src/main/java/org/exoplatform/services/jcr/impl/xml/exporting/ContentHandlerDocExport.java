/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.xml.exporting;

import java.io.IOException;
import java.util.List;

import javax.jcr.NamespaceException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.JCRName;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.util.ISO9075;
import org.exoplatform.services.jcr.impl.util.StringConverter;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version
 */
public class ContentHandlerDocExport extends ContentHandlerExport {
  private AttributesImpl currentAttr = null;

  protected String       encoding;           ;

  public ContentHandlerDocExport(ContentHandler handler,
      SessionImpl session,
      ItemDataConsumer dataManager,
      boolean skipBinary,
      boolean noRecurse) throws NamespaceException, RepositoryException {
    super(handler, session, dataManager, skipBinary, noRecurse);
  }

  private String getNodeName(NodeData data) throws RepositoryException {
    InternalQName internalNodeName = ISO9075.encode(data.getQPath().getName());
    String nodeName = session.getLocationFactory().createJCRName(internalNodeName).getAsString();
    if (nodeName.length() <= 0) {
      nodeName = "jcr:root";
    }

    return nodeName;
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

          // encode node name
          InternalQName internalPropName = ISO9075.encode(property.getQPath().getName());

          JCRName name2 = session.getLocationFactory().createJCRName(internalPropName);
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

          currentAttr.addAttribute(name2.getNamespace(),
              name2.getName(),
              name2.getAsString(),
              "CDATA",
              strValue != "" ? strValue.substring(1) : strValue);
        }

        contentHandler.startElement("",
            getNodeName(node),
            node.getQPath().getName().getName(),
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
    String nodeName = getNodeName(node);

    try {
      if (!node.getQPath().getName().equals(Constants.JCR_XMLTEXT)) {
        contentHandler.endElement("", nodeName, node.getQPath().getName().getName());
      }
    } catch (SAXException e) {
      throw new RepositoryException(e);
    }
  }

  @Override
  protected void leaving(PropertyData property, int level) throws RepositoryException {
  }
}
