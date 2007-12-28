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
package org.exoplatform.services.jcr.rmi.api.remote.core;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

public interface RemoteXASession extends Remote {

  /**
   * @return XAResource
   */
  XAResource getXAResource() throws RemoteException;

  /**
   * Enlists XAResource in TM
   * 
   * @throws XAException
   */
  void enlistResource() throws XAException, RemoteException;

  /**
   * Delists XAResource in TM
   * 
   * @throws XAException
   */
  void delistResource() throws XAException, RemoteException;
}
