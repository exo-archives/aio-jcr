/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.command;

import org.exoplatform.services.webdav.common.command.WebDavCommand;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: MergeCommand.java 12304 2007-01-25 10:23:57Z gavrikvetal $
 */

public class MergeCommand extends WebDavCommand {

  protected boolean process() {    
    davResponse().answerNotImplemented();
    return false;
  }
  
}
