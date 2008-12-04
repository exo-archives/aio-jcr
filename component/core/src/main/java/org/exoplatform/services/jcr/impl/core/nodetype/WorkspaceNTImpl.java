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
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: WorkspaceNTImpl.java 11907 2008-03-13 15:36:21Z ksm $
 */
@Deprecated
public class WorkspaceNTImpl implements ExtendedNodeType {

  private NodeTypeImpl nodeType;

  private SessionImpl  session;

  @Deprecated
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

}
