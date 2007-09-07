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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.ValueFormatException;

import org.apache.ws.commons.util.Base64;
import org.exoplatform.commons.utils.QName;
import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;
import org.exoplatform.services.jcr.dataflow.ItemDataTraversingVisitor;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.dataflow.NodeDataOrderComparator;
import org.exoplatform.services.jcr.impl.dataflow.PropertyDataOrderComparator;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.ISO9075;

public abstract class ExportXmlBase extends ItemDataTraversingVisitor {

  public static final String        MULTI_VALUE_DELIMITER          = " ";

  public static final String        DEFAULT_EMPTY_NAMESPACE_PREFIX = "jcr_default_empty"
                                                                       + "_namespace_prefix";

  protected static final String     JCR_ROOT                       = "jcr:root";

  // private int binaryConduct = BINARY_PROCESS;

  // protected LocationFactory locationFactory;

  protected boolean                 noRecurse;

  protected final SessionImpl       session;

  protected final String            SV_NAMESPACE_URI;

  private final boolean             skipBinary;

  protected final NamespaceRegistry namespaceRegistry;

  protected Map<String, String>     writedNamespaces;

  public ExportXmlBase(SessionImpl session,
      ItemDataConsumer dataManager,
      boolean skipBinary,
      int maxLevel) throws NamespaceException, RepositoryException {
    super(dataManager, maxLevel);
    this.session = session;
    this.skipBinary = skipBinary;
    this.namespaceRegistry = session.getWorkspace().getNamespaceRegistry();
    this.SV_NAMESPACE_URI = session.getNamespaceURI("sv");
    this.writedNamespaces = new HashMap<String, String>();
  }

  public abstract void export(NodeData node) throws Exception;

  public boolean isNoRecurse() {
    return noRecurse;
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
      if (skipBinary) {
        charValue = "";
      } else {
        charValue = Base64.encode(data.getAsByteArray(), 0, (int) data.getLength(), 0, "");
      }
      break;
    case PropertyType.NAME:
    case PropertyType.DATE:
    case PropertyType.PATH:
      // TODO namespace mapping for values
      try {
        charValue = session.getRepository().getSystemSession().getValueFactory()
            .loadValue((TransientValueData) data, type).getString();
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

  protected String getExportName(ItemData data, boolean encode) throws RepositoryException {
    String nodeName;
    QPath itemPath = data.getQPath();
    if (Constants.ROOT_PATH.equals(itemPath)) {
      nodeName = JCR_ROOT;
    } else {

      InternalQName internalNodeName = itemPath.getName();
      if (encode) {
        internalNodeName = ISO9075.encode(itemPath.getName());
      }
      String prefix = namespaceRegistry.getPrefix(internalNodeName.getNamespace());
      nodeName = prefix.length() == 0 ? "" : prefix + ":";
      if ("".equals(itemPath.getName().getName())
          && itemPath.isDescendantOf(Constants.EXO_NAMESPACES_PATH, false)) {
        nodeName += DEFAULT_EMPTY_NAMESPACE_PREFIX;
      } else {
        nodeName += internalNodeName.getName();
      }

    }
    return nodeName;
  }

  public boolean isSkipBinary() {
    return skipBinary;
  }
}