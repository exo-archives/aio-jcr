/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.lang.reflect.Method;
import java.lang.annotation.Annotation;

import org.picocontainer.Startable;
import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.container.InvalidResourceDescriptorException;
import org.exoplatform.services.rest.container.ResourceDescriptor;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.container.ResourceContainerResolvingStrategy;

/**
 * Created by The eXo Platform SARL.<br/>
 * For binding and unbinding ResourceContainers.<br/>
 * @author Gennady Azarenkov
 * @version $Id: $
 */
public class ResourceBinder implements Startable {

  private List < ResourceDescriptor > resourceDescriptors;
  private List < ResourceContainerResolvingStrategy > bindStrategies;
  private ExoContainerContext containerContext;
  private ExoContainer container;
  private static Log logger = ExoLogger.getLogger("ResourceBinder");

  /**
   * Constructor sets the resolving strategy.
   * Currently HTTPAnnotatedContainerResolvingStrategy
   * (annotations used for description ResourceContainers) 
   * @param params class name for ResourceContainerResolvingStrategy
   * @param containerContext ExoContainer context
   * @throws Exception any exception
   */
  public ResourceBinder(InitParams params,
      ExoContainerContext containerContext) throws Exception {

    this.containerContext = containerContext;
    this.resourceDescriptors = new ArrayList < ResourceDescriptor >();
    this.bindStrategies = new ArrayList < ResourceContainerResolvingStrategy >();

    Iterator < ValueParam > i = params.getValueParamIterator();
    while (i.hasNext()) {
      ValueParam v = i.next();
      ResourceContainerResolvingStrategy rs =
        (ResourceContainerResolvingStrategy) Class.forName(v.getValue()).newInstance();
      bindStrategies.add(rs);
    }
  }
  
  /**
   * Bind ResourceContainer resourceCont if validation for this container is ok.
   * @param resourceCont the Resource Container
   * @throws InvalidResourceDescriptorException if validation filed.
   */
  public void bind(ResourceContainer resourceCont) throws InvalidResourceDescriptorException {
    for (ResourceContainerResolvingStrategy strategy : bindStrategies) {
      List < ResourceDescriptor > resList = strategy.resolve(resourceCont);
      validate(resList);
      resourceDescriptors.addAll(resList);
      logger.info("Bind new ResourceContainer: " + resourceCont);
sortResources(resourceDescriptors, 0 , resourceDescriptors.size()-1);
    }
  }

  /**
   * Unbind single ResourceContainer.
   * @param resourceCont the ResourceContainer which should be unbinded
   */
  public void unbind(ResourceContainer resourceCont) {
    int i = 0;
    List < ResourceDescriptor > tmp = new ArrayList < ResourceDescriptor >(resourceDescriptors);  
    for (ResourceDescriptor resource : tmp) {
      if (resource.getResourceContainer().equals(resourceCont)) {
        resourceDescriptors.remove(i);
      } else {
        i++;
      }
    }
  }
  
  /**
   * Clear the list of ResourceContainer description.
   */
  public void clear() {
    this.resourceDescriptors.clear();
  }
  
  /**
   * @return all resoursec descriptors.
   */
  public List < ResourceDescriptor > getAllDescriptors() {
    return this.resourceDescriptors;
  }

  /**
   * Validation for ResourceContainer.
   * Not allowed have two ResourceContainers with the same URIPatterns
   * And ALL ResourceContainers must have the reqired annotation  
   * @param newDescriptors descriptors of ResourceContainer for binding
   * @throws InvalidResourceDescriptorException if ResourceContainer is not valid.
   */
  private void validate(List < ResourceDescriptor > newDescriptors)
      throws InvalidResourceDescriptorException {
    
    for (ResourceDescriptor newDesc : newDescriptors) {
      URIPattern npattern = newDesc.getURIPattern();
      String nhttpMethod = newDesc.getAcceptableMethod();
 
      for (ResourceDescriptor storedDesc : resourceDescriptors) {
        URIPattern spattern = storedDesc.getURIPattern();
        String shttpMethod = storedDesc.getAcceptableMethod();
        // check URI pattern
//        if (spattern.matches(npattern).getString()) 
//            || npattern.matches(spattern).getString())) {
        if (spattern.matches(npattern) || npattern.matches(spattern)) {
          // check HTTP method.
          if (shttpMethod.equalsIgnoreCase(nhttpMethod)) {
            throw new InvalidResourceDescriptorException("The resource descriptor pattern '"
                + newDesc.getURIPattern().getString()
                + "' can not be defined because of existed '"
                + storedDesc.getURIPattern().getString());
          }
        }
      }

      Method method = newDesc.getServer();
      Class < ? > [] requestedParams = method.getParameterTypes();
      Annotation[][] paramAnno = method.getParameterAnnotations();
      boolean hasRequestEntity = false;
      // check method parameters
      for (int i = 0; i < paramAnno.length; i++) {
        if (paramAnno[i].length == 0) {
          if (method.getAnnotation(InputTransformer.class) == null
              && newDesc.getResourceContainer().getClass()
              .getAnnotation(InputTransformer.class) == null) {
            throw new InvalidResourceDescriptorException(
            "One not annotated object found, but transformer in methods(class)"
                + " annotation is not specified. This is not allowed!");
          }
          if (!hasRequestEntity) { 
            hasRequestEntity = true;
          } else {
            throw new InvalidResourceDescriptorException(
            "Only one not annotated object with must represent HTTP Request.\n"
              + "Not allowed to have this: "
              + requestedParams[i].getCanonicalName()
              + "' ");
          }
        }
      }
    }
  }
  
  private List < ResourceDescriptor > sortResources(List < ResourceDescriptor > l, int i0, int k0) {
    int i = i0;
    int k = k0;
    if (k0 > i0) {
      while (i <= k) {
        if (resourceDescriptors.get(i).getURIPattern().getParamNames().size()
            < resourceDescriptors.get(k).getURIPattern().getParamNames().size()) {
          swapResources(l, i, k);
        }
        i++;
        k--;
        if (i0 < k) {
          sortResources(l, i0, k);
        }
        if (i < k0) {
          sortResources(l, i, k0);
        }
      }
    }
    return l;
  }
  
  private void swapResources(List < ResourceDescriptor >l, int i, int k) {
    Object o = l.get(i);
    l.set(i, l.remove(k));
    l.add(k, (ResourceDescriptor)o);
  }
  
  

  /* (non-Javadoc)
   * @see org.picocontainer.Startable#start()
   */
  public void start() {
    container = containerContext.getContainer();
    List < ResourceContainer > list = 
      container.getComponentInstancesOfType(ResourceContainer.class);
    for (ResourceContainer c : list) {
      try {
        bind(c);
      } catch (InvalidResourceDescriptorException irde) {
        logger.error("Can't add ResourceContainer Component: " + c.getClass().getName());
      }
    }
  }
  
  /* (non-Javadoc)
   * @see org.picocontainer.Startable#stop()
   */
  public void stop() {
    clear();
  }
}
