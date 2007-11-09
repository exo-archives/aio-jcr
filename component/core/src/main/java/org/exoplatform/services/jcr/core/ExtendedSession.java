/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.core;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.InvalidSerializedDataException;
import javax.jcr.ItemExistsException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.ConstraintViolationException;

import org.exoplatform.services.jcr.impl.core.LocationFactory;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public interface ExtendedSession extends Session {
  
  public String getId();
  
  /**
   * @return Returns the locationFactory.
   */
  public LocationFactory getLocationFactory();
  
  public void importXML(String parentAbsPath,
                        InputStream in,
                        int uuidBehavior,
                        boolean respectPropertyDefinitionsConstraints) throws IOException,
      PathNotFoundException,
      ItemExistsException,
      ConstraintViolationException,
      InvalidSerializedDataException,
      RepositoryException;
}
