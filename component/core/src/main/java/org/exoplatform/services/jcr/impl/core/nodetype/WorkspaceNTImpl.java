/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core.nodetype;

import java.util.ArrayList;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitions;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.core.SessionImpl;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: WorkspaceNTImpl.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class WorkspaceNTImpl implements ExtendedNodeType {

  private NodeTypeImpl nodeType;

  private SessionImpl  session;

  public WorkspaceNTImpl(NodeTypeImpl nodeType, SessionImpl session) {
    super();
    this.session = session;
    this.nodeType = nodeType;
  }

  public String getName() {
    String n = null;
      try {
        n = session.getLocationFactory().createJCRName(nodeType.getQName()).getAsString();
      } catch (RepositoryException e) {
        // should never happen
        throw new RuntimeException("TYPE NAME >>> " + n + " " + e, e);
    }
    return n;
  }

  public NodeType[] getDeclaredSupertypes() {
    return nodeType.getDeclaredSupertypes();
  }

  public NodeType[] getSupertypes() {
    return nodeType.getSupertypes();
  }

  public boolean canAddChildNode(String childNodeName, String nodeTypeName) {
    return nodeType.canAddChildNode(childNodeName, nodeTypeName);
  }

  public boolean canAddChildNode(String childNodeName) {
    return nodeType.canAddChildNode(childNodeName);
  }

  public boolean canRemoveItem(String itemName) {
    return nodeType.canRemoveItem(itemName);
  }

  public boolean canSetProperty(String propertyName, Value value) {
    return nodeType.canSetProperty(propertyName, value);
  }

  public boolean canSetProperty(String propertyName, Value[] values) {
    return nodeType.canSetProperty(propertyName, values);
  }

  public NodeDefinition[] getChildNodeDefinitions() {
    return nodeType.getChildNodeDefinitions();
  }

  public NodeDefinition[] getDeclaredChildNodeDefinitions() {
    return nodeType.getDeclaredChildNodeDefinitions();
  }

  public PropertyDefinition[] getDeclaredPropertyDefinitions() {
    return nodeType.getDeclaredPropertyDefinitions();
  }

  public String getPrimaryItemName() {
    return nodeType.getPrimaryItemName();
  }

  public PropertyDefinition[] getPropertyDefinitions() {
    return nodeType.getPropertyDefinitions();
  }

  public boolean hasOrderableChildNodes() {
    return nodeType.hasOrderableChildNodes();
  }

  public boolean isMixin() {
    return nodeType.isMixin();
  }

  public boolean isNodeType(String nodeTypeName) {
    return nodeType.isNodeType(nodeTypeName);
  }

  public boolean isNodeType(InternalQName nodeTypeQName) {
    return nodeType.isNodeType(nodeTypeQName);
  }

  public boolean equals(Object obj) {
    return nodeType.equals(obj);
  }

  public InternalQName getQName() {
    return nodeType.getQName();
  }

  public NodeDefinition getChildNodeDefinition(InternalQName name) {
    return nodeType.getChildNodeDefinition(name);
  }

  public PropertyDefinitions getPropertyDefinitions(InternalQName name) {
    return nodeType.getPropertyDefinitions(name);
  }

  public boolean isChildNodePrimaryTypeAllowed(String typeName) {
    return nodeType.isChildNodePrimaryTypeAllowed(typeName);
  }

  // other methods

  public NodeDefinition getChildNodeDefinition(String typeName) {
    return nodeType.getChildNodeDefinition(typeName);
  }

  public ArrayList getManadatoryItemDefs() {
    return nodeType.getManadatoryItemDefs();
  }

  public PropertyDefinitions getPropertyDefinitions(String name) {
    return nodeType.getPropertyDefinitions(name);
  }

  public void setDeclaredNodeDefs(NodeDefinition[] nodeDefinitions) {
    nodeType.setDeclaredNodeDefs(nodeDefinitions);
  }

  public void setDeclaredPropertyDefs(PropertyDefinition[] propertyDefinitions) {
    nodeType.setDeclaredPropertyDefs(propertyDefinitions);
  }

  public void setDeclaredSupertypes(NodeType[] nodeTypes) {
    nodeType.setDeclaredSupertypes(nodeTypes);
  }

  public void setMixin(boolean mixin) {
    nodeType.setMixin(mixin);
  }

  public void setName(String name) {
    try {
      nodeType.setName(name);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void setOrderableChild(boolean child) {
    nodeType.setOrderableChild(child);
  }

  public void setPrimaryItemName(String primaryItemName) {
    nodeType.setPrimaryItemName(primaryItemName);
  }

}
