/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.core.value;

import java.io.IOException;
import java.util.Calendar;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.exoplatform.services.jcr.datamodel.IllegalNameException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.core.JCRName;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;

/**
 * a <code>NAME</code> value impl (a
 * string that is namespace-qualified).
 *
 * @author Gennady Azarenkov 
 */
public class NameValue extends BaseValue {

  public static final int TYPE = PropertyType.NAME;
  
  private final LocationFactory locationFactory;
  
  public NameValue(InternalQName name, LocationFactory locationFactory) throws IOException {
    super(TYPE, new TransientValueData(name));
    this.locationFactory = locationFactory;
  }

  public NameValue(TransientValueData data, LocationFactory locationFactory)
      throws IOException {
    super(TYPE, data);
    this.locationFactory = locationFactory;
  }
  
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.core.value.BaseValue#getString()
   */
  public String getString() throws ValueFormatException, IllegalStateException,
      RepositoryException {
    JCRName name = locationFactory.createJCRName(getQName());
    return name.getAsString();
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.core.value.BaseValue#getDate()
   */
  public Calendar getDate() throws ValueFormatException, IllegalStateException,
      RepositoryException {
    throw new ValueFormatException(
        "conversion to date failed: inconvertible types");
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.core.value.BaseValue#getLong()
   */
  public long getLong() throws ValueFormatException, IllegalStateException,
      RepositoryException {
    throw new ValueFormatException(
        "conversion to long failed: inconvertible types");
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.core.value.BaseValue#getBoolean()
   */
  public boolean getBoolean() throws ValueFormatException,
      IllegalStateException, RepositoryException {
    throw new ValueFormatException(
        "conversion to boolean failed: inconvertible types");
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.core.value.BaseValue#getDouble()
   */
  public double getDouble() throws ValueFormatException, IllegalStateException,
      RepositoryException {
    throw new ValueFormatException(
        "conversion to double failed: inconvertible types");
  }
  
  /**
   * @return qname
   * @throws ValueFormatException
   * @throws IllegalStateException
   * @throws RepositoryException
   */
  public InternalQName getQName() throws ValueFormatException, IllegalStateException, RepositoryException {
    try {
		return InternalQName.parse(getInternalString());
	} catch (IllegalNameException e) {
		throw new RepositoryException(e);
	}
  }

}