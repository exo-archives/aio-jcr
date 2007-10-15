/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.property.dav;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.RepositoryResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.resource.WorkspaceResource;
import org.exoplatform.services.webdav.common.response.DavStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class ParentNameProp extends AbstractDAVProperty {
  
  private String parentName = "";
  
  public ParentNameProp() {
    super(DavProperty.PARENTNAME);
  }
  
  @Override
  protected boolean initialize(WebDavResource resource) throws RepositoryException {
    if (resource instanceof RepositoryResource) {
      return false;
    }
    
    if (resource instanceof WorkspaceResource) {
      parentName = "/";
      status = DavStatus.OK;
      return true;
    }
    
    if (!(resource instanceof AbstractNodeResource)) {
      return false;
    }
    
    Node node = ((AbstractNodeResource)resource).getNode();

    if (node.getDepth() > 1) {
      parentName = node.getParent().getName();
    } else {
      parentName = node.getSession().getWorkspace().getName();
    }

    status = DavStatus.OK;
    return true;
  }
  
  @Override
  public void serialize(Document rootDoc, Element parentElement) {
    super.serialize(rootDoc, parentElement);
    if (status != DavStatus.OK) {
      return;
    }
    propertyElement.setTextContent(parentName);
  }
  
  public void setParentName(String parentName) {
    this.parentName = parentName;
  }
  
  public String getParentName() {
    return parentName;
  }
  
}
