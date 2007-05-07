/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.command;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PostCommand.java 12304 2007-01-25 10:23:57Z gavrikvetal $
 */

public class PostCommand extends WebDavCommand {

  private static Log log = ExoLogger.getLogger("jcr.PostCommand");
  
  protected boolean process() {    
    log.info("POST COMMAND CALL..............");
    return false;
  }
  
}
