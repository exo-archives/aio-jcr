/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest.wrapper.http;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.URIPattern;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.ConsumeMimeType;
import org.exoplatform.services.rest.ProduceMimeType;
import org.exoplatform.services.rest.wrapper.ResourceDescriptor;
import org.exoplatform.services.rest.wrapper.ResourceWrapper;
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

  private HTTPResourceDescriptor methodMapping(String middleUri, Method method,
      ResourceWrapper connector) {
    
    HTTPMethod m = method.getAnnotation(HTTPMethod.class);
    URITemplate u = method.getAnnotation(URITemplate.class);
    ConsumeMimeType c = method.getAnnotation(ConsumeMimeType.class);
    ProduceMimeType p = method.getAnnotation(ProduceMimeType.class);
    if(m != null && u != null){
      Class[] params = method.getParameterTypes();
      Annotation[][] a =  method.getParameterAnnotations();
      Annotation[] anno = new Annotation[a.length];
      for(int i = 0; i < a.length; i++) {
        if(a[i].length > 0)
          anno[i] = a[i][0];
      }
      String uri = ("".equals(middleUri)) ? u.value() : (middleUri + u.value());
      String cmt = (p != null) ? p.value() : "*";
      String pmt = (c != null) ? c.value() : "*";
      return new HTTPResourceDescriptor(method, m.value(), uri, anno, params, cmt, pmt, connector);
    }  
    return null;
  }

  private String middleUri(Class clazz) {
    Annotation anno = clazz.getAnnotation(URITemplate.class);
    if(anno == null)
      return "";
    return ((URITemplate)anno).value();
    
  }
  
  public class HTTPResourceDescriptor implements ResourceDescriptor{
    private String methodName;
    private URIPattern uriPattern;
    private String consumeMediaType;
    private String produceMediaType;
    private Method servingMethod;
    private Annotation[] methodParameterAnnotations;
    private Class[] methodParameters;
    private ResourceWrapper wrapper;
    
    public HTTPResourceDescriptor(Method method, String name, String uri,
        Annotation[] methodParameterAnnotations,
        Class[] methodParameters,
        String consumeMediaType, String produceMediaType, ResourceWrapper wrapper) {
      
      this.methodName = name;
      this.methodParameterAnnotations = methodParameterAnnotations;
      this.methodParameters = methodParameters;
      this.uriPattern = new URIPattern(uri);
      this.consumeMediaType = consumeMediaType;
      this.produceMediaType = produceMediaType;
      this.servingMethod = method;
      this.wrapper = wrapper;
    }


    public ResourceWrapper getWrapper() {
      return wrapper;
    }

    public Method getServer() {
      return servingMethod;
    }
    
    public Annotation[] getMethodParameterAnnotations() {
      return methodParameterAnnotations;
    }
    
    public Class[] getMethodParameters() {
      return methodParameters;
    }

    public URIPattern getURIPattern() {
      return uriPattern;
    }
    
    public String getAcceptableMethod() {
      return methodName;
    }
    
    public String getConsumeMediaType() {
      return consumeMediaType;
    }

    public String getProduceMediaType() {
      return produceMediaType;
    }
    
  }

}