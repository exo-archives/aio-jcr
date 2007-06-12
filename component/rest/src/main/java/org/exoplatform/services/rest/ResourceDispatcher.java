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
import org.exoplatform.services.rest.transformer.EntityTransformer;
import org.exoplatform.services.rest.data.MimeTypes;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class ResourceDispatcher implements Connector {

  private List<ResourceDescriptor> resourceDescriptors;

  public ResourceDispatcher(ExoContainerContext containerContext) throws Exception {
    ExoContainer container = containerContext.getContainer();
    ResourceBinder binder = 
      (ResourceBinder) container.getComponentInstanceOfType(ResourceBinder.class);
    this.resourceDescriptors = binder.getAllDescriptors();
  }

  public Response<?> dispatch(Request request) throws Exception {
    String requestedURI = request.getResourceIdentifier().getURI().getPath();
    String methodName = request.getMethodName();
    
    String acceptedMimeTypes = (request.getHeaderParams().getFirst("accept") != null) ?
        request.getHeaderParams().getFirst("accept") : MimeTypes.ALL;
    MimeTypes requestedMimeTypes = new MimeTypes(acceptedMimeTypes);

    for (ResourceDescriptor resource : resourceDescriptors) {
      MimeTypes producedMimeTypes = new MimeTypes(resource.getProducedMimeTypes());
      if (resource.getAcceptableMethod().equalsIgnoreCase(methodName)
          && resource.getURIPattern().matches(requestedURI)
          && (compareMimeTypes(requestedMimeTypes.getMimeTypes(),
              producedMimeTypes.getMimeTypes()))) {
        
        request.getResourceIdentifier().initParameters(resource.getURIPattern());
        Annotation[] methodParametersAnnotations = resource.getMethodParameterAnnotations();
        Class<?>[] methodParameters = resource.getMethodParameters();
        Object[] objs = new Object[methodParameters.length];

        for (int i = 0; i < methodParametersAnnotations.length; i++) {

          if (methodParametersAnnotations[i] == null) {
            if("java.io.InputStream".equals(methodParameters[i]))
              objs[i] = methodParameters[i];
            else {
              EntityTransformer<?> transformer =
                (EntityTransformer<?>)Class.forName(resource.getTransformerName()).newInstance();
              objs[i] = transformer.readFrom(request.getEntityStream());
            }
          } else {
            Annotation a = methodParametersAnnotations[i];

            if ("org.exoplatform.services.rest.URIParam".equals(a.annotationType()
                .getCanonicalName())) {

              URIParam u = (URIParam) a;
              objs[i] = request.getResourceIdentifier().getParameters().get(u.value());
            } else if ("org.exoplatform.services.rest.HeaderParam".equals(a.annotationType()
                .getCanonicalName())) {

              HeaderParam h = (HeaderParam) a;
              objs[i] = request.getHeaderParams().get(h.value());
            } else if ("org.exoplatform.services.rest.QueryParam".equals(a.annotationType()
                .getCanonicalName())) {

              QueryParam q = (QueryParam) a;
              objs[i] = request.getQueryParams().get(q.value());
            }
          }
        }
        return (Response<?>) resource.getServer().invoke(resource.getResourceContainer(), objs);
      }
    }
    throw new NoSuchMethodException("No method found for " + methodName + " " + requestedURI + " "
       + request.getHeaderParams().getFirst("accept"));
  }

  private boolean compareMimeTypes(String[] requested, String[] produced) {
    for(String r : requested) {
      for(String p : produced) {
        if("*/*".equals(p))
          return true;
        if(p.equals(r))
          return true;
        String[] rsubtype = r.split("/");
        String[] psubtype = p.split("/");
        if(psubtype[0].equals(rsubtype[0]) && "*".equals(psubtype[1])) {
          return true;
        }
      }
    }
    return false;
  }
  
}
