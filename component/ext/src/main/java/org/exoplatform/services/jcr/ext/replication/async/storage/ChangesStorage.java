/**
 * 
 */
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
package org.exoplatform.services.jcr.ext.replication.async.storage;

import java.io.IOException;
import java.util.List;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Actually ChangesLog impl. But without getAllStates().
 * 
 * <br/>
 * Date: 16.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public interface ChangesStorage<T extends ItemState> {

  /**
   * Return this storage backed ChangesFile(s).
   * 
   * <br/>
   * WARN! Be careful using this method. Each ChangesStorage impl uses different formats of content
   * stored in ChangesFile.
   * 
   * @return array of ChangesFile
   */
  ChangesFile[] getChangesFile();

  /**
   * Dump all chnages info to the String. For DEBUG purpose.
   * 
   * @throws ClassCastException
   * @throws IOException
   * @throws ClassNotFoundException
   * @return String
   */
  String dump() throws ClassCastException, IOException, ClassNotFoundException;

  /**
   * Delete this storage.
   * 
   * @throws IOException
   */
  void delete() throws IOException;

  /**
   * Get sequence of all changes.
   * 
   * @return Collection
   */
  MarkableIterator<T> getChanges() throws IOException, ClassCastException, ClassNotFoundException;

  /**
   * Return changes (ItemState) count.
   * 
   * @return int
   */
  int size() throws IOException, ClassCastException, ClassNotFoundException;

  // ***********************************************

  /**
   * Get last ItemState by Item id.
   * 
   * @param itemIdentifier
   *          String, Item id
   * @return ItemState
   */
  T getItemState(String itemIdentifier);

  /**
   * Get last ItemState by parent and Item name.
   * 
   * @param parentData
   *          NodeData of the parent
   * @param name
   *          QPathEntry, Item name
   * @return ItemState
   */
  T getItemState(NodeData parentData, QPathEntry name);

  // =========== custom ==============

  /**
   * Find next item in sequence begining from current position by criteria.
   * 
   * @param iterator
   *          iterator of the sequence
   * @param identifier
   *          identifier to identify the needed item
   * @return next item in the sequence by criteria.
   */
  T findNextState(MarkableIterator<T> iterator, String identifier) throws IOException,
                                                                  ClassCastException,
                                                                  ClassNotFoundException;

  /**
   * Find item in sequence by criteria.
   * 
   * @param identifier
   *          identifier to identify the needed item
   * @param qpath
   *          qpath to identify the needed item
   * @param state
   *          state to identify the needed item
   * @param nextItems
   *          for result item will be find next item
   * @return item in the sequence by criteria.
   */
  public T findItemState(String identifier, QPath path, int state, List<T> nextItems) throws IOException,
                                                                                     ClassCastException,
                                                                                     ClassNotFoundException;

  /**
   * Find item in sequence by criteria.
   * 
   * @param identifier
   *          identifier to identify the needed item
   * @param qpath
   *          qpath to identify the needed item
   * @param state
   *          state to identify the needed item
   * @return item in the sequence by criteria.
   */
  public T findItemState(String identifier, QPath qpath, int state) throws IOException,
                                                                   ClassCastException,
                                                                   ClassNotFoundException;

  /**
   * Returns list with changes at the rootPath and its descendants.
   * 
   * @param rootPath
   *          root path
   * @return item state at the rootPath and its descendants
   */
  List<T> getChanges(QPath rootPath) throws IOException, ClassCastException, ClassNotFoundException;

  /**
   * Returns list with changes at the rootPath and its descendants.
   * 
   * @param rootPath
   *          root path
   * @param nextItems
   *          for each items in results will be find next item
   * @return item state at the rootPath and its descendants
   */
  List<T> getChanges(QPath rootPath, List<T> nextItems) throws IOException,
                                                       ClassCastException,
                                                       ClassNotFoundException;

  /**
   * Returns list with changes at the rootPath and its descendants.
   * 
   * @param rootPath
   *          root path
   * @param nextItems
   *          for each items in results will be find next item
   * @param updateSeq
   *          for each items in results will be find update subsequence
   * @return item state at the rootPath and its descendants
   */
  public List<T> getChanges(QPath rootPath, List<T> nextItems, List<List<T>> updateSeq) throws IOException,
                                                                                       ClassCastException,
                                                                                       ClassNotFoundException;

  /**
   * Get list items of update subsequence.
   * 
   * @param iterator
   *          iterator of the sequence
   * @param firstState
   *          state from which update subsequence is begining
   * @return list items of update subsequence
   */
  List<T> getUpdateSequence(MarkableIterator<T> iterator, T firstState) throws IOException,
                                                                       ClassCastException,
                                                                       ClassNotFoundException;

  /**
   * Get list items of mxin subsequence.
   * 
   * @param iterator
   *          iterator of the sequence
   * @param firstState
   *          state from which mixin subsequence is begining
   * @return list items of mixin subsequence
   */
  List<T> getMixinSequence(MarkableIterator<T> iterator, T firstState) throws IOException,
                                                                      ClassCastException,
                                                                      ClassNotFoundException;

  /**
   * Get all items that contain changes of versionable uuid property.
   * 
   * @return list of items with changes of versionable uuid property.
   */
  public List<ItemState> getVUChanges() throws IOException,
                                       ClassCastException,
                                       ClassNotFoundException;

  /**
   * Get value of version history property.
   * 
   * @param uuid
   *          parent node uuid
   * @return value of property or null of satisfied property does not found
   */
  public String getVHPropertyValue(String uuid) throws IOException,
                                               ClassCastException,
                                               ClassNotFoundException;

  /**
   * Get unique pathes of all items that contain changes specified items.
   * 
   * @param identifier
   *          item identifier
   * @return list of pathes
   */
  public List<QPath> getUniquePathesByUUID(String identifier) throws IOException,
                                                             ClassCastException,
                                                             ClassNotFoundException;
}
