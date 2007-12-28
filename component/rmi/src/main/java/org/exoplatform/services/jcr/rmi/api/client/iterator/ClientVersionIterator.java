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

import javax.jcr.Session;
import javax.jcr.version.Version;
import javax.jcr.version.VersionIterator;

import org.exoplatform.services.jcr.rmi.api.client.LocalAdapterFactory;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteIterator;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteVersion;

/**
 * A ClientIterator for iterating remote versions.
 */
public class ClientVersionIterator extends ClientIterator implements VersionIterator {

  /** The current session. */
  private final Session session;

  /**
   * Creates a ClientVersionIterator instance.
   * 
   * @param iterator remote iterator
   * @param session current session
   * @param factory local adapter factory
   */
  public ClientVersionIterator(RemoteIterator iterator, Session session, LocalAdapterFactory factory) {
    super(iterator, factory);
    this.session = session;
  }

  /**
   * Creates and returns a local adapter for the given remote version.
   * 
   * @param remote remote referecne
   * @return local adapter
   * @see ClientIterator#getObject(Object)
   */
  protected Object getObject(Object remote) {
    return getFactory().getVersion(session, (RemoteVersion) remote);
  }

  /**
   * Returns the next version in this iteration.
   * 
   * @return next version
   * @see VersionIterator#nextVersion()
   */
  public Version nextVersion() {
    return (Version) next();
  }

}
