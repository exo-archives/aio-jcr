/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.config;

import java.util.ArrayList;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: ValueStorageEntry.java 12843 2007-02-16 09:11:18Z peterit $
 */

public class ValueStorageEntry extends MappedParametrizedObjectEntry {
  
  private ArrayList filters;
  
  public ValueStorageEntry() {
    super();
  }
  
  public ValueStorageEntry(String type, ArrayList params) {
    super(type, params);
  }

  public ArrayList getFilters() {
    return filters;
  }

  public void setFilters(ArrayList filters) {
    this.filters = filters;
  }
}
