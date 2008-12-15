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

import javax.jcr.NamespaceRegistry;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.ConstraintViolationException;

import org.apache.commons.logging.Log;
import org.apache.ws.commons.util.Base64;
import org.apache.ws.commons.util.Base64.DecodingException;

import org.exoplatform.services.jcr.access.AccessManager;
import org.exoplatform.services.jcr.core.ExtendedPropertyType;
import org.exoplatform.services.jcr.core.nodetype.NodeDefinitionData;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeData;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionData;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionDatas;
import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.IllegalPathException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.value.BaseValue;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.ISO9075;
import org.exoplatform.services.jcr.impl.util.StringConverter;
import org.exoplatform.services.jcr.impl.xml.importing.dataflow.ImportNodeData;
import org.exoplatform.services.jcr.impl.xml.importing.dataflow.ImportPropertyData;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.ConversationState;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: DocumentViewImporter.java 14221 2008-05-14 08:27:41Z ksm $
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
  public DocumentViewImporter(NodeData parent,
                              QPath ancestorToSave,
                              int uuidBehavior,
                              ItemDataConsumer dataConsumer,
                              NodeTypeDataManager ntManager,
                              LocationFactory locationFactory,
                              ValueFactoryImpl valueFactory,
                              NamespaceRegistry namespaceRegistry,
                              AccessManager accessManager,
                              ConversationState userState,
                              Map<String, Object> context,
                              RepositoryImpl repository,
                              String currentWorkspaceName) {
    super(parent,
          ancestorToSave,
          uuidBehavior,
          dataConsumer,
          ntManager,
          locationFactory,
          valueFactory,
          namespaceRegistry,
          accessManager,
          userState,
          context,
          repository,
          currentWorkspaceName);
    xmlCharactersProperty = null;
    xmlCharactersPropertyValue = null;
  }

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.services.jcr.impl.xml.importing.Importer#characters(char[],
   * int, int)
   */
  public void characters(char[] ch, int start, int length) throws RepositoryException {

    StringBuilder text = new StringBuilder();
    text.append(ch, start, length);
    if (log.isDebugEnabled()) {
      log.debug("Property:xmltext=" + text + " Parent=" + getParent().getQPath().getAsString());
    }

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

      changesLog.add(new ItemState(nodeData, ItemState.ADDED, true, getAncestorToSave()));
      if (log.isDebugEnabled()) {
        log.debug("New node " + nodeData.getQPath().getAsString());
      }

      ImportPropertyData newProperty = new ImportPropertyData(QPath.makeChildPath(nodeData.getQPath(),
                                                                                  Constants.JCR_PRIMARYTYPE),
                                                              IdGenerator.generate(),
                                                              0,
                                                              PropertyType.NAME,
                                                              nodeData.getIdentifier(),
                                                              false);

      newProperty.setValue(new TransientValueData(Constants.NT_UNSTRUCTURED));
      changesLog.add(new ItemState(newProperty, ItemState.ADDED, true, getAncestorToSave()));
      newProperty = new ImportPropertyData(QPath.makeChildPath(nodeData.getQPath(),
                                                               Constants.JCR_XMLCHARACTERS),
                                           IdGenerator.generate(),
                                           0,
                                           PropertyType.STRING,
                                           nodeData.getIdentifier(),
                                           false);
      newProperty.setValue(new TransientValueData(text.toString()));

      changesLog.add(new ItemState(newProperty, ItemState.ADDED, true, getAncestorToSave()));
      xmlCharactersProperty = newProperty;
      xmlCharactersPropertyValue = text.toString();
    }

  }

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.services.jcr.impl.xml.importing.Importer#endElement(java
   * .lang.String, java.lang.String, java.lang.String)
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
    List<NodeTypeData> nodeTypes = new ArrayList<NodeTypeData>();

    HashMap<InternalQName, String> propertiesMap = new HashMap<InternalQName, String>();

    List<InternalQName> mixinNodeTypes = new ArrayList<InternalQName>();
    InternalQName jcrName = locationFactory.parseJCRName(nodeName).getInternalName();

    parseAttr(atts, nodeTypes, mixinNodeTypes, propertiesMap, jcrName);

    ImportNodeData nodeData = createNode(nodeTypes, propertiesMap, mixinNodeTypes, jcrName);

    NodeData parentNodeData = getParent();
    changesLog.add(new ItemState(nodeData, ItemState.ADDED, true, getAncestorToSave()));

    tree.push(nodeData);

    if (log.isDebugEnabled()) {
      log.debug("Node " + ": " + nodeData.getQPath().getAsString());
    }

    Iterator<InternalQName> keys = propertiesMap.keySet().iterator();

    PropertyData newProperty;

    while (keys.hasNext()) {
      newProperty = null;

      InternalQName propName = keys.next();
      if (log.isDebugEnabled()) {
        log.debug("Property NAME: " + propName + "=" + propertiesMap.get(propName));
      }

      if (propName.equals(Constants.JCR_PRIMARYTYPE)) {
        InternalQName childName = locationFactory.parseJCRName(propertiesMap.get(Constants.JCR_PRIMARYTYPE))
                                                 .getInternalName();
        if (!nodeTypeDataManager.isChildNodePrimaryTypeAllowed(childName,
                                                               parentNodeData.getPrimaryTypeName(),
                                                               parentNodeData.getMixinTypeNames())) {
          throw new ConstraintViolationException("Can't add node "
              + nodeData.getQName().getAsString() + " to "
              + parentNodeData.getQPath().getAsString() + " node type "
              + propertiesMap.get(Constants.JCR_PRIMARYTYPE)
              + " is not allowed as child's node type for parent node type "
              + parentNodeData.getPrimaryTypeName().getAsString());

        }
        newProperty = endPrimaryType(nodeData.getPrimaryTypeName());

      } else if (propName.equals(Constants.JCR_MIXINTYPES)) {

        newProperty = endMixinTypes(mixinNodeTypes, propName);

      } else if (nodeData.isMixReferenceable() && propName.equals(Constants.JCR_UUID)) {
        newProperty = endUuid(nodeData, propName);

      } else {
        PropertyDefinitionData pDef;
        PropertyDefinitionDatas defs;
        InternalQName[] nTypes = mixinNodeTypes.toArray(new InternalQName[mixinNodeTypes.size() + 1]);
        nTypes[nTypes.length - 1] = nodeData.getPrimaryTypeName();
        defs = nodeTypeDataManager.getPropertyDefinitions(propName, nTypes);
        if (defs == null || defs.getAnyDefinition() == null) {
          if (!((Boolean) context.get(ContentImporter.RESPECT_PROPERTY_DEFINITIONS_CONSTRAINTS))) {
            log.warn("Property definition not found for " + propName.getAsString());
            continue;
          }
          throw new RepositoryException("Property definition not found for "
              + propName.getAsString());

        }

        pDef = defs.getAnyDefinition();

        if ((pDef == null) || (defs == null)) {
          throw new RepositoryException("no propertyDefinition found");
        }

        if (pDef.getRequiredType() == PropertyType.BINARY) {
          newProperty = endBinary(propertiesMap, newProperty, propName);

        } else {
          StringTokenizer spaceTokenizer = new StringTokenizer(propertiesMap.get(propName));

          List<ValueData> values = new ArrayList<ValueData>();
          int pType = pDef.getRequiredType() > 0 ? pDef.getRequiredType() : PropertyType.STRING;

          if ("".equals(propertiesMap.get(propName))) {

            // Skip empty non string values
            if (pType != PropertyType.STRING) {
              continue;
            }

            Value value = valueFactory.createValue(StringConverter.denormalizeString(propertiesMap.get(propName)),
                                                   pType);
            values.add(((BaseValue) value).getInternalData());
          } else {
            while (spaceTokenizer.hasMoreTokens()) {
              String elem = spaceTokenizer.nextToken();

              Value value = valueFactory.createValue(StringConverter.denormalizeString(elem), pType);
              if (log.isDebugEnabled()) {
                String valueAsString = null;
                try {
                  valueAsString = value.getString();
                } catch (Exception e) {
                  log.error("Can't present value as string. " + e.getMessage());
                  valueAsString = "[Can't present value as string]";
                }
                log.debug("Property " + ExtendedPropertyType.nameFromValue(pType) + ": " + propName
                    + "=" + valueAsString);
              }
              values.add(((BaseValue) value).getInternalData());
            }
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

          if (nodeData.isMixVersionable()) {
            endVersionable(nodeData, values, propName);
          }
        }
      }
      // skip versionable
      if (!Constants.JCR_VERSIONHISTORY.equals(propName)
          && !Constants.JCR_BASEVERSION.equals(propName)
          && !Constants.JCR_PREDECESSORS.equals(propName)) {
        changesLog.add(new ItemState(newProperty, ItemState.ADDED, true, getAncestorToSave()));
      }
    }
    if (nodeData.isMixVersionable()) {
      createVersionHistory(nodeData);
    }
  }

  /**
   * @param nodeTypes
   * @param propertiesMap
   * @param mixinNodeTypes
   * @param jcrName
   * @return
   * @throws PathNotFoundException
   * @throws IllegalPathException
   * @throws RepositoryException
   */
  private ImportNodeData createNode(List<NodeTypeData> nodeTypes,
                                    HashMap<InternalQName, String> propertiesMap,
                                    List<InternalQName> mixinNodeTypes,
                                    InternalQName jcrName) throws PathNotFoundException,
                                                          IllegalPathException,
                                                          RepositoryException {
    ImportNodeData nodeData = new ImportNodeData(getParent(), jcrName, getNodeIndex(getParent(),
                                                                                    jcrName,
                                                                                    null));
    InternalQName[] allNodeTypes = new InternalQName[nodeTypes.size() + mixinNodeTypes.size()];
    for (int i = 0; i < nodeTypes.size(); i++) {
      allNodeTypes[i] = nodeTypes.get(i).getName();
    }
    for (int i = 0; i < mixinNodeTypes.size(); i++) {
      allNodeTypes[nodeTypes.size() + i] = mixinNodeTypes.get(i);
    }
    nodeData.setPrimaryTypeName(locationFactory.parseJCRName(propertiesMap.get(Constants.JCR_PRIMARYTYPE))
                                               .getInternalName());

    nodeData.setOrderNumber(getNextChildOrderNum(getParent()));
    nodeData.setMixinTypeNames(mixinNodeTypes.toArray(new InternalQName[mixinNodeTypes.size()]));
    nodeData.setMixReferenceable(nodeTypeDataManager.isNodeType(Constants.MIX_REFERENCEABLE,
                                                                allNodeTypes));
    nodeData.setIdentifier(IdGenerator.generate());

    if (nodeData.isMixReferenceable()) {
      nodeData.setMixVersionable(nodeTypeDataManager.isNodeType(Constants.MIX_VERSIONABLE,
                                                                allNodeTypes));
      checkReferenceable(nodeData, propertiesMap.get(Constants.JCR_UUID));
    }
    return nodeData;
  }

  private PropertyData endBinary(HashMap<InternalQName, String> propertiesMap,
                                 PropertyData newProperty,
                                 InternalQName propName) throws RepositoryException {
    try {
      newProperty = TransientPropertyData.createPropertyData(getParent(),
                                                             propName,
                                                             PropertyType.BINARY,
                                                             false,
                                                             new TransientValueData(new ByteArrayInputStream(Base64.decode(propertiesMap.get(propName)))));

    } catch (DecodingException e) {
      throw new RepositoryException(e);
    }
    return newProperty;
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
    Value value = valueFactory.createValue(nodeData.getIdentifier(), PropertyType.STRING);
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

        nodeData.setContainsVersionhistory(dataConsumer.getItemData(nodeData.getVersionHistoryIdentifier()) != null);
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
                         List<NodeTypeData> nodeTypes,
                         List<InternalQName> mixinNodeTypes,
                         HashMap<InternalQName, String> props,
                         InternalQName nodeName) throws PathNotFoundException, RepositoryException {
    // default primary type
    if (!atts.containsKey("jcr:primaryType")) {
      NodeData parent = getParent();

      NodeDefinitionData nodeNt = nodeTypeDataManager.findChildNodeDefinition(nodeName,
                                                                              parent.getPrimaryTypeName(),
                                                                              parent.getMixinTypeNames());
      NodeTypeData nodeType;
      if (nodeNt.getName().equals(Constants.JCR_ANY_NAME) && nodeNt.getDefaultPrimaryType() != null) {
        nodeType = nodeTypeDataManager.findNodeType(nodeNt.getDefaultPrimaryType());
      } else {
        nodeType = nodeTypeDataManager.findNodeType(nodeNt.getName());
      }

      if (nodeType == null)
        throw new ConstraintViolationException("Can not define node-type for node "
            + nodeName.getAsString() + ", parent node type "
            + parent.getPrimaryTypeName().getAsString());

      nodeTypes.add(nodeType);
      props.put(Constants.JCR_PRIMARYTYPE, locationFactory.createJCRName(nodeType.getName())
                                                          .getAsString());
    }

    if (atts != null) {
      for (String key : atts.keySet()) {

        String attValue = atts.get(key);

        String propName = ISO9075.decode(key);
        if (log.isDebugEnabled()) {
          log.debug(propName + ":" + attValue);
        }
        InternalQName propInternalQName = locationFactory.parseJCRName(propName).getInternalName();

        if (Constants.JCR_PRIMARYTYPE.equals(propInternalQName)) {
          String primaryNodeType = StringConverter.denormalizeString(attValue);
          InternalQName ntName = locationFactory.parseJCRName(primaryNodeType).getInternalName();
          NodeTypeData nodeType = nodeTypeDataManager.findNodeType(ntName);
          if (nodeType == null)
            throw new ConstraintViolationException("Can not find node type " + primaryNodeType);
          nodeTypes.add(nodeType);
          props.put(propInternalQName, primaryNodeType);
        } else if (Constants.JCR_MIXINTYPES.equals(propInternalQName)) {
          String[] amTypes = attValue.split(" ");
          for (int mi = 0; mi < amTypes.length; mi++) {
            amTypes[mi] = StringConverter.denormalizeString(amTypes[mi]);
            InternalQName name = locationFactory.parseJCRName(amTypes[mi]).getInternalName();
            mixinNodeTypes.add(name);
            NodeTypeData nodeType = nodeTypeDataManager.findNodeType(name);
            if (nodeType == null)
              throw new ConstraintViolationException("Can not find node type " + amTypes[mi]);

            nodeTypes.add(nodeType);
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
