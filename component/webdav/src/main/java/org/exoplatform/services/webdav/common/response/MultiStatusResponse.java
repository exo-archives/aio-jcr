/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.response;

import org.exoplatform.services.webdav.common.document.XmlSerializable;
import org.exoplatform.services.webdav.common.property.WebDavProperty;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: Response.java 12635 2007-02-07 12:57:47Z gavrikvetal $
 */

public interface MultiStatusResponse extends XmlSerializable {

  Href getHref();
  
  void setStatus(int status);
  
  int getStatus();
  
  void setDescription(String description);
  
  String getDescription();
  
  void addProperty(WebDavProperty property, boolean excludeNotOk);  
  
}
