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
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.response.DavStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class HasChildrenProp extends AbstractDAVProperty {
  
  private boolean hasChildrens = true;

  public HasChildrenProp() {
    super(DavProperty.HASCHILDREN);
  }
  
  @Override
  protected boolean initialize(WebDavResource resource) throws RepositoryException {
    status = DavStatus.OK;
    
    if (!(resource instanceof AbstractNodeResource)) {
      return true;
    }
    
    Node node = ((AbstractNodeResource)resource).getNode();
    
    if (node.isNodeType(DavConst.NodeTypes.NT_FILE)) {
      hasChildrens = false;
      return false;
    }

    if (!node.hasNodes()) {
      hasChildrens = false;
    }
    
    return true;
  }
  
  @Override
  public void serialize(Document rootDoc, Element parentElement) {
    propertyValue = (hasChildrens ? "1" : "0");
    super.serialize(rootDoc, parentElement);
  }  
  
}
