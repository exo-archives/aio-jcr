/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.command;

import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.common.resource.DavResource;
import org.exoplatform.services.webdav.common.resource.FakeResource;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: MkColCommand.java 12304 2007-01-25 10:23:57Z gavrikvetal $
 */

public class MkColCommand extends WebDavCommand {
  
  protected boolean process() throws RepositoryException {
    DavResource resource = getResourceFactory().getSrcResource(true);
    
    if (!(resource instanceof FakeResource)) {
      davResponse().answerForbidden();
      return false;
    }
    
    ((FakeResource)resource).createAsCollection();
    
    davResponse().answerCreated();    
    return true;
  }

}
