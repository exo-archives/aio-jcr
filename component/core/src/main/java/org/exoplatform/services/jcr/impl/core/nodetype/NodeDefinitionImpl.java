/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.core.nodetype;

import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.services.jcr.datamodel.InternalQName;

/**
 * Created by The eXo Platform SARL.
 * 
 * @author Gennady Azarenkov
 * @version $Id: NodeDefinitionImpl.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class NodeDefinitionImpl extends ItemDefinitionImpl implements NodeDefinition {

  private NodeType   defaultNodeType;

  private NodeType[] requiredNodeTypes;

  private boolean    multiple;

  public NodeDefinitionImpl(String name,
      NodeType declaringNodeType,
      NodeType[] requiredNodeTypes,
      NodeType defaultNodeType,
      boolean autoCreate,
      boolean mandatory,
      int onVersion,
      boolean readOnly,
      boolean multiple,
      InternalQName qName) {

    super(name, declaringNodeType, autoCreate, onVersion, readOnly, mandatory, qName);

    this.declaringNodeType = declaringNodeType;
    this.requiredNodeTypes = requiredNodeTypes;
    this.defaultNodeType = defaultNodeType;
    this.multiple = multiple;

  }

  public NodeDefinitionImpl(String name, InternalQName qName) {
    super(name, qName);
  }

  /**
   * @see javax.jcr.nodetype.NodeDefinition#getRequiredPrimaryTypes
   */
  public NodeType[] getRequiredPrimaryTypes() {

    return requiredNodeTypes;
  }

  /**
   * @see javax.jcr.nodetype.NodeDefinition#getDefaultPrimaryType
   */
  public NodeType getDefaultPrimaryType() {
    return defaultNodeType;
  }

  /**
   * @see javax.jcr.nodetype.NodeDefinition#allowSameNameSibs
   */
  public boolean allowsSameNameSiblings() {
    return multiple;
  }

  /**
   * @param defaultNodeType The defaultNodeType to set.
   */
  public void setDefaultNodeType(NodeType defaultNodeType) {
    this.defaultNodeType = defaultNodeType;
  }

  /**
   * @param multiple The multiple to set.
   */
  public void setMultiple(boolean multiple) {
    this.multiple = multiple;
  }

  /**
   * @param requiredNodeTypes The requiredNodeTypes to set.
   */
  public void setRequiredNodeTypes(NodeType[] requiredNodeTypes) {
    this.requiredNodeTypes = requiredNodeTypes;
  }

  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (!(obj instanceof NodeDefinitionImpl))
      return false;
    if (this.getName() == null)
      return ((NodeDefinitionImpl) obj).getName() == null;
    return this.getName().equals(((NodeDefinitionImpl) obj).getName());
  }
}
