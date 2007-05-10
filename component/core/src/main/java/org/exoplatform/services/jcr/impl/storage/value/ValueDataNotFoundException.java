/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.storage.value;

import javax.jcr.RepositoryException;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class ValueDataNotFoundException extends RepositoryException {

  private static final long serialVersionUID = -3480032427540892483L;

  public ValueDataNotFoundException() {
    super();
    // TODO Auto-generated constructor stub
  }

  public ValueDataNotFoundException(String message, Throwable rootCause) {
    super(message, rootCause);
    // TODO Auto-generated constructor stub
  }

  public ValueDataNotFoundException(String message) {
    super(message);
    // TODO Auto-generated constructor stub
  }

  public ValueDataNotFoundException(Throwable rootCause) {
    super(rootCause);
    // TODO Auto-generated constructor stub
  }

}
