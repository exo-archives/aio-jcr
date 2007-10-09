/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.resource;

import java.util.ArrayList;

import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.resource.resourcedata.CollectionResourceData;
import org.exoplatform.services.webdav.common.resource.resourcedata.ResourceData;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class AbstractWebDavResource implements WebDavResource {
  
  protected WebDavService webDavService;
  
  private String resourceHref;
  
  public AbstractWebDavResource(
      WebDavService webDavService,
      String resourceHref) {
    this.webDavService = webDavService;
    this.resourceHref = resourceHref;
  }
  
  public String getRootHref() {
    return resourceHref;
  }

  public ArrayList<WebDavResource> getChildResources() throws RepositoryException {
    return new ArrayList<WebDavResource>();
  }

  public String getHref() throws RepositoryException {
    return resourceHref;
  }

  public String getName() throws RepositoryException {
    return "jcr";
  }

  public ResourceData getResourceData() throws Exception {    
    return new CollectionResourceData(this);
  }

  public boolean isCollection() throws RepositoryException {
    return true;
  }

}
