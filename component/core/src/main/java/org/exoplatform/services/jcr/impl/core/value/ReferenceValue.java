/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core.value;

import java.io.IOException;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.exoplatform.services.jcr.datamodel.Identifier;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;

/**
 * a <code>REFERENCE</code> value impl
 * (a Identifier of an existing node).
 * 
 * @author Gennady Azarenkov 
 */
public class ReferenceValue extends BaseValue {

  public static final int TYPE = PropertyType.REFERENCE;
  
  private final Identifier identifier;

  public ReferenceValue(Identifier identifier) throws IOException {
    super(TYPE, new TransientValueData(identifier));
    this.identifier = identifier;
  }

  public ReferenceValue(TransientValueData data) throws IOException, RepositoryException {
    super(TYPE, data);
    this.identifier = new Identifier(getInternalString());
  }

  /**
   * @see Value#getDate
   */
  public Calendar getDate() throws ValueFormatException, IllegalStateException,
      RepositoryException {
    //setValueConsumed();

    throw new ValueFormatException(
        "conversion to date failed: inconvertible types");
  }

  /**
   * @see Value#getLong
   */
  public long getLong() throws ValueFormatException, IllegalStateException,
      RepositoryException {
    //setValueConsumed();

    throw new ValueFormatException(
        "conversion to long failed: inconvertible types");
  }

  /**
   * @see Value#getBoolean
   */
  public boolean getBoolean() throws ValueFormatException,
      IllegalStateException, RepositoryException {
    //setValueConsumed();

    throw new ValueFormatException(
        "conversion to boolean failed: inconvertible types");
  }

  /**
   * @see Value#getDouble
   */
  public double getDouble() throws ValueFormatException, IllegalStateException,
      RepositoryException {
    //setValueConsumed();

    throw new ValueFormatException(
        "conversion to double failed: inconvertible types");
  }
  
  public String getReference() throws ValueFormatException,
      IllegalStateException, RepositoryException {
    return getInternalString();
  }

  public Identifier getIdentifier() {
    return identifier;
  }
  
}