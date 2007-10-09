/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.response;

import java.util.ArrayList;

import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.common.property.WebDavProperty;
import org.exoplatform.services.webdav.common.property.factory.PropertyDefine;
import org.exoplatform.services.webdav.common.request.documents.CommonPropDocument;
import org.exoplatform.services.webdav.common.resource.RepositoryResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;

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
  
  public ArrayList<MultiStatusResponse> getResponses(int depth) throws RepositoryException {
    ArrayList<WebDavResource> resources = new ArrayList<WebDavResource>();

    if (!(rootResource instanceof RepositoryResource)) {
      resources.add(rootResource);
    }
    
    // for IE specifics... :)
    if (rootResource instanceof RepositoryResource && depth == 0) {
      depth = 1;
    }
    
    ArrayList<WebDavResource> childs = getChildResources(rootResource, depth); 
    resources.addAll(childs);
    
    ArrayList<MultiStatusResponse> responses = new ArrayList<MultiStatusResponse>();
    
    for (int i = 0; i < resources.size(); i++) {
      WebDavResource resource = resources.get(i);
      responses.add(getResponse(resource));
    }
    
    return responses;
  }
  
  protected ArrayList<WebDavResource> getChildResources(WebDavResource resource, int depth) throws RepositoryException {
    ArrayList<WebDavResource> childs = new ArrayList<WebDavResource>();
    
    if (depth <= 0) {
      return childs;
    }
    
    if (!resource.isCollection()) {
      return childs;
    }
    
    ArrayList<WebDavResource> curChilds = resource.getChildResources();
    
    for (int i = 0; i < curChilds.size(); i++) {
      WebDavResource curChild = curChilds.get(i);
      childs.add(curChild);
      childs.addAll(getChildResources(curChild, depth - 1));
    }
    
    return childs;
  }  
  
  public MultiStatusResponse getOwnResponse() throws RepositoryException {
    return getResponse(rootResource);
  }
  
  protected MultiStatusResponse getResponse(WebDavResource resource) throws RepositoryException {
    Href href = new Href(resource.getHref());
    MultiStatusResponse response = new MultiStatusResponseImpl(href); 
    initResponse(response, resource);
    
    return response;
  }
  
  protected void initResponse(MultiStatusResponse response, WebDavResource resource) throws RepositoryException {
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
