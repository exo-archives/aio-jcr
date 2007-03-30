/**
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.jcr.benchmark.init;

import javax.jcr.Repository;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public abstract class JCRInitializer {
  
  
  protected Repository repository;
  
  public abstract void initialize();
  
  public final Repository getRepository() {
    return repository;
  }


}
