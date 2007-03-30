/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.property.dav;

import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.resource.DavResource;
import org.exoplatform.services.webdav.common.resource.WorkspaceResource;
import org.exoplatform.services.webdav.common.response.DavStatus;
import org.w3c.dom.Document;
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
  protected boolean initialize(DavResource resource) throws RepositoryException {
    status = DavStatus.OK;
    
    if (resource instanceof WorkspaceResource) {
      isRoot = true;
      return true;
    }
    
    isRoot = false;
    return true;
  } 
 
  @Override
  public void serialize(Document rootDoc, Element parentElement) {
    propertyValue = (isRoot ? "1" : "0");
    super.serialize(rootDoc, parentElement);
  }
  
  public void setIsRoot(boolean isRoot) {
    this.isRoot = isRoot;
  }
  
  public boolean isRoot() {
    return isRoot;
  }
  
}
