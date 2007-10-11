/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.property.dav;

import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class ChildCountProp extends AbstractDAVProperty {
  
  private int childCount = 0; 
  
  public ChildCountProp() {
    super(DavProperty.CHILDCOUNT);
  }
  
  @Override
  protected boolean initialize(WebDavResource resource) throws RepositoryException {    
    childCount = resource.getChildResources().size();
    status = WebDavStatus.OK;
    return true;
  }

  @Override
  public Element serialize(Element parentElement) {
    propertyValue = "" + childCount;
    super.serialize(parentElement);
    return propertyElement;
  }
  
  public void setChildCount(int childCount) {
    this.childCount = childCount;
  }
  
  public int childCount() {
    return childCount;
  }
  
}
