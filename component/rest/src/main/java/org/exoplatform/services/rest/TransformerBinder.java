/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import org.picocontainer.Startable;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 * @deprecated
 */

public class TransformerBinder implements Startable {
  
  private List<EntityTransformer<?>> transformers;
  private ExoContainerContext containerContext;
  private ExoContainer container;
  
  public TransformerBinder(ExoContainerContext containerContext) throws Exception {

    this.containerContext = containerContext;
    this.transformers = new ArrayList<EntityTransformer<?>>();
  }
  
  public void bind(EntityTransformer<?> transf) {
    // TODO add check (not allowed to transformers for the same type of data)
    transformers.add(transf);
  }
  
  public void unbind(EntityTransformer<?> transf) {
    transformers.remove(transf);
  }
  
  public int transformersNumber() {
    return transformers.size();
  }
  
  public void start() {
    container = containerContext.getContainer();
    List<EntityTransformer<?>> list = 
      container.getComponentInstancesOfType(EntityTransformer.class);
    for(EntityTransformer<?> t : list)
        bind(t);
  }
  
  public void stop() {
  }

}
