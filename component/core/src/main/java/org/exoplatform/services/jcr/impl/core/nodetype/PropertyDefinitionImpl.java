/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.core.nodetype;

import javax.jcr.PropertyType;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.services.jcr.datamodel.InternalQName;

/**
 * Created by The eXo Platform SARL        .
 *
 * @author Gennady Azarenkov
 * @version $Id: PropertyDefinitionImpl.java 13528 2007-03-19 08:16:46Z rainf0x $
 */

public class PropertyDefinitionImpl extends ItemDefinitionImpl implements PropertyDefinition {
	
	private int requiredType;

	private String[] valueConstraints;

	private Value[] defaultValues;

	private boolean multiple;
  
  public PropertyDefinitionImpl(String name, NodeType declaringNodeType,
      int requiredType, String[] valueConstraints, Value[] defaultValues,
      boolean autoCreate, boolean mandatory, int onVersion, boolean readOnly,
      boolean multiple, InternalQName qName) {

    super(name, declaringNodeType, autoCreate, onVersion, readOnly, mandatory, qName);

    this.requiredType = requiredType;
    this.valueConstraints = valueConstraints;
    this.defaultValues = defaultValues;
    this.multiple = multiple;
  }

	public PropertyDefinitionImpl() {
		super();
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
	 * @param multiple The multiple to set.
	 */
	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	/**
	 * @param requiredType The requiredType to set.
	 */
	public void setRequiredType(int requiredType) {
		this.requiredType = requiredType;
	}

	/**
	 * @param valueConstraints The valueConstraints to set.
	 */
	public void setValueConstraints(String[] valueConstraints) {
		this.valueConstraints = valueConstraints;
	}

//	public String toString() {
//		return "PropertyDefImpl: " + name;
//	}

  /**
   * Compare property definitions for equality by name, required type and miltiplicity flag.
   * NOTE: UNDEFINED is equals to UNDEFINED only. 
   * NOTE: PD without name is equals to PD without name (TODO: but where to use it?)
   */
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
    
		if (obj instanceof PropertyDefinitionImpl) {
      PropertyDefinitionImpl pdImpl = (PropertyDefinitionImpl) obj;
      boolean sameName = false;
      if (this.getQName() == null) {
        if (pdImpl.getQName() == null)
          sameName = true;
      } else {
        sameName = this.getQName().equals(pdImpl.getQName());
      }
  			
      return sameName && this.getRequiredType() == pdImpl.getRequiredType() && this.isMultiple() == pdImpl.isMultiple() ;
    }
    return false;
	}
}
