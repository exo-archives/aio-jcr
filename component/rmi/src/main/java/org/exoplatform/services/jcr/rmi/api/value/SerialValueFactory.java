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
package org.exoplatform.services.jcr.rmi.api.value;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.ValueFormatException;

import org.exoplatform.services.jcr.core.value.ExtendedValue;

/**
 * The <code>SerialValueFactory</code> class is used in the RMI infrastructure
 * to create serializable <code>Value</code> instances on the client side.
 * <p>
 * This class works in conjunction with the implementations of the
 * <code>javax.jcr.Value</code> interface found in this package.
 * <p>
 * This class may be extended to overwrite any of the
 * <code>createXXXValue</code> methods to create instances of the respective
 * type of {@link  org.exoplatform.services.jcr.rmi.value.StatefullValue}implementation.
 * The methods of the <code>ValueFactory</code> interface are declared final
 * to guard against breaking the rules.
 */
public class SerialValueFactory implements ValueFactory {

  /** The singleton value factory instance */
  private static final SerialValueFactory INSTANCE = new SerialValueFactory();

  /**
   * Returns the <code>ValueFactory</code> instance, which currently is a
   * singleton instance of this class.
   * <p>
   * Future revisions will support some kind of configuration to specify which
   * concrete class should be used.
   */
  public static final SerialValueFactory getInstance() {
    return INSTANCE;
  }

  /**
   * Utility method for decorating an array of values. The returned array will
   * contain serializable value decorators for all the given values. Note that
   * the contents of the original values will only be copied when the decorators
   * are serialized.
   * <p>
   * If the given array is <code>null</code>, then an empty array is
   * returned.
   * 
   * @param values the values to be decorated
   * @return array of decorated values
   */
  public static Value[] makeSerialValueArray(Value[] values) {
    if (values != null) {
      Value[] serials = new Value[values.length];
      for (int i = 0; i < values.length; i++) {
        serials[i] = makeSerialValue(values[i]);
      }
      return serials;
    } else {
      return null;
    }
  }

  /**
   * Utility method for decorating a value. Note that the contents of the
   * original values will only be copied when the decorators are serialized.
   * 
   * @param value the value to be decorated
   * @return the decorated value
   */
  public static Value makeSerialValue(Value value) {
    // if the value is already serializable, just return it
    // - or should we test for SerialValue ??
    if (value instanceof SerialValue || null == value) {
      return value;
    }

    // convert to a general vaule
    return new SerialValue(new StatefulValueAdapter(value));
  }

  /**
   * Default constructor only visible to extensions of this class. See class
   * comments for details.
   */
  protected SerialValueFactory() {
  }

  /** {@inheritDoc} */
  public final Value createValue(String value) {
    // There is no need to pack null value
    // for transfer through RMI
    return value == null ? null : new SerialValue(createStringValue(value));
  }

  /** {@inheritDoc} */
  public final Value createValue(String value, int type) throws ValueFormatException {
    // There is no need to pack null value
    // for transfer through RMI
    if (value.equals("null")) {
      return null;
    }

    StatefulValue intValue;
    switch (type) {
    case PropertyType.BINARY:
      intValue = createBinaryValue(value);
      break;
    case PropertyType.BOOLEAN:
      intValue = createBooleanValue(value);
      break;
    case PropertyType.DATE:
      intValue = createDateValue(value);
      break;
    case PropertyType.DOUBLE:
      intValue = createDoubleValue(value);
      break;
    case PropertyType.LONG:
      intValue = createLongValue(value);
      break;
    case PropertyType.NAME:
      intValue = createNameValue(value);
      break;
    case PropertyType.PATH:
      intValue = createPathValue(value);
      break;
    case PropertyType.REFERENCE:
      intValue = createReferenceValue(value);
      break;
    case PropertyType.STRING:
      intValue = createStringValue(value);
      break;
    default:
      throw new ValueFormatException("Unknown type " + type);
    }

    return new SerialValue(intValue);
  }

  /** {@inheritDoc} */
  public final Value createValue(long value) {
    return new SerialValue(createLongValue(value));
  }

  /** {@inheritDoc} */
  public final Value createValue(double value) {
    return new SerialValue(createDoubleValue(value));
  }

  /** {@inheritDoc} */
  public final Value createValue(boolean value) {
    return new SerialValue(createBooleanValue(value));
  }

  /** {@inheritDoc} */
  public final Value createValue(Calendar value) {
    return value == null ? null : new SerialValue(createDateValue(value));
  }

  /** {@inheritDoc} */
  public final Value createValue(InputStream value) {
    return value == null ? null : new SerialValue(createBinaryValue(value));
  }

  /** {@inheritDoc} */
  public final Value createValue(Node value) throws RepositoryException {
    if (value.isNodeType("mix:referenceable")) {
      return createValue(value.getUUID(), PropertyType.REFERENCE);
    } else {
      return createValue(value.getPath(), PropertyType.REFERENCE);
    }
  }

  // ---------- API to overwrite to use extended classes ----------------------

  /**
   * Creates an instance of the {@link StringValue} class or an extension
   * thereof.
   * <p>
   * This implementation just calls {@link StringValue#StringValue(String)} with
   * the string <code>value</code>.
   * 
   * @param value The string making up the value itself.
   */
  protected StringValue createStringValue(String value) {
    return new StringValue(value);
  }

  /**
   * Creates an instance of the {@link NameValue} class or an extension thereof.
   * <p>
   * This implementation just calls {@link NameValue#NameValue(String)} with the
   * string <code>value</code>.
   * 
   * @param value The string making up the value itself.
   * @throws ValueFormatException if the string is not a synthactically correct
   *           JCR name.
   */
  protected NameValue createNameValue(String value) throws ValueFormatException {
    return new NameValue(value);
  }

  /**
   * Creates an instance of the {@link PathValue} class or an extension thereof.
   * <p>
   * This implementation just calls {@link PathValue#PathValue(String)} with the
   * string <code>value</code>.
   * 
   * @param value The string making up the value itself.
   * @throws ValueFormatException if the string is not a synthactically correct
   *           JCR path.
   */
  protected PathValue createPathValue(String value) throws ValueFormatException {
    return new PathValue(value);
  }

  /**
   * Creates an instance of the {@link ReferenceValue} class or an extension
   * thereof.
   * <p>
   * This implementation just calls
   * {@link ReferenceValue#ReferenceValue(String)} with the string
   * <code>value</code>.
   * 
   * @param value The string making up the value itself.
   * @throws ValueFormatException if the string is not a synthactically correct
   *           JCR reference.
   */
  protected ReferenceValue createReferenceValue(String value) throws ValueFormatException {
    return new ReferenceValue(value);
  }

  /**
   * Creates an instance of the {@link LongValue} class or an extension thereof.
   * <p>
   * This implementation just calls {@link LongValue#LongValue(long)} with the
   * long <code>value</code>.
   * 
   * @param value The long making up the value itself.
   */
  protected LongValue createLongValue(long value) {
    return new LongValue(value);
  }

  /**
   * Creates an instance of the {@link LongValue} class or an extension thereof
   * from the string representation of the <code>long</code> number.
   * <p>
   * This implementation just calls {@link LongValue#LongValue(String)} with the
   * string <code>value</code>.
   * 
   * @param value The string representation of the <code>long</code> number
   *          making up the value itself.
   * @throws ValueFormatException if the string cannot be converted to a long
   *           number.
   */
  protected LongValue createLongValue(String value) throws ValueFormatException {
    return new LongValue(value);
  }

  /**
   * Creates an instance of the {@link DoubleValue} class or an extension
   * thereof.
   * <p>
   * This implementation just calls {@link DoubleValue#DoubleValue(double)} with
   * the double <code>value</code>.
   * 
   * @param value The double making up the value itself.
   */
  protected DoubleValue createDoubleValue(double value) {
    return new DoubleValue(value);
  }

  /**
   * Creates an instance of the {@link DoubleValue} class or an extension
   * thereof from the string representation of the <code>double</code> number.
   * <p>
   * This implementation just calls {@link DoubleValue#DoubleValue(String)} with
   * the string <code>value</code>.
   * 
   * @param value The string representation of the <code>long</code> number
   *          making up the value itself.
   * @throws ValueFormatException if the string cannot be converted to a double
   *           number.
   */
  protected DoubleValue createDoubleValue(String value) throws ValueFormatException {
    return new DoubleValue(value);
  }

  /**
   * Creates an instance of the {@link DateValue} class or an extension thereof.
   * <p>
   * This implementation just calls {@link DateValue#DateValue(Calendar)} with
   * the <code>Calendar</code> <code>value</code>.
   * 
   * @param value The <code>Calendar</code> making up the value itself.
   */
  protected DateValue createDateValue(Calendar value) {
    return new DateValue(value);
  }

  /**
   * Creates an instance of the {@link DateValue} class or an extension thereof
   * from the string representation of <code>Calendar</code> instance
   * formatted as specified in the JCR specification.
   * <p>
   * This implementation just calls {@link DateValue#DateValue(String)} with the
   * string <code>value</code>.
   * 
   * @param value The string representation of the <code>Calendar</code>
   *          instance making up the value itself.
   * @throws ValueFormatException if the string cannot be converted to a
   *           <code>Calendar</code> instance.
   */
  protected DateValue createDateValue(String value) throws ValueFormatException {
    return new DateValue(value);
  }

  /**
   * Creates an instance of the {@link BooleanValue} class or an extension
   * thereof.
   * <p>
   * This implementation just calls {@link BooleanValue#BooleanValue(boolean)}
   * with the boolean <code>value</code>.
   * 
   * @param value The boolean making up the value itself.
   */
  protected BooleanValue createBooleanValue(boolean value) {
    return new BooleanValue(value);
  }

  /**
   * Creates an instance of the {@link BooleanValue} class or an extension
   * thereof from the string representation of the <code>boolean</code>.
   * <p>
   * This implementation just calls {@link BooleanValue#BooleanValue(String)}
   * with the string <code>value</code>.
   * 
   * @param value The string representation of the <code>boolean</code> making
   *          up the value itself.
   * @throws ValueFormatException if the string cannot be converted to a long
   *           number.
   */
  protected BooleanValue createBooleanValue(String value) {
    return new BooleanValue(value);
  }

  /**
   * Creates an instance of the {@link BinaryValue} class or an extension
   * thereof.
   * <p>
   * This implementation just calls {@link BinaryValue#BinaryValue(InputStream)}
   * with the <code>InputStream</code> <code>value</code>.
   * 
   * @param value The <code>InputStream</code> making up the value itself.
   */
  protected BinaryValue createBinaryValue(InputStream value) {
    return new BinaryValue(value);
  }

  /**
   * Creates an instance of the {@link BinaryValue} class or an extension
   * thereof from the string whose UTF-8 representation is used as the binary
   * data.
   * <p>
   * This implementation just calls {@link BinaryValue#BinaryValue(String)} with
   * the string <code>value</code>.
   * 
   * @param value The string whose UTF-8 representation is making up the value
   *          itself.
   * @throws ValueFormatException if the UTF-8 representation of the string
   *           cannot be created.
   */
  protected BinaryValue createBinaryValue(String value) throws ValueFormatException {
    return new BinaryValue(value);
  }

  public PermissionValue createPermissionValue(String string) throws IOException {
    // TODO Auto-generated method stub
    return PermissionValue.parseValue(string);
  }

}
