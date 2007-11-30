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

package org.exoplatform.services.jcr.core;

import javax.jcr.Session;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
/**
 * Created by The eXo Platform SAS.<br/>
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
