/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.core.nodetype;

import java.io.InputStream;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;

import org.exoplatform.services.jcr.datamodel.InternalQName;

/**
 * Created by The eXo Platform SARL . <br/>
 * Node Type manager
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: ExtendedNodeTypeManager.java 12843 2007-02-16 09:11:18Z peterit $
 */

public interface ExtendedNodeTypeManager extends NodeTypeManager {

  public static final int IGNORE_IF_EXISTS = 0;

  // public static final int REPLACE_IF_EXISTS = 1;
  public static final int FAIL_IF_EXISTS = 2;

  /**
   * The node-type node should be created and saved(!) as
   * /jcr:system/jcr:nodetypes/ <name>as nt:nodeType node before calling this
   * method
   */
  void registerNodeType(ExtendedNodeType nodeType, int alreadyExistsBehaviour)
      throws RepositoryException;

  /**
   * Registers node type from class containing the NT definition The class
   * should have constructor with one parameter NodeTypeManager
   * 
   * @param nodeTypeType -
   *          Class containing node type definition
   * @param alreadyExistsBehaviour
   *          if node type with such a name already exists: IGNORE_IF_EXISTS -
   *          does not register new node (default) FAIL_IF_EXISTS - throws
   *          RepositoryException REPLACE_IF_EXISTS - replaces registerd type
   *          with new one
   * @throws RepositoryException
   * @deprecated
   */
  void registerNodeType(Class<ExtendedNodeType> nodeTypeType, int alreadyExistsBehaviour)
      throws RepositoryException, InstantiationException;

  /**
   * Registers node type using value object
   * 
   * @param nodeTypeValue
   * @param alreadyExistsBehaviour
   * @throws RepositoryException
   */
  void registerNodeType(NodeTypeValue nodeTypeValue, int alreadyExistsBehaviour)
      throws RepositoryException;

  /**
   * Registers all node types using XML binding value objects from xml stream
   * 
   * @param xml
   * @param alreadyExistsBehaviour
   * @throws RepositoryException
   */
  void registerNodeTypes(InputStream xml, int alreadyExistsBehaviour)
      throws RepositoryException;
  
  NodeType getNodeType(InternalQName qname) throws NoSuchNodeTypeException, RepositoryException;
}