/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.command;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: MoveCommand.java 12304 2007-01-25 10:23:57Z gavrikvetal $
 */

public class MoveCommand extends WebDavCommand {
  
  protected boolean process() throws RepositoryException {    
    if (davRequest().getSrcWorkspace().equals(davRequest().getDestWorkspace())) {
      jcrDestSession().move(davRequest().getSrcPath(), davRequest().getDestPath());
      jcrDestSession().save();
    } else {
      jcrDestSession().getWorkspace().copy(
          davRequest().getSrcWorkspace(), 
          davRequest().getSrcPath(),
          davRequest().getDestPath());
      Node srcNode = (Node)jcrSrcSession().getItem(davRequest().getSrcPath()); 
      srcNode.remove();
      jcrSrcSession().save();
    }
    davResponse().answerCreated();
    return true;
  }  
  
}
