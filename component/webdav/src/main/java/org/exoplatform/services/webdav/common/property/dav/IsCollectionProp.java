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
import org.exoplatform.services.webdav.common.resource.WorkspaceResource;
import org.exoplatform.services.webdav.common.response.DavStatus;
import org.exoplatform.services.webdav.deltav.resource.VersionResource;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: IsCollectionProp.java 12525 2007-02-02 12:26:47Z gavrikvetal $
 */

public class IsCollectionProp extends AbstractDAVProperty {
  
  private boolean collection;
  
  public IsCollectionProp() {
    super(DavProperty.ISCOLLECTION);
    isCollection(true);
  }
  
  public void isCollection(boolean isCollection) {
    this.collection = isCollection; 

    if (isCollection) {
      propertyValue = "1";
    } else {
      propertyValue = "0";
    }
  }
  
  public boolean isCollection() {
    return collection;
  }
  
  @Override
  protected boolean initialize(DavResource resource) throws RepositoryException {
    if (resource instanceof WorkspaceResource) {
      status = DavStatus.OK;      
      return true;
    }
    
    if (!(resource instanceof AbstractNodeResource)) {
      return false;
    }
    
    Node node = null;
    if (resource instanceof VersionResource) {
      node = ((VersionResource)resource).getOwnResource().getNode();
    } else {
      node = ((AbstractNodeResource)resource).getNode();
    }
    
    if (node.isNodeType(DavConst.NodeTypes.NT_FILE)) {
      isCollection(false);        
    }
  
    status = DavStatus.OK;    

    return true;
  }
  
}
