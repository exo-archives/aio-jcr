/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.ftp;

import org.apache.commons.chain.impl.ContextBase;
import org.exoplatform.services.ftp.client.FtpClientSession;

/**
 * Created by The eXo Platform SARL
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
