/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.property.dav;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.property.CommonProperty;
import org.exoplatform.services.webdav.common.resource.DavResource;
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
  public boolean set(DavResource resource) {    
    status = DavStatus.INTERNAL_SERVER_ERROR;    
    return false;
  }
  
  @Override
  public boolean remove(DavResource resource) {    
    status = DavStatus.INTERNAL_SERVER_ERROR;
    return false;
  }  

  public void serialize(Document rootDoc, Element parentElement) {
    serialize(rootDoc, parentElement, DavConst.DAV_PREFIX + propertyName);
  }  
  
}
