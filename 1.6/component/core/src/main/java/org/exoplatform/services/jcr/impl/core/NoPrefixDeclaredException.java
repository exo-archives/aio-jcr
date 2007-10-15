/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: NoPrefixDeclaredException.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class NoPrefixDeclaredException extends Exception {

  public NoPrefixDeclaredException() {
    super();
  }

  public NoPrefixDeclaredException(String arg0) {
    super(arg0);
  }

  public NoPrefixDeclaredException(String arg0, Throwable arg1) {
    super(arg0, arg1);
  }

  public NoPrefixDeclaredException(Throwable arg0) {
    super(arg0);
  }

}
