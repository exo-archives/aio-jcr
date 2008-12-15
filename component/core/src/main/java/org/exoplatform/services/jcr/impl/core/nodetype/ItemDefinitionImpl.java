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
import org.exoplatform.services.jcr.impl.Constants;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author Gennady Azarenkov
 * @version $Id: ItemDefinitionImpl.java 11907 2008-03-13 15:36:21Z ksm $
 */

public abstract class ItemDefinitionImpl implements ExtendedItemDefinition {

  protected final NodeType declaringNodeType;

  protected final String   name;

  protected final boolean  autoCreate;

  protected final int      onVersion;

  protected final boolean  readOnly;

  protected final boolean  mandatory;

  protected int            hashCode;

  public ItemDefinitionImpl(String name,
                            NodeType declaringNodeType,
                            boolean autoCreate,
                            int onVersion,
                            boolean readOnly,
                            boolean mandatory) {
    this.declaringNodeType = declaringNodeType;
    this.autoCreate = autoCreate;
    this.onVersion = onVersion;
    this.readOnly = readOnly;
    this.mandatory = mandatory;
    this.name = name;

    int hk = 7;
    hk = 31 * hk + (name != null ? name.hashCode() : 0);
    hk = 31 * hk + (autoCreate ? 0 : 1);
    hk = 31 * hk + (readOnly ? 0 : 1);
    hk = 31 * hk + (mandatory ? 0 : 1);
    hk = 31 * hk + onVersion;
    this.hashCode = hk;
  }

  @Override
  public int hashCode() {
    return hashCode;
  }

  /**
   * {@inheritDoc}
   */
  public String getName() {
    return name;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isAutoCreated() {
    return autoCreate;
  }

  /**
   * {@inheritDoc}
   */
  public int getOnParentVersion() {
    return onVersion;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isProtected() {
    return readOnly;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isMandatory() {
    return mandatory;
  }

  /**
   * {@inheritDoc}
   */
  public NodeType getDeclaringNodeType() {
    return declaringNodeType;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isResidualSet() {
    return this.name.equals(Constants.JCR_ANY_NAME.getName());
  }
}
