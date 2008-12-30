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
package org.exoplatform.services.jcr.ext.replication.async.merge;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/> FOR DEBUG/TESTS ONLY!
 * 
 * <br/>Date: 26.12.2008
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a> 
 * @version $Id$
 */
public class CompositeChangesStorage<T extends ItemState> implements ChangesStorage<ItemState> {

  private final TransactionChangesLog chlog;
  
  private final Member member;
  
  public CompositeChangesStorage(TransactionChangesLog chlog, Member member) {
    this.chlog = chlog;
    this.member = member;
  }
  
  CompositeChangesStorage(TransactionChangesLog chlog) {
    this(chlog, null);
  }
  
  /**
   * {@inheritDoc}
   */
  public int size() {
    return chlog.getSize();
  }
  
  public void addLog(PlainChangesLog log) {
    chlog.addLog(log);
  }
  
  /**
   * {@inheritDoc}
   */
  public int findLastState(QPath itemPath) {
    return chlog.getLastState(itemPath);
  }

  /**
   * {@inheritDoc}
   */
  public Iterator<ItemState> getChanges() {
    return chlog.getAllStates().iterator();
  }

  /**
   * {@inheritDoc}
   */
  public Collection<ItemState> getDescendantChanges(QPath root) {
    throw new RuntimeException("Deprecated");
  }

  /**
   * {@inheritDoc}
   */
  public Collection<ItemState> getDescendantsChanges(QPath rootPath,
                                                     boolean onlyNodes,
                                                     boolean unique) {
    return chlog.getDescendantsChanges(rootPath, onlyNodes, unique);
  }

  /**
   * {@inheritDoc}
   */
  public ItemState getItemState(NodeData parentData, QPathEntry name) {
    return chlog.getItemState(parentData, name);
  }

  /**
   * {@inheritDoc}
   */
  public ItemState getItemState(String itemIdentifier) {
    return chlog.getItemState(itemIdentifier);
  }

  /**
   * {@inheritDoc}
   */
  public Member getMember() {
    return member;
  }

  /**
   * {@inheritDoc}
   */
  public ItemState getNextItemState(ItemState item) {
    return chlog.getNextItemState(item);
  }

  /**
   * {@inheritDoc}
   */
  public ItemState getNextItemStateByIndexOnUpdate(ItemState startState, int prevIndex) {
    return chlog.getNextItemStateByIndexOnUpdate(startState, prevIndex);
  }

  /**
   * {@inheritDoc}
   */
  public ItemState getNextItemStateByUUIDOnUpdate(ItemState startState, String UUID) {
    return chlog.getNextItemStateByUUIDOnUpdate(startState, UUID);
  }

  /**
   * {@inheritDoc}
   */
  public List<ItemState> getUpdateSequence(ItemState startState) {
    return chlog.getUpdateSequence(startState);
  }

  /**
   * {@inheritDoc}
   */
  public void delete() throws IOException {
    // TODO Auto-generated method stub
    
  }

  /**
   * {@inheritDoc}
   */
  public ChangesFile[] getChangesFile() {
    // TODO Auto-generated method stub
    return null;
  }

  
  
}
