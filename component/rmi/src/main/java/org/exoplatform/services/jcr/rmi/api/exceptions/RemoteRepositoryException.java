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
package org.exoplatform.services.jcr.rmi.api.exceptions;

import java.rmi.RemoteException;

import javax.jcr.RepositoryException;

/**
 * JCR-RMI remote exception. Used by the JCR-RMI client to wrap RMI errors into
 * RepositoryExceptions to avoid breaking the JCR interfaces.
 * <p>
 * Note that if a RemoteException is received by call with no declared
 * exceptions, then the RemoteException is wrapped into a
 * RemoteRuntimeException.
 */
public class RemoteRepositoryException extends RepositoryException {
  /**
   * 
   */
  private static final long serialVersionUID = -5416715967460746517L;

  /**
   * Creates a RemoteRepositoryException based on the given RemoteException.
   * 
   * @param ex the remote exception
   */
  public RemoteRepositoryException(RemoteException ex) {
    super(ex);
  }

}
