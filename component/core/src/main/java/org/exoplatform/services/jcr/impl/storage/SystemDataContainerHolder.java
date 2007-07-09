/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.storage;

import org.exoplatform.services.jcr.storage.WorkspaceDataContainer;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class SystemDataContainerHolder {
    private WorkspaceDataContainer dataContainer;
    
    public SystemDataContainerHolder(WorkspaceDataContainer dataContainer) {
      this.dataContainer = dataContainer;
    }
    
    public WorkspaceDataContainer getContainer() {
      return dataContainer;
    }
}
