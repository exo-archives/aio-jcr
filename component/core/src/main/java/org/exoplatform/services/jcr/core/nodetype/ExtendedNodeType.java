/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.core.nodetype;

import java.util.ArrayList;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ItemDefinition;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.services.jcr.datamodel.InternalQName;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: ExtendedNodeType.java 12843 2007-02-16 09:11:18Z peterit $
 */

public interface ExtendedNodeType extends NodeType {

  PropertyDefinitions getPropertyDefinitions(InternalQName name);

  NodeDefinition getChildNodeDefinition(InternalQName name);

  boolean isNodeType(InternalQName nodeTypeQName);

  boolean isChildNodePrimaryTypeAllowed(String typeName);

  NodeDefinition getChildNodeDefinition(String name);

  ArrayList<ItemDefinition> getManadatoryItemDefs();

  PropertyDefinitions getPropertyDefinitions(String name);

  InternalQName getQName();

  void setDeclaredNodeDefs(NodeDefinition[] declaredNodeDefs);

  void setDeclaredPropertyDefs(PropertyDefinition[] declaredPropertyDefs);

  void setDeclaredSupertypes(NodeType[] declaredSupertypes);

  void setMixin(boolean mixin);

  void setName(String name) throws RepositoryException;

  void setOrderableChild(boolean orderableChild);

  void setPrimaryItemName(String primaryItemName);
}
