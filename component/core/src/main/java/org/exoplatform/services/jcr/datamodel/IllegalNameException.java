/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.datamodel;

import javax.jcr.RepositoryException;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: IllegalNameException.java 12843 2007-02-16 09:11:18Z peterit $
 */

public class IllegalNameException extends RepositoryException {

  public IllegalNameException() {
    super();
  }

  public IllegalNameException(String arg0) {
    super(arg0);
  }

  public IllegalNameException(String arg0, Throwable arg1) {
    super(arg0, arg1);
  }

  public IllegalNameException(Throwable arg0) {
    super(arg0);
  }

}
