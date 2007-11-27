/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS. All rights reserved.          *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.xml.importing;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.NoSuchNodeTypeException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.ExtendedPropertyType;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitions;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.datamodel.IllegalPathException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.JCRName;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.value.BaseValue;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.dataflow.version.VersionHistoryDataHelper;
import org.exoplatform.services.jcr.impl.xml.XmlSaveType;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class SystemViewImporter extends BaseXmlImporter {
  /**
   * 
   */
  private static Log         log = ExoLogger.getLogger(SystemViewImporter.class);

  /**
   * 
   */
  private InternalQName      currentPropName;

  /**
   * 
   */
  private List<DecodedValue> currentPropValues;

  /**
   * 
   */
  private int                currentPropType;

  /**
   * @param parent
   * @param uuidBehavior
   * @param saveType
   * @param respectPropertyDefinitionsConstraints
   */
  public SystemViewImporter(final NodeImpl parent,
                             final int uuidBehavior,
                             final XmlSaveType saveType,
                             final boolean respectPropertyDefinitionsConstraints) {
    super(parent, uuidBehavior, saveType, respectPropertyDefinitionsConstraints);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.xml.importing.Importer#characters(char[],
   *      int, int)
   */
  public void characters(char[] ch, int start, int length) throws RepositoryException {
    // property values
    if (currentPropValues.size() > 0) {
      DecodedValue curPropValue = currentPropValues.get(currentPropValues.size() - 1);
      if (currentPropType == PropertyType.BINARY) {
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

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.xml.importing.Importer#endElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public void endElement(String uri, String localName, String name) throws RepositoryException {
    InternalQName elementName = locationFactory.parseJCRName(name).getInternalName();

    if (Constants.SV_NODE.equals(elementName)) {
      // sv:node element
      endNode();
    } else if (Constants.SV_PROPERTY.equals(elementName)) {
      // sv:property element

      ImportedPropertyData propertyData = endProperty();
      if (propertyData != null)
        changesLog.add(new ItemState(propertyData, ItemState.ADDED, true, tree.peek().getQPath()));
    } else if (Constants.SV_VALUE.equals(elementName)) {
      // sv:value element
    } else {
      throw new RepositoryException("invalid element in system view xml document: " + localName);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.xml.importing.Importer#startElement(java.lang.String,
   *      java.lang.String, java.lang.String, java.util.Map)
   */
  public void startElement(String namespaceURI,
                           String localName,
                           String name,
                           Map<String, String> atts) throws RepositoryException {
    InternalQName elementName = locationFactory.parseJCRName(name).getInternalName();

    if (Constants.SV_NODE.equals(elementName)) {
      // sv:node element

      // node name (value of sv:name attribute)
      String svName = getAttribute(atts, Constants.SV_NAME);
      if (svName == null) {
        throw new RepositoryException("Missing mandatory sv:name attribute of element sv:node");
      }

      if ("jcr:root".equals(svName)) {
        svName = "";
      }
      NodeData parentData = null;

      parentData = tree.peek();
      InternalQName currentNodeName = locationFactory.parseJCRName(svName).getInternalName();
      int nodeIndex = getNodeIndex(parentData, currentNodeName, null);
      ImportedNodeData newNodeData = new ImportedNodeData(parentData, currentNodeName, nodeIndex);

      newNodeData.setOrderNumber(getNextChildOrderNum(parentData));
      newNodeData.setIdentifier(IdGenerator.generate());

      changesLog.add(new ItemState(newNodeData, ItemState.ADDED, true, parentData.getQPath()));

      tree.push(newNodeData);

    } else if (Constants.SV_PROPERTY.equals(elementName)) {
      // sv:property element

      currentPropValues = new ArrayList<DecodedValue>();

      // property name (value of sv:name attribute)
      String svName = getAttribute(atts, Constants.SV_NAME);
      if (svName == null) {
        throw new RepositoryException("missing mandatory sv:name attribute of element sv:property");
      }
      currentPropName = locationFactory.parseJCRName(svName).getInternalName();

      // property type (sv:type attribute)
      String type = getAttribute(atts, Constants.SV_TYPE);
      if (type == null) {
        throw new RepositoryException("missing mandatory sv:type attribute of element sv:property");
      }
      try {
        currentPropType = ExtendedPropertyType.valueFromName(type);
      } catch (IllegalArgumentException e) {
        throw new RepositoryException("Unknown property type: " + type, e);
      }
    } else if (Constants.SV_VALUE.equals(elementName)) {
      // sv:value element

      currentPropValues.add(new DecodedValue());

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
  private ImportedPropertyData endMixinTypes() throws PathNotFoundException,
                                              RepositoryException,
                                              NoSuchNodeTypeException {
    ImportedPropertyData propertyData;
    InternalQName[] mixinNames = new InternalQName[currentPropValues.size()];
    List<ValueData> values = new ArrayList<ValueData>(currentPropValues.size());
    ImportedNodeData currentNodeInfo = (ImportedNodeData) tree.peek();
    for (int i = 0; i < currentPropValues.size(); i++) {

      String value = currentPropValues.get(i).toString();

      mixinNames[i] = locationFactory.parseJCRName(value).getInternalName();
      currentNodeInfo.addNodeType((ntManager.getNodeType(mixinNames[i])));
      values.add(new TransientValueData(value.toString()));
    }

    currentNodeInfo.setMixinTypeNames(mixinNames);

    propertyData = new ImportedPropertyData(QPath.makeChildPath(currentNodeInfo.getQPath(),
                                                                currentPropName),
                                            IdGenerator.generate(),
                                            0,
                                            currentPropType,
                                            currentNodeInfo.getIdentifier(),
                                            true);
    propertyData.setValues(parseValues());
    return propertyData;
  }

  /**
   * @throws RepositoryException
   */
  private void endNode() throws RepositoryException {
    ImportedNodeData currentNodeInfo = (ImportedNodeData) tree.pop();

    currentNodeInfo.setMixinTypeNames(currentNodeInfo.getMixinTypeNames());

    if (currentNodeInfo.isMixVersionable() && !currentNodeInfo.isContainsVersionhistory()) {
      PlainChangesLogImpl changes = new PlainChangesLogImpl();
      // using VH helper as for one new VH, all changes in changes log
      new VersionHistoryDataHelper(currentNodeInfo,
                                   changes,
                                   session.getTransientNodesManager(),
                                   session.getWorkspace().getNodeTypeManager(),
                                   currentNodeInfo.getVersionHistoryIdentifier(),
                                   currentNodeInfo.getBaseVersionIdentifier());
      for (ItemState state : changes.getAllStates()) {
        if (state.getData().getQPath().isDescendantOf(Constants.JCR_SYSTEM_PATH, false)) {
          changesLog.add(state);
        }
      }

    }
  }

  /**
   * @return
   * @throws PathNotFoundException
   * @throws RepositoryException
   * @throws NoSuchNodeTypeException
   */
  private ImportedPropertyData endPrimaryType() throws PathNotFoundException,
                                               RepositoryException,
                                               NoSuchNodeTypeException {
    ImportedPropertyData propertyData;
    String sName = currentPropValues.get(0).toString();
    InternalQName primaryTypeName = locationFactory.parseJCRName(sName).getInternalName();

    ImportedNodeData nodeData = (ImportedNodeData) tree.peek();

    //
    nodeData.addNodeType((ntManager.getNodeType(primaryTypeName)));
    nodeData.setPrimaryTypeName(primaryTypeName);

    propertyData = new ImportedPropertyData(QPath.makeChildPath(nodeData.getQPath(),
                                                                currentPropName),
                                            IdGenerator.generate(),
                                            0,
                                            currentPropType,
                                            nodeData.getIdentifier(),
                                            false);
    propertyData.setValues(parseValues());

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
  private ImportedPropertyData endProperty() throws PathNotFoundException,
                                            RepositoryException,
                                            NoSuchNodeTypeException,
                                            IllegalPathException,
                                            ValueFormatException {
    ImportedPropertyData propertyData = null;
    if (Constants.JCR_PRIMARYTYPE.equals(currentPropName)) {

      propertyData = endPrimaryType();

    } else if (Constants.JCR_MIXINTYPES.equals(currentPropName)) {
      propertyData = endMixinTypes();

    } else if (Constants.JCR_UUID.equals(currentPropName)) {
      propertyData = endUuid();

    } else {
      ImportedNodeData currentNodeInfo = (ImportedNodeData) tree.peek();
      List<ValueData> values = parseValues();

      // determinating is property multivalue;
      boolean isMultivalue = true;

      PropertyDefinitions defs;
      try {
        defs = ntManager.findPropertyDefinitions(currentPropName,
                                                 currentNodeInfo.getPrimaryTypeName(),
                                                 currentNodeInfo.getMixinTypeNames());
      } catch (RepositoryException e) {
        if (!respectPropertyDefinitionsConstraints) {
          log.warn(e.getLocalizedMessage());
          return null;
        }
        throw e;
      }

      if (values.size() == 1) {
        // there is single-value defeniton
        if (defs.getDefinition(false) != null) {
          isMultivalue = false;
        }
      } else {
        if ((defs.getDefinition(true) == null) && (defs.getDefinition(false) != null)) {
          throw new ValueFormatException("Can not assign multiple-values "
              + "Value to a single-valued property " + currentPropName.getName());
        }
      }
      log.debug("Import " + currentPropName.getName() + " size=" + currentPropValues.size()
          + " isMultivalue=" + isMultivalue);

      propertyData = new ImportedPropertyData(QPath.makeChildPath(currentNodeInfo.getQPath(),
                                                                  currentPropName),
                                              IdGenerator.generate(),
                                              0,
                                              currentPropType,
                                              currentNodeInfo.getIdentifier(),
                                              isMultivalue);
      propertyData.setValues(values);

      if (currentNodeInfo.isMixVersionable())
        endVersionable(currentNodeInfo, values);

    }
    return propertyData;
  }

  /**
   * @return
   * @throws RepositoryException
   * @throws PathNotFoundException
   * @throws IllegalPathException
   */
  private ImportedPropertyData endUuid() throws RepositoryException,
                                        PathNotFoundException,
                                        IllegalPathException {
    ImportedPropertyData propertyData;
    ImportedNodeData currentNodeInfo = (ImportedNodeData) tree.pop();

    currentNodeInfo.setMixReferenceable(isNodeType(Constants.MIX_REFERENCEABLE,
                                                   currentNodeInfo.getCurrentNodeTypes()));
    String identifier = null;
    if (currentNodeInfo.isMixReferenceable()) {
      currentNodeInfo.setMixVersionable(isNodeType(Constants.MIX_VERSIONABLE,
                                                   currentNodeInfo.getCurrentNodeTypes()));
      identifier = validateUuidCollision(currentPropValues.get(0).toString());
      if (identifier != null) {
        boolean reloadSNS = uuidBehavior == ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING
            || uuidBehavior == ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING;
        QPath newPath = null;
        if (reloadSNS) {
          NodeData currentParentData = tree.peek();
          // current node already in list
          int nodeIndex = getNodeIndex(currentParentData,
                                       currentNodeInfo.getQName(),
                                       currentNodeInfo.getIdentifier());
          newPath = QPath.makeChildPath(currentParentData.getQPath(),
                                        currentNodeInfo.getQName(),
                                        nodeIndex);
          currentNodeInfo.setQPath(newPath);
        }
        String oldIdentifer = currentNodeInfo.getIdentifier();
        // update parentIdentifer
        for (ItemState state : changesLog.getAllStates()) {
          ItemData data = state.getData();
          if (data.getParentIdentifier().equals(oldIdentifer)) {
            ((ImportedItemData) data).setParentIdentifer(identifier);
            if (reloadSNS)
              ((ImportedItemData) data).setQPath(QPath.makeChildPath(newPath, data.getQPath()
                                                                                  .getName()));

          }

        }

        currentNodeInfo.setIdentifier(identifier);

      }
      if (uuidBehavior == ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING)
        currentNodeInfo.setParentIdentifer(tree.peek().getIdentifier());
    }

    propertyData = new ImportedPropertyData(QPath.makeChildPath(currentNodeInfo.getQPath(),
                                                                currentPropName),
                                            IdGenerator.generate(),
                                            0,
                                            currentPropType,
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
  private void endVersionable(ImportedNodeData currentNodeInfo, List<ValueData> values) throws RepositoryException {
    {
      if (currentPropName.equals(Constants.JCR_VERSIONHISTORY)) {
        String versionHistoryIdentifier = null;
        try {
          versionHistoryIdentifier = ((TransientValueData) values.get(0)).getString();

        } catch (IOException e) {
          throw new RepositoryException(e);
        }
        currentNodeInfo.setVersionHistoryIdentifier(versionHistoryIdentifier);
        currentNodeInfo.setContainsVersionhistory(session.getTransientNodesManager()
                                                         .getItemData(versionHistoryIdentifier) != null);

      } else if (currentPropName.equals(Constants.JCR_BASEVERSION)) {
        try {
          currentNodeInfo.setBaseVersionIdentifier(((TransientValueData) values.get(0)).getString());
        } catch (IOException e) {
          throw new RepositoryException(e);
        }
      }

    }
  }

  /**
   * Returns the value of the named XML attribute.
   * 
   * @param attributes set of XML attributes
   * @param name attribute name
   * @return attribute value, or <code>null</code> if the named attribute is
   *         not found
   * @throws RepositoryException
   */

  private String getAttribute(Map<String, String> attributes, InternalQName name) throws RepositoryException {
    JCRName jname = locationFactory.createJCRName(name);
    return attributes.get(jname.getAsString());
  }

  /**
   * Returns the list of ValueData for current property
   * 
   * @return
   * @throws RepositoryException
   */
  private List<ValueData> parseValues() throws RepositoryException {
    List<ValueData> values = new ArrayList<ValueData>(currentPropValues.size());
    for (int k = 0; k < currentPropValues.size(); k++) {

      if (currentPropType == PropertyType.BINARY) {
        try {
          InputStream vStream = currentPropValues.get(k).getInputStream();

          TransientValueData binaryValue = new TransientValueData(vStream);
          binaryValue.setMaxBufferSize(session.getValueFactory().getMaxBufferSize());
          binaryValue.setFileCleaner(session.getValueFactory().getFileCleaner());
          // Call to spool file into tmp
          binaryValue.getAsStream();
          vStream.close();
          currentPropValues.get(k).remove();
          values.add(binaryValue);

        } catch (IOException e) {
          throw new RepositoryException(e);
        }

      } else {
        String val = new String(currentPropValues.get(k).toString());
        values.add(((BaseValue) session.getValueFactory().createValue(val, currentPropType)).getInternalData());
      }
    }

    return values;

  }
}
