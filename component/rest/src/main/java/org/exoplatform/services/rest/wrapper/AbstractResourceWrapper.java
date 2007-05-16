/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.wrapper;

import org.picocontainer.Startable;
import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.ResourceRouter;
import org.exoplatform.services.rest.wrapper.InvalidResourceDescriptorException;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */

public abstract class AbstractResourceWrapper implements Startable, ResourceWrapper {
  
  private ResourceRouter resRouter;
  Log logger = ExoLogger.getLogger("rest.wrapper.AbstractResourceWrapper");
  
  public AbstractResourceWrapper(ResourceRouter resRouter){
    this.resRouter = resRouter;
    logger.info("RESOURCE_ROUTER: " + resRouter);
  }
  
  public void start() {
    try {
      resRouter.bind(this);
      logger.info("ReosourceWrapper Component: " + this.getClass().getName() +
          "was added to ResourceRouter");
    }catch(InvalidResourceDescriptorException irde){
      logger.error("Cann't add ResourceWrapper Component: " + this.getClass().getName() +
          "to ReosourceRouter");
    }
  }

  public void stop() {
    resRouter.unbind(this);
  }
}
