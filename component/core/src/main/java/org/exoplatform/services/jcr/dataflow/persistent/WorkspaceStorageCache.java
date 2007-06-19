/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.dataflow.persistent;

import java.util.List;

import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPathEntry;

/**
 * Created by The eXo Platform SARL        .
 *
 * Defines storage cache contract 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: WorkspaceStorageCache.java 12843 2007-02-16 09:11:18Z peterit $
 */
public interface WorkspaceStorageCache extends ItemsPersistenceListener {
  
  public static final int PUT_ON_RO_POLICY = 1;
  public static final int PUT_ON_RW_POLICY = 2;

  /**
   * 
   * @param parentUuid
   * @param path
   * @return itemData by parent uuid and item name with index or null if not found
   */
  ItemData get(String parentUuid, QPathEntry name);
  
  /**
   * @param identifier
   * @return ItemData by Identifier or null if not found
   */
  ItemData get(String identifier);
  
  /**
   * @param parent
   * @return child nodes for parent if found; empty list if no items found; null if no items initialized
   */
  List <NodeData> getChildNodes(NodeData parent);
  
  /**
   * @param parent
   * @return child properties for parent if found; empty list if no items found; null if no items initialized
   */
  List <PropertyData> getChildProperties(NodeData parent);
  
  /**
   * Adds (or updates if found) ItemData.  
   * @param data
   */
  void put(ItemData data);
  
  /**
   * Adds (update should not be the case!) list of child nodes. The list can be empty. If list is null the operation is ignored.  
   * @param parent
   * @param childNodes
   */
  void addChildNodes(NodeData parent, List <NodeData> childNodes);
  
  /**
   * Adds (update should not be the case!) list of child properties. The list can be empty. If list is null the operation is ignored.  
   * @param parent
   * @param childNodes
   */
  void addChildProperties(NodeData parent, List <PropertyData> childProperties);
  
  /**
   * Removes data and its children from cache 
   * @param data
   */
  void remove(ItemData data);
  
  /**
   *  
   * @return enabled status flag, if true then cache is enabled
   */
  boolean isEnabled();
    
}
