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
import org.exoplatform.services.rest.InputTransformer;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.ConsumedMimeTypes;
import org.exoplatform.services.rest.ProducedMimeTypes;
import org.exoplatform.services.rest.data.MimeTypes;
import org.exoplatform.services.rest.transformer.InputEntityTransformer;
import org.exoplatform.services.rest.transformer.OutputEntityTransformer;

/**
 * Created by The eXo Platform SARL .
 * @author Gennady Azarenkov
 * @version $Id: $
 */
public class HTTPAnnotatedContainerResolvingStrategy implements ResourceContainerResolvingStrategy {

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.rest.container.ResourceContainerResolvingStrategy#resolve
   * (org.exoplatform.services.rest.container.ResourceContainer)
   */
  public List < ResourceDescriptor > resolve(ResourceContainer resourceContainer) {
    List < ResourceDescriptor > resources = new ArrayList < ResourceDescriptor >();
    for (Method method : resourceContainer.getClass().getMethods()) {
      HTTPResourceDescriptor descr = methodMapping(method, resourceContainer);
      if (descr != null) {
        resources.add(descr);
      }
    }
    return resources;
  }

  private HTTPResourceDescriptor methodMapping(Method method, ResourceContainer resourceCont) {
    String middleUri = middleUri(resourceCont.getClass());
    HTTPMethod httpMethodAnnotation = method.getAnnotation(HTTPMethod.class);
    URITemplate uriTemplateAnnotation = method.getAnnotation(URITemplate.class);
    if (httpMethodAnnotation != null
        && (uriTemplateAnnotation != null || !"".equals(middleUri))) {

      String uri = (!"".equals(middleUri)) ? glueUri(middleUri, uriTemplateAnnotation)
          : uriTemplateAnnotation.value();
      String httpMethodName = httpMethodAnnotation.value();
      return new HTTPResourceDescriptor(method, httpMethodName, uri, resourceCont);
    }
    return null;
  }

  /*
   * Glue two Strings in one. Is used for creation uri string. 
   */
  private String glueUri(String middleUri, URITemplate u) {
    if (u == null) {
      return middleUri;
    }
    String uri = u.value();
    if (middleUri.endsWith("/") && uri.startsWith("/")) {
      uri = middleUri + uri.replaceFirst("/", "");
    } else if (!middleUri.endsWith("/") && !uri.startsWith("/")) {
      uri = middleUri + "/" + uri;
    } else {
      uri = middleUri + uri;
    }
    return uri;
  }

  /*
   * Get uri string from URITemplate annotation.
   */
  private String middleUri(Class < ? extends ResourceContainer > clazz) {
    Annotation anno = clazz.getAnnotation(URITemplate.class);
    if (anno == null) {
      return "";
    }
    return ((URITemplate) anno).value();
  }

  /**
   * Consists information about ResourceContainer.
   */
  public class HTTPResourceDescriptor implements ResourceDescriptor {

    private String httpMethodName;
    private URIPattern uriPattern;
    private String consumedMimeTypes;
    private String producedMimeTypes;
    private Class < ? extends InputEntityTransformer > inputTransformerType;
    private Class < ? extends OutputEntityTransformer > outputTransformerType;
    private Method servingMethod;
    private Annotation[] methodParameterAnnotations;
    private Class < ? >[] methodParameters;
    private ResourceContainer resourceContainer;

    /**
     * @param method the method of class ResourceContainer
     * @param httpMethodName HTTP method name
     * @param uri URI for serving by current method of ResourceContainer
     * @param resourceContainer ResourceContainer
     */
    public HTTPResourceDescriptor(Method method, String httpMethodName, String uri,
        ResourceContainer resourceContainer) {

      this.servingMethod = method;
      this.httpMethodName = httpMethodName;
      this.uriPattern = new URIPattern(uri);
      this.resourceContainer = resourceContainer;

      methodParameters = servingMethod.getParameterTypes();
      methodParameterAnnotations = resolveParametersAnnotations();

      ConsumedMimeTypes consumedMimeTypesAnnotation = method.getAnnotation(ConsumedMimeTypes.class);
      ProducedMimeTypes producedMimeTypesAnnotation = method.getAnnotation(ProducedMimeTypes.class);
      consumedMimeTypes = (consumedMimeTypesAnnotation != null) ? consumedMimeTypesAnnotation
          .value() : MimeTypes.ALL;
      producedMimeTypes = (producedMimeTypesAnnotation != null) ? producedMimeTypesAnnotation
          .value() : MimeTypes.ALL;

      InputTransformer containerInputTransformer = resourceContainer.getClass().getAnnotation(
          InputTransformer.class);
      InputTransformer methodInputTransformer = method.getAnnotation(InputTransformer.class);
      if (containerInputTransformer != null && methodInputTransformer == null) {
        inputTransformerType = containerInputTransformer.value();
      } else if (methodInputTransformer != null) {
        inputTransformerType = methodInputTransformer.value();
      }

      OutputTransformer containerOutputTransformer = resourceContainer.getClass().getAnnotation(
          OutputTransformer.class);
      OutputTransformer methodOutputTransformer = method.getAnnotation(OutputTransformer.class);
      if (containerOutputTransformer != null && methodOutputTransformer == null) {
        outputTransformerType = containerOutputTransformer.value();
      } else if (methodOutputTransformer != null) {
        outputTransformerType = methodOutputTransformer.value();
      }
    }

    /*
     * (non-Javadoc)
     * @see org.exoplatform.services.rest.container.ResourceDescriptor#getResourceContainer()
     */
    public ResourceContainer getResourceContainer() {
      return resourceContainer;
    }

    /*
     * (non-Javadoc)
     * @see org.exoplatform.services.rest.container.ResourceDescriptor#getServer()
     */
    public Method getServer() {
      return servingMethod;
    }

    /*
     * (non-Javadoc)
     * @see org.exoplatform.services.rest.container.ResourceDescriptor#getInputTransformerType()
     */
    public Class < ? extends InputEntityTransformer > getInputTransformerType() {
      return inputTransformerType;
    }

    /*
     * (non-Javadoc)
     * @see org.exoplatform.services.rest.container.ResourceDescriptor#getOutputTransformerType()
     */
    public Class < ? extends OutputEntityTransformer > getOutputTransformerType() {
      return outputTransformerType;
    }

    /*
     * (non-Javadoc)
     * @see org.exoplatform.services.rest.container.ResourceDescriptor#getMethodParameterAnnotations()
     */
    public Annotation[] getMethodParameterAnnotations() {
      return methodParameterAnnotations;
    }

    /*
     * (non-Javadoc)
     * @see org.exoplatform.services.rest.container.ResourceDescriptor#getMethodParameters()
     */
    public Class < ? >[] getMethodParameters() {
      return methodParameters;
    }

    /*
     * (non-Javadoc)
     * @see org.exoplatform.services.rest.container.ResourceDescriptor#getURIPattern()
     */
    public URIPattern getURIPattern() {
      return uriPattern;
    }

    /*
     * (non-Javadoc)
     * @see org.exoplatform.services.rest.container.ResourceDescriptor#getAcceptableMethod()
     */
    public String getAcceptableMethod() {
      return httpMethodName;
    }

    /*
     * (non-Javadoc)
     * @see org.exoplatform.services.rest.container.ResourceDescriptor#getConsumedMimeTypes()
     */
    public String getConsumedMimeTypes() {
      return consumedMimeTypes;
    }

    /*
     * (non-Javadoc)
     * @see org.exoplatform.services.rest.container.ResourceDescriptor#getProducedMimeTypes()
     */
    public String getProducedMimeTypes() {
      return producedMimeTypes;
    }

    private Annotation[] resolveParametersAnnotations() {
      Annotation[][] a = servingMethod.getParameterAnnotations();
      Annotation[] anno = new Annotation[a.length];
      for (int i = 0; i < a.length; i++) {
        if (a[i].length > 0) {
          anno[i] = a[i][0];
        }
      }
      return anno;
    }

  }

}
