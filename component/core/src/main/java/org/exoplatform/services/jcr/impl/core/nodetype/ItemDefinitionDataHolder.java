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
import java.util.List;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.core.nodetype.NodeDefinitionData;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeData;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionData;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionDatas;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/> Per-repository component holding all
 * Child Nodes and Properties Definitions as flat Map For ex definition for
 * jcr:primaryType will be repeated as many times as many primary nodetypes is
 * registered (as each primary nodetype extends nt:base directly or indirectly)
 * and so on.
 * 
 * @author Gennady Azarenkov
 * @version $Id: ItemDefinitionDataHolder.java 11907 2008-03-13 15:36:21Z ksm $
 */

public class ItemDefinitionDataHolder {

  private static Log                                            LOG = ExoLogger.getLogger("jcr.ItemDefinitionDataHolder");

  private final HashMap<ChildNodeDefKey, NodeDefinitionData>    nodeDefinitions;

  private final HashMap<PropertyDefKey, PropertyDefinitionData> propertyDefinitions;

  private final HashMap<DefaultNodeDefKey, NodeDefinitionData>  defNodeDefinitions;

  public ItemDefinitionDataHolder(NodeTypeDataHierarchyHolder nodeTypesHierarchy) {
    this.nodeDefinitions = new HashMap<ChildNodeDefKey, NodeDefinitionData>();
    this.propertyDefinitions = new HashMap<PropertyDefKey, PropertyDefinitionData>();
    this.defNodeDefinitions = new HashMap<DefaultNodeDefKey, NodeDefinitionData>();
  }

  /**
   * @param parentNodeType - name of parent node type
   * @param childName name of child node
   * @param childNodeType name of child node type
   * @return Child NodeDefinition or null if not found
   */
  public NodeDefinitionData getChildNodeDefinition(InternalQName parentNodeType,
                                                   InternalQName childName,
                                                   InternalQName childNodeType) {

    NodeDefinitionData def = getNodeDefinitionFromThisOrSupertypes(parentNodeType,
                                                                   childName,
                                                                   childNodeType);

    // residual
    if (def == null)
      def = getNodeDefinitionFromThisOrSupertypes(parentNodeType,
                                                  Constants.JCR_ANY_NAME,
                                                  childNodeType);

    return def;
  }

  // public NodeDefinitionData[] getAllChildNodeDefinitions(InternalQName...
  // nodeTypes) {
  //
  // }

  private NodeDefinitionData getNodeDefinitionFromThisOrSupertypes(InternalQName parentNodeType,
                                                                   InternalQName childName,
                                                                   InternalQName childNodeType) {

    NodeDefinitionData def = nodeDefinitions.get(new ChildNodeDefKey(parentNodeType,
                                                                     childName,
                                                                     childNodeType));
    if (def != null)
      return def;

    // asks supers in DATA manager
    // for (InternalQName su : nodeTypesHierarchy.getSupertypes(parentNodeType))
    // {
    // def = nodeDefinitions.get(new ChildNodeDefKey(su, childName,
    // childNodeType));
    // if (def != null)
    // break;
    // }

    return def;
  }

  /**
   * @param pr name of parent node types
   * @param childName name of child node
   * @return default ChildNodeDefinition or null if not found
   */
  public NodeDefinitionData getDefaultChildNodeDefinition(InternalQName childName,
                                                          InternalQName... nodeTypes) {

    for (InternalQName parentNodeType : nodeTypes) {
      NodeDefinitionData def = defNodeDefinitions.get(new DefaultNodeDefKey(parentNodeType,
                                                                            childName));
      if (def != null)
        return def;
    }

    // residual
    for (InternalQName parentNodeType : nodeTypes) {
      NodeDefinitionData def = defNodeDefinitions.get(new DefaultNodeDefKey(parentNodeType,
                                                                            Constants.JCR_ANY_NAME));
      if (def != null)
        return def;
    }

    return null;
  }

  /**
   * @param parentNodeType name of parent node type
   * @param propertyName name of child property
   * @param multiValued
   * @return Child PropertyDefinition or null if not found
   */
  public PropertyDefinitionDatas getPropertyDefinitions(final InternalQName propertyName,
                                                        final InternalQName... nodeTypes) {

    PropertyDefinitionDatas pdefs = new PropertyDefinitionDatas();

    for (InternalQName nt : nodeTypes) {
      // single-valued
      PropertyDefinitionData def = propertyDefinitions.get(new PropertyDefKey(nt,
                                                                              propertyName,
                                                                              false));
      if (def != null && pdefs.getDefinition(def.isMultiple()) == null)
        pdefs.setDefinition(def); // set if same is not exists

      // multi-valued
      def = propertyDefinitions.get(new PropertyDefKey(nt, propertyName, true));
      if (def != null && pdefs.getDefinition(def.isMultiple()) == null)
        pdefs.setDefinition(def); // set if same is not exists

      // try residual

    }

    return pdefs;
  }

  /**
   * @param parentNodeType name of parent node type
   * @param childName name of child property
   * @param multiValued
   * @return Child PropertyDefinition or null if not found
   */
  public PropertyDefinitionData getPropertyDefinition(InternalQName childName,
                                                      boolean multiValued,
                                                      InternalQName parentNodeType) {

    PropertyDefKey key = new PropertyDefKey(parentNodeType, childName, multiValued);
    PropertyDefinitionData def = propertyDefinitions.get(key);

    // try residual def
    if (def == null) {
      return propertyDefinitions.get(new PropertyDefKey(parentNodeType,
                                                        Constants.JCR_ANY_NAME,
                                                        multiValued));
    }

    return def;
  }

  void putAllDefinitions(List<NodeTypeData> nodeTypes) {
    for (NodeTypeData nodeType : nodeTypes) {
      putDefinitions(nodeType);
    }
  }

  /**
   * adds Child Node/Property Definitions for incoming NodeType (should be
   * called by NodeTypeManager in register method)
   * 
   * @param nodeType
   */
  void putDefinitions(NodeTypeData nodeType) {
    // nodeTypesHierarchy.addNodeType(nodeType);

    // put child node defs
    NodeDefinitionData[] nodeDefs = nodeType.getDeclaredChildNodeDefinitions();
    for (NodeDefinitionData nodeDef : nodeDefs) {
      // put required node type defs
      // TODO put super's child node defs
      for (InternalQName rnt : nodeDef.getRequiredPrimaryTypes()) {
        ChildNodeDefKey nodeDefKey = new ChildNodeDefKey(nodeType.getName(), nodeDef.getName(), rnt);
        nodeDefinitions.put(nodeDefKey, nodeDef);

        if (LOG.isDebugEnabled()) {
          LOG.debug("NodeDef added: parent NT: " + nodeType.getName().getAsString()
              + " child nodeName: " + nodeDef.getName().getAsString() + " childNT: "
              + rnt.getAsString() + " hash: " + nodeDefKey.hashCode());
        }
      }

      // put default node definition
      DefaultNodeDefKey defNodeDefKey = new DefaultNodeDefKey(nodeType.getName(), nodeDef.getName());
      defNodeDefinitions.put(defNodeDefKey, nodeDef);

      if (LOG.isDebugEnabled()) {
        LOG.debug("Default NodeDef added: parent NT: " + nodeType.getName().getAsString()
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

      if (LOG.isDebugEnabled()) {
        LOG.debug("PropDef added: parent NT: " + nodeType.getName().getAsString()
            + " child propName: " + propDef.getName().getAsString() + " isMultiple: "
            + propDef.isMultiple() + " hash: " + propDefKey.hashCode());
      }
    }

    // TODO traverse supertypes for child nodes and props
    // for (InternalQName su : nodeType.getDeclaredSupertypeNames()) {
    // nodeTypesHierarchy.
    // }
  }

  /**
   * @see about hash code generation:
   *      http://www.geocities.com/technofundo/tech/java/equalhash.html
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
