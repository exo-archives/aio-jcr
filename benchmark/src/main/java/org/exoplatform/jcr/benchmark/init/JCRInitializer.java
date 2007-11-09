/**
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.jcr.benchmark.init;

import javax.jcr.Repository;

import com.sun.japex.Params;


/**
 * Created by The eXo Platform SARL .
 * Abstract class encapsulates mechanizm of repository initialization
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public abstract class JCRInitializer {

  protected Repository repository;

  /**
   * Initializes repository
   * @param params
   */
  public abstract void initialize(Params params);

  /**
   * @return repository
   */
  public final Repository getRepository() {
    return repository;
  }

}
