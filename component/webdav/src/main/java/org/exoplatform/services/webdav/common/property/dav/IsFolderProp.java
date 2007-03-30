/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.property.dav;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.DavResource;
import org.exoplatform.services.webdav.common.response.DavStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class IsFolderProp extends AbstractDAVProperty {
  
  private boolean isFolder = true;

  public IsFolderProp() {
    super(DavProperty.ISFOLDER);
  }

  @Override
  protected boolean initialize(DavResource resource) throws RepositoryException {
    status = DavStatus.OK;
    if (!(resource instanceof AbstractNodeResource)) {
      return true;
    }
    
    Node node = ((AbstractNodeResource)resource).getNode();
    
    if (node.isNodeType(DavConst.NodeTypes.NT_FILE)) {
      isFolder = false;
    }      
    return true;
  }
  
  @Override
  public void serialize(Document rootDoc, Element parentElement) {
    propertyValue = (isFolder ? "1" : "0");
    super.serialize(rootDoc, parentElement);
  }
  
  public void setIsFolder(boolean isFolder) {
    this.isFolder = isFolder;
  }
  
  public boolean isFolder() {
    return isFolder;
  }
  
}
