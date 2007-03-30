/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.storage.jdbc;

import javax.jcr.ItemNotFoundException;

import org.exoplatform.services.jcr.datamodel.InternalQPath;

/**
 * Created by The eXo Platform SARL
 *
 * 13.12.2006
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: PrimaryTypeNotFoundException.java 13310 2007-03-09 13:32:48Z peterit $
 */
public class PrimaryTypeNotFoundException extends ItemNotFoundException {
  
  private final InternalQPath path;
  
  PrimaryTypeNotFoundException(String message, InternalQPath path) {
    super(message);
    this.path = path;
  }

  public InternalQPath getItemPath() {
    return path;
  }
}
