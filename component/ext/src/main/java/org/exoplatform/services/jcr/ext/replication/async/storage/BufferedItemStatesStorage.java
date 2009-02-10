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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/>Date: 30.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: EditableItemStatesStorage.java 27527 2009-01-28 08:32:30Z serg $
 */
public class BufferedItemStatesStorage<T extends ItemState> extends AbstractChangesStorage<T>
    implements EditableChangesStorage<T> {

  private static final Log         LOG             = ExoLogger.getLogger("ext.BufferedItemStatesStorage");

  /**
   * Max ChangesLog file size in Kb.
   */
  private static final long        MAX_BUFFER_SIZE = 16 * 1024 * 1024;

  /**
   * ItemStates storage directory.
   */
  protected final File             storagePath;

  protected final Member           member;

  protected final long             maxBufferSize;

  protected final ResourcesHolder  resHolder;

  /**
   * Index used as unique name for ChangesFiles. Incremented each time.
   */
  private static Long              index           = new Long(0);

  /**
   * Output Stream opened on current ChangesFile or ByteArray.
   */
  protected ObjectOutputStream     currentStream;

  /**
   * Current ChangesFile to store changes.
   */
  protected EditableChangesFile    currentFile;

  /**
   * Current byte array to store changes.
   */
  protected BAOutputStream         currentByteArray;

  /**
   * Internal cache.
   */
  protected SoftReference<List<T>> cache           = new SoftReference<List<T>>(null);

  /**
   * Cache index. Used as value for cache invalidation. If the value equals -1, the cache is
   * invalid.
   */
  protected int                    cacheIndex      = -1;

  class ArrayOrFileOutputStream extends OutputStream {

    private OutputStream currentOut;

    public ArrayOrFileOutputStream() throws IOException {
      if (currentFile == null) {
        // create bytearrayoutStream;
        currentByteArray = new BAOutputStream();
        currentOut = currentByteArray;
      } else {
        throw new StorageIOException("File exists but Object outputStream allready closed.");
      }
    }

    @Override
    public void write(int b) throws IOException {
      currentOut.write(b);
      currentOut.flush();
      if (currentByteArray != null && (currentByteArray.size() > maxBufferSize)) {
        changeBufferToFile();
      }
    }

    public void write(byte b[]) throws IOException {
      currentOut.write(b);
      currentOut.flush();
      if (currentByteArray != null && (currentByteArray.size() > maxBufferSize)) {
        changeBufferToFile();
      }
    }

    public void write(byte b[], int off, int len) throws IOException {
      currentOut.write(b, off, len);
      currentOut.flush();
      if (currentByteArray != null && (currentByteArray.size() > maxBufferSize)) {
        changeBufferToFile();
      }
    }

    public void close() throws IOException {
      currentOut.close();
    }

    private void changeBufferToFile() throws IOException {

      if (currentFile == null) {
        currentFile = createChangesFile();

        currentOut = currentFile.getOutputStream();

        // move changes from byte array to file
        currentOut.write(currentByteArray.getBuf(), 0, currentByteArray.size());
        currentByteArray.close();
        currentByteArray = null;
      } else {
        throw new StorageIOException("File exists.");
      }
    }
  }

  class BAOutputStream extends ByteArrayOutputStream {

    public BAOutputStream() {
      super();
    }

    public byte[] getBuf() {
      return this.buf;
    }
  }

  class ItemStateIterator<S extends ItemState> implements Iterator<S> {

    private ObjectInputStream in = null;

    private S                 nextItem;

    public ItemStateIterator() throws IOException, ClassCastException, ClassNotFoundException {
      if (currentFile != null) {
        in = new ObjectInputStream(currentFile.getInputStream());
      } else if (currentByteArray != null) {
        in = new ObjectInputStream(new ByteArrayInputStream(currentByteArray.getBuf(),
                                                            0,
                                                            currentByteArray.size()));
      }
      nextItem = readNext();
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
    public S next() {
      if (nextItem == null)
        throw new NoSuchElementException();

      S retVal = nextItem;
      try {
        nextItem = readNext();
      } catch (IOException e) {
        throw new StorageRuntimeException(e.getMessage()
                                              + (currentFile != null ? (" file: ["
                                                  + currentFile.toString() + "]") : " byte array"),
                                          e);
      } catch (ClassNotFoundException e) {
        throw new StorageRuntimeException(e.getMessage()
                                              + (currentFile != null ? (" file: ["
                                                  + currentFile.toString() + "]") : " byte array"),
                                          e);
      } catch (ClassCastException e) {
        throw new StorageRuntimeException(e.getMessage()
                                              + (currentFile != null ? (" file: ["
                                                  + currentFile.toString() + "]") : " byte array"),
                                          e);
      }
      return retVal;
    }

    /**
     * {@inheritDoc}
     */
    public void remove() {
      throw new RuntimeException("Remove not allowed!");

    }

    @SuppressWarnings("unchecked")
    private S readNext() throws IOException, ClassNotFoundException, ClassCastException {
      if (in != null) {
        try {
          return (S) in.readObject();
        } catch (EOFException e) {
          in.close();
          in = null;
          // End of list
          return null;
        } catch (Throwable e) {
          in.close();
          in = null;
          throw new StorageIOException(e.getMessage(), e);
        }
      } else
        return null;
    }

  }

  /**
   * Class constructor.
   * 
   * @param storagePath
   *          storage Path
   */
  public BufferedItemStatesStorage(File storagePath, Member member, ResourcesHolder resHolder) {
    this.member = member;
    this.storagePath = storagePath;
    this.maxBufferSize = MAX_BUFFER_SIZE;
    this.resHolder = resHolder;
  }

  /**
   * Class constructor.
   * 
   * FOR TESTS!
   * 
   * @param storagePath
   *          storage Path
   */
  public BufferedItemStatesStorage(File storagePath,
                                   Member member,
                                   long maxBuffer,
                                   ResourcesHolder resHolder) {
    this.member = member;
    this.storagePath = storagePath;
    this.maxBufferSize = maxBuffer;
    this.resHolder = resHolder;
  }

  /**
   * {@inheritDoc}
   */
  public ChangesFile[] getChangesFile() {
    try {
      closeObjectOutputStream();
    } catch (IOException e) {
      throw new StorageRuntimeException(e.getMessage()
          + (currentFile != null ? (" file: [" + currentFile.toString() + "]") : " byte array"), e);
    }

    if (currentFile != null) {
      return new ChangesFile[] { currentFile };
    } else if (currentByteArray != null) {
      long id;
      synchronized (index) {
        id = index++;
      }
      return new ChangesFile[] { new MemoryChangesFile(new byte[] {},
                                                       id,
                                                       currentByteArray.toByteArray()) };
    } else {
      return new ChangesFile[] {};
    }
  }

  /**
   * {@inheritDoc}
   */
  public void add(T change) throws IOException {
    initOutputStream();
    currentStream.writeObject(change);
    currentStream.flush();

    // cache
    List<T> list = cache.get();
    if (list == null) {
      if (cacheIndex >= 0)
        // invalid cache, i.e. ref was enqueued by GC - will not cache this change
        return;

      // initial cache creation
      cache = new SoftReference<List<T>>(list = new ArrayList<T>());
    }

    list.add(change);
    cacheIndex++;
  }

  /**
   * {@inheritDoc}
   */
  public void addAll(ChangesStorage<T> changes) throws IOException {
    initOutputStream();
    try {
      List<T> list = cache.get();
      if (list == null) {
        if (cacheIndex == -1)
          // initial cache creation
          cache = new SoftReference<List<T>>(list = new ArrayList<T>());
        // else it's invalid cache, i.e. soft ref was enqueued by GC - will not cache this changes
      }

      for (Iterator<T> chi = changes.getChanges(); chi.hasNext();) {
        T change = chi.next();

        currentStream.writeObject(change);

        // caching
        if (list != null) {
          list.add(change);
          cacheIndex++;
        }
      }

      currentStream.flush();
    } catch (final ClassCastException e) {
      throw new StorageIOException(e.getMessage(), e);
    } catch (final ClassNotFoundException e) {
      throw new StorageIOException(e.getMessage(), e);
    }
  }

  private void initOutputStream() throws IOException {
    if (currentStream == null) {
      currentStream = new ObjectOutputStream(new ArrayOrFileOutputStream());
    }
  }

  private void closeObjectOutputStream() throws IOException {
    if (currentStream != null) {
      currentStream.close();
      currentStream = null;
    }
  }

  /**
   * Creates ChangesFile in ItemStatesStorage.
   * 
   * @return created ChangesFile
   * @throws IOException
   */
  private EditableChangesFile createChangesFile() throws IOException {
    long id;
    synchronized (index) {
      id = index++;
    }
    File file = new File(storagePath, Long.toString(id));

    if (file.exists()) {
      throw new IOException("File already exists " + file.getAbsolutePath());
    }

    byte[] crc = new byte[] {}; // crc is ignored
    return new SimpleOutputChangesFile(file, crc, id, resHolder);
  }

  /**
   * {@inheritDoc}
   */
  public Iterator<T> getChanges() throws IOException, ClassCastException, ClassNotFoundException {
    if (currentStream != null)
      currentStream.flush();

    List<T> list = cache.get();
    if (list == null) {
      // read all in cache
      list = new ArrayList<T>();
      for (Iterator<T> iter = new ItemStateIterator<T>(); iter.hasNext();)
        list.add(iter.next());

      cache = new SoftReference<List<T>>(list);
    }

    return new ReadOnlyIterator<T>(list.iterator());
  }

  public Member getMember() {
    return member;
  }

  public void delete() throws IOException {
    List<T> list = cache.get();
    if (list != null) {
      list.clear();
      cache.clear();
    }

    if (currentFile != null)
      currentFile.delete();
  }

  public int size() throws IOException, ClassCastException, ClassNotFoundException {
    Iterator<T> it = getChanges(); // read all in cache

    List<T> list = cache.get();
    if (list != null) {
      return list.size();
    } else {
      int i = 0;
      while (it.hasNext()) {
        i++;
        it.next();
      }
      return i;
    }
  }

}
