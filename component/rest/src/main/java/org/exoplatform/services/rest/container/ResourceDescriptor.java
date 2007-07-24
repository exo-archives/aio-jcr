/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest.container;

import java.lang.reflect.Method;
import java.lang.annotation.Annotation;

import org.exoplatform.services.rest.URIPattern;
import org.exoplatform.services.rest.transformer.InputEntityTransformer;
import org.exoplatform.services.rest.transformer.OutputEntityTransformer;

/**
 * Created by The eXo Platform SARL. <br/>
 * Abstraction of resource description.<br/>
 * @author Gennady Azarenkov
 * @version $Id: $
 */
public interface ResourceDescriptor {
  /**
   * HTTP method for serving by ResourceContainer.
   * @return HTTP method name
   */
  String getAcceptableMethod();
  
  /**
   * Discribe mime type(s) which RessourceContainer can consume. 
   * @return mime type(s)
   */
  String getConsumedMimeTypes();
  
  /**
   * Discribe mime type(s) which RessourceContainer can produce. 
   * @return mime type(s)
   */
  String getProducedMimeTypes();
  
  /**
   * Object of ResourecContainer.
   * @return ResourceContainer
   */
  ResourceContainer getResourceContainer();
  
  /**
   * Method of ResourceContainer object. Then this method 
   * may be called by ResourceDispatcher.
   * @return Method
   */
  Method getServer();
  
  /**
   * Return URIPattern object.
   * @return URIPattern
   */
  URIPattern getURIPattern();
  
  /**
   * Return the array of annotation for method parameters.
   * @return method parameter annotations
   */
  Annotation[] getMethodParameterAnnotations();
  
  /**
   * Return the array of types method parameters.
   * @return meethod parameters types
   */
  Class < ? > [] getMethodParameters();
  
  /**
   * Return Class of OutputEntityTransformer. ResourceDispatcher uses this class
   * for creation instance of OutputEntityTransformer.
   * @return Class of OutputEntityTransformer
   */
  Class < ? extends OutputEntityTransformer > getOutputTransformerType();
  
  /**
   * Return Class of InputEntityTransformer. ResourceDispatcher uses this class
   * for creation instance of InputEntityTransformer.
   * @return Class of InputEntityTransformer
   */
  Class < ? extends InputEntityTransformer > getInputTransformerType();
  
}
