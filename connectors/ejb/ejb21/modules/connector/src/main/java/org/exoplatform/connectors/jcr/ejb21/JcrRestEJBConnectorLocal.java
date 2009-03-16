/**
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

package org.exoplatform.connectors.jcr.ejb21;

import java.io.IOException;

import javax.ejb.EJBLocalObject;

import org.exoplatform.services.rest.ext.transport.SerialRequest;
import org.exoplatform.services.rest.ext.transport.SerialResponse;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public interface JcrRestEJBConnectorLocal extends EJBLocalObject {

  /**
   * @param request wrapper for REST request that gives possibility transfer
   *          request via RMI
   * @return wrapper around REST response that gives possibility transfer
   *         request via RMI
   * @throws IOException if any i/o errors occurs
   */
  SerialResponse service(SerialRequest request) throws IOException;

}
