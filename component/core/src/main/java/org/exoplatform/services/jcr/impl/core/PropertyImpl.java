/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.core;

import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.VersionException;

import org.exoplatform.services.jcr.core.ExtendedProperty;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitions;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.impl.core.nodetype.PropertyDefinitionImpl;
import org.exoplatform.services.jcr.impl.core.value.BaseValue;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: PropertyImpl.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class PropertyImpl extends ItemImpl implements ExtendedProperty {

  protected int type;
  
  //private PropertyDefinitions definitions;
  private PropertyDefinition propertyDef ;
  
  /**
   * just to simplify operations
   */
  private TransientPropertyData propertyData;

  PropertyImpl(ItemData data, SessionImpl session) throws RepositoryException, ConstraintViolationException {
    super(data, session);
    loadData(data);
  }


  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.core.ItemImpl#loadData(org.exoplatform.services.jcr.datamodel.ItemData)
   */
  void loadData(ItemData data) throws RepositoryException, ConstraintViolationException {

    if (!(data instanceof TransientPropertyData))
      throw new RepositoryException("Load data: TransientPropertyData is expected, but have "+data);

    this.data = data;
    this.propertyData = (TransientPropertyData)data;
    this.type = propertyData.getType();
    
    // [PN] 03.01.07 
    this.location = session.getLocationFactory().createJCRPath(getData().getQPath());
    this.propertyDef = null;
    initDefinitions(this.propertyData.isMultiValued());
  }


  /**
   * @see javax.jcr.Property#getValue
   */
  public Value getValue() throws ValueFormatException, RepositoryException {

    checkValid();
    
    if (isMultiValued())
      throw new ValueFormatException("The property " + getPath() + " is multi-valued (6.2.4)");

    
    if (propertyData.getValues() != null && propertyData.getValues().size() == 0)
      throw new ValueFormatException("The single valued property " + getPath() + " is empty");
    
    return valueFactory.loadValue((TransientValueData)propertyData.getValues().get(0), propertyData.getType());


  }

  /**
   * @see javax.jcr.Property#getValues
   */
  public Value[] getValues() throws ValueFormatException, RepositoryException {
    
    checkValid();
    
    // Check property definition and life-state flag both
    if (!isMultiValued()) {
      throw new ValueFormatException("The property " + getPath() + " is single-valued (6.2.4)");
    }

    // The array returned is a copy of the stored values
    return getValueArray();
  }

  /**
   * @see javax.jcr.Property#getString
   */
  public String getString() throws ValueFormatException, RepositoryException {
    try {
      return getValue().getString();
    } catch (ValueFormatException e) {
      throw new ValueFormatException("PropertyImpl.getString() for " + getPath() + " failed: " + e);
    } catch (IllegalStateException e) {
      throw new ValueFormatException("PropertyImpl.getString() for " + getPath() + " failed: " + e);
    }
  }

  /**
   * @see javax.jcr.Property#getDouble
   */
  public double getDouble() throws ValueFormatException, RepositoryException {
    try {
      return getValue().getDouble();
    } catch (IllegalStateException e) {
      throw new ValueFormatException("PropertyImpl.getDouble() failed: " + e);
    }
  }

  /**
   * @see javax.jcr.Property#getLong
   */
  public long getLong() throws ValueFormatException, RepositoryException {
    try {
      return getValue().getLong();
    } catch (IllegalStateException e) {
      throw new ValueFormatException("PropertyImpl.getLong() failed: " + e);
    }
  }

  /**
   * @see javax.jcr.Property#getStream
   */
  public InputStream getStream() throws ValueFormatException, RepositoryException {
    try {
      return getValue().getStream();
    } catch (IllegalStateException e) {
      throw new ValueFormatException("PropertyImpl.getStream() failed: " + e);
    }
  }

  /**
   * @see javax.jcr.Property#getDate
   */
  public Calendar getDate() throws ValueFormatException, RepositoryException {
    try {
      return getValue().getDate();
    } catch (IllegalStateException e) {
      throw new ValueFormatException("PropertyImpl.getDate() failed: " + e);
    }
  }

  /**
   * @see javax.jcr.Property#getBoolean
   */
  public boolean getBoolean() throws ValueFormatException, RepositoryException {
    try {
      return getValue().getBoolean();
    } catch (IllegalStateException e) {
      throw new ValueFormatException("PropertyImpl.getBoolean() failed: " + e);
    }
  }

  /**
   * @see javax.jcr.Property#getNode
   */
  public Node getNode() throws ValueFormatException, RepositoryException {
    try {
      String identifier = ((BaseValue) getValue()).getReference();
      return session.getNodeByUUID(identifier);
    } catch (IllegalStateException e) {
      throw new ValueFormatException("PropertyImpl.getNode() failed: " + e);
    }
  }

  /**
   * @see javax.jcr.Property#getLength
   */
  public long getLength() throws ValueFormatException, RepositoryException {

    return ((BaseValue) getValue()).getLength();
  }

  /**
   * @see javax.jcr.Property#getLengths
   */
  public long[] getLengths() throws ValueFormatException, RepositoryException {

    Value[] thisValues = getValues();
    
    long[] lengths = new long[thisValues.length];
    for (int i = 0; i < lengths.length; i++) {
      lengths[i] =  ((BaseValue) thisValues[i]).getLength();
    }
    return lengths;
  }

  /**
   * @see javax.jcr.Property#getDefinition
   */
  public PropertyDefinition getDefinition() throws RepositoryException {
    
    checkValid();
    
    if (propertyDef == null) {
      throw new RepositoryException("FATAL: property definition is NULL " + getPath() + " "
          + propertyData.getValues());
    }
    
    return propertyDef;
    
  }
  
  /**
   * @throws RepositoryException
   * @throws ConstraintViolationException
   */
  private void initDefinitions(boolean multiple) throws RepositoryException, ConstraintViolationException {

    NodeType[] nodeTypes = parent().getAllNodeTypes();
    PropertyDefinitions defs = null;
    PropertyDefinitions definitions = null;
    for (int i = 0; i < nodeTypes.length; i++) {
      defs = ((ExtendedNodeType) nodeTypes[i]).getPropertyDefinitions(getInternalName());
      if (defs.getAnyDefinition() != null) { // includes residual set
        definitions = defs;
        if (!((PropertyDefinitionImpl) defs.getAnyDefinition()).isResidualSet())
          break;
      }
    }

    if (definitions == null)
      throw new ConstraintViolationException("Definition for property " + getPath() + " not found.");
    propertyDef = definitions.getDefinition(multiple);
  }



  /**
   * @see javax.jcr.Property#getType
   */
  public int getType() {    
    return type;
  }


  /**
   * @see javax.jcr.Property#setValue
   */
  public void setValue(Value value) throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {

    checkValid();
    
    doUpdateProperty(parent(), getInternalName(),
        value, false, PropertyType.UNDEFINED);
  }

  /**
   * @see javax.jcr.Property#setValue
   */
  public void setValue(Value[] values) throws ValueFormatException, VersionException,
      LockException, ConstraintViolationException, RepositoryException {
    
    checkValid();

    doUpdateProperty(parent(), getInternalName(),
        values, true, PropertyType.UNDEFINED);
  }
  
  /**
   * @return multiValued property of data field (PropertyData) it's a life-state
   *         property field which contains multiple-valued flag for value(s)
   *         data. Can be set in property creation time or from persistent
   *         storage.
   */
  public boolean isMultiValued() {
    return ((PropertyData) data).isMultiValued();
  }

  /**
   * @see javax.jcr.Property#setValue
   */
  public void setValue(String value) throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    setValue(valueFactory.createValue(value));
  }

  /**
   * @see javax.jcr.Property#setValue
   */
  public void setValue(InputStream stream) throws ValueFormatException, VersionException,
      LockException, ConstraintViolationException, RepositoryException {
    setValue(valueFactory.createValue(stream));
  }

  /**
   * @see javax.jcr.Property#setValue
   */
  public void setValue(double number) throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    setValue(valueFactory.createValue(number));
  }

  /**
   * @see javax.jcr.Property#setValue
   */
  public void setValue(long number) throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    setValue(valueFactory.createValue(number));
  }

  /**
   * @see javax.jcr.Property#setValue
   */
  public void setValue(Calendar date) throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    setValue(valueFactory.createValue(date));
  }

  /**
   * @see javax.jcr.Property#setValue
   */
  public void setValue(boolean b) throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    setValue(valueFactory.createValue(b));
  }

  /**
   * @see javax.jcr.Property#setValue
   */
  public void setValue(Node value) throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    setValue(valueFactory.createValue(value));
  }

  /**
   * @see javax.jcr.Property#setValue
   */
  public void setValue(String[] values) throws ValueFormatException, VersionException,
      LockException, ConstraintViolationException, RepositoryException {

    Value[] strValues = null;
    if (values != null) {
      strValues = new Value[values.length];
      for (int i = 0; i < values.length; i++)
        strValues[i] = valueFactory.createValue(values[i]);
    }
    setValue(strValues);
  }

  // ////////////////// Item implementation ////////////////////

  /**
   * @see javax.jcr.Item#accept
   */
  public void accept(ItemVisitor visitor) throws RepositoryException {
    checkValid();
    
    visitor.visit(this);
  }

  /**
   * @see javax.jcr.Item#isNode
   */
  public boolean isNode() {
    return false;
  }

  // ----------------------- ExtendedProperty -----------------------
  
  public void updateValue(InputStream value, int index, long length, long position) 
      throws ValueFormatException, VersionException, LockException, 
      ConstraintViolationException, RepositoryException {
    
    PropertyData pdata = (PropertyData) getData();
    TransientValueData vdata = (TransientValueData) pdata.getValues().get(index);
    
    // TODO
    //vdata.update(value, length, position)
    
    setValue(valueFactory.loadValue(vdata, PropertyType.BINARY));
  }   
  
  //////////////////////////////////

  /**
   * @return
   * @throws RepositoryException
   */
  public Value[] getValueArray() throws RepositoryException {
    
    Value[] values = new Value[propertyData.getValues().size()];
    for(int i=0; i<values.length; i++) {
      values[i] = valueFactory.loadValue((TransientValueData)propertyData.getValues().get(i), propertyData.getType());
    }
    return values; 
  }

  public String dump() {
    String vals = "Property " + getPath() + " values: ";
    try {  
      for (int i = 0; i < getValueArray().length; i++) {
        vals += new String(((BaseValue) getValueArray()[i]).getInternalData().getAsByteArray()) + ";";
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
    return vals;
  }

  // ----------------------- Object -----------------------

  /**
   * @author [PN] 18.04.06
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PropertyImpl) {
      try {
        // by path
        return getLocation().equals(((PropertyImpl) obj).getLocation());
      } catch(Exception e) {
        return false;
      }
    }
    return false;
  }  
  
  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    try {
      return getLocation().getAsString(false).hashCode();
    } catch(Exception e) {
      return super.hashCode();
    }
  }
}