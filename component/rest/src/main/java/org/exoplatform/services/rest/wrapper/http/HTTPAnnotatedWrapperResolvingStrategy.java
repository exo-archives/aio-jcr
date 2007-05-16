/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest.wrapper.http;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.rest.URIPattern;
import org.exoplatform.services.rest.wrapper.ResourceDescriptor;
import org.exoplatform.services.rest.wrapper.ResourceWrapper;
import org.exoplatform.services.rest.wrapper.URITemplate;
import org.exoplatform.services.rest.wrapper.WrapperResolvingStrategy;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class HTTPAnnotatedWrapperResolvingStrategy implements WrapperResolvingStrategy{

  public List <ResourceDescriptor> resolve(ResourceWrapper wrapper) {
    List <ResourceDescriptor> resources = new ArrayList <ResourceDescriptor>();
    String middleUri = middleUri(wrapper.getClass());
    for(Method method : wrapper.getClass().getMethods()) {
      HTTPResourceDescriptor mm = methodMapping(middleUri, method, wrapper);
      if(mm != null)
        resources.add(mm);
    }
    return resources;
  }

  private HTTPResourceDescriptor methodMapping(String middleUri, Method method, ResourceWrapper connector) {
    HTTPMethod m = (HTTPMethod)method.getAnnotation(HTTPMethod.class);
    if(m != null) 
      return new HTTPResourceDescriptor(method, m.name(), m.uri(), m.allowedContentType(), connector);  
    else 
      return null;
  }

  private String middleUri(Class clazz) {
    
    Annotation anno = clazz.getAnnotation(URITemplate.class);
    if(anno == null)
      return "";
    else
      return ((URITemplate)anno).uri();
    
  }

  
  public class HTTPResourceDescriptor implements ResourceDescriptor{
    private String methodName;
    private URIPattern uriPattern;
    private String acceptableMediaType;
    private Method servingMethod;
    private ResourceWrapper wrapper;

    public HTTPResourceDescriptor(Method method, String name, String uri, String acceptableContentType,
        ResourceWrapper wrapper) {
      this.methodName = name;
      this.uriPattern = new URIPattern(uri);
      this.acceptableMediaType = acceptableContentType;
      this.servingMethod = method;
      this.wrapper = wrapper;
    }


    public ResourceWrapper getWrapper() {
      return wrapper;
    }

    public Method getServer() {
      return servingMethod;
    }
    
    public URIPattern getURIPattern() {
      return uriPattern;
    }

    
    public String getAcceptableMethod() {
      return methodName;
    }
    
    public String getAcceptableMediaType() {
      return acceptableMediaType;
    }

    
  }
}