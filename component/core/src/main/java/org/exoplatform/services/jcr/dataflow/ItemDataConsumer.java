/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.dataflow;

import java.util.List;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.datamodel.InternalQPath;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: ItemDataConsumer.java 12843 2007-02-16 09:11:18Z peterit $
 * 
 * Basic (Level 1) data flow inmemory operations  
 *  
 * Common Rule for Read : If there is some storage in this manager –
 * try to get the data from here first, if not found – call super.someMethod
 */
public interface ItemDataConsumer {

  /**
   * @param path
   * @return data by path
   */
  ItemData getItemData(InternalQPath path) throws RepositoryException;

  /**
   * @param uuid
   * @return data by uuid
   */
  ItemData getItemData(String uuid) throws RepositoryException;

  /**
   * @param parentUUID
   * @return children data 
   */
  List <NodeData> getChildNodesData(NodeData parent) throws RepositoryException;

  /**
   * @param parentUUID
   * @return children data 
   */
  List <PropertyData> getChildPropertiesData(NodeData parent) throws RepositoryException;
  
  /**
   * @param uuid
   * @return
   * @throws RepositoryException
   */
  List <PropertyData> getReferencesData(String uuid) throws RepositoryException;
  
  /**
   * @param path
   * @return AccessControlList for incoming path:
   * (1) if item at path is an AccessControllable Node its ACL is returned
   * (2) if item is Root but not AccessControllable node Defaulr ACL is returned
   * (3) else if ACL inheritance is supported by outlined Consumer implementation traverse path to find nearest AccessControllable ancestor's or Root node's ACL  
   * or null in a case if: 
   * (1) there are no item at path and ACL inheritance is not supported by outlined Consumer implementation
   * (2) item at path is a Property and ACL inheritance is not supported by outlined Consumer implementation  
   * (3) item at path is a not AccessControllable Node nor Root node and ACL inheritance is not supported by outlined Consumer implementation   
   * @throws RepositoryException
   */
  AccessControlList getACL(InternalQPath path) throws RepositoryException;

}
