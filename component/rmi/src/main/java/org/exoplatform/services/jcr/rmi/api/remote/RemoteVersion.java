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
package org.exoplatform.services.jcr.rmi.api.remote;

import java.rmi.RemoteException;
import java.util.Calendar;

import javax.jcr.RepositoryException;

/**
 * Remote version of the JCR {@link javax.jcr.version.Version Version}
 * interface. Used by the
 * {@link org.exoplatform.services.jcr.rmi.impl.server.ServerVersion ServerVersion}
 * and
 * {@link org.exoplatform.services.jcr.rmi.api.client.ClientVersion ClientVersion}
 * adapters to provide transparent RMI access to remote versions.
 * <p>
 * The methods in this interface are documented only with a reference to a
 * corresponding Version method. The remote object will simply forward the
 * method call to the underlying Version instance. Argument and return values,
 * as well as possible exceptions, are copied over the network. Complex return
 * values (like Versions) are returned as remote references to the corresponding
 * remote interfaces. Iterator values are transmitted as object arrays. RMI
 * errors are signalled with RemoteExceptions.
 * 
 * @see javax.jcr.version.Version
 * @see org.exoplatform.services.jcr.rmi.api.client.ClientVersion
 * @see org.exoplatform.services.jcr.rmi.impl.server.ServerVersion
 */
public interface RemoteVersion extends RemoteNode {

  /**
   * Remote version of the
   * {@link javax.jcr.version.Version#getContainingHistory() Version.getContainingHistory()}
   * method.
   * 
   * @return a <code>RemoteVersionHistory</code> object.
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  // RemoteVersionHistory getContainingHistory() throws RepositoryException;
  /**
   * Remote version of the
   * {@link javax.jcr.version.Version#getCreated() Version.getCreated()} method.
   * 
   * @return a <code>Calendar</code> object.
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  Calendar getCreated() throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.version.Version#getSuccessors() Version.getSuccessors()}
   * method.
   * 
   * @return a <code>RemoteVersion</code> array.
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  RemoteVersion[] getSuccessors() throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.version.Version#getPredecessors() Version.getPredecessors()}
   * method.
   * 
   * @return a <code>RemoteVersion</code> array.
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  RemoteVersion[] getPredecessors() throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.version.Version#getContainingHistory()}
   * Version.getContainingHistory()} method.
   * 
   * @return a <code>RemoteVersionHistory</code>.
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  RemoteVersionHistory getContainingHistory() throws RepositoryException, RemoteException;

}
