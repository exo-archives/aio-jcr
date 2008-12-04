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
import java.util.Collection;

import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;

import org.exoplatform.services.jcr.datamodel.InternalQName;

/**
 * Created by The eXo Platform SAS. <br/>
 * 
 * Node Type manager.
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: ExtendedNodeTypeManager.java 11907 2008-03-13 15:36:21Z ksm $
 */

public interface ExtendedNodeTypeManager extends NodeTypeManager {

  public static final int IGNORE_IF_EXISTS  = 0;

  public static final int FAIL_IF_EXISTS    = 2;

  public static final int REPLACE_IF_EXISTS = 4;

  NodeType findNodeType(InternalQName qname) throws NoSuchNodeTypeException, RepositoryException;

  /**
   * The node-type node should be created and saved(!) as /jcr:system/jcr:nodetypes/"name" as
   * nt:nodeType node before calling this method.
   */
  //void registerNodeType(ExtendedNodeType nodeType, int alreadyExistsBehaviour) throws RepositoryException;

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
  //void registerNodeType(Class<ExtendedNodeType> nodeTypeType, int alreadyExistsBehaviour) throws RepositoryException,
  //                                                                                       InstantiationException;

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
   *          a InputStream
   * @param alreadyExistsBehaviour
   *          a int
   * @throws RepositoryException
   */
  void registerNodeTypes(InputStream xml, int alreadyExistsBehaviour) throws RepositoryException;

  // ------ JCR 2 related features methods ------

  /**
   * Return <code>NodeTypeValue</code> for a given nodetype name.
   * 
   * Used for nodetype update. Value can be edited and registered via
   * <code>registerNodeType(NodeTypeValue nodeTypeValue, int alreadyExistsBehaviour)</code>.
   * 
   * @param ntName
   *          nodetype name
   * @return NodeTypeValue
   * @throws NoSuchNodeTypeException
   *           if no nodetype found with the name
   * @throws RepositoryException
   *           Repository error
   */
  NodeTypeValue getNodeTypeValue(String ntName) throws NoSuchNodeTypeException, RepositoryException;

  /**
   * Registers or updates the specified <code>Collection</code> of <code>NodeTypeValue</code>
   * objects. This method is used to register or update a set of node types with mutual
   * dependencies. Returns an iterator over the resulting <code>NodeType</code> objects. <p/> The
   * effect of the method is "all or nothing"; if an error occurs, no node types are registered or
   * updated. <p/> Throws an <code>InvalidNodeTypeDefinitionException</code> if a
   * <code>NodeTypeDefinition</code> within the <code>Collection</code> is invalid or if the
   * <code>Collection</code> contains an object of a type other than <code>NodeTypeDefinition</code>
   * . <p/> Throws a <code>NodeTypeExistsException</code> if <code>allowUpdate</code> is
   * <code>false</code> and a <code>NodeTypeDefinition</code> within the <code>Collection</code>
   * specifies a node type name that is already registered. <p/> Throws an
   * <code>UnsupportedRepositoryOperationException</code> if this implementation does not support
   * node type registration.
   * 
   * @param values
   *          a collection of <code>NodeTypeValue</code>s
   * @param alreadyExistsBehaviour
   *          a int
   * @return the registered node types.
   * @throws InvalidNodeTypeDefinitionException
   *           if a <code>NodeTypeDefinition</code> within the <code>Collection</code> is invalid or
   *           if the <code>Collection</code> contains an object of a type other than
   *           <code>NodeTypeDefinition</code>.
   * @throws NodeTypeExistsException
   *           if <code>allowUpdate</code> is <code>false</code> and a
   *           <code>NodeTypeDefinition</code> within the <code>Collection</code> specifies a node
   *           type name that is already registered.
   * @throws UnsupportedRepositoryOperationException
   *           if this implementation does not support node type registration.
   * @throws RepositoryException
   *           if another error occurs.
   */
  public NodeTypeIterator registerNodeTypes(Collection<NodeTypeValue> values,
                                            int alreadyExistsBehaviour) throws UnsupportedRepositoryOperationException,
                                                                       RepositoryException;

  /**
   * Unregisters the specified node type.
   * 
   * @param name
   *          a <code>String</code>.
   * @throws UnsupportedRepositoryOperationException
   *           if this implementation does not support node type registration.
   * @throws NoSuchNodeTypeException
   *           if no registered node type exists with the specified name.
   * @throws RepositoryException
   *           if another error occurs.
   */
  public void unregisterNodeType(String name) throws UnsupportedRepositoryOperationException,
                                             NoSuchNodeTypeException,
                                             RepositoryException;

  /**
   * Unregisters the specified set of node types.<p/> Used to unregister a set of node types with
   * mutual dependencies.
   * 
   * @param names
   *          a <code>String</code> array
   * @throws UnsupportedRepositoryOperationException
   *           if this implementation does not support node type registration.
   * @throws NoSuchNodeTypeException
   *           if one of the names listed is not a registered node type.
   * @throws RepositoryException
   *           if another error occurs.
   */
  public void unregisterNodeTypes(String[] names) throws UnsupportedRepositoryOperationException,
                                                 NoSuchNodeTypeException,
                                                 RepositoryException;
}
