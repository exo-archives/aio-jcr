/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.ftp.commands;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.exoplatform.services.ftp.FtpClientSession;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public interface FtpCommand extends Command {
  
  boolean execute(Context context) throws Exception;
  
  void run(String []params) throws Exception;
  
  FtpClientSession clientSession();
  
}
