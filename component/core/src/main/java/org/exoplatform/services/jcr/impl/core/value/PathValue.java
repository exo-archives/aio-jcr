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

import org.exoplatform.services.jcr.datamodel.InternalQPath;
import org.exoplatform.services.jcr.impl.core.JCRPath;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;

/**
 *  a <code>PATH</code> value impl (an
 * absolute or relative workspace path).
 * 
 * @author Gennady Azarenkov 
 */
public class PathValue extends BaseValue {

  public static final int TYPE = PropertyType.PATH;
  
  private final LocationFactory locationFactory;
  
  public PathValue(InternalQPath path, LocationFactory locationFactory) throws IOException {
    super(TYPE, new TransientValueData(path));
    this.locationFactory = locationFactory;
  }
  
  public PathValue(TransientValueData data, LocationFactory locationFactory)
      throws IOException, RepositoryException {
    super(TYPE, data);
    this.locationFactory = locationFactory;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.core.value.BaseValue#getString()
   */
  public String getString() throws ValueFormatException, IllegalStateException,
      RepositoryException {
    JCRPath path = locationFactory.createJCRPath(getQPath());
    return path.getAsString(false);
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
   * @return qpath
   * @throws ValueFormatException
   * @throws IllegalStateException
   * @throws RepositoryException
   */
  public InternalQPath getQPath() throws ValueFormatException, IllegalStateException,
    RepositoryException {
    return InternalQPath.parse(getInternalString());
  }
}