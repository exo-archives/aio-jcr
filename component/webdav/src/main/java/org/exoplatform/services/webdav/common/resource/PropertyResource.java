/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.resource;

import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.resource.resourcedata.JcrPropertyData;
import org.exoplatform.services.webdav.common.resource.resourcedata.ResourceData;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class PropertyResource extends AbstractWebDavResource {
  
  private static Log log = ExoLogger.getLogger("jcr.PropertyResource");

  private Property property;

  public PropertyResource(
      WebDavService webDavService,
      String rootHref,
      Property property) {
    super(webDavService, rootHref);
    this.property = property;
    
    log.info("Construct.................");
  }
  
  public boolean isCollection() throws RepositoryException {
    return false;
  }
  
  public String getName() throws RepositoryException {
    return property.getName();
  }
  
  public ResourceData getResourceData() throws Exception {
    return new JcrPropertyData(property);
  }  
  
}
