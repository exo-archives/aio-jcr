/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.xml.exporting;

import java.io.IOException;

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
import org.exoplatform.services.jcr.impl.core.JCRName;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.util.ISO9075;
import org.exoplatform.services.jcr.impl.util.StringConverter;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class StreamDocExport extends StreamExport {

  public StreamDocExport(XMLStreamWriter writer,
      SessionImpl session,
      ItemDataConsumer dataManager,
      boolean skipBinary,
      boolean noRecurse) throws NamespaceException, RepositoryException {
    super(writer, session, dataManager, skipBinary, noRecurse);
    // TODO Auto-generated constructor stub
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
      if (!node.getQPath().getName().equals(Constants.JCR_XMLTEXT)) {
        writer.writeStartElement("", getNodeName(node), "");
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
        // encode node name
        InternalQName internalPropName = ISO9075.encode(propName);

        JCRName name2 = session.getLocationFactory().createJCRName(internalPropName);
        String strValue = "";

        for (ValueData valueData : property.getValues()) {
          String strVal = getValueAsStringForExport(valueData, property.getType());
          if (strVal == "") {
            continue;
          }
          strValue += " "
              + (property.getType() == PropertyType.BINARY ? strVal : StringConverter
                  .normalizeString(strVal, true));
        }

        writer.writeAttribute(name2.getPrefix(),
            name2.getNamespace(),
            name2.getName(),
            strValue != "" ? strValue.substring(1) : strValue);
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
        writer.writeEndElement();// endElement("", nodeName,
        // node.getQPath().getName().getName());
      }
    } catch (XMLStreamException e) {
      throw new RepositoryException(e);
    }
  }

  @Override
  protected void leaving(PropertyData property, int level) throws RepositoryException {
    // TODO Auto-generated method stub

  }
}
