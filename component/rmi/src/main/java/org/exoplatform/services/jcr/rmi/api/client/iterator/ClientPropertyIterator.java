/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.api.client.iterator;

import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;

import org.exoplatform.services.jcr.rmi.api.client.LocalAdapterFactory;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteIterator;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteProperty;

/**
 * A ClientIterator for iterating remote properties.
 */
public class ClientPropertyIterator extends ClientIterator implements PropertyIterator {

  /** The current session. */
  private final Session session;

  /**
   * Creates a ClientPropertyIterator instance.
   * 
   * @param iterator remote iterator
   * @param session current session
   * @param factory local adapter factory
   */
  public ClientPropertyIterator(RemoteIterator iterator, Session session,
      LocalAdapterFactory factory) {
    super(iterator, factory);
    this.session = session;
  }

  /**
   * Creates and returns a local adapter for the given remote property.
   * 
   * @param remote remote referecne
   * @return local adapter
   * @see ClientIterator#getObject(Object)
   */
  protected Object getObject(Object remote) {
    return getFactory().getProperty(session, (RemoteProperty) remote);
  }

  /**
   * Returns the next property in this iteration.
   * 
   * @return next property
   * @see PropertyIterator#nextProperty()
   */
  public Property nextProperty() {
    return (Property) next();
  }

}
