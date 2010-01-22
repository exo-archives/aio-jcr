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

import java.util.List;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPathEntry;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id$
 * 
 *          Basic (Level 1) data flow inmemory operations
 * 
 *          Common Rule for Read : If there is some storage in this manager try to get the data from
 *          here first, if not found call super.someMethod
 */
public interface ItemDataConsumer {

  /**
   * @param parent
   * @param name
   * @return data by parent and name
   * @throws RepositoryException
   */
  ItemData getItemData(NodeData parent, QPathEntry name) throws RepositoryException;

  /**
   * @param identifier
   * @return data by identifier
   */
  ItemData getItemData(String identifier) throws RepositoryException;

  /**
   * @param parentIdentifier
   * @return children data
   */
  List<NodeData> getChildNodesData(NodeData parent) throws RepositoryException;

  /**
   * Get children nodes count of the parent node.
   * 
   * @param parent
   *          NodeData
   * @return int, child nodes count
   */
  int getChildNodesCount(NodeData parent) throws RepositoryException;

  /**
   * @param parentIdentifier
   * @return children data
   */
  List<PropertyData> getChildPropertiesData(NodeData parent) throws RepositoryException;

  List<PropertyData> listChildPropertiesData(final NodeData nodeData) throws RepositoryException;

  /**
   * @param identifier
   *          - referenceable id
   * @param skipVersionStorage
   *          - if true references will be returned according the JSR-170 spec, without items from
   *          version storage
   * @return - list of REFERENCE properties
   * @throws RepositoryException
   */
  List<PropertyData> getReferencesData(String identifier, boolean skipVersionStorage) throws RepositoryException;
}
