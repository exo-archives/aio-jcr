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
import java.util.Iterator;
import java.util.List;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Actually ChangesLog impl. But without getAllStates().
 * 
 * <br/>Date: 16.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public interface ChangesStorage<T extends ItemState> {

  /**
   * Return this storage backed ChangesFile(s).
   * 
   * <br/>WARN! Be careful using this method. Each ChangesStorage impl uses different formats of
   * content stored in ChangesFile.
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
  Iterator<T> getChanges() throws IOException, ClassCastException, ClassNotFoundException;

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
   * TODO can we rely on sequence on log?
   * 
   * getNextItemState.
   * 
   * @param item
   * @return
   */
  T findNextState(ItemState fromState, String identifier) throws IOException,
                                                         ClassCastException,
                                                         ClassNotFoundException;

  /**
   * Tell if state presents in storage.
   * 
   * @param state
   *          ItemState
   * @return boolean
   * @throws IOException
   *           if error
   */
  boolean hasState(ItemState state) throws IOException, ClassCastException, ClassNotFoundException;

  /**
   * Tell if state presents in storage.
   * 
   * @param identifier
   * @param path
   * @param state
   * @return
   * @throws IOException
   * @throws ClassCastException
   * @throws ClassNotFoundException
   */
  boolean hasState(String identifier, QPath path, int state) throws IOException,
                                                            ClassCastException,
                                                            ClassNotFoundException;

  /**
   * Tell if state presents in storage after specified.
   * 
   * @param state
   *          ItemState
   * @return boolean
   * @throws IOException
   *           if error
   */
  public T findNextState(ItemState fromState, String identifier, QPath path, int state) throws IOException,
                                                                                       ClassCastException,
                                                                                       ClassNotFoundException;

  /**
   * findState.
   * 
   * merger2
   * 
   * @param identifier
   * @param path
   * @param state
   * @return
   * @throws IOException
   * @throws ClassCastException
   * @throws ClassNotFoundException
   */
  public T findItemState(String identifier, QPath path, int state) throws IOException,
                                                                  ClassCastException,
                                                                  ClassNotFoundException;

  /**
   * TODO
   * 
   * Return changes for a given path
   * 
   * @param rootPath
   * 
   * @return Collection of ItemState
   */
  List<T> getChanges(QPath rootPath) throws IOException, ClassCastException, ClassNotFoundException;

  /**
   * getUpdateSequence.
   * 
   * @param startState
   *          ItemState
   * @return List of ItemState
   */
  List<T> getUpdateSequence(ItemState firstState) throws IOException,
                                                 ClassCastException,
                                                 ClassNotFoundException;

  /**
   * getMixinSequence.
   * 
   * @param firstState
   * @return
   * @throws IOException
   * @throws ClassCastException
   * @throws ClassNotFoundException
   */
  List<T> getMixinSequence(ItemState firstState) throws IOException,
                                                ClassCastException,
                                                ClassNotFoundException;

  /**
   * findNodeInVS.
   * 
   * @param uuid
   * @return
   * @throws IOException
   * @throws ClassCastException
   * @throws ClassNotFoundException
   */
  public QPath findVSChanges(String uuid) throws IOException,
                                         ClassCastException,
                                         ClassNotFoundException;

  /**
   * findVHProperty.
   * 
   * @param uuid
   * @return
   * @throws IOException
   * @throws ClassCastException
   * @throws ClassNotFoundException
   */
  public String findVHProperty(String uuid) throws IOException,
                                           ClassCastException,
                                           ClassNotFoundException;

  /**
   * getUniquePathesByUUID.
   * 
   * @param identifier
   * @return
   * @throws IOException
   * @throws ClassCastException
   * @throws ClassNotFoundException
   */
  public List<QPath> getUniquePathesByUUID(String identifier) throws IOException,
                                                             ClassCastException,
                                                             ClassNotFoundException;
}
