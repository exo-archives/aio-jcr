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

package org.exoplatform.services.jcr.impl.core.nodetype;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.OnParentVersionAction;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.core.ExtendedPropertyType;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.IllegalNameException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.value.NameValue;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.NodeDataReader;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * @author Gennady Azarenkov
 * @version $Id: NodeTypeDataPersister.java 13716 2007-03-23 10:42:43Z rainf0x $
 */

public class NodeTypeDataPersister {
  
  public static Log log = ExoLogger.getLogger("jcr.NodeTypeDataPersister");
  
  private DataManager dataManager;
  
  private PlainChangesLog changesLog;
  
  private NodeData ntRoot;
  
  private ValueFactoryImpl valueFactory = null;
  
  private LocationFactory locationFactory;
  
  public NodeTypeDataPersister(DataManager dataManager, 
      ValueFactoryImpl valueFactory, LocationFactory locationFactory) {
    this.dataManager = dataManager;
    this.valueFactory = valueFactory;
    this.locationFactory = locationFactory;
    this.changesLog = new PlainChangesLogImpl();
    try {
      NodeData jcrSystem = (NodeData) dataManager.getItemData(Constants.SYSTEM_UUID);
      if (jcrSystem != null)
        this.ntRoot = (NodeData) dataManager.getItemData(jcrSystem, new QPathEntry(Constants.JCR_NODETYPES, 1));
    } catch (RepositoryException e) {
      log.warn("Nodetypes storage (/jcr:system/jcr:nodetypes node) is not initialized.");
    }
  }
  
  boolean isPersisted() {
    return ntRoot != null;
  }
   
  /**
   * Parse name in form of JCR(JSR-170) names conversion string. e.g. name_space:item_name, nt:base 
   * 
   * @param name
   * @return
   * @throws IllegalNameException
   */
  private InternalQName parseName(String name) throws RepositoryException {
    
    return locationFactory.parseJCRName(name).getInternalName();
  }
  
  void saveChanges() throws RepositoryException, InvalidItemStateException {
    dataManager.save(new TransactionChangesLog(changesLog)); 
    changesLog.clear();
  }  

  public synchronized void initNodetypesRoot(NodeData nsSystem, boolean addACL) {
    
    if (ntRoot == null) {
      long start = System.currentTimeMillis();
      
      TransientNodeData root = TransientNodeData.createNodeData(nsSystem,
          Constants.JCR_NODETYPES, Constants.NT_UNSTRUCTURED, Constants.NODETYPESROOT_UUID);
      
      TransientPropertyData primaryType = TransientPropertyData.createPropertyData(
          root, Constants.JCR_PRIMARYTYPE, PropertyType.NAME, false);
      primaryType.setValue(new TransientValueData(root.getPrimaryTypeName()));
      
      changesLog.add(ItemState.createAddedState(root))
                .add(ItemState.createAddedState(primaryType));
      
      if(addACL) {
        AccessControlList acl = new AccessControlList(); 
        root.setMixinTypeNames(new InternalQName[] {Constants.EXO_ACCESS_CONTROLLABLE} );
        // jcr:mixinTypes
        TransientPropertyData rootMixinTypes = TransientPropertyData
            .createPropertyData(root, Constants.JCR_MIXINTYPES, PropertyType.NAME, false);
        rootMixinTypes.setValue(new TransientValueData(Constants.EXO_ACCESS_CONTROLLABLE));

        TransientPropertyData exoOwner = TransientPropertyData.createPropertyData(
            root, Constants.EXO_OWNER, PropertyType.STRING, false);
        exoOwner.setValue(new TransientValueData(acl.getOwner()));
        TransientPropertyData exoPerms = TransientPropertyData.createPropertyData(
            root, Constants.EXO_PERMISSIONS, ExtendedPropertyType.PERMISSION, true);
        List<ValueData> perms = new ArrayList<ValueData>();
        for(int i=0; i<acl.getPermissionEntries().size(); i++) {
          AccessControlEntry entry = acl.getPermissionEntries().get(i);
          perms.add(new TransientValueData(entry));
        }
        exoPerms.setValues(perms);
        changesLog.add(ItemState.createAddedState(rootMixinTypes))
                  .add(ItemState.createAddedState(exoOwner))
                  .add(ItemState.createAddedState(exoPerms));
        changesLog.add(new ItemState(root, ItemState.MIXIN_CHANGED, false, null));
      }
      ntRoot = root;
      log.info("/jcr:system/jcr:nodetypes is created, creation time: "
          + (System.currentTimeMillis() - start) + " ms");
    } else {
      log.warn("/jcr:system/jcr:nodetypes already exists");
    }
  }
  
  public synchronized void initStorage(List<NodeType> nodetypes)
      throws PathNotFoundException, RepositoryException {

    if (!isPersisted()) {
      log.warn("Nodetypes storage (/jcr:system/jcr:nodetypes node) is not exists. Possible is not initialized (call initNodetypesRoot() before)");
      return;
    }
    long ntStart = System.currentTimeMillis();
    for (NodeType nt: nodetypes) {
      try {
        addNodeType(nt);
        log.info("Node type " + nt.getName() + " is initialized. ");
      } catch (ItemExistsException e) {
        log.warn("Node exists " + nt.getName() + ". Error: " + e.getMessage());
      }
    }
    saveChanges();
    log.info("Node types initialized. Time: "+ (System.currentTimeMillis() - ntStart) + " ms");

  }

  public boolean hasNodeTypeData(String nodeTypeName) throws RepositoryException {
    return hasNodeTypeData(parseName(nodeTypeName));
  }
  
  public boolean hasNodeTypeData(InternalQName nodeTypeName) throws RepositoryException {
    try {
      return getNodeTypesData(nodeTypeName).size()>0;
    } catch(PathNotFoundException e) {
      return false;
    }
  }
  
  private List<NodeDataReader> getNodeTypesData(InternalQName nodeTypeName) throws RepositoryException {
    
    NodeDataReader ntReader = new NodeDataReader(ntRoot, dataManager, valueFactory);
    ntReader.forNode(nodeTypeName);
    ntReader.read();
    
    ntReader.getNodes(nodeTypeName);
    
    return ntReader.getNodes(nodeTypeName);
  }
  
  public NodeData addNodeType(NodeType nodeType)
      throws PathNotFoundException, RepositoryException, ValueFormatException {

    if (!isPersisted()) {
      log.warn("Nodetypes storage (/jcr:system/jcr:nodetypes node) is not initialized.");
      return null;
    }
    
    NodeData ntNode = TransientNodeData.createNodeData(ntRoot, 
        parseName(nodeType.getName()), Constants.NT_NODETYPE);
    
    TransientPropertyData primaryType = TransientPropertyData.createPropertyData(
        ntNode, Constants.JCR_PRIMARYTYPE, PropertyType.NAME, false);
    primaryType.setValue(new TransientValueData(ntNode.getPrimaryTypeName()));

    TransientPropertyData name = TransientPropertyData.createPropertyData(ntNode, 
        Constants.JCR_NODETYPENAME, PropertyType.NAME, false); // jcr:nodeTypeName
    name.setValue(new TransientValueData(parseName(nodeType.getName())));

    TransientPropertyData isMixin = TransientPropertyData.createPropertyData(ntNode, 
        Constants.JCR_ISMIXIN, PropertyType.BOOLEAN, false); // jcr:isMixin
    isMixin.setValue(new TransientValueData(nodeType.isMixin()));

    TransientPropertyData hasOrderableChildNodes = TransientPropertyData.createPropertyData(ntNode, 
        Constants.JCR_HASORDERABLECHILDNODES, PropertyType.BOOLEAN, false); // jcr:hasOrderableChildNodes
    hasOrderableChildNodes.setValue(new TransientValueData(nodeType.hasOrderableChildNodes()));

    changesLog.add(ItemState.createAddedState(ntNode))
      .add(ItemState.createAddedState(primaryType))
      .add(ItemState.createAddedState(name))
      .add(ItemState.createAddedState(isMixin))
      .add(ItemState.createAddedState(hasOrderableChildNodes));
    
    if (nodeType.getPrimaryItemName() != null) {
      TransientPropertyData primaryItemName = TransientPropertyData.createPropertyData(ntNode, 
          Constants.JCR_PRIMARYITEMNAME, PropertyType.NAME, false); // jcr:primaryItemName
      primaryItemName.setValue(new TransientValueData(parseName(nodeType.getPrimaryItemName())));
      changesLog.add(ItemState.createAddedState(primaryItemName));
    }
    List <ValueData> parents = new ArrayList<ValueData>(); 
    for (int i = 0; i < nodeType.getDeclaredSupertypes().length; i++) {
      parents.add(new TransientValueData(parseName(nodeType.getDeclaredSupertypes()[i].getName())));
    }
    if (parents.size() != 0) {
      TransientPropertyData supertypes = TransientPropertyData.createPropertyData(
          ntNode, Constants.JCR_SUPERTYPES, PropertyType.NAME, true); // jcr:supertypes
      supertypes.setValues(parents);
      changesLog.add(ItemState.createAddedState(supertypes));
    }
    
    if (nodeType.getDeclaredPropertyDefinitions().length > 0) {
      for (int i = 0; i < nodeType.getDeclaredPropertyDefinitions().length; i++) {
        NodeData childProps = TransientNodeData.createNodeData(ntNode,
            Constants.JCR_PROPERTYDEFINITION, Constants.NT_PROPERTYDEFINITION, i + 1);
        
        TransientPropertyData cpPrimaryType = TransientPropertyData.createPropertyData(
            childProps, Constants.JCR_PRIMARYTYPE, PropertyType.NAME, false);
        cpPrimaryType.setValue(new TransientValueData(childProps.getPrimaryTypeName()));
        
        changesLog.add(ItemState.createAddedState(childProps))
                  .add(ItemState.createAddedState(cpPrimaryType));

        initPropertyDefProps(childProps, nodeType.getDeclaredPropertyDefinitions()[i]);
      }
    }

    if (nodeType.getDeclaredChildNodeDefinitions().length > 0) { 
      for (int i = 0; i < nodeType.getDeclaredChildNodeDefinitions().length; i++) {
        NodeData childNodes = TransientNodeData.createNodeData(ntNode,
            Constants.JCR_CHILDNODEDEFINITION, Constants.NT_CHILDNODEDEFINITION, i + 1);
        
        TransientPropertyData cnPrimaryType = TransientPropertyData.createPropertyData(
            childNodes, Constants.JCR_PRIMARYTYPE, PropertyType.NAME, false);
        cnPrimaryType.setValue(new TransientValueData(childNodes.getPrimaryTypeName()));
        
        changesLog.add(ItemState.createAddedState(childNodes))
                  .add(ItemState.createAddedState(cnPrimaryType));
        initNodeDefProps(childNodes, nodeType.getDeclaredChildNodeDefinitions()[i]);
      }
    }
    return ntNode;
  }

  private void initPropertyDefProps(NodeData parent, PropertyDefinition def)
      throws ValueFormatException, RepositoryException {

    if (def.getName() != null) { //Mandatory false
      TransientPropertyData name = TransientPropertyData.createPropertyData(
          parent, Constants.JCR_NAME, PropertyType.NAME, false);
      name.setValue(new TransientValueData(parseName(def.getName())));
      changesLog.add(ItemState.createAddedState(name));
    }
    
    TransientPropertyData autoCreated = TransientPropertyData.createPropertyData(
        parent, Constants.JCR_AUTOCREATED, PropertyType.BOOLEAN, false);
    autoCreated.setValue(new TransientValueData(def.isAutoCreated()));
    
    TransientPropertyData isMandatory = TransientPropertyData.createPropertyData(
        parent, Constants.JCR_MANDATORY, PropertyType.BOOLEAN, false);
    isMandatory.setValue(new TransientValueData(def.isMandatory()));
    
    TransientPropertyData onParentVersion = TransientPropertyData.createPropertyData(
        parent, Constants.JCR_ONPARENTVERSION, PropertyType.STRING, false);
    onParentVersion.setValue(new TransientValueData(
        OnParentVersionAction.nameFromValue(def.getOnParentVersion())));
    
    TransientPropertyData isProtected = TransientPropertyData.createPropertyData(
        parent, Constants.JCR_PROTECTED, PropertyType.BOOLEAN, false);
    isProtected.setValue(new TransientValueData(def.isProtected()));
    
    TransientPropertyData requiredType = TransientPropertyData.createPropertyData(
        parent, Constants.JCR_REQUIREDTYPE, PropertyType.STRING, false);
    requiredType.setValue(new TransientValueData(
        ExtendedPropertyType.nameFromValue(def.getRequiredType())));
    
    TransientPropertyData isMultiple = TransientPropertyData.createPropertyData(
        parent, Constants.JCR_MULTIPLE, PropertyType.BOOLEAN, false);
    isMultiple.setValue(new TransientValueData(def.isMultiple()));
    
    changesLog.add(ItemState.createAddedState(autoCreated))
      .add(ItemState.createAddedState(isMandatory))
      .add(ItemState.createAddedState(onParentVersion))
      .add(ItemState.createAddedState(isProtected))
      .add(ItemState.createAddedState(requiredType))
      .add(ItemState.createAddedState(isMultiple));
    
    if (def.getValueConstraints() != null && def.getValueConstraints().length != 0) {
      List <ValueData> valueConstraintsValues = new ArrayList<ValueData>(); 
      for (int i = 0; i < def.getValueConstraints().length; i++) {
        valueConstraintsValues.add(new TransientValueData(def.getValueConstraints()[i]));
      }
      TransientPropertyData valueConstraints = TransientPropertyData.createPropertyData(
          parent, Constants.JCR_VALUECONSTRAINTS, PropertyType.STRING, true);
      valueConstraints.setValues(valueConstraintsValues);
      changesLog.add(ItemState.createAddedState(valueConstraints));
    }
    
    if (def.getDefaultValues() != null && def.getDefaultValues().length != 0) {
      List <ValueData> defaultValuesValues = new ArrayList<ValueData>(); 
      for (int i = 0; i < def.getDefaultValues().length; i++) {
        // [PN] 27.07.06
        if (def.getDefaultValues()[i] != null)
          defaultValuesValues.add(new TransientValueData(def.getDefaultValues()[i].getString()));
      }
      TransientPropertyData defaultValues = TransientPropertyData.createPropertyData(
          parent, Constants.JCR_DEFAULTVALUES, PropertyType.STRING, true);
      defaultValues.setValues(defaultValuesValues);
      changesLog.add(ItemState.createAddedState(defaultValues));
    }
  }

  private void initNodeDefProps(NodeData parent, NodeDefinition def)
      throws ValueFormatException, RepositoryException {

    if (def.getName() != null) { //Mandatory false
      TransientPropertyData name = TransientPropertyData.createPropertyData(
          parent, Constants.JCR_NAME, PropertyType.NAME, false);
      name.setValue(new TransientValueData(parseName(def.getName())));
      changesLog.add(ItemState.createAddedState(name));
    }
    
    TransientPropertyData autoCreated = TransientPropertyData.createPropertyData(
        parent, Constants.JCR_AUTOCREATED, PropertyType.BOOLEAN, false);
    autoCreated.setValue(new TransientValueData(def.isAutoCreated()));
    
    TransientPropertyData isMandatory = TransientPropertyData.createPropertyData(
        parent, Constants.JCR_MANDATORY, PropertyType.BOOLEAN, false);
    isMandatory.setValue(new TransientValueData(def.isMandatory()));
    
    TransientPropertyData onParentVersion = TransientPropertyData.createPropertyData(
        parent, Constants.JCR_ONPARENTVERSION, PropertyType.STRING, false);
    onParentVersion.setValue(new TransientValueData(
        OnParentVersionAction.nameFromValue(def.getOnParentVersion())));
    
    TransientPropertyData isProtected = TransientPropertyData.createPropertyData(
        parent, Constants.JCR_PROTECTED, PropertyType.BOOLEAN, false);
    isProtected.setValue(new TransientValueData(def.isProtected()));
    
    TransientPropertyData sameNameSiblings = TransientPropertyData.createPropertyData(
        parent, Constants.JCR_SAMENAMESIBLINGS, PropertyType.BOOLEAN, false);
    sameNameSiblings.setValue(new TransientValueData(def.allowsSameNameSiblings()));
    
    if (def.getDefaultPrimaryType() != null) { //Mandatory false
      TransientPropertyData defaultPrimaryType = TransientPropertyData.createPropertyData(
          parent, Constants.JCR_DEFAULTPRIMNARYTYPE, PropertyType.NAME, false);
      defaultPrimaryType.setValue(new TransientValueData(parseName(def.getDefaultPrimaryType().getName())));
      changesLog.add(ItemState.createAddedState(defaultPrimaryType));
    }
    
    changesLog.add(ItemState.createAddedState(autoCreated))
      .add(ItemState.createAddedState(isMandatory))
      .add(ItemState.createAddedState(onParentVersion))
      .add(ItemState.createAddedState(isProtected))
      .add(ItemState.createAddedState(sameNameSiblings));    
    
    if (def.getRequiredPrimaryTypes() != null && def.getRequiredPrimaryTypes().length != 0) {
      List <ValueData> requiredPrimaryTypesValues = new ArrayList<ValueData>(); 
      for (int i = 0; i < def.getRequiredPrimaryTypes().length; i++) {
        requiredPrimaryTypesValues.add(new TransientValueData(parseName(def.getRequiredPrimaryTypes()[i].getName())));
      }
      TransientPropertyData requiredPrimaryTypes = TransientPropertyData.createPropertyData(
          parent, Constants.JCR_REQUIREDPRIMARYTYPES, PropertyType.NAME, true);
      requiredPrimaryTypes.setValues(requiredPrimaryTypesValues);
      changesLog.add(ItemState.createAddedState(requiredPrimaryTypes));
    }  
  }

  private NodeType findType(String nodeTypeName, List<NodeType> ntList) {
    for (NodeType regNt: ntList) {
      if (regNt.getName().equals(nodeTypeName)) {
        return regNt;
      }
    }
    return null;
  }
    
  public List<NodeType> loadNodetypes(List<NodeType> registeredNodeTypes, NodeTypeManagerImpl ntManager) throws PathNotFoundException, RepositoryException {
    
    if (!isPersisted()) {
      NodeData jcrSystem = (NodeData) dataManager.getItemData(Constants.SYSTEM_UUID);
      if (jcrSystem != null)
        this.ntRoot = (NodeData) dataManager.getItemData(jcrSystem, new QPathEntry(Constants.JCR_NODETYPES, 1));
      else
        throw new RepositoryException("jcr:system is not found. Possible the workspace is not initialized properly"); 
    }
    
    if (!isPersisted()) {
      log.warn("Nodetypes storage (/jcr:system/jcr:nodetypes node) is not initialized. No nodetypes loaded.");
      return new ArrayList<NodeType>();
    }
    
    List<NodeType> ntList = new ArrayList<NodeType>();
    List<NodeType> loadedList = new ArrayList<NodeType>();
    ntList.addAll(registeredNodeTypes);
    
    boolean nextCycle = false;
    List<NodeType> registeringTypes = new ArrayList<NodeType>();
    
    int registerCyclesCount = 1;
    do {
      
      long cycleStart = System.currentTimeMillis();
      log.info(">>> Node types registration cycle " + registerCyclesCount + " started");
      
      NodeDataReader ntReader = new NodeDataReader(ntRoot, dataManager, valueFactory);
      ntReader.forNodesByType(Constants.NT_NODETYPE); // for nt:nodeType
      ntReader.read();
      
      nextNodeType: for (NodeDataReader ntr: ntReader.getNodesByType(Constants.NT_NODETYPE)) {
        
        long ntStart = System.currentTimeMillis();
        
        ntr.forProperty(Constants.JCR_NODETYPENAME, PropertyType.NAME);
        ntr.read();
        String ntName = ntr.getPropertyValue(Constants.JCR_NODETYPENAME).getString();
        NodeType existedNodeType = findType(ntName, ntList);
        if (existedNodeType != null) {
          if (log.isDebugEnabled())
            log.debug("Already reagistered " + ntName);
          continue nextNodeType; // already registered node type with this name
        }
        if (log.isDebugEnabled())
          log.debug("Reagistering from storage " + ntName + " " + (System.currentTimeMillis() - ntStart));
        
        ntr.forProperty(Constants.JCR_PRIMARYTYPE, PropertyType.NAME)
           .forProperty(Constants.JCR_ISMIXIN, PropertyType.BOOLEAN)
           .forProperty(Constants.JCR_HASORDERABLECHILDNODES, PropertyType.BOOLEAN)
           .forProperty(Constants.JCR_PRIMARYITEMNAME, PropertyType.NAME)
           .forProperty(Constants.JCR_SUPERTYPES, PropertyType.NAME);
        ntr.forNodesByType(Constants.NT_PROPERTYDEFINITION)
           .forNodesByType(Constants.NT_CHILDNODEDEFINITION);
        ntr.read();
        if (log.isDebugEnabled())
          log.debug("Node type readed " + ntName + " " + (System.currentTimeMillis() - ntStart));
        
        NodeTypeImpl type = new NodeTypeImpl(ntManager);
        type.setName(ntr.getPropertyValue(Constants.JCR_NODETYPENAME).getString());
        type.setMixin(ntr.getPropertyValue(Constants.JCR_ISMIXIN).getBoolean());
        type.setOrderableChild(ntr.getPropertyValue(Constants.JCR_HASORDERABLECHILDNODES).getBoolean());
        try {
          type.setPrimaryItemName(ntr.getPropertyValue(Constants.JCR_PRIMARYITEMNAME).getString());
        } catch(PathNotFoundException e) { // Mandatory false
        }
        
        if (!registeringTypes.contains(type))
          registeringTypes.add(type);
        
        // -------- Super types --------
        try {
          List<Value> dst = ntr.getPropertyValues(Constants.JCR_SUPERTYPES);
          NodeType[] declaredSupertypes = new NodeType[dst.size()];
          for (int i = 0; i < dst.size(); i++) {
            String superTypeName = dst.get(i).getString();
            declaredSupertypes[i] = findType(superTypeName, ntList);
            if (declaredSupertypes[i] == null) {
              if (nextCycle && findType(superTypeName, registeringTypes) == null)
                // here is this type and we try register 
                throw new ConstraintViolationException("Supertype " + superTypeName 
                    + " is not registered in repository (but need to be registered before nodetype " + type.getName() 
                    + "). Node type resistration aborted.");
              log.info("Supertype " + superTypeName 
                  + " is not registered. " + type.getName() 
                  + " node type will be registered in a next cycle.");
              continue nextNodeType;
            }
          }
          type.setDeclaredSupertypes(declaredSupertypes);
        } catch(PathNotFoundException e) {
        }
  
        // -------- Property definitions --------
        if (log.isDebugEnabled())
          log.debug("Property definitions for " + ntName + " " + (System.currentTimeMillis() - ntStart));
        try {
          List<NodeDataReader> pdNodes = ntr.getNodesByType(Constants.NT_PROPERTYDEFINITION);
          PropertyDefinition[] declaredPropertyDefs = new PropertyDefinition[pdNodes.size()];
          for (int pdi=0; pdi<pdNodes.size(); pdi++) {
            NodeDataReader pdr = pdNodes.get(pdi);
            
            pdr.forProperty(Constants.JCR_NAME, PropertyType.NAME) // jcr:name
               .forProperty(Constants.JCR_AUTOCREATED, PropertyType.BOOLEAN) // jcr:autoCreated
               .forProperty(Constants.JCR_MANDATORY, PropertyType.BOOLEAN) // jcr:mandatory
               .forProperty(Constants.JCR_PROTECTED, PropertyType.BOOLEAN) // jcr:protected
               .forProperty(Constants.JCR_MULTIPLE, PropertyType.BOOLEAN) // jcr:multiple
               .forProperty(Constants.JCR_ONPARENTVERSION, PropertyType.STRING) // jcr:onParentVersion
               .forProperty(Constants.JCR_REQUIREDTYPE, PropertyType.STRING) // jcr:requiredType
               .forProperty(Constants.JCR_VALUECONSTRAINTS, PropertyType.STRING) // jcr:valueConstraints
               .forProperty(Constants.JCR_DEFAULTVALUES, PropertyType.STRING); // jcr:defaultValues        
            pdr.read();
            String[] valueConstraints = null;
            try {
              List<Value> valueConstraintValues = pdr.getPropertyValues(Constants.JCR_VALUECONSTRAINTS);
             valueConstraints = new String[valueConstraintValues.size()];
              for (int j = 0; j < valueConstraintValues.size(); j++) {
                if (valueConstraintValues.get(j) != null)
                  valueConstraints[j] = valueConstraintValues.get(j).getString();
                else
                  valueConstraints[j] = null;
              }
            } catch (PathNotFoundException e) { // Mandatory false
            } 
            Value[] defaultValues = null;
            try {
              List<Value> dvl = pdr.getPropertyValues(Constants.JCR_DEFAULTVALUES);
              defaultValues = new Value[dvl.size()];
              for (int i=0; i<dvl.size(); i++) {
                defaultValues[i] = dvl.get(i);
              }
            } catch (PathNotFoundException e) { // Mandatory false
            }
            
            NameValue nameValue = (NameValue) pdr.getPropertyValue(Constants.JCR_NAME);
            PropertyDefinitionImpl pDef = new PropertyDefinitionImpl(nameValue.getString(),
                type,
                ExtendedPropertyType.valueFromName(pdr.getPropertyValue(Constants.JCR_REQUIREDTYPE)
                    .getString()),
                valueConstraints,
                defaultValues,
                pdr.getPropertyValue(Constants.JCR_AUTOCREATED).getBoolean(),
                pdr.getPropertyValue(Constants.JCR_MANDATORY).getBoolean(),
                OnParentVersionAction.valueFromName(pdr
                    .getPropertyValue(Constants.JCR_ONPARENTVERSION).getString()),
                pdr.getPropertyValue(Constants.JCR_PROTECTED).getBoolean(),
                pdr.getPropertyValue(Constants.JCR_MULTIPLE).getBoolean(),
                nameValue.getQName());
            if (log.isDebugEnabled())
              log.debug("Property definitions readed " + pDef.getName() + " " + (System.currentTimeMillis() - ntStart));
            
            declaredPropertyDefs[pdi] = pDef;
          }
          type.setDeclaredPropertyDefs(declaredPropertyDefs);
        } catch(PathNotFoundException e) { // Mandatory false
        }
  
        // --------- Child nodes definitions ----------
        if (log.isDebugEnabled())
          log.debug("Child nodes definitions for " + ntName + " " + (System.currentTimeMillis() - ntStart));
        try {
          List<NodeDataReader> cdNodes = ntr.getNodesByType(Constants.NT_CHILDNODEDEFINITION);
          NodeDefinition[] declaredChildNodesDefs = new NodeDefinition[cdNodes.size()];
          for (int cdi=0; cdi<cdNodes.size(); cdi++) {
            NodeDataReader cdr = cdNodes.get(cdi);
            
            cdr.forProperty(Constants.JCR_NAME, PropertyType.NAME) // jcr:name
               .forProperty(Constants.JCR_REQUIREDPRIMARYTYPES, PropertyType.NAME) // jcr:requiredPrimaryTypes
               .forProperty(Constants.JCR_AUTOCREATED, PropertyType.BOOLEAN) // jcr:autoCreated
               .forProperty(Constants.JCR_MANDATORY, PropertyType.BOOLEAN) // jcr:mandatory
               .forProperty(Constants.JCR_PROTECTED, PropertyType.BOOLEAN) // jcr:protected
               .forProperty(Constants.JCR_ONPARENTVERSION, PropertyType.STRING) // jcr:onParentVersion
               .forProperty(Constants.JCR_SAMENAMESIBLINGS, PropertyType.STRING) // jcr:sameNameSiblings
               .forProperty(Constants.JCR_DEFAULTPRIMNARYTYPE, PropertyType.NAME); // jcr:defaultPrimaryType
            cdr.read();
            
            NodeDefinitionImpl nDef = null ;
            try {
              
              NameValue nameValue = (NameValue) cdr.getPropertyValue(Constants.JCR_NAME);
              nDef = new NodeDefinitionImpl(nameValue.getString(),nameValue.getQName());
            } catch (PathNotFoundException e) { // Mandatory false
            }
            try {
              String defaultNodeTypeName = cdr.getPropertyValue(Constants.JCR_DEFAULTPRIMNARYTYPE).getString();
              NodeType defaultNodeType = findType(defaultNodeTypeName, ntList);
              if (defaultNodeType != null)
                nDef.setDefaultNodeType(defaultNodeType);
              else if (defaultNodeType == null && defaultNodeTypeName.equals(type.getName())) 
                nDef.setDefaultNodeType(type);
            } catch (PathNotFoundException e) { // Mandatory false
            }
            try {
              List<Value> requiredNodeTypesValues = cdr.getPropertyValues(Constants.JCR_REQUIREDPRIMARYTYPES);
              NodeType[] requiredNodeTypes = new NodeType[requiredNodeTypesValues.size()];
              for (int j = 0; j < requiredNodeTypesValues.size(); j++) {
                if (requiredNodeTypesValues.get(j) != null) {
                  String requiredNodeTypeName = requiredNodeTypesValues.get(j).getString();
                  NodeType requiredNodeType = findType(requiredNodeTypeName, ntList);
                  if (requiredNodeType != null) {
                    requiredNodeTypes[j] = requiredNodeType;
                  } else {
                    if (nextCycle && findType(requiredNodeTypeName, registeringTypes) == null)
                      throw new ConstraintViolationException(
                          "Required node type of NodeDefinition "
                          + nDef.getName() + " is not registered in repository. Required node type " 
                          + requiredNodeTypeName + " must be registered before " + type.getName() 
                          + ". Node type resistration aborted.");
                    log.info("Required node type of NodeDefinition "
                        + nDef.getName() + " is not registered." + type.getName() 
                        + " node type will be registered in a next cycle.");
                    continue nextNodeType;
                  }
                } else {
                  throw new ConstraintViolationException("Required node type is null."
                      + " Type: " + type.getName() + ". NodeDefinition: " + nDef.getName()
                      + ". Node type resistration aborted.");
                }
              }
              nDef.setRequiredNodeTypes(requiredNodeTypes);
              nDef.setDeclaringNodeType(type);
              nDef.setAutoCreate(cdr.getPropertyValue(Constants.JCR_AUTOCREATED).getBoolean());
              nDef.setMandatory(cdr.getPropertyValue(Constants.JCR_MANDATORY).getBoolean());
              nDef.setReadOnly(cdr.getPropertyValue(Constants.JCR_PROTECTED).getBoolean());
              nDef.setMultiple(cdr.getPropertyValue(Constants.JCR_SAMENAMESIBLINGS).getBoolean());
              nDef.setOnVersion(
                  OnParentVersionAction.valueFromName(
                      cdr.getPropertyValue(Constants.JCR_ONPARENTVERSION).getString()));
            } catch (PathNotFoundException e) {
              throw new ConstraintViolationException("Mandatory property did not set."
                  + " NodeDefinition: " + nDef.getName() + ". Type: " + type.getName() + ". Error: "
                  + e.getMessage() + ". Node type resistration aborted.", e);
            }
            
            if (log.isDebugEnabled())
              log.debug("Child nodes definitions readed " + nDef.getName() + " " + (System.currentTimeMillis() - ntStart));
            
            declaredChildNodesDefs[cdi] = nDef;
          }
          type.setDeclaredNodeDefs(declaredChildNodesDefs);
        } catch(PathNotFoundException e) { // Mandatory false
        }
        
        // -------- NodeType done --------
        ntList.add(type);
        loadedList.add(type);
        log.info("NodeType " + type.getName() + " loaded. " + (System.currentTimeMillis() - ntStart) + " ms");
      }
      nextCycle = true;
      log.info("<<< Node types registration cycle " + registerCyclesCount + " finished. "
          + (System.currentTimeMillis() - cycleStart) + " ms");
      registerCyclesCount++; // for owerflow limitation
      if (registerCyclesCount >= 1000)
        throw new RepositoryException("Maximum cycles count of NodeType registrations reached, 1000. Registration breaked."); 
    } while (registeringTypes.size() > loadedList.size());
    return loadedList;
  }  
 
}
