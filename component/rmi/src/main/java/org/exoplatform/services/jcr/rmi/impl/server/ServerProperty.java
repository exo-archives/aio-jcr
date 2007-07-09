/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.impl.server;

import java.rmi.RemoteException;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.exoplatform.services.jcr.rmi.api.remote.RemoteAdapterFactory;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteProperty;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemotePropertyDefinition;
import org.exoplatform.services.jcr.rmi.api.value.SerialValueFactory;

/**
 * Remote adapter for the JCR {@link javax.jcr.Property Property} interface.
 * This class makes a local property available as an RMI service using the
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteProperty RemoteProperty}
 * interface.
 * 
 * @see javax.jcr.Property
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteProperty
 */
public class ServerProperty extends ServerItem implements RemoteProperty {

  /**
   * 
   */
  private static final long serialVersionUID = -6718443462911336325L;

  /** The adapted local property. */
  private Property          property;

  /**
   * Creates a remote adapter for the given local property.
   * 
   * @param property local property
   * @param factory remote adapter factory
   * @throws RemoteException on RMI errors
   */
  public ServerProperty(Property property, RemoteAdapterFactory factory) throws RemoteException {
    super(property, factory);
    this.property = property;
  }

  /** {@inheritDoc} */
  public Value getValue() throws RepositoryException, RemoteException {
    try {
      return SerialValueFactory.makeSerialValue(property.getValue());
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public Value[] getValues() throws RepositoryException, RemoteException {
    try {
      return SerialValueFactory.makeSerialValueArray(property.getValues());
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void setValue(Value value) throws RepositoryException, RemoteException {
    try {
      property.setValue(value);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void setValue(Value[] values) throws RepositoryException, RemoteException {
    try {
      property.setValue(values);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public long getLength() throws RepositoryException, RemoteException {
    try {
      return property.getLength();
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public long[] getLengths() throws RepositoryException, RemoteException {
    try {
      return property.getLengths();
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public RemotePropertyDefinition getDefinition() throws RepositoryException, RemoteException {
    try {
      return getFactory().getRemotePropertyDefinition(property.getDefinition());
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public int getType() throws RepositoryException, RemoteException {
    try {
      return property.getType();
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

}
