/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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
