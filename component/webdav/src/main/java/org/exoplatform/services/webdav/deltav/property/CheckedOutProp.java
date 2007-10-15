/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.deltav.property;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.property.dav.AbstractDAVProperty;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.response.DavStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: CheckedOutProp.java 12525 2007-02-02 12:26:47Z gavrikvetal $
 */

public class CheckedOutProp extends AbstractDAVProperty {

  public CheckedOutProp() {
    super(DavProperty.CHECKEDOUT);
  }

  @Override
  protected boolean initialize(WebDavResource resource) throws RepositoryException {
    if (!(resource instanceof AbstractNodeResource)) {
      return false;
    }
    
    Node node = ((AbstractNodeResource)resource).getNode();
    
    if (!node.isNodeType(DavConst.NodeTypes.MIX_VERSIONABLE)) {
      return false;
    }       
    
    if (!node.isCheckedOut()) {
      return false;
    }
    
    status = DavStatus.OK;
        
    return true;
  }
  
  @Override
  public void serialize(Document rootDoc, Element parentElement) {
    super.serialize(rootDoc, parentElement);
  }
  
}
