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

/**
 * JCR-RMI remote runtime exception. Used by the JCR-RMI client to wrap RMI
 * errors into RuntimeExceptions to avoid breaking the JCR interfaces.
 * <p>
 * Note that if a RemoteException is received by call that declares to throw
 * RepositoryExceptions, then the RemoteException is wrapped into a
 * RemoteRepositoryException.
 */
public class RemoteRuntimeException extends RuntimeException {
  /**
   * 
   */
  private static final long serialVersionUID = 1473439759106501999L;

  /**
   * Creates a RemoteRuntimeException based on the given RemoteException.
   * 
   * @param ex the remote exception
   */
  public RemoteRuntimeException(RemoteException ex) {
    super(ex);
  }
}
