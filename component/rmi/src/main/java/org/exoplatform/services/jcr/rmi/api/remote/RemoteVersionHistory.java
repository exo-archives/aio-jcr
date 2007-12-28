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

import javax.jcr.RepositoryException;

/**
 * Remote version of the JC
 * {@link javax.jcr.version.VersionHistory VersionHistory} interface. Used by
 * the
 * {@link org.exoplatform.services.jcr.rmi.impl.server.ServerVersionHistory ServerVersionHistory}
 * and
 * {@link org.exoplatform.services.jcr.rmi.api.client.ClientVersionHistory ClientVersionHistory}
 * adapters to provide transparent RMI access to remote version histories.
 * <p>
 * The methods in this interface are documented only with a reference to a
 * corresponding VersionHistory method. The remote object will simply forward
 * the method call to the underlying VersionHistory instance. Argument and
 * return values, as well as possible exceptions, are copied over the network.
 * Complex return values (like Versions) are returned as remote references to
 * the corresponding remote interfaces. Iterator values are transmitted as
 * object arrays. RMI errors are signalled with RemoteExceptions.
 * 
 * @see javax.jcr.version.Version
 * @see org.exoplatform.services.jcr.rmi.api.client.ClientVersionHistory
 * @see org.exoplatform.services.jcr.rmi.impl.server.ServerVersionHistory
 */
public interface RemoteVersionHistory extends RemoteNode {

  /**
   * Remote version of the
   * {@link javax.jcr.version.VersionHistory#getVersionableUUID() VersionHistory.getVersionableUUID()}
   * method.
   * 
   * @return the UUID of the versionable node for which this is the version
   *         history.
   * @throws RepositoryException if an error occurs.
   * @throws RemoteException on RMI errors
   */
  // String getVersionableUUID() throws RepositoryException, RemoteException;
  /**
   * Remote version of the
   * {@link javax.jcr.version.VersionHistory#getRootVersion() VersionHistory.getRootVersion()}
   * method.
   * 
   * @return a <code>Version</code> object.
   * @throws RepositoryException if an error occurs.
   * @throws RemoteException on RMI errors
   */
  RemoteVersion getRootVersion() throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.version.VersionHistory#getAllVersions() VersionHistory.getAllVersions()}
   * method.
   * 
   * @return remote versions
   * @throws RepositoryException if an error occurs.
   * @throws RemoteException on RMI errors
   */
  RemoteIterator getAllVersions() throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.version.VersionHistory#getVersion(String) VersionHistory.getVersion(String)}
   * method.
   * 
   * @param versionName a version name
   * @return a <code>Version</code> object.
   * @throws RepositoryException if an error occurs.
   * @throws RemoteException on RMI errors
   */
  RemoteVersion getVersion(String versionName) throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.version.VersionHistory#getVersionByLabel(String) VersionHistory.getVersionByLabel(String)}
   * method.
   * 
   * @param label a version label
   * @return a <code>Version</code> object.
   * @throws RepositoryException if an error occurs.
   * @throws RemoteException on RMI errors
   */
  RemoteVersion getVersionByLabel(String label) throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.version.VersionHistory#addVersionLabel(String, String, boolean)
   * VersionHistory.addVersionLabel(String, String, boolean)} method.
   * 
   * @param versionName the name of the version to which the label is to be
   *          added.
   * @param label the label to be added.
   * @param moveLabel if <code>true</code>, then if <code>label</code> is
   *          already assigned to a version in this version history, it is moved
   *          to the new version specified; if <code>false</code>, then
   *          attempting to assign an already used label will throw a
   *          <code>VersionException</code>.
   * @throws RepositoryException if another error occurs.
   * @throws RemoteException on RMI errors
   */
  void addVersionLabel(String versionName, String label, boolean moveLabel)
      throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.version.VersionHistory#removeVersionLabel(String) VersionHistory.removeVersionLabel(String)}
   * method.
   * 
   * @param label a version label
   * @throws RepositoryException if another error occurs.
   * @throws RemoteException on RMI errors
   */
  void removeVersionLabel(String label) throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.version.VersionHistory#hasVersionLabel(String) VersionHistory.hasVersionLabel(String)}
   * method.
   * 
   * @param label a version label
   * @return a <code>boolean</code>
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  boolean hasVersionLabel(String label) throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.version.VersionHistory#hasVersionLabel(javax.jcr.version.Version, String) hasVersionLabel(Version, String)}
   * method.
   * 
   * @param versionUUID The UUID of the version whose labels are to be returned.
   * @param label a version label
   * @return a <code>boolean</code>.
   * @throws RepositoryException if another error occurs.
   * @throws RemoteException on RMI errors
   */
  boolean hasVersionLabel(String versionUUID, String label) throws RepositoryException,
      RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.version.VersionHistory#getVersionLabels() VersionHistory.getVersionLabels()}
   * method.
   * 
   * @return a <code>String</code> array containing all the labels of the
   *         version history
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  String[] getVersionLabels() throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.version.VersionHistory#getVersionLabels(javax.jcr.version.Version) VersionHistory.getVersionLabels(Version)}
   * method.
   * 
   * @param versionUUID The UUID of the version whose labels are to be returned.
   * @return a <code>String</code> array containing all the labels of the
   *         given version
   * @throws RepositoryException if another error occurs.
   * @throws RemoteException on RMI errors
   */
  String[] getVersionLabels(String versionUUID) throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.version.VersionHistory#removeVersion(String) VersionHistory.removeVersion(String)}
   * method.
   * 
   * @param versionName the name of a version in this version history.
   * @throws RepositoryException if another error occurs.
   * @throws RemoteException on RMI errors
   */
  void removeVersion(String versionName) throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.version.VersionHistory#getVersionableUUID()}
   * VersionHistory.getVersionableUUID()} method.
   * 
   * @return the uuid of the versionable node
   * @throws RepositoryException if another error occurs.
   * @throws RemoteException on RMI errors
   */
  String getVersionableUUID() throws RepositoryException, RemoteException;

}
