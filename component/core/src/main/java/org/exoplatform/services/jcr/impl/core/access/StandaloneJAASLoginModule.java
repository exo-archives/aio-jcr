package org.exoplatform.services.jcr.impl.core.access;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.organization.auth.ExoLoginJAASLoginModule;

public class StandaloneJAASLoginModule extends ExoLoginJAASLoginModule {

  @Override
  public ExoContainer getContainer() throws Exception {
    return StandaloneContainer.getInstance();
  }

  @Override
  public void postProcessOperations() throws Exception {
    
  }

  @Override
  public void preProcessOperations() throws Exception {
    
  }
  
  

}
