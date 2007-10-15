/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.deltav.command;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.command.WebDavCommand;
import org.exoplatform.services.webdav.common.resource.NodeResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.deltav.resource.DeltaVResource;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: VersionControlCommand.java 12304 2007-01-25 10:23:57Z gavrikvetal $
 */

public class VersionControlCommand extends WebDavCommand {

  protected boolean process() throws RepositoryException {
    WebDavResource resource = getResourceFactory().getSrcResource(false);
    
    if (resource instanceof DeltaVResource) {
      davResponse().answerOk();
      return true;
    }
    
    if (!(resource instanceof NodeResource)) {
      davResponse().answerForbidden();
      return false;
    }
    
    Node node = ((NodeResource)resource).getNode();    

    if (davContext().getConfig().isAutoMixLockable()) {
      if (!node.isNodeType(DavConst.NodeTypes.MIX_LOCKABLE)) {
        node.addMixin(DavConst.NodeTypes.MIX_LOCKABLE);
        node.getSession().save();
      }
    }
    
    node.addMixin(DavConst.NodeTypes.MIX_VERSIONABLE);
    node.getSession().save();

    davResponse().answerOk();    
    return true;    
  }
  
}
