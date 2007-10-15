/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest.container;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.URIPattern;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.EntityTransformerClass;
import org.exoplatform.services.rest.ConsumedMimeTypes;
import org.exoplatform.services.rest.ProducedMimeTypes;
import org.exoplatform.services.rest.data.MimeTypes;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class HTTPAnnotatedContainerResolvingStrategy
    implements ResourceContainerResolvingStrategy {

  public List<ResourceDescriptor> resolve(ResourceContainer resourceCont) {
    List<ResourceDescriptor> resources = new ArrayList<ResourceDescriptor>();
    String middleUri = middleUri(resourceCont.getClass());
    String containersTransformerName = getContainersTransformerName(resourceCont.getClass());
    for (Method method : resourceCont.getClass().getMethods()) {
      HTTPResourceDescriptor mm = 
        methodMapping(middleUri, method, containersTransformerName, resourceCont);
      if (mm != null)
        resources.add(mm);
    }
    return resources;
  }

//  private HTTPResourceDescriptor methodMapping(String middleUri, Method method,
//      String ctr, ResourceContainer connector) {

  private HTTPResourceDescriptor methodMapping(String middleUri, Method method,
      String ctr, ResourceContainer connector) {
    HTTPMethod m = method.getAnnotation(HTTPMethod.class);
    URITemplate u = method.getAnnotation(URITemplate.class);
    ConsumedMimeTypes c = method.getAnnotation(ConsumedMimeTypes.class);
    ProducedMimeTypes p = method.getAnnotation(ProducedMimeTypes.class);
    EntityTransformerClass tr = method.getAnnotation(EntityTransformerClass.class);
    if (m != null && (u != null || !"".equals(middleUri))) {
      Class<?>[] params = method.getParameterTypes();
      Annotation[][] a = method.getParameterAnnotations();
      Annotation[] anno = new Annotation[a.length];
      for (int i = 0; i < a.length; i++) {
        if (a[i].length > 0)
          anno[i] = a[i][0];
      }
      String uri = (!"".equals(middleUri)) ? glueUri(middleUri, u) : u.value();
      String consumedMimeTypes = (c != null) ? c.value() : MimeTypes.ALL; 
      String producedMimeTypes = (p != null) ? p.value() : MimeTypes.ALL; 
      String transformerName = null;
      if(tr == null && ctr != null)
        transformerName = ctr;
      else if(tr != null)
        transformerName = tr.value();
        
      return new HTTPResourceDescriptor(method, m.value(), uri, transformerName,
          anno, params, consumedMimeTypes, producedMimeTypes, connector);
    }
    return null;
  }
  
  private String glueUri(String middleUri, URITemplate u) {
    if(u == null)
      return middleUri;
    String uri = u.value();
    if (middleUri.endsWith("/") && uri.startsWith("/"))
      uri = middleUri + uri.replaceFirst("/", "");
    else if (!middleUri.endsWith("/") && !uri.startsWith("/"))
      uri = middleUri + "/" + uri;
    else
      uri = middleUri + uri;
    return uri;
  }

  private String middleUri(Class<?> clazz) {
    Annotation anno = clazz.getAnnotation(URITemplate.class);
    if (anno == null)
      return "";
    return ((URITemplate) anno).value();
  }
/*
  private URITemplate middleUri(Class<?> clazz) {
    return (URITemplate)clazz.getAnnotation(URITemplate.class);
  }
*/
  
  private String getContainersTransformerName(Class<?> clazz) {
    Annotation anno = clazz.getAnnotation(EntityTransformerClass.class);
    if (anno == null)
      return null;
    return ((EntityTransformerClass) anno).value();
  }

  public class HTTPResourceDescriptor implements ResourceDescriptor {

    private String            methodName;
    private URIPattern        uriPattern;
    private String            transformerName;
    private String            consumedMimeTypes;
    private String            producedMimeTypes;
    private Method            servingMethod;
    private Annotation[]      methodParameterAnnotations;
    private Class<?>[]        methodParameters;
    private ResourceContainer resourceCont;

    public HTTPResourceDescriptor(Method method, String name, String uri,
        String transformerName, Annotation[] methodParameterAnnotations,
        Class<?>[] methodParameters, String consumedMimeTypes, String producedMimeTypes,
        ResourceContainer resourceCont) {

      this.servingMethod = method;
      this.methodName = name;
      this.transformerName = transformerName;
      this.methodParameterAnnotations = methodParameterAnnotations;
      this.methodParameters = methodParameters;
      this.uriPattern = new URIPattern(uri);
      this.consumedMimeTypes = consumedMimeTypes;
      this.producedMimeTypes = producedMimeTypes;
      this.resourceCont = resourceCont;
    }

    public ResourceContainer getResourceContainer() {
      return resourceCont;
    }

    public Method getServer() {
      return servingMethod;
    }

    public String getTransformerName() {
      return transformerName;
    }

    public Annotation[] getMethodParameterAnnotations() {
      return methodParameterAnnotations;
    }

    public Class<?>[] getMethodParameters() {
      return methodParameters;
    }

    public URIPattern getURIPattern() {
      return uriPattern;
    }

    public String getAcceptableMethod() {
      return methodName;
    }

    public String getConsumedMimeTypes() {
      return consumedMimeTypes;
    }

    public String getProducedMimeTypes() {
      return producedMimeTypes;
    }

  }

}