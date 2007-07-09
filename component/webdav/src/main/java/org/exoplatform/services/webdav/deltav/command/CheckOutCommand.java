/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.deltav.command;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.common.command.WebDavCommand;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.deltav.resource.DeltaVResource;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: CheckOutCommand.java 12304 2007-01-25 10:23:57Z gavrikvetal $
 */

public class CheckOutCommand extends WebDavCommand {
  
  protected boolean process() throws RepositoryException {    
    WebDavResource resource = getResourceFactory().getSrcResource(false);
    
    if (!(resource instanceof DeltaVResource)) {
      davResponse().answerForbidden();      
      return false;
    }
    
    Node node = ((DeltaVResource)resource).getNode();
    
    node.checkout();
    node.getSession().save();
    
    davResponse().answerOk();
    
    return true;
  }
  
}
