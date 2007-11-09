/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.core;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.InvalidSerializedDataException;
import javax.jcr.ItemExistsException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Workspace;
import javax.jcr.nodetype.ConstraintViolationException;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public interface ExtendedWorkspace extends Workspace {
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
