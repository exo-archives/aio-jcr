/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.lock.command;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.common.command.WebDavCommand;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.NodeResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.deltav.resource.DeltaVResource;
import org.exoplatform.services.webdav.lock.FakeLockTable;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PutCommand.java 12004 2007-01-17 12:03:57Z geaz $
 */

public class UnLockCommand extends WebDavCommand {
  
  protected boolean process() throws RepositoryException {
    String resourcePath = davRequest().getSrcWorkspace() + davRequest().getSrcPath();      
    FakeLockTable lockTable = davContext().getLockTable();      
    String presentLockToken = lockTable.getLockToken(resourcePath);

    if (presentLockToken != null) {
      lockTable.unLockResource(resourcePath);
      davResponse().answerNoContent();
      return true;
    }
    
    WebDavResource resource = getResourceFactory().getSrcResource(false);
    
    if (!(resource instanceof NodeResource) &&
        !(resource instanceof DeltaVResource)) {
      throw new AccessDeniedException();
    }

    Node node = ((AbstractNodeResource)resource).getNode();    
    node.unlock();
    node.getSession().save();
    
    davResponse().answerNoContent();    
    return true;
  }
  
}
