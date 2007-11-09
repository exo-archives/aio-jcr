/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.core.nodetype;

import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.services.jcr.datamodel.InternalQName;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: PropertyDefinitionImpl.java 13528 2007-03-19 08:16:46Z rainf0x $
 */

public class PropertyDefinitionImpl extends ItemDefinitionImpl implements PropertyDefinition {

  private final int requiredType;

  private String[]  valueConstraints;

  private Value[]   defaultValues;

  @Override
  public int hashCode() {
    return this.hashCode;
  }

  private final boolean multiple;

  // protected int hashCode;

  public PropertyDefinitionImpl(String name,
      NodeType declaringNodeType,
      int requiredType,
      String[] valueConstraints,
      Value[] defaultValues,
      boolean autoCreate,
      boolean mandatory,
      int onVersion,
      boolean readOnly,
      boolean multiple,
      InternalQName qName) {

    super(name, declaringNodeType, autoCreate, onVersion, readOnly, mandatory, qName);

    this.requiredType = requiredType;
    this.valueConstraints = valueConstraints;
    this.defaultValues = defaultValues;
    this.multiple = multiple;
    this.hashCode = (31 * this.hashCode + requiredType)*31 + (multiple ? 0 : 31);
  }

  /**
   * @see javax.jcr.nodetype.PropertyDefinition#getRequiredType
   */
  public int getRequiredType() {
    return requiredType;
  }

  /**
   * @see javax.jcr.nodetype.PropertyDefinition#getValueConstraints
   */
  public String[] getValueConstraints() {
    return valueConstraints;
  }

  /**
   * @see javax.jcr.nodetype.PropertyDefinition#getDefaultValues
   */
  public Value[] getDefaultValues() {
    if (defaultValues != null && defaultValues.length > 0)
      return defaultValues;
    else
      return null;
  }

  /**
   * @see javax.jcr.nodetype.PropertyDefinition#isMultiple
   */
  public boolean isMultiple() {
    return multiple;
  }

  /**
   * @param defaultValues The defaultValues to set.
   */
  public void setDefaultValues(Value[] defaultValues) {
    this.defaultValues = defaultValues;
  }

  /**
   * @param valueConstraints The valueConstraints to set.
   */
  public void setValueConstraints(String[] valueConstraints) {
    this.valueConstraints = valueConstraints;
  }

  /**
   * Compare property definitions for equality by name, required type and
   * miltiplicity flag. NOTE: UNDEFINED is equals to UNDEFINED only. NOTE: PD
   * without name is equals to PD without name (TODO: but where to use it?)
   */
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (super.equals(obj))
      return true;
    if (obj instanceof PropertyDefinitionImpl) {
      return obj.hashCode() == hashCode;
    }
    return false;
  }
}
