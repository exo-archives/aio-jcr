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
package org.exoplatform.services.ftp;

import org.apache.commons.chain.impl.ContextBase;
import org.exoplatform.services.ftp.client.FtpClientSession;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class FtpContext extends ContextBase  {

  protected FtpClientSession clientSession;
  protected String []params;
  
  public FtpContext(FtpClientSession clientSession, String []params) {
    super();
    this.clientSession = clientSession;
    this.params = params;
  }

  public String []getParams() {
    return params;
  }
  
  public FtpClientSession getFtpClientSession() {
    return clientSession;
  }
  
}
