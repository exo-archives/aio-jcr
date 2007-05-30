/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import java.util.List;
import java.lang.annotation.Annotation;

import org.exoplatform.services.rest.container.ResourceDescriptor;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.ExoContainer;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class ResourceDispatcher implements Connector {
  
  
  private List<ResourceDescriptor> resourceDescriptors;
  
  public ResourceDispatcher(ExoContainerContext containerContext) throws Exception {
    ExoContainer container = containerContext.getContainer();
    ResourceBinder binder = (ResourceBinder)container.getComponentInstanceOfType(ResourceBinder.class);
    this.resourceDescriptors = binder.getAllDescriptors();
  }

  public Response dispatch(Request request) throws Exception {
    String requestedURI = request.getResourceIdentifier().getURI().getPath();
    String methodName = request.getMethodName();
    String mediaType = request.getAcceptedMediaType();

    for(ResourceDescriptor resource : resourceDescriptors) {
      if(resource.getAcceptableMethod().equalsIgnoreCase(methodName)
         && resource.getURIPattern().matches(requestedURI)
         && resource.getProduceMediaType().equals(mediaType)) {
          
        request.getResourceIdentifier().initParameters(resource.getURIPattern());
        Annotation[] methodParametersAnnotations = resource.getMethodParameterAnnotations();
        Class[] methodParameters = resource.getMethodParameters();
        Object[] objs = new Object[methodParameters.length];
        for(int i = 0; i < methodParameters.length; i++) {
          if("org.exoplatform.services.rest.Representation".equals(
              methodParameters[i].getCanonicalName()))

            objs[i] = request.getEntity();
            
            if("java.lang.String".equals(methodParameters[i].getCanonicalName())) {
              Annotation a = methodParametersAnnotations[i];

              if("org.exoplatform.services.rest.URIParam".equals(
                  a.annotationType().getCanonicalName())) { 
                
                URIParam u = (URIParam)a;
                objs[i] = request.getResourceIdentifier().getParameters().get(u.value());
              } else if("org.exoplatform.services.rest.HeaderParam".equals(
                  a.annotationType().getCanonicalName())) {
                
                HeaderParam h = (HeaderParam)a;
                objs[i] = request.getHttpHeaderParameters().get(h.value());
              } else if("org.exoplatform.services.rest.QueryParam".equals(
                  a.annotationType().getCanonicalName())) {
              
                QueryParam q = (QueryParam)a;
                objs[i] = request.getHttpQueryParameters().get(q.value());
              }
            }
          }
          return (Response)resource.getServer().invoke(resource.getResourceContainer(), objs);
       }  
    }
    throw new NoSuchMethodException("No method found for " + methodName + " "
        + requestedURI + " " + mediaType);
  }

}
