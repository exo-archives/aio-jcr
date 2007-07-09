/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.ftpclient.commands;

import org.exoplatform.frameworks.ftpclient.client.FtpClientSession;

/**
* Created by The eXo Platform SARL        .
* @author Vitaly Guly
* @version $Id: $
*/

public interface FtpCommand {

  int run(FtpClientSession clientSession);
  
  int execute();
  
  int getReply() throws Exception;
  
  String getDescription();
  
}
