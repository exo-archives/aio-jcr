/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core.query;

import org.exoplatform.services.jcr.impl.core.SessionImpl;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: QueryManagerFactory.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class QueryManagerFactory {
  
  private final SearchManager searchManager;
  
  //private final LogService logService;
  
  public QueryManagerFactory(final SearchManager searchManager) {
//      final LogService logService) {
    super();
    this.searchManager = searchManager;
    //this.logService = logService;
  }
  
  public QueryManagerImpl getQueryManager(SessionImpl session) {
    return new QueryManagerImpl(session, searchManager);
  }

}
