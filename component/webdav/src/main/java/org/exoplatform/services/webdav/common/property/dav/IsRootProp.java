/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.property.dav;

import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.resource.WorkspaceResource;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class IsRootProp extends AbstractDAVProperty {
  
  private boolean isRoot = false;

  public IsRootProp() {
    super(DavProperty.ISROOT);
  }
  
  @Override
  protected boolean initialize(WebDavResource resource) throws RepositoryException {
    status = WebDavStatus.OK;
    
    if (resource instanceof WorkspaceResource) {
      isRoot = true;
      return true;
    }
    
    isRoot = false;
    return true;
  } 
 
  @Override
  public Element serialize(Element parentElement) {
    propertyValue = (isRoot ? "1" : "0");
    return super.serialize(parentElement);
  }
  
  public void setIsRoot(boolean isRoot) {
    this.isRoot = isRoot;
  }
  
  public boolean isRoot() {
    return isRoot;
  }
  
}
