/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.config;

import java.util.ArrayList;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: CacheEntry.java 12843 2007-02-16 09:11:18Z peterit $
 */

public class CacheEntry extends MappedParametrizedObjectEntry {
  
  private boolean enabled;

  public CacheEntry() {
    super();
  }

  public CacheEntry(ArrayList params) {
    super("org.exoplatform.services.jcr.impl.storage.cache.WorkspaceCache", params);
  }

  /**
   * @return Returns the enabled.
   */
  public boolean isEnabled() {
    return enabled;
  }
  /**
   * @param enabled The enabled to set.
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
