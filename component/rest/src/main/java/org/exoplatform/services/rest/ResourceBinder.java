/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.lang.reflect.Method;
//import java.lang.annotation.Annotation;

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
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class ResourceBinder implements Startable {

  private List <ResourceDescriptor> resourceDescriptors;
  private List <ResourceContainerResolvingStrategy> bindStrategies;
  private ExoContainerContext containerContext;
  private ExoContainer container;
  Log logger = ExoLogger.getLogger("ResourceBinder");

  public ResourceBinder(InitParams params, ExoContainerContext containerContext) throws Exception {
    this.containerContext = containerContext;
    this.resourceDescriptors = new ArrayList <ResourceDescriptor>();
    this.bindStrategies = new ArrayList <ResourceContainerResolvingStrategy>();

    Iterator<ValueParam> i = params.getValueParamIterator();
    while(i.hasNext()) {
      ValueParam v = i.next();
      ResourceContainerResolvingStrategy ws = (ResourceContainerResolvingStrategy)Class.forName(v.getValue()).newInstance();
      bindStrategies.add(ws);
    }
  }
  
  public void bind(ResourceContainer resourceCont) throws InvalidResourceDescriptorException {
    for(ResourceContainerResolvingStrategy strategy : bindStrategies) {
      List <ResourceDescriptor> resList = strategy.resolve(resourceCont);
      validate(resList);
      resourceDescriptors.addAll(resList);
    }
  }

  public void unbind(ResourceContainer resourceCont) {
    int i=0;
    List <ResourceDescriptor> tmp = new ArrayList <ResourceDescriptor> (resourceDescriptors);  
    for(ResourceDescriptor resource : tmp) {
      if(resource.getResourceContainer().equals(resourceCont)) {
        resourceDescriptors.remove(i);
      } else {
        i++;
      }
    }
  }
  
  public void clear() {
    this.resourceDescriptors.clear();
  }
  
  public List getAllDescriptors() {
    return this.resourceDescriptors;
  }

  private void validate(List <ResourceDescriptor> newDescriptors)
      throws InvalidResourceDescriptorException {
    for(ResourceDescriptor newDesc:newDescriptors) {
      URIPattern npattern = newDesc.getURIPattern();
      Method method = newDesc.getServer();
      Class[] requestedParams = method.getParameterTypes();
      boolean hasRequestRepresentation = false;
      for(Class param : requestedParams) {
        if("org.exoplatform.services.rest.Representation".equals(param.getCanonicalName())){
          if(!hasRequestRepresentation) 
            hasRequestRepresentation = true;
          else throw new InvalidResourceDescriptorException (
              "You alredy have one of org.exoplatform.services.rest.Representation object");
        } else if(!"java.lang.String".equals(param.getCanonicalName())) {
            throw new InvalidResourceDescriptorException ( "Only java.lang.String " + 
                "and org.exoplatform.services.rest.Representation object" + 
                "are alowed to use for ResourceContainer objects");
        }
      }
      for(ResourceDescriptor storedDesc:resourceDescriptors) {
        URIPattern spattern = storedDesc.getURIPattern();
          // check URI pattern
        if(spattern.matches(npattern.getString()) ||
            npattern.matches(spattern.getString())) {
          throw new InvalidResourceDescriptorException("The resource descriptor pattern '"+
              newDesc.getURIPattern().getString() + "' can not be defined because of existed '"+
              storedDesc.getURIPattern().getString());
        }
      }
    }
  }

  public void start() {
    container = containerContext.getContainer();
    List<ResourceContainer> list = 
      container.getComponentInstancesOfType(ResourceContainer.class);
    for(ResourceContainer c : list) {
      try {
        bind(c);
      } catch(InvalidResourceDescriptorException irde) {
        logger.error("Cann't add ResourceContainer Component: " + c.getClass().getName());
      }
    }
  }
  
  public void stop() {
  }
}
