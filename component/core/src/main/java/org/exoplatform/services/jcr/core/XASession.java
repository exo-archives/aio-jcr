/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.core;

import javax.jcr.Session;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
/**
 * Created by The eXo Platform SARL        .<br/>
 * XASession
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: XASession.java 12843 2007-02-16 09:11:18Z peterit $
 */

public interface XASession extends Session {
  
  /**
   * @return XAResource
   */
  XAResource getXAResource();
  
  /**
   * Enlists XAResource in TM
   * @throws XAException
   */
  void enlistResource() throws XAException;
  
  /**
   * Delists XAResource in TM
   * @throws XAException
   */
  void delistResource() throws XAException;
}
