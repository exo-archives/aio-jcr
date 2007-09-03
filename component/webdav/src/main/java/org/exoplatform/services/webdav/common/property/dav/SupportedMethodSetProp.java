/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.property.dav;

import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: SupportedMethodSetProp.java 12525 2007-02-02 12:26:47Z gavrikvetal $
 */

public class SupportedMethodSetProp extends AbstractDAVProperty {
  
  private String []availableMethods = {"PROPFIND", "OPTIONS"};

  public SupportedMethodSetProp() {
    super(DavProperty.SUPPORTEDMETHODSET);
  }
  
  @Override
  protected boolean initialize(WebDavResource resource) throws RepositoryException {
    status = WebDavStatus.OK;    
    return true;
  }
  
  @Override
  public Element serialize(Element parentElement) {
    super.serialize(parentElement);
    if (status != WebDavStatus.OK) {
      return propertyElement;
    }
    
    for (int i = 0; i < availableMethods.length; i++) {
      String curMethodName = availableMethods[i];
      
      Element methodElement = parentElement.getOwnerDocument().createElement(DavConst.DAV_PREFIX + DavProperty.SUPPORTEDMETHOD);
      propertyElement.appendChild(methodElement);
      methodElement.setAttribute(DavProperty.NAME, curMethodName);
    }
    
    return propertyElement;
  }
  
}
