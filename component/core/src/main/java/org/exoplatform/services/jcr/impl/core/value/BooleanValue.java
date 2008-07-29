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
package org.exoplatform.services.jcr.impl.core.value;

import java.io.IOException;
import java.util.Calendar;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;

/**
 * a boolean value implementation.
 * 
 * @author Gennady Azarenkov
 */
public class BooleanValue extends BaseValue {

  public static final int TYPE = PropertyType.BOOLEAN;
  
  public BooleanValue(boolean bool)  throws IOException {
    super(TYPE, new TransientValueData(bool));
  }

  public BooleanValue(TransientValueData data) throws IOException {
    super(TYPE, data);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.core.value.BaseValue#getDate()
   */
  public Calendar getDate() throws ValueFormatException, IllegalStateException, RepositoryException {
    throw new ValueFormatException("conversion to date failed: inconvertible types");
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.core.value.BaseValue#getLong()
   */
  public long getLong() throws ValueFormatException, IllegalStateException, RepositoryException {
    throw new ValueFormatException("conversion to long failed: inconvertible types");
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.core.value.BaseValue#getDouble()
   */
  public double getDouble() throws ValueFormatException, IllegalStateException, RepositoryException {
    throw new ValueFormatException("conversion to double failed: inconvertible types");
  }
}
