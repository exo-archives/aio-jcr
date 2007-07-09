/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.response;

import java.util.ArrayList;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.common.property.WebDavProperty;
import org.exoplatform.services.webdav.common.property.factory.PropertyDefine;
import org.exoplatform.services.webdav.common.request.documents.CommonPropDocument;
import org.exoplatform.services.webdav.common.resource.RepositoryResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.deltav.resource.DeltaVResource;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class ResponseBuilder {
  
  protected WebDavResource rootResource;
  protected CommonPropDocument reqProps;
  
  public ResponseBuilder(WebDavResource rootResource, CommonPropDocument reqProps) {
    this.rootResource = rootResource;
    this.reqProps = reqProps;
  }
  
  public ArrayList<Response> getResponses(int depth) throws RepositoryException {
    ArrayList<WebDavResource> resources = new ArrayList<WebDavResource>();
    
    if (!(rootResource instanceof RepositoryResource)) {
      resources.add(rootResource);
    }
    
    resources.addAll(getChildResources(rootResource, depth - 1));
    
    ArrayList<Response> responses = new ArrayList<Response>();
    
    for (int i = 0; i < resources.size(); i++) {
      WebDavResource resource = resources.get(i);
      responses.add(getResponse(resource));
    }
    
    return responses;
  }
  
  protected ArrayList<WebDavResource> getChildResources(WebDavResource resource, int depth) throws RepositoryException {
    ArrayList<WebDavResource> childs = new ArrayList<WebDavResource>();
    if (depth < 0) {
      return childs;
    }
    
    if (!resource.isCollection()) {
      return childs;
    }
    
    ArrayList<WebDavResource> curChilds = resource.getChildResources();
    
    for (int i = 0; i < curChilds.size(); i++) {
      WebDavResource curChild = curChilds.get(i);
      childs.add(curChild);
    }
    
    return curChilds;
  }  
  
  public Response getOwnResponse() throws RepositoryException {
    return getResponse(rootResource);
  }
  
  protected Response getResponse(WebDavResource resource) throws RepositoryException {
    Href href = new Href(resource.getHref());
    
    Response response = new ResponseImpl(href); 
    initResponse(response, resource);
    
    return response;
  }
  
  protected void initResponse(Response response, WebDavResource resource) throws RepositoryException {
    ArrayList<PropertyDefine> defines = null;
    boolean isNeedSearch = reqProps.isNeedSearchProperties(); 
    if (isNeedSearch) {
      defines = reqProps.searchPropertiesForResource(resource);
    } else {
      defines = reqProps.getDefines();
    }
    
    for (int i = 0; i < defines.size(); i++) {
      PropertyDefine define = defines.get(i);      
      WebDavProperty property = define.getProperty();
      property.refresh(resource, response.getHref());      
      response.addProperty(property, isNeedSearch);
    }
  }  
  
}
