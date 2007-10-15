/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.api.client;

import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Calendar;

import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.services.jcr.core.value.ExtendedValue;
import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRepositoryException;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteProperty;
import org.exoplatform.services.jcr.rmi.api.value.SerialValueFactory;

/**
 * Local adapter for the JCR-RMI
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteProperty RemoteProperty}
 * inteface. This class makes a remote property locally available using the JCR
 * {@link javax.jcr.Property Property} interface.
 * 
 * @see javax.jcr.Property
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteProperty
 */

public class ClientProperty extends ClientItem implements Property {

  /** The adapted remote property. */
  private RemoteProperty remote;

  /**
   * Creates a local adapter for the given remote property.
   * 
   * @param session current session
   * @param remote remote property
   * @param factory local adapter factory
   */
  public ClientProperty(Session session, RemoteProperty remote, LocalAdapterFactory factory) {
    super(session, remote, factory);
    this.remote = remote;
  }

  /**
   * Calls the {@link ItemVisitor#visit(Property) ItemVisitor.visit(Property}
   * method of the given visitor. Does not contact the remote property, but the
   * visitor may invoke other methods that do contact the remote property.
   * {@inheritDoc}
   */
  @Override
  public void accept(ItemVisitor visitor) throws RepositoryException {
    visitor.visit(this);
  }

  /**
   * Returns the boolean value of this property. Implemented as
   * getValue().getBoolean(). {@inheritDoc}
   */
  public boolean getBoolean() throws RepositoryException {
    return getValue().getBoolean();
  }

  /**
   * Returns the date value of this property. Implemented as
   * getValue().getDate(). {@inheritDoc}
   */
  public Calendar getDate() throws RepositoryException {
    return getValue().getDate();
  }

  /**
   * Returns the double value of this property. Implemented as
   * getValue().getDouble(). {@inheritDoc}
   */
  public double getDouble() throws RepositoryException {
    return getValue().getDouble();
  }

  /**
   * Returns the long value of this property. Implemented as
   * getValue().getLong(). {@inheritDoc}
   */
  public long getLong() throws RepositoryException {
    return getValue().getLong();
  }

  /**
   * Returns the binary value of this property. Implemented as
   * getValue().getStream(). {@inheritDoc}
   */
  public InputStream getStream() throws RepositoryException {
    return getValue().getStream();
  }

  /**
   * Returns the string value of this property. Implemented as
   * getValue().getString(). {@inheritDoc}
   */
  public String getString() throws RepositoryException {
    return getValue().getString();
  }

  /** {@inheritDoc} */
  public Value getValue() throws RepositoryException {
    try {
      return remote.getValue();
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public Value[] getValues() throws RepositoryException {
    try {
      return remote.getValues();
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /**
   * Sets the boolean value of this property. Implemented as setValue(new
   * BooleanValue(value)). {@inheritDoc}
   */
  public void setValue(boolean value) throws RepositoryException {
    setValue(getSession().getValueFactory().createValue(value));
  }

  /**
   * Sets the date value of this property. Implemented as setValue(new
   * DateValue(value)). {@inheritDoc}
   */
  public void setValue(Calendar value) throws RepositoryException {
    if (value == null) {
      setValue((Value) null);
    } else {
      setValue(getSession().getValueFactory().createValue(value));
    }
  }

  /**
   * Sets the double value of this property. Implemented as setValue(new
   * DoubleValue(value)). {@inheritDoc}
   */
  public void setValue(double value) throws RepositoryException {
    setValue(getSession().getValueFactory().createValue(value));
  }

  /**
   * Sets the binary value of this property. Implemented as setValue(new
   * BinaryValue(value)). {@inheritDoc}
   */
  public void setValue(InputStream value) throws RepositoryException {
    if (value == null) {
      setValue((Value) null);
    } else {
      setValue(getSession().getValueFactory().createValue(value));
    }
  }

  /**
   * Sets the long value of this property. Implemented as setValue(new
   * LongValue(value)). {@inheritDoc}
   */
  public void setValue(long value) throws RepositoryException {
    setValue(getSession().getValueFactory().createValue(value));
  }

  /**
   * Sets the reference value of this property. Implemented as setValue(new
   * ReferenceValue(value)). {@inheritDoc}
   */
  public void setValue(Node value) throws RepositoryException {
    if (value == null) {
      setValue((Value) null);
    } else {
      if (!value.isNodeType("mix:referenceable")) {
        throw new ValueFormatException();
      }
      setValue(getSession().getValueFactory().createValue(value));
    }
  }

  /**
   * Sets the string value of this property. Implemented as setValue(new
   * StringValue(value)). {@inheritDoc}
   */
  public void setValue(String value) throws RepositoryException {
    if (value == null) {
      setValue((Value) null);
    } else {
      setValue(getSession().getValueFactory().createValue(value));
    }
  }

  /**
   * Sets the string values of this property. Implemented as setValue(new
   * Value[] { new StringValue(strings[0]), ... }). {@inheritDoc}
   */
  public void setValue(String[] strings) throws RepositoryException {
    if (strings == null) {
      setValue((Value[]) null);
    } else {
      Value[] values = new Value[strings.length];
      for (int i = 0; i < strings.length; i++) {
        values[i] = getSession().getValueFactory().createValue(strings[i]);
      }
      setValue(values);
    }
  }

  /** {@inheritDoc} */
  public void setValue(Value value) throws RepositoryException {
    try {
      if (value == null) {
        remote.setValue((Value) null);
      } else {
        remote.setValue(SerialValueFactory.makeSerialValue(value));
      }
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void setValue(Value[] values) throws RepositoryException {
    try {
      if (values == null) {
        remote.setValue((Value[]) null);
      } else {

        remote.setValue(SerialValueFactory.makeSerialValueArray(values));
      }
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /**
   * Returns the reference value of this property. Implemented by converting the
   * reference value to an UUID string and using the current session to look up
   * the referenced node. {@inheritDoc}
   */
  public Node getNode() throws RepositoryException {
    try {
      String uuid = ((ExtendedValue) getValue()).getReference();
      return getSession().getNodeByUUID(uuid);
    } catch (IllegalStateException e) {
      throw new ValueFormatException("PropertyImpl.getNode() failed: " + e);
    }

  }

  /** {@inheritDoc} */
  public long getLength() throws RepositoryException {
    try {
      return remote.getLength();
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public long[] getLengths() throws RepositoryException {
    try {
      return remote.getLengths();
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public PropertyDefinition getDefinition() throws RepositoryException {
    try {
      return getFactory().getPropertyDef(remote.getDefinition());
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public int getType() throws RepositoryException {
    try {
      return remote.getType();
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

}
