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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/>
 * per-repository component holding all Child Nodes and Properties Definitions
 * as flat Map
 * For ex definition for jcr:primaryType will be repeated as many times as
 * many primary nodetypes is registered (as each primary nodetype extends 
 * nt:base directly or indirectly) and so on.
 * 
 * @author Gennady Azarenkov
 * @version $Id: ItemDefinitionsHolder.java 11907 2008-03-13 15:36:21Z ksm $
 */

public class ItemDefinitionsHolder {
  
  private static Log log = ExoLogger.getLogger("jcr.ItemDefinitionsHolder");
  
  private static InternalQName RESIDUAL_NAME = new InternalQName(null, "*");
  
  private final HashMap <ChildNodeDefKey, NodeDefinitionImpl> nodeDefinitions;
  private final HashMap <ChildPropertyDefKey, PropertyDefinitionImpl> propertyDefinitions;
  private final HashMap <DefaultNodeDefKey, NodeDefinitionImpl> defNodeDefinitions;
  private final NodeTypesHierarchyHolder nodeTypesHierarchy;
  
  public ItemDefinitionsHolder(NodeTypesHierarchyHolder nodeTypesHierarchy) {
    this.nodeDefinitions = new HashMap <ChildNodeDefKey, NodeDefinitionImpl>();
    this.propertyDefinitions = new HashMap <ChildPropertyDefKey, PropertyDefinitionImpl>();
    this.defNodeDefinitions = new HashMap <DefaultNodeDefKey, NodeDefinitionImpl>();
    this.nodeTypesHierarchy = nodeTypesHierarchy;
  }
  
  /**
   * @param parentNodeType - name of parent node type
   * @param childName name of child node
   * @param childNodeType name of child node type
   * @return Child NodeDefinition or null if not found
   */
  public NodeDefinitionImpl getChildNodeDefinition(
      InternalQName parentNodeType,
      InternalQName childName,
      InternalQName childNodeType) {

    NodeDefinitionImpl def = getNodeDefinitionFromThisOrSupertypes(parentNodeType, childName, childNodeType);

    // try residual def
    if(def == null) 
      def = getNodeDefinitionFromThisOrSupertypes(parentNodeType, RESIDUAL_NAME, childNodeType);

    if(log.isDebugEnabled()) {
      log.debug("Get NodeDef: parent NT: "+parentNodeType.getAsString()+
          " child nodeName: "+childName.getAsString()+
          " childNT: "+childNodeType.getAsString());
    }

    return def;

  }
  
  private NodeDefinitionImpl getNodeDefinitionFromThisOrSupertypes(InternalQName parentNodeType,
      InternalQName childName,
      InternalQName childNodeType) {
    ChildNodeDefKey key = new ChildNodeDefKey(parentNodeType, 
        childName, childNodeType);
    NodeDefinitionImpl def = nodeDefinitions.get(key);
    if(def != null)
      return def;
    Iterator <InternalQName> i = nodeTypesHierarchy.getSuperypes(parentNodeType).iterator();
    while(i.hasNext()) {
      key = new ChildNodeDefKey(parentNodeType, 
          childName, i.next());
      def = nodeDefinitions.get(key);
      if(def != null)
        break;
    }
    return def;
  }

  /**
   * @param parentNodeTypes name of parent node types
   * @param childName name of child node
   * @return default ChildNodeDefinition or null if not found 
   */
  public NodeDefinitionImpl getDefaultChildNodeDefinition(
      List <InternalQName> parentNodeTypes,
      InternalQName childName) {
    for(InternalQName parentNodeType:parentNodeTypes){
      DefaultNodeDefKey key = new DefaultNodeDefKey(parentNodeType, 
        childName);
      NodeDefinitionImpl def = defNodeDefinitions.get(key);
      if(def != null)
        return def;
    }
    return null;
  }

  /**
   * @param parentNodeType name of parent node type
   * @param childName name of child property
   * @param multiValued 
   * @return Child PropertyDefinition or null if not found
   */
  public PropertyDefinitionImpl getChildPropertyDefinition(
      InternalQName parentNodeType,
      InternalQName childName,
      boolean multiValued) {
    
    ChildPropertyDefKey key = new ChildPropertyDefKey(parentNodeType, 
        childName, multiValued);
    PropertyDefinitionImpl def = propertyDefinitions.get(key);
    
    // try residual def
    if(def == null) {
      key = new ChildPropertyDefKey(parentNodeType, 
          RESIDUAL_NAME, multiValued);
      return propertyDefinitions.get(key);
    } else
      return def;
      
    
  }
  
  void putAllDefinitions(List<ExtendedNodeType> nodeTypes) {
    for(ExtendedNodeType nodeType: nodeTypes) {
      putDefinitions(nodeType);
    }
  }
  
  /**
   * adds Child Node/Property Definitions for incoming NodeType (should be called by NodeTypeManager in register method)
   * @param nodeType
   */
  void putDefinitions(ExtendedNodeType nodeType) {
    NodeTypeImpl nodeTypeImpl = (NodeTypeImpl) nodeType;
    nodeTypesHierarchy.addNodeType(nodeTypeImpl);
    // put node defs
    NodeDefinition[] nodeDefs = nodeType.getChildNodeDefinitions();
    for(NodeDefinition nodeDef : nodeDefs) {
      NodeDefinitionImpl nodeDefImpl = (NodeDefinitionImpl)nodeDef;
      // put required node defs
      for(NodeType requiredNT: nodeDefImpl.getRequiredPrimaryTypes()) {
        ExtendedNodeType requiredNodeType = (ExtendedNodeType)requiredNT;
        ChildNodeDefKey nodeDefKey = new ChildNodeDefKey(
          nodeTypeImpl.getQName(), 
          nodeDefImpl.getQName(), 
          requiredNodeType.getQName()); 
        nodeDefinitions.put(nodeDefKey, nodeDefImpl);
        if(log.isDebugEnabled()) {
          log.debug("NodeDef added: parent NT: "+nodeTypeImpl.getQName()+
              " child nodeName: "+nodeDefImpl.getQName()+
              " childNT: "+requiredNodeType.getQName()+" hash: "+nodeDefKey.hashCode());
        }
      }
      // put default node definition
      DefaultNodeDefKey defNodeDefKey = new DefaultNodeDefKey(
          nodeTypeImpl.getQName(), 
          nodeDefImpl.getQName()); 
      defNodeDefinitions.put(defNodeDefKey, nodeDefImpl);
      if(log.isDebugEnabled()) {
        log.debug("Default NodeDef added: parent NT: "+nodeTypeImpl.getQName()+
            " child nodeName: "+nodeDefImpl.getQName()+" hash: "+defNodeDefKey.hashCode());
      }

    }

    // put prop defs
    PropertyDefinition[] propDefs = nodeType.getPropertyDefinitions();
    for(PropertyDefinition propDef : propDefs) {
      PropertyDefinitionImpl propDefImpl = (PropertyDefinitionImpl)propDef;
      ChildPropertyDefKey propDefKey = new ChildPropertyDefKey(
          nodeTypeImpl.getQName(), 
          propDefImpl.getQName(), 
          propDefImpl.isMultiple()); 
      propertyDefinitions.put(propDefKey, propDefImpl);
      if(log.isDebugEnabled()) {
        log.debug("PropDef added: parent NT: "+nodeTypeImpl.getQName()+
            " child propName: "+propDefImpl.getQName()+
            " isMultiple: "+propDefImpl.isMultiple()+" hash: "+propDefKey.hashCode());
      }
    }
  }

  /**
   * @see about hash code generation: 
   * http://www.geocities.com/technofundo/tech/java/equalhash.html
   */
  private abstract class ItemDefKey {
    protected int hashCode;
    
    protected ItemDefKey(InternalQName parentNodeType, InternalQName childName) {
      hashCode = 7;
      hashCode = 31 * hashCode + (null == parentNodeType ? 0 : parentNodeType.hashCode());
      hashCode = 31 * hashCode + (null == childName ? 0 : childName.hashCode());
    }
    
    @Override
    public int hashCode() {
      return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
      if(obj == this)
        return true;
      if((obj == null) || (obj.getClass() != this.getClass()))
        return false;
      ItemDefKey test = (ItemDefKey)obj;
      return hashCode == test.hashCode();
    }

  }
  
  private class ChildNodeDefKey extends ItemDefKey {

    private ChildNodeDefKey(InternalQName parentNodeType, 
        InternalQName childName, 
        InternalQName childNodeType) {
      super(parentNodeType, childName);
      hashCode = 31 * hashCode + (null == childNodeType ? 0 : childNodeType.hashCode());
    }
  }
  
  private class DefaultNodeDefKey extends ItemDefKey {
    
    private DefaultNodeDefKey(InternalQName parentNodeType, 
        InternalQName childName) {
      super(parentNodeType, childName);
    }
  }

  private class ChildPropertyDefKey extends ItemDefKey {
    
    private ChildPropertyDefKey(InternalQName parentNodeType, 
        InternalQName childName, 
        boolean multiValued) {
      super(parentNodeType, childName);
      hashCode = 31 * hashCode + (multiValued ? 1 : 0);
    }
  }

}
