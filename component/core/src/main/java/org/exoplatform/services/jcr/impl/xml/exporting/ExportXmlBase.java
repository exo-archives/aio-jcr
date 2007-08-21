/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.xml.exporting;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.jcr.NamespaceException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.ValueFormatException;

import org.apache.ws.commons.util.Base64;
import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;
import org.exoplatform.services.jcr.dataflow.ItemDataTraversingVisitor;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.dataflow.NodeDataOrderComparator;
import org.exoplatform.services.jcr.impl.dataflow.PropertyDataOrderComparator;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;

public abstract class ExportXmlBase extends ItemDataTraversingVisitor {

  /**
   * Serialized only definition of of each <code>PropertyType.BINARY</code>
   * property without values
   */
  public static final int     BINARY_EMPTY          = 2;

  /**
   * Actual value of each <code>PropertyType.BINARY</code> property is
   * recorded using Base64 encoding
   */
  public static final int     BINARY_PROCESS        = 0;

  /**
   * Any properties of <code>PropertyType.BINARY</code> will be ignored and
   * will not appear in the serialized output
   */
  public static final int     BINARY_SKIP           = 1;

  public static final String  MULTI_VALUE_DELIMITER = " ";

  private int                 binaryConduct         = BINARY_PROCESS;

  protected LocationFactory   locationFactory;

  protected boolean           noRecurse;

  protected final SessionImpl session;

  protected final String      SV_NAMESPACE_URI;

  public ExportXmlBase(SessionImpl session, ItemDataConsumer dataManager, int maxLevel) throws NamespaceException,
      RepositoryException {
    super(dataManager, maxLevel);
    this.session = session;
    this.locationFactory = session.getLocationFactory();
    SV_NAMESPACE_URI = session.getNamespaceURI("sv");
  }

  public abstract void export(NodeData node) throws Exception;

  /**
   * Specify how properties of <code>PropertyType.BINARY</code> serialized
   * 
   * @see <code>BINARY_PROCESS</code>, <code>BINARY_SKIP</code>,
   *      <code>BINARY_EMPTY</code>
   */
  public int getBinaryConduct() {
    return binaryConduct;
  }

  public boolean isNoRecurse() {
    return noRecurse;
  }

  public void setBinaryConduct(int binaryConduct) {
    if ((binaryConduct != BINARY_PROCESS) && (binaryConduct != BINARY_SKIP)
        && (binaryConduct != BINARY_EMPTY)) {
      throw new java.lang.IllegalArgumentException("binaryConduct must be one of "
          + "BINARY_PROCESS,BINARY_SKIP, BINARY_EMPTY");
    }
    this.binaryConduct = binaryConduct;
  }

  public void setNoRecurse(boolean noRecurse) {
    this.noRecurse = noRecurse;
  }

  @Override
  public void visit(NodeData node) throws RepositoryException {
    try {
      entering(node, currentLevel);
      if ((maxLevel == -1) || (currentLevel < maxLevel)) {
        currentLevel++;

        List<PropertyData> properies = dataManager.getChildPropertiesData(node);
        // Sorting properties
        Collections.sort(properies, new PropertyDataOrderComparator());
        for (PropertyData data : properies) {
          InternalQName propName = data.getQPath().getName();

          // 7.3.3 Respecting Property Semantics
          // When an element or attribute representing such a property is
          // encountered, an implementation may either skip it or respect it.
          if (Constants.JCR_LOCKISDEEP.equals(propName) || Constants.JCR_LOCKOWNER.equals(propName)) {
            continue;
          }
          data.accept(this);
        }
        if (!isNoRecurse() && (currentLevel > 0)) {
          List<NodeData> nodes = dataManager.getChildNodesData(node);
          // Sorting nodes
          Collections.sort(nodes, new NodeDataOrderComparator());
          for (NodeData data : nodes) {
            data.accept(this);
          }
        }
        currentLevel--;
      }
      leaving(node, currentLevel);
    } catch (RepositoryException re) {
      currentLevel = 0;
      throw re;
    }

  }

  /**
   * Return string representation of values prepared for export. Be attentive
   * method encode binary values in memory. It is possible OutOfMemoryError on
   * large Values.
   * 
   * @param data
   * @param type
   * @return
   * @throws IllegalStateException
   * @throws IOException
   * @throws RepositoryException
   */
  protected String getValueAsStringForExport(ValueData data, int type) throws IllegalStateException,
      IOException,
      RepositoryException {
    String charValue = null;

    switch (type) {
    case PropertyType.BINARY:
      if ((getBinaryConduct() == BINARY_SKIP) || (getBinaryConduct() == BINARY_EMPTY)) {
        charValue = "";
      } else {
        charValue = Base64.encode(data.getAsByteArray(), 0, (int) data.getLength(), 0, "");
      }
      break;
    case PropertyType.NAME:
    case PropertyType.DATE:
    case PropertyType.PATH:

      try {
        charValue = session.getValueFactory().loadValue((TransientValueData) data, type)
            .getString();
      } catch (ValueFormatException e) {
        throw new RepositoryException(e);
      } catch (UnsupportedRepositoryOperationException e) {
        throw new RepositoryException(e);
      }
      break;
    default:
      charValue = new String(data.getAsByteArray(), Constants.DEFAULT_ENCODING);
      break;
    }
    return charValue;
  }
}