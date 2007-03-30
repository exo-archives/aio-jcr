/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.command;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.DavResource;
import org.exoplatform.services.webdav.common.resource.NodeResource;
import org.exoplatform.services.webdav.deltav.resource.DeltaVResource;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: DeleteCommand.java 12304 2007-01-25 10:23:57Z gavrikvetal $
 */

public class DeleteCommand extends WebDavCommand {

  protected boolean process() throws RepositoryException {    
    DavResource resource = getResourceFactory().getSrcResource(false);    
    
    if (!(resource instanceof NodeResource) &&
        !(resource instanceof DeltaVResource)) {      
      throw new AccessDeniedException();
    }
    
    
    Node node = ((AbstractNodeResource)resource).getNode();    
    
    node.remove();    
    node.getSession().save();
    
    davResponse().answerNoContent();    
    return true;
  }
  
}
