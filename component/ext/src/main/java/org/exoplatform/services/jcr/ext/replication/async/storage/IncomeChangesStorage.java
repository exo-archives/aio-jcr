/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id$
 */
public class IncomeChangesStorage<T extends ItemState> implements MemberChangesStorage<T> {

  protected static final Log        LOG = ExoLogger.getLogger("jcr.IncomeChangesStorage");

  /**
   * Income storage (warraped).
   */
  protected final ChangesStorage<T> storage;

  /**
   * Storage owner member info.
   */
  protected final Member            member;

  public IncomeChangesStorage(ChangesStorage<T> income, Member member) {
    this.storage = income;
    this.member = member;
  }

  /**
   * {@inheritDoc}
   */
  public Member getMember() {
    return member;
  }

  /**
   * @throws IOException
   * @see org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogStorage#delete()
   */
  public void delete() throws IOException {
    storage.delete();
  }

  /**
   * {@inheritDoc}
   */
  public String dump() throws ClassCastException, IOException, ClassNotFoundException {
    return storage.dump();
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  public boolean equals(Object obj) {
    if (obj instanceof IncomeChangesStorage)
      return storage.equals(obj) && member.equals(((IncomeChangesStorage<T>) obj).member);
    else
      return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @throws ClassNotFoundException
   * @throws ClassCastException
   */
  public int findLastState(QPath itemPath) throws IOException,
                                          ClassCastException,
                                          ClassNotFoundException {
    return storage.findLastState(itemPath);
  }

  public T findNextState(ItemState fromState, String identifier, QPath path, int state) throws IOException,
                                                                                       ClassCastException,
                                                                                       ClassNotFoundException {
    return storage.findNextState(fromState, identifier, path, state);
  }

  /**
   * @param fromState
   * @param identifier
   * @param path
   * @return
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws ClassCastException
   * @see org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogStorage#findNextState(org.exoplatform.services.jcr.dataflow.ItemState,
   *      java.lang.String, org.exoplatform.services.jcr.datamodel.QPath)
   */
  public T findNextState(ItemState fromState, String identifier, QPath path) throws IOException,
                                                                            ClassCastException,
                                                                            ClassNotFoundException {
    return storage.findNextState(fromState, identifier, path);
  }

  /**
   * @param fromState
   * @param identifier
   * @return
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws ClassCastException
   * @see org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogStorage#findNextState(org.exoplatform.services.jcr.dataflow.ItemState,
   *      java.lang.String)
   */
  public T findNextState(ItemState fromState, String identifier) throws IOException,
                                                                ClassCastException,
                                                                ClassNotFoundException {
    return storage.findNextState(fromState, identifier);
  }

  /**
   * @param toState
   * @param path
   * @param state
   * @return
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws ClassCastException
   * @see org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogStorage#findPrevState(org.exoplatform.services.jcr.dataflow.ItemState,
   *      org.exoplatform.services.jcr.datamodel.QPath, int)
   */
  public T findPrevState(ItemState toState, QPath path, int state) throws IOException,
                                                                  ClassCastException,
                                                                  ClassNotFoundException {
    return storage.findPrevState(toState, path, state);
  }

  /**
   * @param toState
   * @param identifier
   * @param path
   * @param state
   * @return
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws ClassCastException
   * @see org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogStorage#findPrevState(org.exoplatform.services.jcr.dataflow.ItemState,
   *      java.lang.String, org.exoplatform.services.jcr.datamodel.QPath, int)
   */
  public T findPrevState(ItemState toState, String identifier, QPath path, int state) throws IOException,
                                                                                     ClassCastException,
                                                                                     ClassNotFoundException {
    return storage.findPrevState(toState, identifier, path, state);
  }

  /**
   * @return
   * @throws IOException
   * @throws ClassCastException
   * @throws ClassNotFoundException
   * @see org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogStorage#getChanges()
   */
  public Iterator<T> getChanges() throws IOException, ClassCastException, ClassNotFoundException {
    return storage.getChanges();
  }

  /**
   * @param firstState
   * @param rootPath
   * @param unique
   * @return
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws ClassCastException
   * @see org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogStorage#getChanges(org.exoplatform.services.jcr.dataflow.ItemState,
   *      org.exoplatform.services.jcr.datamodel.QPath, boolean)
   */
  public List<T> getChanges(ItemState firstState, QPath rootPath, boolean unique) throws IOException,
                                                                                 ClassCastException,
                                                                                 ClassNotFoundException {
    return storage.getChanges(firstState, rootPath, unique);
  }

  /**
   * @param firstState
   * @param rootPath
   * @return
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws ClassCastException
   * @see org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogStorage#getChanges(org.exoplatform.services.jcr.dataflow.ItemState,
   *      org.exoplatform.services.jcr.datamodel.QPath)
   */
  public List<T> getChanges(ItemState firstState, QPath rootPath) throws IOException,
                                                                 ClassCastException,
                                                                 ClassNotFoundException {
    return storage.getChanges(firstState, rootPath);
  }

  /**
   * @return
   * @see org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogStorage#getChangesFile()
   */
  public ChangesFile[] getChangesFile() {
    return storage.getChangesFile();
  }

  /**
   * @param firstState
   * @param rootPath
   * @param unique
   * @return
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws ClassCastException
   * @see org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogStorage#getDescendantsChanges(org.exoplatform.services.jcr.dataflow.ItemState,
   *      org.exoplatform.services.jcr.datamodel.QPath, boolean)
   */
  public List<T> getDescendantsChanges(ItemState firstState, QPath rootPath, boolean unique) throws IOException,
                                                                                            ClassCastException,
                                                                                            ClassNotFoundException {
    return storage.getDescendantsChanges(firstState, rootPath, unique);
  }

  /**
   * @param parentData
   * @param name
   * @return
   * @see org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogStorage#getItemState(org.exoplatform.services.jcr.datamodel.NodeData,
   *      org.exoplatform.services.jcr.datamodel.QPathEntry)
   */
  public T getItemState(NodeData parentData, QPathEntry name) {
    return storage.getItemState(parentData, name);
  }

  /**
   * @param itemIdentifier
   * @return
   * @see org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogStorage#getItemState(java.lang.String)
   */
  public T getItemState(String itemIdentifier) {
    return storage.getItemState(itemIdentifier);
  }

  /**
   * @param startState
   * @return
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws ClassCastException
   * @see org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogStorage#getMixinSequence(org.exoplatform.services.jcr.dataflow.ItemState)
   */
  public List<T> getMixinSequence(ItemState startState) throws IOException,
                                                       ClassCastException,
                                                       ClassNotFoundException {
    return storage.getMixinSequence(startState);
  }

  /**
   * @param fromState
   * @param prevIndex
   * @return
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws ClassCastException
   * @see org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogStorage#getNextItemStateByIndexOnUpdate(org.exoplatform.services.jcr.dataflow.ItemState,
   *      int)
   */
  public T getNextItemStateByIndexOnUpdate(ItemState fromState, int prevIndex) throws IOException,
                                                                              ClassCastException,
                                                                              ClassNotFoundException {
    return storage.getNextItemStateByIndexOnUpdate(fromState, prevIndex);
  }

  /**
   * @param fromState
   * @param UUID
   * @return
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws ClassCastException
   * @see org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogStorage#getNextItemStateByUUIDOnUpdate(org.exoplatform.services.jcr.dataflow.ItemState,
   *      java.lang.String)
   */
  public T getNextItemStateByUUIDOnUpdate(ItemState fromState, String UUID) throws IOException,
                                                                           ClassCastException,
                                                                           ClassNotFoundException {
    return storage.getNextItemStateByUUIDOnUpdate(fromState, UUID);
  }

  /**
   * @param startState
   * @return
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws ClassCastException
   * @see org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogStorage#getRenameSequence(org.exoplatform.services.jcr.dataflow.ItemState)
   */
  public List<T> getRenameSequence(ItemState startState) throws IOException,
                                                        ClassCastException,
                                                        ClassNotFoundException {
    return storage.getRenameSequence(startState);
  }

  /**
   * @param firstState
   * @return
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws ClassCastException
   * @see org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogStorage#getUpdateSequence(org.exoplatform.services.jcr.dataflow.ItemState)
   */
  public List<T> getUpdateSequence(ItemState firstState) throws IOException,
                                                        ClassCastException,
                                                        ClassNotFoundException {
    return storage.getUpdateSequence(firstState);
  }

  /**
   * @return
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return storage.hashCode();
  }

  /**
   * @param state
   * @return
   * @throws IOException
   * @throws ClassCastException
   * @throws ClassNotFoundException
   * @see org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogStorage#hasState(org.exoplatform.services.jcr.dataflow.ItemState)
   */
  public boolean hasState(ItemState state) throws IOException,
                                          ClassCastException,
                                          ClassNotFoundException {
    return storage.hasState(state);
  }

  /**
   * @param identifier
   * @param path
   * @param state
   * @return
   * @throws ClassNotFoundException
   * @throws IOException
   * @throws ClassCastException
   * @see org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogStorage#hasState(java.lang.String,
   *      org.exoplatform.services.jcr.datamodel.QPath, int)
   */
  public boolean hasState(String identifier, QPath path, int state) throws ClassCastException,
                                                                   IOException,
                                                                   ClassNotFoundException {
    return storage.hasState(identifier, path, state);
  }

  /**
   * @return
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws ClassCastException
   * @see org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogStorage#size()
   */
  public int size() throws IOException, ClassCastException, ClassNotFoundException {
    return storage.size();
  }

  /**
   * @return
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return storage.toString();
  }

}
