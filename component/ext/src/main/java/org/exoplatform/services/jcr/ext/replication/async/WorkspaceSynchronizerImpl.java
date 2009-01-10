/**
 * 
 */
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
package org.exoplatform.services.jcr.ext.replication.async;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.jcr.InvalidItemStateException;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.PersistentDataManager;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.LocalStorage;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/>Date: 12.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class WorkspaceSynchronizerImpl implements WorkspaceSynchronizer {
  
  class WraperList<T extends ItemState> implements List<ItemState>{

    private ChangesStorage<ItemState> storage;
    public WraperList(ChangesStorage<ItemState> storage){
      this.storage = storage;
    }
    
    public boolean add(ItemState o) {
      // TODO Auto-generated method stub
      return false;
    }

    public void add(int index, ItemState element) {
      // TODO Auto-generated method stub
    }

    public boolean addAll(Collection<? extends ItemState> c) {
      // TODO Auto-generated method stub
      return false;
    }

    public boolean addAll(int index, Collection<? extends ItemState> c) {
      // TODO Auto-generated method stub
      return false;
    }

    public void clear() {
      // TODO Auto-generated method stub
    }

    public boolean contains(Object o) {
      // TODO Auto-generated method stub
      return false;
    }

    public boolean containsAll(Collection<?> c) {
      // TODO Auto-generated method stub
      return false;
    }

    public ItemState get(int index) {
      // TODO Auto-generated method stub
      return null;
    }

    public int indexOf(Object o) {
      // TODO Auto-generated method stub
      return 0;
    }

    public boolean isEmpty() {
      // TODO Auto-generated method stub
      return false;
    }

    public Iterator<ItemState> iterator() {
      try{
        return this.storage.getChanges();
      }catch(IOException e){
        //TODO
        return null;
      }
    }

    public int lastIndexOf(Object o) {
      // TODO Auto-generated method stub
      return 0;
    }

    public ListIterator<ItemState> listIterator() {
      // TODO Auto-generated method stub
      return null;
    }

    public ListIterator<ItemState> listIterator(int index) {
      // TODO Auto-generated method stub
      return null;
    }

    public boolean remove(Object o) {
      // TODO Auto-generated method stub
      return false;
    }

    public ItemState remove(int index) {
      // TODO Auto-generated method stub
      return null;
    }

    public boolean removeAll(Collection<?> c) {
      // TODO Auto-generated method stub
      return false;
    }

    public boolean retainAll(Collection<?> c) {
      // TODO Auto-generated method stub
      return false;
    }

    public ItemState set(int index, ItemState element) {
      // TODO Auto-generated method stub
      return null;
    }

    public int size() {
      // TODO Auto-generated method stub
      return 0;
    }

    public List<ItemState> subList(int fromIndex, int toIndex) {
      // TODO Auto-generated method stub
      return null;
    }

    public Object[] toArray() {
      // TODO Auto-generated method stub
      return null;
    }

    public <T> T[] toArray(T[] a) {
      // TODO Auto-generated method stub
      return null;
    }
    
  }
  
  
  class WraperChangesLog implements ItemStateChangesLog{

    private ChangesStorage<ItemState> storage;
    public WraperChangesLog(ChangesStorage<ItemState> synchronizedChanges){
      storage = synchronizedChanges;
    }
    
    public String dump() {
      // TODO Auto-generated method stub
      return null;
    }

    public List<ItemState> getAllStates() {
      return new WraperList<ItemState>(storage);
    }

    public int getSize() {
      // TODO Auto-generated method stub
      return 0;
    }
    
  }
  
  private static final Log              LOG = ExoLogger.getLogger("ext.WorkspaceSynchronizerImpl");

  protected final LocalStorage          storage;

  protected final PersistentDataManager workspace;

  public WorkspaceSynchronizerImpl(PersistentDataManager workspace, LocalStorage storage) {
    this.storage = storage;
    this.workspace = workspace;
  }

  /**
   * Return local changes.<br/> 1. to a merger<br/> 2. to a receiver
   * 
   * @return ChangesStorage
   */
  public ChangesStorage<ItemState> getLocalChanges() {
    return storage.getLocalChanges();
  }

  /**
   * {@inheritDoc}
   */
  public void save(ChangesStorage<ItemState> synchronizedChanges) {
    // TODO save to Workspace data manager
    
    
    

    LOG.info("WorkspaceSynchronizer.save " + synchronizedChanges.getMember());

    ItemStateChangesLog changes = new WraperChangesLog(synchronizedChanges);

    // TODO for demo
    try {
      //for (Iterator<ItemState> iter = synchronizedChanges.getChanges(); iter.hasNext();)
        //changes.add(iter.next());

      workspace.save(changes);
    } catch (InvalidItemStateException e) {
      // TODO message fix
      LOG.info("WorkspaceSynchronizer.save error " + e, e);
    } catch (UnsupportedOperationException e) {
      // TODO message fix
      LOG.info("WorkspaceSynchronizer.save error " + e, e);
    } catch (RepositoryException e) {
      // TODO message fix
      LOG.info("WorkspaceSynchronizer.save error " + e, e);
    }// catch (IOException e) {
      // TODO message fix
     // LOG.info("WorkspaceSynchronizer.save error " + e, e);
    //}
  }

}
