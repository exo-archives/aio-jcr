/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.ftp;

import org.exoplatform.services.ftp.commands.FtpCommand;
import org.exoplatform.services.jcr.core.ManageableRepository;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public interface FtpServer {
  
  boolean start();
  
  boolean stop();

  boolean unRegisterClient(FtpClientSession clientSession);
  
  FtpConfig getConfiguration();
  
  FtpDataChannelManager getDataChannelManager();
  
  int getClientsCount();
  
  ManageableRepository getRepository();
  
  FtpCommand getCommand(String commandName);
  
}
