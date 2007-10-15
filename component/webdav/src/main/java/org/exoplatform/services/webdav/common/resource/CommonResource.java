/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.resource;

import java.util.ArrayList;

import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.WebDavCommandContext;
import org.exoplatform.services.webdav.common.resource.resourcedata.CollectionResourceData;
import org.exoplatform.services.webdav.common.resource.resourcedata.ResourceData;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: DavCommonResource.java 12635 2007-02-07 12:57:47Z gavrikvetal $
 */

public abstract class CommonResource implements WebDavResource {
  
  protected WebDavCommandContext context;
  
  protected String resourceHref;
  
  public CommonResource(WebDavCommandContext context) {
    this.context = context;
    resourceHref = context.getWebDavRequest().getServerPrefix();
  }

  public String getHref() throws RepositoryException {
    return resourceHref;
  }  
  
  public boolean isCollection() throws RepositoryException {
    throw new RepositoryException();
  }
  
  public String getName() throws RepositoryException {
    throw new RepositoryException();
  }
  
  public ResourceData getResourceData() throws Exception {
    ResourceData collectionResourceData = new CollectionResourceData(this);
    return collectionResourceData;
  }
  
  public ArrayList<String> getAvailableMethods() {
    return context.getAvailableCommands();
  }
  
  public ArrayList<WebDavResource> getChildResources() throws RepositoryException {
    return new ArrayList<WebDavResource>();
  }
  
  public int getType() {
    return 0;
  }  
  
//  public Session getSession() throws RepositoryException {
//    return null;
//  }
  
//  protected void initResponse(CommonPropDocument reqProps, Response response) throws RepositoryException {
//    ArrayList<PropertyDefine> defines = null;
//    boolean isNeedSearch = reqProps.isNeedSearchProperties(); 
//    if (isNeedSearch) {
//      defines = reqProps.searchPropertiesForResource(this);
//    } else {
//      defines = reqProps.getDefines();
//    }
//    
//    for (int i = 0; i < defines.size(); i++) {
//      PropertyDefine define = defines.get(i);      
//      WebDavProperty property = define.getProperty();
//      property.refresh(this, response.getHref());      
//      response.addProperty(property, isNeedSearch);
//    }
//  }
  
  //public abstract Response getResponse(CommonPropDocument reqProps) throws RepositoryException; 
  
  
  
  //public abstract int getChildCount() throws RepositoryException;
  
//  public final ArrayList<Response> getChildsResponses(CommonPropDocument reqProps, int depth) throws RepositoryException {    
//    ArrayList<Response> responses = new ArrayList<Response>();
//    
//    Response curResponse = getResponse(reqProps); 
//    
//    if (curResponse != null) {
//      responses.add(curResponse);
//    }
//    
//    if (depth > 0 && isCollection()) {
//      ArrayList<Resource> childs = getChildsResources();
//      for (int i = 0; i < childs.size(); i++) {      
//        Resource child = childs.get(i);
//        if (child.isCollection()) {
//          responses.addAll(child.getChildsResponses(reqProps, depth - 1));
//        } else {
//          responses.add(child.getResponse(reqProps));
//        }
//        
//      }
//    }
//    
//    return responses;
//  }  
  
}
