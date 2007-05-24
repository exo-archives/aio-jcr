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

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.rest.wrapper.InvalidResourceDescriptorException;
import org.exoplatform.services.rest.wrapper.ResourceDescriptor;
import org.exoplatform.services.rest.wrapper.ResourceWrapper;
import org.exoplatform.services.rest.wrapper.WrapperResolvingStrategy;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class ResourceRouter implements Connector {
  
  
  private List <ResourceDescriptor> resourceDescriptors;
  private List <WrapperResolvingStrategy> bindStrategies;
  private String wrapperStrategy = null;
  
  public ResourceRouter(InitParams params) throws Exception {
    this.resourceDescriptors = new ArrayList <ResourceDescriptor>();
    this.bindStrategies = new ArrayList <WrapperResolvingStrategy>();

    Iterator<ValueParam> i = params.getValueParamIterator();
    while(i.hasNext()) {
      ValueParam v = i.next();
      WrapperResolvingStrategy ws = (WrapperResolvingStrategy)Class.forName(v.getValue()).newInstance();
      bindStrategies.add(ws);
    }
  }

  public Response serve(Request request) throws Exception {
    String requestedURI = request.getResourceIdentifier().getURI().getPath();
    String methodName = request.getMethodName();
    String mediaType = request.acceptedMediaType;

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
              } else if("org.exoplatform.services.rest.ProduceMimeType".equals(
                  a.annotationType().getCanonicalName())) {
              
                ProduceMimeType p = (ProduceMimeType)a;
                objs[i] = p.value();
              }
            }
          }
          return (Response)resource.getServer().invoke(resource.getWrapper(), objs);
       }  
    }
    throw new NoSuchMethodException("No method found for " + methodName + " " + requestedURI +
        " " + mediaType);
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
            "and org.exoplatform.services.rest.Representation object " + 
            "are alowed to use for ResourceWrapper objects");
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
}
