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

    throw new ValueFormatException(
        "conversion to date failed: inconvertible types");
  }

  /**
   * @see Value#getLong
   */
  public long getLong() throws ValueFormatException, IllegalStateException,
      RepositoryException {

    throw new ValueFormatException(
        "conversion to long failed: inconvertible types");
  }

  /**
   * @see Value#getBoolean
   */
  public boolean getBoolean() throws ValueFormatException,
      IllegalStateException, RepositoryException {

    throw new ValueFormatException(
        "conversion to boolean failed: inconvertible types");
  }

  /**
   * @see Value#getDouble
   */
  public double getDouble() throws ValueFormatException, IllegalStateException,
      RepositoryException {

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