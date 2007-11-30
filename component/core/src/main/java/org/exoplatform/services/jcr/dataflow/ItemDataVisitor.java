/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.services.jcr.dataflow;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;

/**
 * Created by The eXo Platform SAS.
 * @author Gennady Azarenkov
 * @version $Id: ItemDataVisitor.java 12843 2007-02-16 09:11:18Z peterit $
 */

public interface ItemDataVisitor {
  
  /**
   * @return data manager wired data
   */
  ItemDataConsumer getDataManager();
  
  /**
   * visit propertyData
   * @param property
   * @throws RepositoryException
   */
  void visit(PropertyData property) throws RepositoryException;

  /**
   * visit NodeData
   * @param node
   * @throws RepositoryException
   */
  void visit(NodeData node) throws RepositoryException;

}
