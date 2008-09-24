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
package org.exoplatform.services.jcr.core.nodetype;

import java.io.InputStream;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;

import org.exoplatform.services.jcr.datamodel.InternalQName;

/**
 * Created by The eXo Platform SAS. <br/> Node Type manager.
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: ExtendedNodeTypeManager.java 11907 2008-03-13 15:36:21Z ksm $
 */

public interface ExtendedNodeTypeManager extends NodeTypeManager {

  public static final int IGNORE_IF_EXISTS = 0;

  // public static final int REPLACE_IF_EXISTS = 1;
  public static final int FAIL_IF_EXISTS   = 2;

  /**
   * The node-type node should be created and saved(!) as /jcr:system/jcr:nodetypes/"name" as
   * nt:nodeType node before calling this method.
   */
  void registerNodeType(ExtendedNodeType nodeType, int alreadyExistsBehaviour) throws RepositoryException;

  /**
   * Registers node type from class containing the NT definition. The class should have constructor
   * with one parameter NodeTypeManager.
   * 
   * @param nodeTypeType
   *          - Class containing node type definition
   * @param alreadyExistsBehaviour
   *          if node type with such a name already exists: IGNORE_IF_EXISTS - does not register new
   *          node (default) FAIL_IF_EXISTS - throws RepositoryException REPLACE_IF_EXISTS -
   *          replaces registerd type with new one
   * @throws RepositoryException
   * @deprecated
   */
  void registerNodeType(Class<ExtendedNodeType> nodeTypeType, int alreadyExistsBehaviour) throws RepositoryException,
                                                                                         InstantiationException;

  /**
   * Registers node type using value object.
   * 
   * @param nodeTypeValue
   * @param alreadyExistsBehaviour
   * @throws RepositoryException
   */
  void registerNodeType(NodeTypeValue nodeTypeValue, int alreadyExistsBehaviour) throws RepositoryException;

  /**
   * Registers all node types using XML binding value objects from xml stream.
   * 
   * @param xml
   * @param alreadyExistsBehaviour
   * @throws RepositoryException
   */
  void registerNodeTypes(InputStream xml, int alreadyExistsBehaviour) throws RepositoryException;

  NodeType getNodeType(InternalQName qname) throws NoSuchNodeTypeException, RepositoryException;
}
