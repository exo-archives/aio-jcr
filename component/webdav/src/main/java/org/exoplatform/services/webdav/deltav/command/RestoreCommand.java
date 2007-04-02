/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.command;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.common.command.WebDavCommand;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.DavResource;
import org.exoplatform.services.webdav.common.response.DavStatus;
import org.exoplatform.services.webdav.deltav.resource.DeltaVResource;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: RestoreCommand.java 12304 2007-01-25 10:23:57Z gavrikvetal $
 */

public class RestoreCommand extends WebDavCommand {
  
  private static Log log = ExoLogger.getLogger("jcr.RestoreCommand");

  protected boolean process() throws RepositoryException {
    DavResource resource = getResourceFactory().getSrcResource(false);
    
    if (!(resource instanceof DeltaVResource)) {
      davResponse().setStatus(DavStatus.FORBIDDEN);
      return false;
    }
    
    try {
      Node node = ((AbstractNodeResource)resource).getNode();
      
      Version baseVersion = node.getBaseVersion();
      
      Version []predesessors = baseVersion.getPredecessors();
      
      if (predesessors.length < 1) {        
        davResponse().setStatus(DavStatus.CONFLICT);
        return true;
      }
      
      Version restoreToVersion = predesessors[0];
      
      node.restore(restoreToVersion, true);
      node.getSession().save();
      
    } catch (RepositoryException rexc) {
      log.info("Unhandled exception. " + rexc.getMessage(), rexc);
      throw rexc;
    }

    davResponse().setStatus(DavStatus.OK);
    return true;
  }
  
}
