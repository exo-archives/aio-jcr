package org.exoplatform.services.jcr.impl.core.access;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.organization.auth.ExoBroadcastJAASLoginModule;

public class StandaloneBroadcastJAASLoginModule extends ExoBroadcastJAASLoginModule {

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
