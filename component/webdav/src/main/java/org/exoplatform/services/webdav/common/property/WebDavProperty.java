/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.property;

import org.exoplatform.services.webdav.common.document.XmlSerializable;
import org.exoplatform.services.webdav.common.property.factory.MappingTable;
import org.exoplatform.services.webdav.common.property.factory.PropertyConfigTable;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.response.Href;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: WebDavProperty.java 12525 2007-02-02 12:26:47Z gavrikvetal $
 */

public interface WebDavProperty extends XmlSerializable {
  
  void setConfiguration(MappingTable mappingTable, PropertyConfigTable propertyConfigTable);

  void setStatus(int status);
  
  int getStatus();
  
  void setName(String propertyName);
  
  String getName();
    
  void setValue(String propertyValue);
  
  String getValue();
  
  void setIsMultiValue();
  
  boolean isMultiValue();
  
  void addMultiValue(String propertyValue);

  boolean refresh(WebDavResource resource, Href href);
  
  boolean set(WebDavResource resource);
  
  boolean remove(WebDavResource resource);
  
}
