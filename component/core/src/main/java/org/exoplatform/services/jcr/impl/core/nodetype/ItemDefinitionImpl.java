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

import javax.jcr.nodetype.NodeType;

import org.exoplatform.services.jcr.core.nodetype.ExtendedItemDefinition;
import org.exoplatform.services.jcr.datamodel.InternalQName;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author Gennady Azarenkov
 * @version $Id: ItemDefinitionImpl.java 11907 2008-03-13 15:36:21Z ksm $
 */

public abstract class ItemDefinitionImpl implements ExtendedItemDefinition {

  protected NodeType            declaringNodeType;

  protected final String        name;

  protected boolean             autoCreate;

  protected int                 onVersion;

  protected boolean             readOnly;

  protected boolean             mandatory;

  protected final InternalQName qName;

  protected int                 hashCode;

  public ItemDefinitionImpl(String name, InternalQName qName) {
    this.name = name;
    this.qName = qName;
    this.hashCode = qName == null ? 0 : qName.hashCode();
  }

  @Override
  public int hashCode() {
    return hashCode;
  }

  public ItemDefinitionImpl(String name,
                            NodeType declaringNodeType,
                            boolean autoCreate,
                            int onVersion,
                            boolean readOnly,
                            boolean mandatory,
                            InternalQName qName) {
    super();
    this.declaringNodeType = declaringNodeType;
    this.autoCreate = autoCreate;
    this.onVersion = onVersion;
    this.readOnly = readOnly;
    this.mandatory = mandatory;
    if (qName == null) {
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

  /*
   * (non-Javadoc)
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
