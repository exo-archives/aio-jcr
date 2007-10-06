/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.read;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.util.TraversingItemVisitor;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public abstract class AbstractItemVisitor extends TraversingItemVisitor {
  
  public AbstractItemVisitor(boolean breadthFirst, int maxLevel) {
    super(breadthFirst, maxLevel);
  }

  @Override
  protected void entering(Property arg0, int arg1) throws RepositoryException {
  }

  @Override
  protected void entering(Node arg0, int arg1) throws RepositoryException {
  }

  @Override
  protected void leaving(Property arg0, int arg1) throws RepositoryException {
  }

  @Override
  protected void leaving(Node arg0, int arg1) throws RepositoryException {
  }
  
}
