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


          //JCRName name2 = session.getLocationFactory().createJCRName(internalPropName);
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
          // encode node name
          InternalQName internalPropName = ISO9075.encode(property.getQPath().getName());
         
          currentAttr.addAttribute(internalPropName.getNamespace(),
              internalPropName.getName(),
              getExportName(property,true),
              "CDATA",
              strValue != "" ? strValue.substring(1) : strValue);
        }
        
        if (Constants.ROOT_PATH.equals(node.getQPath()))
          contentHandler.startElement(Constants.NS_JCR_URI,
              Constants.NS_JCR_PREFIX,
              JCR_ROOT,
              currentAttr);

        else
          contentHandler.startElement(node.getQPath().getName().getNamespace(), node.getQPath()
              .getName().getName(), getExportName(node, true), currentAttr);
        
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
          contentHandler.endElement(Constants.NS_JCR_URI,
              Constants.NS_JCR_PREFIX,
              JCR_ROOT);

        else
        contentHandler.endElement(node.getQPath().getName().getNamespace(), node.getQPath().getName().getName(), getExportName(node,true));
      }
    } catch (SAXException e) {
      throw new RepositoryException(e);
    }
  }

  @Override
  protected void leaving(PropertyData property, int level) throws RepositoryException {
  }
}
