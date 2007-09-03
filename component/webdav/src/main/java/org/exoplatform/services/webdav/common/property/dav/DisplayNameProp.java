/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.property.dav;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.resource.WorkspaceResource;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: DisplayNameProp.java 12525 2007-02-02 12:26:47Z gavrikvetal $
 */

public class DisplayNameProp extends AbstractDAVProperty {
  
  protected String displayName = "";
  
  public DisplayNameProp() {
    super(DavProperty.DISPLAYNAME);
  }
  
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }
  
  public String getDisplayName() {
    return displayName;
  }

  @Override
  protected boolean initialize(WebDavResource resource) throws RepositoryException {
    if (resource instanceof WorkspaceResource) {      
      displayName = ((WorkspaceResource)resource).getWorkspaceName();
      status = WebDavStatus.OK;
      return true;
    }
    
    if (!(resource instanceof AbstractNodeResource)) {
      return false;
    }
    
    Node node = ((AbstractNodeResource)resource).getNode();
    displayName = node.getName();
    
    status = WebDavStatus.OK;
    
    return true;      
  }
  
  @Override
  public Element serialize(Element parentElement) {
    super.serialize(parentElement);
    if (status == WebDavStatus.OK) {
      propertyElement.setTextContent(displayName);
    }
    return propertyElement;
  }
  
}
