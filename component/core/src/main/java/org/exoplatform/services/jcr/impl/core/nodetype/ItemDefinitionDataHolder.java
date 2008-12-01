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

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.nodetype.NodeDefinitionData;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeData;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionData;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionDatas;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/> 
 * 
 * Per-repository component holding all Child Nodes and
 * Properties Definitions as flat Map For ex definition for jcr:primaryType will be repeated as many
 * times as many primary nodetypes is registered (as each primary nodetype extends nt:base directly
 * or indirectly) and so on.
 * 
 * @author Gennady Azarenkov
 * @version $Id: ItemDefinitionsHolder.java 11907 2008-03-13 15:36:21Z ksm $
 */

public class ItemDefinitionDataHolder {

  private static Log                                                 log           = ExoLogger.getLogger("jcr.ItemDefinitionDataHolder");

  private static InternalQName                                       RESIDUAL_NAME = new InternalQName(null,
                                                                                                       "*");

  private final HashMap<ChildNodeDefKey, NodeDefinitionData>         nodeDefinitions;

  private final HashMap<PropertyDefKey, PropertyDefinitionData> propertyDefinitions;

  private final HashMap<DefaultNodeDefKey, NodeDefinitionData>       defNodeDefinitions;

  private final NodeTypeDataHierarchyHolder                             nodeTypesHierarchy;

  public ItemDefinitionDataHolder(NodeTypeDataHierarchyHolder nodeTypesHierarchy) {
    this.nodeDefinitions = new HashMap<ChildNodeDefKey, NodeDefinitionData>();
    this.propertyDefinitions = new HashMap<PropertyDefKey, PropertyDefinitionData>();
    this.defNodeDefinitions = new HashMap<DefaultNodeDefKey, NodeDefinitionData>();
    this.nodeTypesHierarchy = nodeTypesHierarchy;
  }

  /**
   * @param parentNodeType
   *          - name of parent node type
   * @param childName
   *          name of child node
   * @param childNodeType
   *          name of child node type
   * @return Child NodeDefinition or null if not found
   */
  public NodeDefinitionData getChildNodeDefinition(InternalQName parentNodeType,
                                                   InternalQName childName,
                                                   InternalQName childNodeType) {

    NodeDefinitionData def = getNodeDefinitionFromThisOrSupertypes(parentNodeType,
                                                                   childName,
                                                                   childNodeType);

    // try residual def
    if (def == null)
      def = getNodeDefinitionFromThisOrSupertypes(parentNodeType, RESIDUAL_NAME, childNodeType);

    if (log.isDebugEnabled()) {
      log.debug("Get NodeDef: parent NT: " + parentNodeType.getAsString() + " child nodeName: "
          + childName.getAsString() + " childNT: " + childNodeType.getAsString());
    }

    return def;

  }

  private NodeDefinitionData getNodeDefinitionFromThisOrSupertypes(InternalQName parentNodeType,
                                                                   InternalQName childName,
                                                                   InternalQName childNodeType) {
    ChildNodeDefKey key = new ChildNodeDefKey(parentNodeType, childName, childNodeType);
    NodeDefinitionData def = nodeDefinitions.get(key);
    if (def != null)
      return def;
    Iterator<InternalQName> i = nodeTypesHierarchy.getSupertypes(parentNodeType).iterator();
    while (i.hasNext()) {
      key = new ChildNodeDefKey(parentNodeType, childName, i.next());
      def = nodeDefinitions.get(key);
      if (def != null)
        break;
    }
    return def;
  }

  /**
   * @param pr
   *          name of parent node types
   * @param childName
   *          name of child node
   * @return default ChildNodeDefinition or null if not found
   */
  public NodeDefinitionData getDefaultChildNodeDefinition(InternalQName primaryNodeType,
                                                          InternalQName[] mixinNodeTypes,
                                                          InternalQName childName) {
    
    DefaultNodeDefKey key = new DefaultNodeDefKey(primaryNodeType, childName);
    NodeDefinitionData def = defNodeDefinitions.get(key);
    if (def != null)
      return def;
    
    for (InternalQName parentNodeType : mixinNodeTypes) {
      key = new DefaultNodeDefKey(parentNodeType, childName);
      def = defNodeDefinitions.get(key);
      if (def != null)
        return def;
    }
    
    return null;
  }

  
  /**
   * @param parentNodeTypes
   *          name of parent node types
   * @param childName
   *          name of child node
   * @return default ChildNodeDefinition or null if not found
   */
  @Deprecated
  public NodeDefinitionData getDefaultChildNodeDefinition(List<InternalQName> parentNodeTypes,
                                                          InternalQName childName) {
    for (InternalQName parentNodeType : parentNodeTypes) {
      DefaultNodeDefKey key = new DefaultNodeDefKey(parentNodeType, childName);
      NodeDefinitionData def = defNodeDefinitions.get(key);
      if (def != null)
        return def;
    }
    return null;
  }

  /**
   * @param parentNodeType
   *          name of parent node type
   * @param propertyName
   *          name of child property
   * @param multiValued
   * @return Child PropertyDefinition or null if not found
   */
  public PropertyDefinitionDatas getPropertyDefinitions(final InternalQName primaryType,
                                                      final InternalQName[] mixinTypes,
                                                      final InternalQName propertyName) {

    final PropertyDefinitionDatas pdefs = new PropertyDefinitionDatas();
    
    // primary type
    // start with single-valued
    PropertyDefKey key = new PropertyDefKey(primaryType, propertyName, false);
    PropertyDefinitionData def = propertyDefinitions.get(key);
    if (def != null)
      pdefs.setDefinition(def);
    else {
      // try multi-valued
      key = new PropertyDefKey(primaryType, propertyName, true);
      def = propertyDefinitions.get(key);
      pdefs.setDefinition(def);
    }

    // try residual
    
    // mixins
    for (InternalQName mixin : mixinTypes) {
      // single-valued
      key = new PropertyDefKey(mixin, propertyName, false);
      def = propertyDefinitions.get(key);
      if (def != null) {
        if (pdefs.getDefinition(def.isMultiple()) == null)  
          pdefs.setDefinition(def); // TODO set if not exists
      } else {
        // or multi-valued
        key = new PropertyDefKey(mixin, propertyName, true);
        def = propertyDefinitions.get(key);
        if (pdefs.getDefinition(def.isMultiple()) == null)  
          pdefs.setDefinition(def); // TODO set if not exists
      }
      
      // try residual
    }
    
//    if (pdefs.getAnyDefinition() == null) { 
//      // try residual def
//      if (def == null) {
//        key = new PropertyDefKey(parentNodeType, RESIDUAL_NAME, multiValued);
//        return propertyDefinitions.get(key);
//      } else
//        return def;
//    }
    
    return pdefs;
  }
  
  /**
   * @param parentNodeType
   *          name of parent node type
   * @param childName
   *          name of child property
   * @param multiValued
   * @return Child PropertyDefinition or null if not found
   */
  public PropertyDefinitionData getPropertyDefinition(InternalQName parentNodeType,
                                                           InternalQName childName,
                                                           boolean multiValued) {

    PropertyDefKey key = new PropertyDefKey(parentNodeType, childName, multiValued);
    PropertyDefinitionData def = propertyDefinitions.get(key);

    // try residual def
    if (def == null) {
      key = new PropertyDefKey(parentNodeType, RESIDUAL_NAME, multiValued);
      return propertyDefinitions.get(key);
    } else
      return def;
  }

  void putAllDefinitions(List<NodeTypeData> nodeTypes) {
    for (NodeTypeData nodeType : nodeTypes) {
      putDefinitions(nodeType);
    }
  }

  /**
   * adds Child Node/Property Definitions for incoming NodeType (should be called by NodeTypeManager
   * in register method)
   * 
   * @param nodeType
   */
  void putDefinitions(NodeTypeData nodeType) {
    nodeTypesHierarchy.addNodeType(nodeType);
    
    // put child node defs
    NodeDefinitionData[] nodeDefs = nodeType.getDeclaredChildNodeDefinitions();
    for (NodeDefinitionData nodeDef : nodeDefs) {
      // put required node type defs
      // TODO put super's child node defs
      for (InternalQName rnt : nodeDef.getRequiredPrimaryTypes()) {
        ChildNodeDefKey nodeDefKey = new ChildNodeDefKey(nodeType.getName(),
                                                         nodeDef.getName(),
                                                         rnt);
        nodeDefinitions.put(nodeDefKey, nodeDef);
        
        if (log.isDebugEnabled()) {
          log.debug("NodeDef added: parent NT: " + nodeType.getName().getAsString() + " child nodeName: "
              + nodeDef.getName().getAsString() + " childNT: " + rnt.getAsString() + " hash: "
              + nodeDefKey.hashCode());
        }
      }
      
      // put default node definition
      DefaultNodeDefKey defNodeDefKey = new DefaultNodeDefKey(nodeType.getName(),
                                                              nodeDef.getName());
      defNodeDefinitions.put(defNodeDefKey, nodeDef);
      
      if (log.isDebugEnabled()) {
        log.debug("Default NodeDef added: parent NT: " + nodeType.getName().getAsString()
            + " child nodeName: " + nodeDef.getName() + " hash: " + defNodeDefKey.hashCode());
      }
    }

    // put prop defs
    // TODO put super's prop defs
    PropertyDefinitionData[] propDefs = nodeType.getDeclaredPropertyDefinitions();
    for (PropertyDefinitionData propDef : propDefs) {
      PropertyDefKey propDefKey = new PropertyDefKey(nodeType.getName(),
                                                               propDef.getName(),
                                                               propDef.isMultiple());
      propertyDefinitions.put(propDefKey, propDef);
      
      if (log.isDebugEnabled()) {
        log.debug("PropDef added: parent NT: " + nodeType.getName().getAsString() + " child propName: "
            + propDef.getName().getAsString() + " isMultiple: " + propDef.isMultiple() + " hash: "
            + propDefKey.hashCode());
      }
    }
    
    // TODO traverse supertypes for child nodes and props - DO IT FROM NT MANAGER
//    for (InternalQName su : nodeType.getDeclaredSupertypeNames()) {
//      nodeTypesHierarchy.
//    }
  }

  /**
   * @see about hash code generation: http://www.geocities.com/technofundo/tech/java/equalhash.html
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
      if (obj == this)
        return true;
      if ((obj == null) || (obj.getClass() != this.getClass()))
        return false;
      ItemDefKey test = (ItemDefKey) obj;
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

    private DefaultNodeDefKey(InternalQName parentNodeType, InternalQName childName) {
      super(parentNodeType, childName);
    }
  }

  private class PropertyDefKey extends ItemDefKey {

    private PropertyDefKey(InternalQName parentNodeType,
                                InternalQName childName,
                                boolean multiValued) {
      super(parentNodeType, childName);
      hashCode = 31 * hashCode + (multiValued ? 1 : 0);
    }
  }

}
