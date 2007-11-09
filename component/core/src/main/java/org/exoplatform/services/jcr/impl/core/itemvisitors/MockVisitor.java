/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.core.itemvisitors;

import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

/**
 * Created by The eXo Platform SARL        
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: MockVisitor.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class MockVisitor implements ItemVisitor {

  private String str;

  public void visit(Property property) throws RepositoryException {
    str = "property:" + property.getName();
  }

  public void visit(Node node) throws RepositoryException {
    str = "node:" + node.getName();
  }

  public String toString() {
    return str;
  }

}
