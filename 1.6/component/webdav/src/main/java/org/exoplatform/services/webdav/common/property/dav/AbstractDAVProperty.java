/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.property.dav;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.property.CommonProperty;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.response.DavStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PutCommand.java 12004 2007-01-17 12:03:57Z geaz $
 */

public abstract class AbstractDAVProperty extends CommonProperty {
  
  public AbstractDAVProperty(String propertyName) {
    super(propertyName);
  }
  
  @Override
  public boolean set(WebDavResource resource) {    
    status = DavStatus.INTERNAL_SERVER_ERROR;    
    return false;
  }
  
  @Override
  public boolean remove(WebDavResource resource) {    
    status = DavStatus.INTERNAL_SERVER_ERROR;
    return false;
  }
  
  private Node getResNode(AbstractNodeResource resource) throws RepositoryException {
    Node node = ((AbstractNodeResource)resource).getNode();
    
    if (node.isNodeType(DavConst.NodeTypes.NT_VERSION)) {
      node = node.getNode(DavConst.NodeTypes.JCR_FROZENNODE);
    }
    
    return node;
  }

  public void serialize(Document rootDoc, Element parentElement) {
    serialize(rootDoc, parentElement, DavConst.DAV_PREFIX + propertyName);
  }
  
}
