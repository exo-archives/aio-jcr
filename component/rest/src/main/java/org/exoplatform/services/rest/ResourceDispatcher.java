/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.rest.container.InvalidResourceDescriptorException;
import org.exoplatform.services.rest.container.ResourceDescriptor;
import org.exoplatform.services.rest.data.MimeTypes;
import org.exoplatform.services.rest.transformer.EntityTransformer;
import org.exoplatform.services.rest.transformer.EntityTransformerFactory;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

/**
 * ResourceDispatcher finds ResourceContainer with can serve
 * the Request and send this Request to ResourceDispatcher
 */
public class ResourceDispatcher implements Connector {

  private static final String STREAM = "java.io.InputStream";
  private static final String URI_PARAM = "org.exoplatform.services.rest.URIParam";
  private static final String HEADER_PARAM = "org.exoplatform.services.rest.HeaderParam";
  private static final String QUERY_PARAM = "org.exoplatform.services.rest.QueryParam";

  
  private List<ResourceDescriptor> resourceDescriptors;
  
  private ThreadLocal <Context> contextHolder = new ThreadLocal <Context>(); 

  /**
   * Constructor gets all binded ResourceContainers from ResourceBinder
   * @param containerContext ExoContainerContext
   * @throws Exception
   */
  public ResourceDispatcher(ExoContainerContext containerContext) throws Exception {
    ExoContainer container = containerContext.getContainer();
    ResourceBinder binder = 
      (ResourceBinder) container.getComponentInstanceOfType(ResourceBinder.class);
    this.resourceDescriptors = binder.getAllDescriptors();
  }

  /**
   * @param request REST request
   * @return REST response from ResourceContainer
   * @throws Exception
   */
  public Response dispatch(Request request) throws Exception {
    String requestedURI = request.getResourceIdentifier().getURI().getPath();
    String methodName = request.getMethodName();
    
    String acceptedMimeTypes = (request.getHeaderParams().getAll().get("accept") != null) ?
        request.getHeaderParams().getAll().get("accept") : MimeTypes.ALL;
        
    MimeTypes requestedMimeTypes = new MimeTypes(acceptedMimeTypes);

    for (ResourceDescriptor resource : resourceDescriptors) {
      MimeTypes producedMimeTypes = new MimeTypes(resource.getProducedMimeTypes());
      // Check is this ResourceContainer have appropriated parameters, 
      // such URIPattern, HTTP method and mimetype
      if (resource.getAcceptableMethod().equalsIgnoreCase(methodName)
          && resource.getURIPattern().matches(requestedURI)
          && (compareMimeTypes(requestedMimeTypes.getMimeTypes(),
              producedMimeTypes.getMimeTypes()))) {
        ResourceIdentifier identifier = request.getResourceIdentifier(); 
        identifier.initParameters(resource.getURIPattern());
        
        // set initialized context to thread local
        contextHolder.set(new Context(identifier));
        
        Annotation[] methodParametersAnnotations = resource.getMethodParameterAnnotations();
        Class<?>[] methodParameters = resource.getMethodParameters();
        Object[] params = new Object[methodParameters.length];
        // building array of parameters
        for (int i = 0; i < methodParametersAnnotations.length; i++) {

          if (methodParametersAnnotations[i] == null) {
            if(STREAM.equals(methodParameters[i].getCanonicalName())) {
              params[i] = request.getEntityStream();
            } else {
              EntityTransformerFactory transformerFactory =
                (EntityTransformerFactory)Class.forName(resource.getConsumedTransformerFactoryName()).newInstance();
              EntityTransformer transformer = transformerFactory.newTransformer();
              params[i] = transformer.readFrom(request.getEntityStream());
            }
          } else {
            Annotation a = methodParametersAnnotations[i];
            if (URI_PARAM.equals(a.annotationType().getCanonicalName())) {
              URIParam u = (URIParam) a;
              params[i] = request.getResourceIdentifier().getParameters().get(u.value());
              contextHolder.get().setURIParam(u.value(), (String)params[i]);
            } else if (HEADER_PARAM.equals(a.annotationType().getCanonicalName())) {
              HeaderParam h = (HeaderParam) a;
              params[i] = request.getHeaderParams().getAll().get(h.value());
              contextHolder.get().setHeaderParam(h.value(), (String)params[i]);
            } else if (QUERY_PARAM.equals(a.annotationType().getCanonicalName())) {
              QueryParam q = (QueryParam) a;
              params[i] = request.getQueryParams().getAll().get(q.value());
              contextHolder.get().setQueryParam(q.value(), (String)params[i]);
            }
          }
        }
        
        Response resp = (Response) resource.getServer().invoke(resource.getResourceContainer(), params);
        
        if(!resp.isTransformerInitialized() && resp.isEntityInitialized()) {
          resp.setTransformer(getTransformer(resource));
        }
        
        return resp; 

      }
    }
    // if no one ResourceContainer found
    throw new NoSuchMethodException("No method found for " + methodName + " " + requestedURI + " "
       + acceptedMimeTypes);
  }
  
  public Context getRuntimeContext() {
    return contextHolder.get();
  }
  
  private EntityTransformer getTransformer(ResourceDescriptor resource)
      throws InvalidResourceDescriptorException {
    
    try {
      EntityTransformerFactory factory =  (EntityTransformerFactory) Class
          .forName(resource.getProducedTransformerFactoryName()).newInstance();
      return factory.newTransformer();
    } catch (Exception e) {
      throw new InvalidResourceDescriptorException (
          "Could not get EntityTransformerFactory from Response" +
          " or annotation to ResourceDescriptor. Exception: " + e);
    }
  }

  /**
   * Compared requested and produced mimetypes
   * @param requested mimetypes from request
   * @param produced mimetypes wich ResourceContainer can produce
   * @return
   */
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
  
  public class Context {
    private HashMap<String, String> uriParams;
    private MultivaluedMetadata headerParams;
    private MultivaluedMetadata queryParams; 
    private ResourceIdentifier identifier;
    
    public Context(ResourceIdentifier identifier) {
      this.identifier = identifier;
      uriParams = new HashMap<String, String>();
      headerParams = new MultivaluedMetadata();
      queryParams = new MultivaluedMetadata();
    }
    
    public String getAbsLocation () {
      return identifier.getBaseURI() + identifier.getURI().toASCIIString();
    }
    
    public String createAbsLocation(String additionalPath) {
      return getAbsLocation() + additionalPath;
    }
    
    private void setURIParam(String key, String value) {
      uriParams.put(key, value);
    }

    private void setHeaderParam(String key, String value) {
      headerParams.add(key, value);
    }
    
    private void setQueryParam(String key, String value) {
      queryParams.add(key, value);
    }
    
  }
}
