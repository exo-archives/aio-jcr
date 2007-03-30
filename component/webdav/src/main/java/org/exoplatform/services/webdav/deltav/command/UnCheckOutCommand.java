/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.deltav.command;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.common.command.WebDavCommand;
import org.exoplatform.services.webdav.common.resource.DavResource;
import org.exoplatform.services.webdav.deltav.resource.DeltaVResource;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: UnCheckOutCommand.java 12304 2007-01-25 10:23:57Z gavrikvetal $
 */

public class UnCheckOutCommand extends WebDavCommand {
  
  private static Log log = ExoLogger.getLogger("jcr.UnCheckOutCommand");
  
  protected boolean process() throws RepositoryException {
    DavResource resource = getResourceFactory().getSrcResource(false);
    
    if (!(resource instanceof DeltaVResource)) {
      davResponse().answerForbidden();
      return false;
    }
    
    Node node = ((DeltaVResource)resource).getNode();    

    try {
      
      Version restoteVersion = node.getBaseVersion(); 
      
      node.restore(restoteVersion, true);
      node.getSession().save();
    } catch (Exception vexc) {
      
      log.info("unhandled exception. " + vexc.getMessage(), vexc);
      
      davResponse().answerForbidden();
      
      return false;
    }
    
    davResponse().answerOk();       
    return true;    
  }
  
}
