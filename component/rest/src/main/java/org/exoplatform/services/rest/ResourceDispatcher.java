/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */
package org.exoplatform.services.rest;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;

//import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
//import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.container.InvalidResourceDescriptorException;
import org.exoplatform.services.rest.container.ResourceDescriptor;
import org.exoplatform.services.rest.data.MimeTypes;
import org.exoplatform.services.rest.transformer.InputEntityTransformer;
import org.exoplatform.services.rest.transformer.OutputEntityTransformer;
import org.exoplatform.services.rest.transformer.EntityTransformerFactory;

/**
 * Created by The eXo Platform SARL.<br/>
 * ResourceDispatcher finds ResourceContainer with can serve the Request and calls method it.
 * @author Gennady Azarenkov
 * @version $Id: $
 */
public class ResourceDispatcher implements Connector {

  private List < ResourceDescriptor > resourceDescriptors;
  private ThreadLocal < Context > contextHolder = new ThreadLocal < Context >();
//  private static Log logger = ExoLogger.getLogger("ResourceDispatcher");

  /**
   * Constructor gets all binded ResourceContainers from ResourceBinder.
   * @param containerContext ExoContainerContext
   * @throws Exception any Exception
   */
  public ResourceDispatcher(ExoContainerContext containerContext) throws Exception {
    ExoContainer container = containerContext.getContainer();
    ResourceBinder binder =
      (ResourceBinder) container.getComponentInstanceOfType(ResourceBinder.class);
    this.resourceDescriptors = binder.getAllDescriptors();
  }

  /**
   * Dispatchs Request to method of ResourceContainer.
   * @param request REST request
   * @return REST response from ResourceContainer
   * @throws Exception any Exception
   */
  public Response dispatch(Request request) throws Exception {
    String requestedURI = request.getResourceIdentifier().getURI();
    String methodName = request.getMethodName();
    String acceptedMimeTypes = (request.getHeaderParams().get("accept") != null) ? request
        .getHeaderParams().get("accept") : MimeTypes.ALL;
    MimeTypes requestedMimeTypes = new MimeTypes(acceptedMimeTypes);
    int j = 0;
    for (ResourceDescriptor resource : resourceDescriptors) {
      MimeTypes producedMimeTypes = new MimeTypes(resource.getProducedMimeTypes());
      // Check is this ResourceContainer have appropriated parameters,
      // such URIPattern, HTTP method and mimetype
      
      if (resource.getAcceptableMethod().equalsIgnoreCase(methodName)
          && resource.getURIPattern().matches(requestedURI)
          && (compareMimeTypes(requestedMimeTypes.getMimeTypes(), producedMimeTypes.getMimeTypes()))) {
        ResourceIdentifier identifier = request.getResourceIdentifier();
        identifier.initParameters(resource.getURIPattern());

        // set initialized context to thread local
        contextHolder.set(new Context(identifier));

        Annotation[] methodParametersAnnotations = resource.getMethodParameterAnnotations();
        Class < ? >[] methodParameters = resource.getMethodParameters();
        Object[] params = new Object[methodParameters.length];
        // building array of parameters
        for (int i = 0; i < methodParametersAnnotations.length; i++) {
          if (methodParametersAnnotations[i] == null) {
            EntityTransformerFactory factory = new EntityTransformerFactory(resource
                .getInputTransformerType());
            InputEntityTransformer transformer = (InputEntityTransformer) factory.newTransformer();
            transformer.setType(methodParameters[i]);
            params[i] = transformer.readFrom(request.getEntityStream());
          } else {
            Annotation a = methodParametersAnnotations[i];
            if (a.annotationType().isAssignableFrom(URIParam.class)) {
              URIParam u = (URIParam) a;
              params[i] = request.getResourceIdentifier().getParameters().get(u.value());
              contextHolder.get().setURIParam(u.value(), (String) params[i]);
            } else if (a.annotationType().isAssignableFrom(HeaderParam.class)) {
              HeaderParam h = (HeaderParam) a;
              params[i] = request.getHeaderParams().get(h.value());
              contextHolder.get().setHeaderParam(h.value(), (String) params[i]);
            } else if (a.annotationType().isAssignableFrom(QueryParam.class)) {
              QueryParam q = (QueryParam) a;
              params[i] = request.getQueryParams().get(q.value());
              contextHolder.get().setQueryParam(q.value(), (String) params[i]);
            }
          }
        }
        Response resp =
          (Response) resource.getServer().invoke(resource.getResourceContainer(), params);
        
        if (!resp.isTransformerInitialized() && resp.isEntityInitialized()) {
          resp.setTransformer(getTransformer(resource));
        }
        if (resp.getEntityMetadata().getLength() == 0) {
          long contentLength = resp.countContentLength();
//          if (contentLength == 0) {
//            logger.warn("Length of content can't be counted."
//                + " May be data represented by InputStream. Content-Length header: 0");
//          }
          resp.getResponseHeaders().putSingle("Content-Length", contentLength + "");
        }
//TODO solution about default Cache-Control        
//        if (resp.getEntityMetadata().getCacheControl() == null) {
//          resp.getResponseHeaders().putSingle("Cache-Control", new CacheControl().getAsString());
//        }
        return resp;
      }
      j++;
    }
    // if no one ResourceContainer found
    throw new NoSuchMethodException("No method found for " + methodName + " " + requestedURI + " "
        + acceptedMimeTypes);
  }
  
  /**
   * Get runtime context.
   * @return the runtimeContext
   */
  public Context getRuntimeContext() {
    return contextHolder.get();
  }

  private OutputEntityTransformer getTransformer(ResourceDescriptor resource)
      throws InvalidResourceDescriptorException {
    try {
      EntityTransformerFactory factory =
        new EntityTransformerFactory(resource.getOutputTransformerType());
      return (OutputEntityTransformer) factory.newTransformer();
    } catch (Exception e) {
      throw new InvalidResourceDescriptorException("Could not get EntityTransformer from Response"
          + " or annotation to ResourceDescriptor. Exception: " + e);
    }
  }

  /**
   * Compared requested and produced mimetypes.
   * @param requested mimetypes from request
   * @param produced mimetypes wich ResourceContainer can produce
   * @return comparetion result
   */
  private boolean compareMimeTypes(String[] requested, String[] produced) {
    for (String r : requested) {
      for (String p : produced) {
        if ("*/*".equals(p)) {
          return true;
        }
        if (p.equals(r)) {
          return true;
        }
        String[] rsubtype = r.split("/");
        String[] psubtype = p.split("/");
        if (psubtype[0].equals(rsubtype[0]) && "*".equals(psubtype[1])) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Keep runtime context and later it can be used for getting some runtime
   * information in ResourceContainer. For example full URL to the requested
   * resource.
   */
  public class Context {
    
    private HashMap < String, String > uriParams;
    private MultivaluedMetadata headerParams;
    private MultivaluedMetadata queryParams;
    private ResourceIdentifier identifier;

    public Context(ResourceIdentifier identifier) {
      this.identifier = identifier;
      uriParams = new HashMap < String, String >();
      headerParams = new MultivaluedMetadata();
      queryParams = new MultivaluedMetadata();
    }

    public String getContextHref() {
      return identifier.getBaseURI();
    }
    
    /**
     * Retrun absolute location to the requested resource.
     * @return the absolte location
     */
    public String getAbsLocation() {
      return identifier.getBaseURI() + identifier.getURI();
    }

    /**
     * Add additation path to absolute loaction and return result.
     * @param additionalPath the additional path
     * @return absolute location
     */
    public String createAbsLocation(String additionalPath) {
      return getAbsLocation() + additionalPath;
    }

    private void setURIParam(String key, String value) {
      uriParams.put(key, value);
    }

    private void setHeaderParam(String key, String value) {
      headerParams.putSingle(key, value);
    }

    private void setQueryParam(String key, String value) {
      queryParams.putSingle(key, value);
    }
  }

}
