/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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
