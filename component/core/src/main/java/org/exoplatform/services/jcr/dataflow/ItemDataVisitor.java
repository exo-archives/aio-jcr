/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.dataflow;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: ItemDataVisitor.java 12843 2007-02-16 09:11:18Z peterit $
 */

public interface ItemDataVisitor {
  
  ItemDataConsumer getDataManager();
  
  void visit(PropertyData property) throws RepositoryException;

  void visit(NodeData node) throws RepositoryException;

}
