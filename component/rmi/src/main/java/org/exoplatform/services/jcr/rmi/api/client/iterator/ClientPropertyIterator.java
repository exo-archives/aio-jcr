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
