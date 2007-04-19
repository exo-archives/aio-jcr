/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.core.nodetype;

import javax.jcr.nodetype.NodeType;

import org.exoplatform.services.jcr.core.nodetype.ExtendedItemDefinition;
import org.exoplatform.services.jcr.datamodel.InternalQName;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: ItemDefinitionImpl.java 12841 2007-02-16 08:58:38Z peterit $
 */

public abstract class ItemDefinitionImpl implements ExtendedItemDefinition {

  protected NodeType declaringNodeType;

  protected final String name;

  protected boolean autoCreate;

  protected int onVersion;

  protected boolean readOnly;

  protected boolean mandatory;

  protected final InternalQName qName;
  
  protected int hashCode;
  public ItemDefinitionImpl(String name,InternalQName qName) {
    this.name = name;
    this.qName = qName;
    this.hashCode = qName == null ? 0 : qName.hashCode();
  }

  @Override
  public int hashCode() {
    return hashCode;
  }

  public ItemDefinitionImpl(String name, NodeType declaringNodeType,
      boolean autoCreate, int onVersion, boolean readOnly, boolean mandatory,
      InternalQName qName) {
    super();
    this.declaringNodeType = declaringNodeType;
    this.autoCreate = autoCreate;
    this.onVersion = onVersion;
    this.readOnly = readOnly;
    this.mandatory = mandatory;
    if (qName == null){
      System.out.println("==================  qName==null! ==========");
      (new Exception()).printStackTrace();
    }
    this.qName = qName;
    this.name = name;
    this.hashCode = qName == null ? 0 : qName.hashCode();
  }

  /**
   * @see javax.jcr.nodetype.ItemDef#getName
   */
  public String getName() {
    return name;
  }

  /**
   * @see javax.jcr.nodetype.ItemDef#isAutoCreate
   */
  public boolean isAutoCreated() {
    return autoCreate;
  }

  /**
   * @see javax.jcr.nodetype.ItemDef#getOnParentVersion
   */
  public int getOnParentVersion() {
    return onVersion;
  }

  /**
   * @see javax.jcr.nodetype.ItemDef#isProtected
   */
  public boolean isProtected() {
    return readOnly;
  }

  /**
   * @see javax.jcr.nodetype.ItemDef#isMandatory
   */
  public boolean isMandatory() {
    return mandatory;
  }

  /**
   * @see javax.jcr.nodetype.ItemDef#getDeclaringNodeType
   */
  public NodeType getDeclaringNodeType() {
    return declaringNodeType;
  }

  // ////////////////////////////////////////////

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.core.nodetype.ExtendedItemDefinition#isResidualSet()
   */
  public boolean isResidualSet() {
    return this.name.equals(ExtendedItemDefinition.RESIDUAL_SET);
  }

  /**
   * @param autoCreate
   *          The autoCreate to set.
   */
  public void setAutoCreate(boolean autoCreate) {
    this.autoCreate = autoCreate;
  }

  /**
   * @param declaringNodeType
   *          The declaringNodeType to set.
   */
  public void setDeclaringNodeType(NodeType declaringNodeType) {
    this.declaringNodeType = declaringNodeType;
  }

  /**
   * @param mandatory
   *          The mandatory to set.
   */
  public void setMandatory(boolean mandatory) {
    this.mandatory = mandatory;
  }

  /**
   * @param name
   *          The name to set.
   */
//  public void setName(String name) {
//    this.name = name;
//  }
  
//  public void setQName(InternalQName qname) {
//    this.qName = qname;
//  }

  /**
   * @param onVersion
   *          The onVersion to set.
   */
  public void setOnVersion(int onVersion) {
    this.onVersion = onVersion;
  }

  /**
   * @param readOnly
   *          The readOnly to set.
   */
  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }
  
  public InternalQName getQName() {
    return qName;
  }
}
