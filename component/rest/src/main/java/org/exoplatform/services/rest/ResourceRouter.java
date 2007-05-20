/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.rest.wrapper.InvalidResourceDescriptorException;
import org.exoplatform.services.rest.wrapper.ResourceDescriptor;
import org.exoplatform.services.rest.wrapper.ResourceWrapper;
import org.exoplatform.services.rest.wrapper.WrapperResolvingStrategy;
import org.exoplatform.services.rest.wrapper.http.HTTPAnnotatedWrapperResolvingStrategy;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class ResourceRouter implements Connector {
  
  private List <ResourceDescriptor> resourceDescriptors;
  private List <WrapperResolvingStrategy> bindStrategies;
  private String wrapperStrategy = null;
  
  public ResourceRouter(InitParams params) {
    ValueParam wstvalue = params.getValueParam("wrapper-strategy");
    if(wstvalue != null) {
      wrapperStrategy = wstvalue.getValue();
    }
    this.resourceDescriptors = new ArrayList <ResourceDescriptor>();
    this.bindStrategies = new ArrayList <WrapperResolvingStrategy>();
    try {
      bindStrategies.add((HTTPAnnotatedWrapperResolvingStrategy)Class.forName(wrapperStrategy).newInstance());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Response serve(Request request) throws Exception {
    String requestedURI = request.getResourceIdentifier().getURI().getPath();
    String methodName = request.getMethodName();        
    for(ResourceDescriptor resource : resourceDescriptors) {
      if(resource.getAcceptableMethod().equalsIgnoreCase(methodName)
         && resource.getURIPattern().matches(requestedURI)) {

        return (Response)resource.getServer().invoke(resource.getWrapper(), request.getEntity());
      }
    }
    throw new NoSuchMethodException("No method found for " + methodName + " " + requestedURI);
  }

  public void bind(ResourceWrapper wrapper) throws InvalidResourceDescriptorException {
    for(WrapperResolvingStrategy strategy : bindStrategies) {
      List <ResourceDescriptor> resList = strategy.resolve(wrapper);
      validate(resList);
      resourceDescriptors.addAll(resList);
    }
  }

  public void unbind(ResourceWrapper wrapper) {
    int i=0;
    List <ResourceDescriptor> tmp = new ArrayList <ResourceDescriptor> (resourceDescriptors);  
    for(ResourceDescriptor resource : tmp) {
      if(resource.getWrapper().equals(wrapper)) {
        resourceDescriptors.remove(i);
      } else {
        i++;
      }
    }
  }
  
  public void clear() {
    this.resourceDescriptors = new ArrayList <ResourceDescriptor>();
  }
  
  public List getAllDescriptors() {
    return this.resourceDescriptors;
  }

  private void validate(List <ResourceDescriptor> newDescriptors)
  		throws InvalidResourceDescriptorException {
	for(ResourceDescriptor newDesc:newDescriptors) {
		URIPattern npattern = newDesc.getURIPattern();
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
}
