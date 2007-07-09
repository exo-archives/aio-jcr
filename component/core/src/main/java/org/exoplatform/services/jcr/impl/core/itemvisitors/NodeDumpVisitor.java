/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core.itemvisitors;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.util.TraversingItemVisitor;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: NodeDumpVisitor.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class NodeDumpVisitor extends TraversingItemVisitor {
  
  private String dumpStr = "";

  protected void entering(Property property, int level)
      throws RepositoryException {
    dumpStr+=" "+property.getPath()+"\n";
  }

  protected void entering(Node node, int level) throws RepositoryException {
    dumpStr+=node.getPath()+"\n";
  }

  protected void leaving(Property property, int level)
      throws RepositoryException {
  }

  protected void leaving(Node node, int level) throws RepositoryException {
  }
  
  public String getDump() {
    return dumpStr;
  }

}
