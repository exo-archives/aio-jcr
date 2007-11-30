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
package org.exoplatform.services.jcr.impl.xml.importing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.PropertyDefinition;

import org.apache.commons.logging.Log;
import org.apache.ws.commons.util.Base64;
import org.apache.ws.commons.util.Base64.DecodingException;
import org.exoplatform.services.ext.action.InvocationContext;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitions;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.datamodel.IllegalPathException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.value.BaseValue;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.dataflow.version.VersionHistoryDataHelper;
import org.exoplatform.services.jcr.impl.util.ISO9075;
import org.exoplatform.services.jcr.impl.util.StringConverter;
import org.exoplatform.services.jcr.impl.xml.XmlSaveType;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class DocumentViewImporter extends BaseXmlImporter {
  /**
   * 
   */
  private static Log         log = ExoLogger.getLogger("jcr.DocNodeImporter");

  /**
   * 
   */
  private ImportPropertyData xmlCharactersProperty;

  /**
   * 
   */
  private String             xmlCharactersPropertyValue;

  /**
   * Document view importer.
   * 
   * @param parent - parent node
   * @param uuidBehavior
   * @param saveType
   * @param respectPropertyDefinitionsConstraints sdf;gkjwpeoirjtg
   */
  public DocumentViewImporter(NodeImpl parent,
                              int uuidBehavior,
                              XmlSaveType saveType,
                              InvocationContext context) {
    super(parent, uuidBehavior, saveType, context);
    xmlCharactersProperty = null;
    xmlCharactersPropertyValue = null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.xml.importing.Importer#characters(char[],
   *      int, int)
   */
  public void characters(char[] ch, int start, int length) throws RepositoryException {

    StringBuilder text = new StringBuilder();
    text.append(ch, start, length);
    if (log.isDebugEnabled())
      log.debug("Property:xmltext=" + text + " Parent=" + getParent().getQPath().getAsString());

    if (xmlCharactersProperty != null) {
      xmlCharactersPropertyValue += text.toString();
      xmlCharactersProperty.setValue(new TransientValueData(xmlCharactersPropertyValue));
    } else {
      TransientNodeData nodeData = TransientNodeData.createNodeData(getParent(),
                                                                    Constants.JCR_XMLTEXT,
                                                                    Constants.NT_UNSTRUCTURED,
                                                                    getNodeIndex(getParent(),
                                                                                 Constants.JCR_XMLTEXT,
                                                                                 null));
      nodeData.setOrderNumber(getNextChildOrderNum(getParent()));

      changesLog.add(new ItemState(nodeData, ItemState.ADDED, true, getParent().getQPath()));
      if (log.isDebugEnabled())
        log.debug("New node " + nodeData.getQPath().getAsString());

      ImportPropertyData newProperty = new ImportPropertyData(QPath.makeChildPath(nodeData.getQPath(),
                                                                                  Constants.JCR_PRIMARYTYPE),
                                                              IdGenerator.generate(),
                                                              0,
                                                              PropertyType.NAME,
                                                              nodeData.getIdentifier(),
                                                              false);

      newProperty.setValue(new TransientValueData(Constants.NT_UNSTRUCTURED));
      changesLog.add(new ItemState(newProperty, ItemState.ADDED, true, nodeData.getQPath()));
      newProperty = new ImportPropertyData(QPath.makeChildPath(nodeData.getQPath(),
                                                               Constants.JCR_XMLCHARACTERS),
                                           IdGenerator.generate(),
                                           0,
                                           PropertyType.STRING,
                                           nodeData.getIdentifier(),
                                           false);
      newProperty.setValue(new TransientValueData(text.toString()));

      changesLog.add(new ItemState(newProperty, ItemState.ADDED, true, nodeData.getQPath()));
      xmlCharactersProperty = newProperty;
      xmlCharactersPropertyValue = text.toString();
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.xml.importing.Importer#endElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public void endElement(String uri, String localName, String qName) throws RepositoryException {
    tree.pop();
    xmlCharactersProperty = null;
  }

  public void startElement(String namespaceURI,
                           String localName,
                           String qName,
                           Map<String, String> atts) throws RepositoryException {

    String nodeName = ISO9075.decode(qName);

    if ("jcr:root".equals(nodeName)) {
      nodeName = "";
    }

    xmlCharactersProperty = null;
    List<ExtendedNodeType> nodeTypes = new ArrayList<ExtendedNodeType>();

    HashMap<InternalQName, String> propertiesMap = new HashMap<InternalQName, String>();

    List<InternalQName> mixinNodeTypes = new ArrayList<InternalQName>();

    parseAttr(atts, nodeTypes, mixinNodeTypes, propertiesMap);

    InternalQName jcrName = locationFactory.parseJCRName(nodeName).getInternalName();

    ImportNodeData nodeData = createNode(nodeTypes, propertiesMap, mixinNodeTypes, jcrName);

    changesLog.add(new ItemState(nodeData, ItemState.ADDED, true, getParent().getQPath()));

    tree.push(nodeData);

    if (log.isDebugEnabled()) {
      log.debug("Node " + ": " + nodeData.getQPath().getAsString());
    }

    Iterator<InternalQName> keys = propertiesMap.keySet().iterator();

    PropertyData newProperty;

    while (keys.hasNext()) {
      newProperty = null;

      InternalQName key = keys.next();
      if (log.isDebugEnabled())
        log.debug("Property NAME: " + key + "=" + propertiesMap.get(key));

      if (key.equals(Constants.JCR_PRIMARYTYPE)) {
        newProperty = endPrimaryType(nodeData.getPrimaryTypeName());

      } else if (key.equals(Constants.JCR_MIXINTYPES)) {

        newProperty = endMixinTypes(mixinNodeTypes, key);

      } else if (nodeData.isMixReferenceable() && key.equals(Constants.JCR_UUID)) {
        newProperty = endUuid(nodeData, key);

      } else {
        PropertyDefinition pDef = getPropertyDefinition(key, nodeTypes);

        if (pDef.getRequiredType() == PropertyType.BINARY) {
          byte[] decoded;
          try {
            decoded = Base64.decode(propertiesMap.get(key));
          } catch (DecodingException e) {
            throw new RepositoryException(e);
          }
          newProperty = TransientPropertyData.createPropertyData(getParent(),
                                                                 key,
                                                                 PropertyType.BINARY,
                                                                 false,
                                                                 new TransientValueData(new ByteArrayInputStream(decoded)));

        } else {
          StringTokenizer spaceTokenizer = new StringTokenizer(propertiesMap.get(key));

          List<ValueData> values = new ArrayList<ValueData>();
          int pType = pDef.getRequiredType() > 0 ? pDef.getRequiredType() : PropertyType.STRING;
          InternalQName propName = key;
          if ("".equals(propertiesMap.get(key))) {

            // Skip empty non string values
            if (pType != PropertyType.STRING) {
              continue;
            }

            Value value = session.getValueFactory()
                                 .createValue(StringConverter.denormalizeString(propertiesMap.get(key)),
                                              pType);
            values.add(((BaseValue) value).getInternalData());
          } else {
            while (spaceTokenizer.hasMoreTokens()) {
              String elem = spaceTokenizer.nextToken();

              Value value = session.getValueFactory()
                                   .createValue(StringConverter.denormalizeString(elem), pType);
              if (log.isDebugEnabled()) {
                String valueAsString = null;
                try {
                  valueAsString = value.getString();
                } catch (Exception e) {
                  log.error("Can't present value as string. " + e.getMessage());
                  valueAsString = "[Can't present value as string]";
                }
                log.debug("Property " + PropertyType.nameFromValue(pType) + ": " + key + "="
                    + valueAsString);
              }
              values.add(((BaseValue) value).getInternalData());
            }
          }

          PropertyDefinitions defs;
          try {
            defs = ntManager.findPropertyDefinitions(propName,
                                                     nodeData.getPrimaryTypeName(),
                                                     mixinNodeTypes.toArray(new InternalQName[mixinNodeTypes.size()]));
          } catch (RepositoryException e) {
            if (!context.getBoolean(ContentImporter.RESPECT_PROPERTY_DEFINITIONS_CONSTRAINTS)) {
              log.warn(e.getLocalizedMessage());
              continue;
            }
            throw e;
          }
          boolean isMultivalue = true;

          // determinating is property multivalue;
          if (values.size() == 1) {
            // there is single-value defeniton
            if (defs.getDefinition(false) != null) {
              isMultivalue = false;
            }
          } else {
            if ((defs.getDefinition(true) == null) && (defs.getDefinition(false) != null)) {
              throw new ValueFormatException("Can not assign multiple-values Value"
                  + " to a single-valued property " + propName.getAsString() + " node "
                  + jcrName.getName());
            }
          }

          newProperty = TransientPropertyData.createPropertyData(getParent(),
                                                                 propName,
                                                                 pType,
                                                                 isMultivalue,
                                                                 values);
          if (nodeData.isMixVersionable())
            endVersionable(nodeData, values, propName);
        }
      }

      changesLog.add(new ItemState(newProperty, ItemState.ADDED, true, getParent().getQPath()));
    }
    if (nodeData.isMixVersionable() && !nodeData.isContainsVersionhistory()) {
      PlainChangesLogImpl changes = new PlainChangesLogImpl();
      // using VH helper as for one new VH, all changes in changes log
      new VersionHistoryDataHelper(nodeData,
                                   changes,
                                   session.getTransientNodesManager(),
                                   session.getWorkspace().getNodeTypeManager(),
                                   nodeData.getVersionHistoryIdentifier(),
                                   nodeData.getBaseVersionIdentifier());
      for (ItemState state : changes.getAllStates()) {
        if (state.getData().getQPath().isDescendantOf(Constants.JCR_SYSTEM_PATH, false)) {
          changesLog.add(state);
        }
      }

    }
  }

  /**
   * @param nodeTypes
   * @param propertiesMap
   * @param mixinNodeTypes
   * @param jcrName
   * @param primaryTypeName
   * @return
   * @throws PathNotFoundException
   * @throws IllegalPathException
   * @throws RepositoryException
   */
  private ImportNodeData createNode(List<ExtendedNodeType> nodeTypes,
                                    HashMap<InternalQName, String> propertiesMap,
                                    List<InternalQName> mixinNodeTypes,
                                    InternalQName jcrName) throws PathNotFoundException,
                                                          IllegalPathException,
                                                          RepositoryException {
    ImportNodeData nodeData = new ImportNodeData(getParent(), jcrName, getNodeIndex(getParent(),
                                                                                    jcrName,
                                                                                    null));

    nodeData.setPrimaryTypeName(locationFactory.parseJCRName(propertiesMap.get(Constants.JCR_PRIMARYTYPE))
                                               .getInternalName());

    nodeData.setOrderNumber(getNextChildOrderNum(getParent()));
    nodeData.setMixinTypeNames(mixinNodeTypes.toArray(new InternalQName[mixinNodeTypes.size()]));
    nodeData.setMixReferenceable(isNodeType(Constants.MIX_REFERENCEABLE, nodeTypes));
    nodeData.setIdentifier(IdGenerator.generate());

    if (nodeData.isMixReferenceable()) {
      nodeData.setMixVersionable(isNodeType(Constants.MIX_VERSIONABLE, nodeTypes));
      String identifier = validateUuidCollision(propertiesMap.get(Constants.JCR_UUID));
      if (identifier != null) {
        nodeData.setIdentifier(identifier);
      }

      if (uuidBehavior == ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING)
        nodeData.setParentIdentifer(tree.peek().getIdentifier());
    }
    return nodeData;
  }

  /**
   * @param mixinNodeTypes
   * @param key
   * @return
   */
  private PropertyData endMixinTypes(List<InternalQName> mixinNodeTypes, InternalQName key) {
    PropertyData newProperty;
    List<ValueData> valuesData = new ArrayList<ValueData>(mixinNodeTypes.size());

    for (InternalQName mixinQname : mixinNodeTypes) {
      valuesData.add(new TransientValueData(mixinQname));
    }

    newProperty = TransientPropertyData.createPropertyData(getParent(),
                                                           key,
                                                           PropertyType.NAME,
                                                           true,
                                                           valuesData);
    return newProperty;
  }

  /**
   * @param props
   * @param key
   * @return
   */
  private PropertyData endPrimaryType(InternalQName primaryTypeName) {
    PropertyData newProperty;
    if (log.isDebugEnabled()) {
      log.debug("Property NAME: " + primaryTypeName);
    }
    newProperty = TransientPropertyData.createPropertyData(getParent(),
                                                           Constants.JCR_PRIMARYTYPE,
                                                           PropertyType.NAME,
                                                           false,
                                                           new TransientValueData(primaryTypeName));
    return newProperty;
  }

  /**
   * @param nodeData
   * @param key
   * @return
   * @throws ValueFormatException
   * @throws UnsupportedRepositoryOperationException
   * @throws RepositoryException
   * @throws IllegalStateException
   */
  private PropertyData endUuid(ImportNodeData nodeData, InternalQName key) throws ValueFormatException,
                                                                          UnsupportedRepositoryOperationException,
                                                                          RepositoryException,
                                                                          IllegalStateException {
    PropertyData newProperty;
    Value value = session.getValueFactory().createValue(nodeData.getIdentifier(),
                                                        PropertyType.STRING);
    if (log.isDebugEnabled()) {
      log.debug("Property STRING: " + key + "=" + value.getString());
    }

    newProperty = TransientPropertyData.createPropertyData(getParent(),
                                                           Constants.JCR_UUID,
                                                           PropertyType.STRING,
                                                           false,
                                                           new TransientValueData(nodeData.getIdentifier()));
    return newProperty;
  }

  /**
   * @param nodeData
   * @param values
   * @param propName
   * @throws RepositoryException
   */
  private void endVersionable(ImportNodeData nodeData,
                              List<ValueData> values,
                              InternalQName propName) throws RepositoryException {
    {
      if (propName.equals(Constants.JCR_VERSIONHISTORY)) {
        try {

          nodeData.setVersionHistoryIdentifier(((TransientValueData) values.get(0)).getString());
        } catch (IOException e) {
          throw new RepositoryException(e);
        }

        nodeData.setContainsVersionhistory(session.getTransientNodesManager()
                                                  .getItemData(nodeData.getVersionHistoryIdentifier()) != null);
      } else if (propName.equals(Constants.JCR_BASEVERSION)) {
        try {
          nodeData.setBaseVersionIdentifier(((TransientValueData) values.get(0)).getString());
        } catch (IOException e) {
          throw new RepositoryException(e);
        }
      }
    }
  }

  /**
   * @param atts
   * @param nodeTypes
   * @param mixinNodeTypes
   * @param props
   * @throws PathNotFoundException
   * @throws RepositoryException
   */
  private void parseAttr(Map<String, String> atts,
                         List<ExtendedNodeType> nodeTypes,
                         List<InternalQName> mixinNodeTypes,
                         HashMap<InternalQName, String> props) throws PathNotFoundException,
                                                              RepositoryException {
    // default primary type

    props.put(Constants.JCR_PRIMARYTYPE, locationFactory.createJCRName(Constants.NT_UNSTRUCTURED)
                                                        .getAsString());
    nodeTypes.add(ntManager.getNodeType(Constants.NT_UNSTRUCTURED));

    if (atts != null) {
      for (String key : atts.keySet()) {

        String attValue = atts.get(key);

        String propName = ISO9075.decode(key);
        if (log.isDebugEnabled())
          log.debug(propName + ":" + attValue);
        InternalQName propInternalQName = locationFactory.parseJCRName(propName).getInternalName();

        if (Constants.JCR_PRIMARYTYPE.equals(propInternalQName)) {
          String primaryNodeType = StringConverter.denormalizeString(attValue);
          InternalQName ntName = locationFactory.parseJCRName(primaryNodeType).getInternalName();
          nodeTypes.add(ntManager.getNodeType(ntName));
          props.put(propInternalQName, primaryNodeType);
        } else if (Constants.JCR_MIXINTYPES.equals(propInternalQName)) {
          String[] amTypes = attValue.split(" ");
          for (int mi = 0; mi < amTypes.length; mi++) {
            amTypes[mi] = StringConverter.denormalizeString(amTypes[mi]);
            mixinNodeTypes.add(locationFactory.parseJCRName(amTypes[mi]).getInternalName());
            nodeTypes.add((ExtendedNodeType) ntManager.getNodeType(amTypes[mi]));
          }
          // value will not be used anywhere; for key only
          props.put(propInternalQName, null);
        } else {
          props.put(propInternalQName, attValue);
        }
      }

    }

  }
}
