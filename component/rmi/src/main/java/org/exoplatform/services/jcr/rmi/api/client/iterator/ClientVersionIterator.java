/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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
