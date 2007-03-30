/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.core.version;

import javax.jcr.version.VersionException;

/**
 * Created by The eXo Platform SARL
 * 11.12.2006
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko </a>
 * @version $Id: LabelNotFoundException.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class LabelNotFoundException extends VersionException {

  LabelNotFoundException(String message) {
    super(message);
  }
  
  LabelNotFoundException(String message, Throwable e) {
    super(message, e);
  }
}
