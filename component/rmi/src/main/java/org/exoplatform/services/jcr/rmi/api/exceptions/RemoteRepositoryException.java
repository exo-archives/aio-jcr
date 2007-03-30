/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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
