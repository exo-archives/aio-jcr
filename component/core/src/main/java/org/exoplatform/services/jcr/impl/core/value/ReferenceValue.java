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

import org.exoplatform.services.jcr.datamodel.Uuid;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;

/**
 * a <code>REFERENCE</code> value impl
 * (a UUID of an existing node).
 * 
 * @author Gennady Azarenkov 
 */
public class ReferenceValue extends BaseValue {

  public static final int TYPE = PropertyType.REFERENCE;
  
  private final Uuid uuid;

  public ReferenceValue(Node target) throws IOException, RepositoryException {
    
    // TODO [PN] Use InternalQName instead jcr:uuid string
    super(TYPE, new TransientValueData(new Uuid(target.getProperty("jcr:uuid").getString())));    
    this.uuid = new Uuid(target.getProperty("jcr:uuid").getString());
  }

  public ReferenceValue(Uuid uuid) throws IOException {
    super(TYPE, new TransientValueData(uuid));
    this.uuid = uuid;
  }

  // [PN] 17.10.06 public is temp. used in VersionHistoryImpl.addVersion() 
  public ReferenceValue(TransientValueData data) throws IOException, RepositoryException {
    super(TYPE, data);
    this.uuid = new Uuid(getInternalString());
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

  public Uuid getUuid() {
    return uuid;
  }
  
}