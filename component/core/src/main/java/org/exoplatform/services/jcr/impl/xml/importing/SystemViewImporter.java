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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.NamespaceRegistry;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.access.AccessManager;
import org.exoplatform.services.jcr.core.ExtendedPropertyType;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionDatas;
import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.IllegalPathException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.JCRName;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.value.BaseValue;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.xml.DecodedValue;
import org.exoplatform.services.jcr.impl.xml.importing.dataflow.ImportNodeData;
import org.exoplatform.services.jcr.impl.xml.importing.dataflow.ImportPropertyData;
import org.exoplatform.services.jcr.impl.xml.importing.dataflow.PropertyInfo;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.ConversationState;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: SystemViewImporter.java 14100 2008-05-12 10:53:47Z gazarenkov $
 */
public class SystemViewImporter extends BaseXmlImporter {
  /**
   * 
   */
  private static Log     log            = ExoLogger.getLogger(SystemViewImporter.class);

  protected PropertyInfo propertyInfo   = new PropertyInfo();

  /**
   * Root node name.
   */
  protected String       ROOT_NODE_NAME = "jcr:root";

  /**
   * @param parent
   * @param uuidBehavior
   * @param saveType
   * @param respectPropertyDefinitionsConstraints
   */
  public SystemViewImporter(NodeData parent,
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
  }

  /**
   * {@inheritDoc}
   */
  public void characters(char[] ch, int start, int length) throws RepositoryException {
    // property values
    if (propertyInfo.getValues().size() > 0) {
      DecodedValue curPropValue = propertyInfo.getValues().get(propertyInfo.getValues().size() - 1);
      if (propertyInfo.getType() == PropertyType.BINARY) {
        try {
          curPropValue.getBinaryDecoder().write(ch, start, length);
        } catch (IOException e) {
          throw new RepositoryException(e);
        }
      } else {
        curPropValue.getStringBuffer().append(ch, start, length);
      }
    } else {
      log.warn("Wrong XML content. Element 'sv:value' expected,"
          + " but SAX event 'characters' occured. characters:[" + new String(ch, start, length)
          + "]");
    }
  }

  /**
   * {@inheritDoc}
   */
  public void endElement(String uri, String localName, String name) throws RepositoryException {
    InternalQName elementName = locationFactory.parseJCRName(name).getInternalName();

    if (Constants.SV_NODE_NAME.equals(elementName)) {
      // sv:node element
      endNode();
    } else if (Constants.SV_PROPERTY_NAME.equals(elementName)) {
      // sv:property element

      ImportPropertyData propertyData = endProperty();
      if (propertyData != null)
        changesLog.add(new ItemState(propertyData, ItemState.ADDED, true, getAncestorToSave()));
    } else if (Constants.SV_VALUE_NAME.equals(elementName)) {
      // sv:value element
    } else {
      throw new RepositoryException("invalid element in system view xml document: " + localName);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void startElement(String namespaceURI,
                           String localName,
                           String name,
                           Map<String, String> atts) throws RepositoryException {
    InternalQName elementName = locationFactory.parseJCRName(name).getInternalName();

    if (Constants.SV_NODE_NAME.equals(elementName)) {
      // sv:node element

      // node name (value of sv:name attribute)
      String svName = getAttribute(atts, Constants.SV_NAME_NAME);
      if (svName == null) {
        throw new RepositoryException("Missing mandatory sv:name attribute of element sv:node");
      }

      NodeData parentData = null;

      parentData = getParent();

      InternalQName currentNodeName = null;
      if (ROOT_NODE_NAME.equals(svName)) {
        currentNodeName = Constants.ROOT_PATH.getName();
      } else {
        currentNodeName = locationFactory.parseJCRName(svName).getInternalName();
      }

      int nodeIndex = getNodeIndex(parentData, currentNodeName, null);
      ImportNodeData newNodeData = new ImportNodeData(parentData, currentNodeName, nodeIndex);
      newNodeData.setOrderNumber(getNextChildOrderNum(parentData));
      newNodeData.setIdentifier(IdGenerator.generate());

      changesLog.add(new ItemState(newNodeData, ItemState.ADDED, true, getAncestorToSave()));

      tree.push(newNodeData);

    } else if (Constants.SV_PROPERTY_NAME.equals(elementName)) {
      // sv:property element

      propertyInfo.setValues(new ArrayList<DecodedValue>());

      // property name (value of sv:name attribute)
      String svName = getAttribute(atts, Constants.SV_NAME_NAME);
      if (svName == null) {
        throw new RepositoryException("missing mandatory sv:name attribute of element sv:property");
      }
      propertyInfo.setName(locationFactory.parseJCRName(svName).getInternalName());
      propertyInfo.setIndentifer(IdGenerator.generate());
      // property type (sv:type attribute)
      String type = getAttribute(atts, Constants.SV_TYPE_NAME);
      if (type == null) {
        throw new RepositoryException("missing mandatory sv:type attribute of element sv:property");
      }
      try {
        propertyInfo.setType(ExtendedPropertyType.valueFromName(type));
      } catch (IllegalArgumentException e) {
        throw new RepositoryException("Unknown property type: " + type, e);
      }
    } else if (Constants.SV_VALUE_NAME.equals(elementName)) {
      // sv:value element

      propertyInfo.getValues().add(new DecodedValue());

    } else {
      throw new RepositoryException("Unknown element " + elementName.getAsString());
    }
  }

  /**
   * @return
   * @throws PathNotFoundException
   * @throws RepositoryException
   * @throws NoSuchNodeTypeException
   */
  private ImportPropertyData endMixinTypes() throws PathNotFoundException,
                                            RepositoryException,
                                            NoSuchNodeTypeException {
    ImportPropertyData propertyData;
    InternalQName[] mixinNames = new InternalQName[propertyInfo.getValuesSize()];
    List<ValueData> values = new ArrayList<ValueData>(propertyInfo.getValuesSize());
    ImportNodeData currentNodeInfo = (ImportNodeData) getParent();
    for (int i = 0; i < propertyInfo.getValuesSize(); i++) {

      String value = propertyInfo.getValues().get(i).toString();

      mixinNames[i] = locationFactory.parseJCRName(value).getInternalName();
      currentNodeInfo.addNodeType((nodeTypeDataManager.findNodeType(mixinNames[i])));
      values.add(new TransientValueData(value.toString()));
    }

    currentNodeInfo.setMixinTypeNames(mixinNames);

    propertyData = new ImportPropertyData(QPath.makeChildPath(currentNodeInfo.getQPath(),
                                                              propertyInfo.getName()),
                                          propertyInfo.getIndentifer(),
                                          0,
                                          propertyInfo.getType(),
                                          currentNodeInfo.getIdentifier(),
                                          true);
    propertyData.setValues(parseValues());
    return propertyData;
  }

  /**
   * endNode.
   * 
   * @throws RepositoryException
   */
  private void endNode() throws RepositoryException {
    ImportNodeData currentNodeInfo = (ImportNodeData) tree.pop();

    currentNodeInfo.setMixinTypeNames(currentNodeInfo.getMixinTypeNames());

    if (currentNodeInfo.isMixVersionable()) {
      createVersionHistory(currentNodeInfo);
    }

  }

  /**
   * endPrimaryType.
   * 
   * @return
   * @throws PathNotFoundException
   * @throws RepositoryException
   * @throws NoSuchNodeTypeException
   */
  private ImportPropertyData endPrimaryType() throws PathNotFoundException,
                                             RepositoryException,
                                             NoSuchNodeTypeException {
    ImportPropertyData propertyData;
    String sName = propertyInfo.getValues().get(0).toString();
    InternalQName primaryTypeName = locationFactory.parseJCRName(sName).getInternalName();

    ImportNodeData nodeData = (ImportNodeData) tree.pop();
    if (!Constants.ROOT_UUID.equals(nodeData.getIdentifier())) {
      NodeData parentNodeData = getParent();
      // nodeTypeDataManager.findChildNodeDefinition(primaryTypeName,)
      if (!nodeTypeDataManager.isChildNodePrimaryTypeAllowed(primaryTypeName,
                                                             parentNodeData.getPrimaryTypeName(),
                                                             parentNodeData.getMixinTypeNames())) {
        throw new ConstraintViolationException("Can't add node "
            + nodeData.getQName().getAsString() + " to " + parentNodeData.getQPath().getAsString()
            + " node type " + sName + " is not allowed as child's node type for parent node type "
            + parentNodeData.getPrimaryTypeName().getAsString());
      }
    }
    //
    nodeData.addNodeType((nodeTypeDataManager.findNodeType(primaryTypeName)));
    nodeData.setPrimaryTypeName(primaryTypeName);

    propertyData = new ImportPropertyData(QPath.makeChildPath(nodeData.getQPath(),
                                                              propertyInfo.getName()),
                                          propertyInfo.getIndentifer(),
                                          0,
                                          propertyInfo.getType(),
                                          nodeData.getIdentifier(),
                                          false);
    propertyData.setValues(parseValues());
    tree.push(nodeData);
    return propertyData;
  }

  /**
   * @return
   * @throws PathNotFoundException
   * @throws RepositoryException
   * @throws NoSuchNodeTypeException
   * @throws IllegalPathException
   * @throws ValueFormatException
   */
  private ImportPropertyData endProperty() throws PathNotFoundException,
                                          RepositoryException,
                                          NoSuchNodeTypeException,
                                          IllegalPathException,
                                          ValueFormatException {
    ImportPropertyData propertyData = null;
    if (Constants.JCR_PRIMARYTYPE.equals(propertyInfo.getName())) {

      propertyData = endPrimaryType();

    } else if (Constants.JCR_MIXINTYPES.equals(propertyInfo.getName())) {
      propertyData = endMixinTypes();

    } else if (Constants.JCR_UUID.equals(propertyInfo.getName())) {
      propertyData = endUuid();

      // skip verionable properties
    } else if (Constants.JCR_VERSIONHISTORY.equals(propertyInfo.getName())
        || Constants.JCR_BASEVERSION.equals(propertyInfo.getName())
        || Constants.JCR_PREDECESSORS.equals(propertyInfo.getName())) {

      propertyData = null;

      endVersionable((ImportNodeData) getParent(), parseValues());
    } else {

      ImportNodeData currentNodeInfo = (ImportNodeData) getParent();
      List<ValueData> values = parseValues();

      // determinating is property multivalue;
      boolean isMultivalue = true;

      PropertyDefinitionDatas defs = nodeTypeDataManager.findPropertyDefinitions(propertyInfo.getName(),
                                                                                 currentNodeInfo.getPrimaryTypeName(),
                                                                                 currentNodeInfo.getMixinTypeNames());

      if (defs == null) {
        if (!((Boolean) context.get(ContentImporter.RESPECT_PROPERTY_DEFINITIONS_CONSTRAINTS)))
          log.warn("Property definition not found for " + propertyInfo.getName());
        else
          throw new RepositoryException("Property definition not found for "
              + propertyInfo.getName());

      }

      if (values.size() == 1) {
        // there is single-value defeniton
        if (defs.getDefinition(false) != null) {
          isMultivalue = false;
        }
      } else {
        if ((defs.getDefinition(true) == null) && (defs.getDefinition(false) != null)) {
          throw new ValueFormatException("Can not assign multiple-values "
              + "Value to a single-valued property " + propertyInfo.getName().getName());
        }
      }
      log.debug("Import " + propertyInfo.getName().getName() + " size="
          + propertyInfo.getValuesSize() + " isMultivalue=" + isMultivalue);

      propertyData = new ImportPropertyData(QPath.makeChildPath(currentNodeInfo.getQPath(),
                                                                propertyInfo.getName()),
                                            propertyInfo.getIndentifer(),
                                            0,
                                            propertyInfo.getType(),
                                            currentNodeInfo.getIdentifier(),
                                            isMultivalue);
      propertyData.setValues(values);

    }
    return propertyData;
  }

  /**
   * @return
   * @throws RepositoryException
   * @throws PathNotFoundException
   * @throws IllegalPathException
   */
  private ImportPropertyData endUuid() throws RepositoryException,
                                      PathNotFoundException,
                                      IllegalPathException {
    ImportPropertyData propertyData;
    ImportNodeData currentNodeInfo = (ImportNodeData) tree.pop();

    currentNodeInfo.setMixReferenceable(nodeTypeDataManager.isNodeType(Constants.MIX_REFERENCEABLE,
                                                                       currentNodeInfo.getNodeTypes()
                                                                                      .toArray(new InternalQName[currentNodeInfo.getNodeTypes()
                                                                                                                                .size()])));

    if (currentNodeInfo.isMixReferenceable()) {
      currentNodeInfo.setMixVersionable(nodeTypeDataManager.isNodeType(Constants.MIX_VERSIONABLE,
                                                                       currentNodeInfo.getNodeTypes()
                                                                                      .toArray(new InternalQName[currentNodeInfo.getNodeTypes()
                                                                                                                                .size()])));
      checkReferenceable(currentNodeInfo, propertyInfo.getValues().get(0).toString());
    }

    propertyData = new ImportPropertyData(QPath.makeChildPath(currentNodeInfo.getQPath(),
                                                              propertyInfo.getName()),
                                          propertyInfo.getIndentifer(),
                                          0,
                                          propertyInfo.getType(),
                                          currentNodeInfo.getIdentifier(),
                                          false);
    propertyData.setValue(new TransientValueData(currentNodeInfo.getIdentifier()));

    tree.push(currentNodeInfo);
    return propertyData;
  }

  /**
   * @param currentNodeInfo
   * @param values
   * @throws RepositoryException
   */
  private void endVersionable(ImportNodeData currentNodeInfo, List<ValueData> values) throws RepositoryException {
    try {

      if (propertyInfo.getName().equals(Constants.JCR_VERSIONHISTORY)) {
        String versionHistoryIdentifier = null;
        versionHistoryIdentifier = ((TransientValueData) values.get(0)).getString();

        currentNodeInfo.setVersionHistoryIdentifier(versionHistoryIdentifier);
        currentNodeInfo.setContainsVersionhistory(dataConsumer.getItemData(versionHistoryIdentifier) != null);

      } else if (propertyInfo.getName().equals(Constants.JCR_BASEVERSION)) {
        currentNodeInfo.setBaseVersionIdentifier(((TransientValueData) values.get(0)).getString());
      }
    } catch (IOException e) {
      throw new RepositoryException(e);
    }

  }

  /**
   * Returns the list of ValueData for current property
   * 
   * @return
   * @throws RepositoryException
   */
  private List<ValueData> parseValues() throws RepositoryException {
    List<ValueData> values = new ArrayList<ValueData>(propertyInfo.getValuesSize());
    for (int k = 0; k < propertyInfo.getValuesSize(); k++) {

      if (propertyInfo.getType() == PropertyType.BINARY) {
        try {
          InputStream vStream = propertyInfo.getValues().get(k).getInputStream();

          TransientValueData binaryValue = new TransientValueData(vStream);
          binaryValue.setMaxBufferSize(valueFactory.getMaxBufferSize());
          binaryValue.setFileCleaner(valueFactory.getFileCleaner());
          // Call to spool file into tmp
          binaryValue.getAsStream();
          vStream.close();
          propertyInfo.getValues().get(k).remove();
          values.add(binaryValue);

        } catch (IOException e) {
          throw new RepositoryException(e);
        }

      } else {
        String val = new String(propertyInfo.getValues().get(k).toString());
        values.add(((BaseValue) valueFactory.createValue(val, propertyInfo.getType())).getInternalData());
      }
    }

    return values;

  }

  /**
   * Returns the value of the named XML attribute.
   * 
   * @param attributes set of XML attributes
   * @param name attribute name
   * @return attribute value, or <code>null</code> if the named attribute is not
   *         found
   * @throws RepositoryException
   */

  protected String getAttribute(Map<String, String> attributes, InternalQName name) throws RepositoryException {
    JCRName jname = locationFactory.createJCRName(name);
    return attributes.get(jname.getAsString());
  }
}
