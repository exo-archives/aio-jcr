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

import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;
import org.exoplatform.services.jcr.impl.Constants;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 10.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class FileSystemChangesStorage implements EditableChangesStorage {

  public static final String PREFIX = "FSPERF";

  public static final String SUFFIX = "FSsuf";

  /*class ItemKey {
    private final String key;

    ItemKey(String itemId) {
      this.key = itemId;
    }

    ItemKey(QPath path) {
      this.key = path.getAsString();
    }*/

  /**
   * {@inheritDoc}
   */
  /* @Override
    public boolean equals(Object obj) {
      return key.equals(obj);
    }*/

  /**
   * {@inheritDoc}
   */
  /* @Override
   public int hashCode() {
     return key.hashCode();
   }
  }*/

  /* class StateLocator {
     private final String logPath;

     private final QPath  path;

     private final String itemId;

     private final int    state;

     StateLocator(String logPath, QPath path, String itemId, int state) {
       this.logPath = logPath;

       // path, id, state used in traversing
       this.path = path;
       this.itemId = itemId;
       this.state = state;
     }*/

  /**
   * Read file and deserialize the state.
   * 
   * @return ItemState
   */
  /*  ItemState getChange() {
      return null; // TODO
    }
  }*/

  class MultiFileIterator<T extends ItemState> implements Iterator<T> {

    private ObjectInputStream       in;

    private final List<ChangesFile> changesFiles;

    private T                       nextItem;

    private int                     currentFileIndex;

    public MultiFileIterator(List<ChangesFile> changesFiles) throws IOException {
      this.changesFiles = changesFiles;
      currentFileIndex = 0;
      try {
        this.in = new ObjectInputStream(this.changesFiles.get(currentFileIndex).getDataStream());
        this.nextItem = readNext();
      } catch (ClassNotFoundException e) {
        // TODO
      } catch (ClassCastException e) {
        // TODO
      }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
      return nextItem != null;
    }

    /**
     * {@inheritDoc}
     */
    public T next() throws NoSuchElementException {
      if (nextItem == null)
        throw new NoSuchElementException();

      T retVal = nextItem;
      try {
        nextItem = readNext();
      } catch (IOException e) {
        throw new NoSuchElementException(e.getMessage());
      } catch (ClassNotFoundException e) {
        throw new NoSuchElementException(e.getMessage());
      } catch (ClassCastException e) {
        throw new NoSuchElementException(e.getMessage());
      }
      return retVal;
    }

    /**
     * {@inheritDoc}
     */
    public void remove() {
      throw new RuntimeException("Remove not implemented");
    }

    @SuppressWarnings("unchecked")
    protected T readNext() throws IOException, ClassNotFoundException, ClassCastException {
      try {
        return (T) in.readObject();
      } catch (EOFException e) {
        // End of list
        in.close();
        if (currentFileIndex == changesFiles.size() - 1) {
          return null;
        } else {
          currentFileIndex++;
          return readNext();
        }
      }
    }
  }

  // protected final LinkedHashMap<ItemKey, StateLocator> index = new LinkedHashMap<ItemKey,
  // StateLocator>();

  // protected final TreeMap<ItemKey, StateLocator> storage = new TreeMap<ItemKey, StateLocator>();
  // // TODO
  // key Comparable
  protected final List<ChangesFile> storage = new ArrayList<ChangesFile>();

  protected final File              storagePath;

  protected final Member            member;

  protected MessageDigest           digest;

  protected File                    currentFile;

  protected ObjectOutputStream      stream;

  public FileSystemChangesStorage(File storagePath, Member member) {
    this.storagePath = storagePath;
    // this.storagePath.mkdirs();
    this.member = member;

    try {
      digest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      // TODO;
      digest = null;
    }
  }

  /**
   * {@inheritDoc}
   */
  public Iterator<ItemState> getChanges() {
    try {
      return new MultiFileIterator<ItemState>(this.storage);
    } catch (IOException e) {
      // TODO
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  public Collection<ItemState> getDescendantsChanges(QPath rootPath,
                                                     boolean onlyNodes,
                                                     boolean unique) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public ItemState getItemState(NodeData parentData, QPathEntry name) {
    throw new RuntimeException("Not implemented");
  }

  /**
   * {@inheritDoc}
   */
  public ItemState getItemState(String itemIdentifier) {
    throw new RuntimeException("Not implemented");
  }

  /**
   * {@inheritDoc}
   */
  public ItemState getNextItemState(ItemState item) {
    Iterator<ItemState> it = getChanges();

    if(it.hasNext()){
      ItemState state =it.next(); 
      if(state.equals(item)){
        return state;
      }
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public ItemState getNextItemStateByUUIDOnUpdate(ItemState startState, String UUID) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj) {
    // TODO Auto-generated method stub
    return super.equals(obj);
  }

  public int getMemberPriority() {
    return member.getPriority();
  }

  /**
   * {@inheritDoc}
   */
  public int findLastState(QPath itemPath) {

    return 0;
  }

  /**
   * {@inheritDoc}
   */
  public ItemState getNextItemStateByIndexOnUpdate(ItemState startState, int prevIndex) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public List<ItemState> getUpdateSequence(ItemState startState) {
    // TODO Auto-generated method stub
    return null;
  }

  public void add(ItemState change) throws IOException {
    initFileForWrite();
    this.stream.writeObject(change);
  }

  public void addAll(SerializedItemStateIterator<ItemState> changes) throws IOException {
    flush();
    storage.add(changes.getChangesFile());
  }

  public Member getMember() {
    return member;
  }

  private void initFileForWrite() throws IOException {
    if (currentFile == null) {
      currentFile = File.createTempFile(PREFIX, SUFFIX, storagePath);
      digest.reset();
      stream = new ObjectOutputStream(new DigestOutputStream(new FileOutputStream(currentFile),
                                                             digest));
    }
  }

  private void flush() throws IOException {
    // wrap current file
    stream.close();

    String crc = new String(digest.digest(), Constants.DEFAULT_ENCODING);

    ChangesFile wrapedFile = new ChangesFile(currentFile, crc, System.currentTimeMillis());

    this.storage.add(wrapedFile);
    currentFile = null;
  }

  /**
   * {@inheritDoc}
   */
  public void delete() throws IOException {
    // TODO Auto-generated method stub

  }

}
