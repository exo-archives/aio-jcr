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

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.ext.replication.async.storage.ItemStatesStorage.FileIterator;

/**
 * Created by The eXo Platform SAS. <br/>Date: 30.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: EditableItemStatesStorage.java 27527 2009-01-28 08:32:30Z serg $
 */
public class BufferedItemStatesStorage<T extends ItemState> extends AbstractChangesStorage<T> implements
    EditableChangesStorage<T> {

  /**
   * Max ChangesLog file size in Kb.
   */
  private static final long         MAX_FILE_SIZE = 32 * 1024 * 1024;

  /**
   * ItemStates storage direcory.
   */
  protected final File              storagePath;
  protected final Member            member;

  protected final List<ChangesFile> storage       = new ArrayList<ChangesFile>();

  /**
   * Output Stream opened on current ChangesFile.
   */
  protected ObjectOutputStream      stream;

  /**
   * Current ChangesFile to store changes.
   */
  protected SimpleChangesFile       currentFile;

  /**
   * Index used as unique name for ChangesFiles. Incremented each time.
   */
  private static Long               index         = new Long(0);

  class MultiFileIterator<S extends ItemState> implements Iterator<S> {

    private final List<ChangesFile> store;

    private ObjectInputStream       in;

    private S                       nextItem;

    private int                     currentFileIndex;

    public MultiFileIterator(List<ChangesFile> store) throws IOException,
        ClassCastException,
        ClassNotFoundException {
      this.store = store;
      if (this.store.size() > 0) {
        currentFileIndex = 0;
        this.in = new ObjectInputStream(this.store.get(currentFileIndex).getInputStream());
        this.nextItem = readNext();
      } else {
        this.in = null;
        this.nextItem = null;
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
    public S next() throws NoSuchElementException {
      if (nextItem == null)
        throw new NoSuchElementException();

      S retVal = nextItem;
      try {
        nextItem = readNext();
      } catch (IOException e) {
        throw new ChangesLogReadException(e.getMessage() + " file: "
            + store.get(currentFileIndex).toString(), e);
      } catch (ClassNotFoundException e) {
        throw new ChangesLogReadException(e.getMessage() + " file: "
            + store.get(currentFileIndex).toString(), e);
      } catch (ClassCastException e) {
        throw new ChangesLogReadException(e.getMessage() + " file: "
            + store.get(currentFileIndex).toString(), e);
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
    protected S readNext() throws IOException, ClassNotFoundException, ClassCastException {
      if (in != null) {
        try {
          return (S) in.readObject();
        } catch (EOFException e) {
          // End of list
          in.close();
          in = null;

          // fetch next
          currentFileIndex++;
          if (currentFileIndex >= store.size()) {
            return null;
          } else {
            in = new ObjectInputStream(store.get(currentFileIndex).getInputStream());
            return readNext();
          }
        }
      } else
        return null;
    }
  }

  /**
   * Class constructor.
   * 
   * @param storagePath storage Path
   */
  public BufferedItemStatesStorage(File storagePath, Member member) {
    this.member = member;
    this.storagePath = storagePath;
  }

  /**
   * {@inheritDoc}
   */
  public ChangesFile[] getChangesFile() {
    try {
      closeFile();
    } catch (IOException e) {
      // TODO
      e.printStackTrace();
    }

    ChangesFile[] files = new ChangesFile[storage.size()];
    storage.toArray(files);
    return files;
  }

  /**
   * {@inheritDoc}
   */
  public void add(T change) throws IOException {
    initFile();
    stream.writeObject(change);

    stream.flush();
    checkFileSize();
  }

  /**
   * {@inheritDoc}
   */
  public void addAll(ChangesStorage<T> changes) throws IOException {
    initFile();
    try {
      for (Iterator<T> chi = changes.getChanges(); chi.hasNext();) {
        stream.writeObject(chi.next());
        stream.flush();
        checkFileSize();
      }

    } catch (final ClassCastException e) {
      throw new IOException(e.getMessage()) {

        /**
         * {@inheritDoc}
         */
        @Override
        public Throwable getCause() {
          return e;
        }
      };

    } catch (final ClassNotFoundException e) {
      throw new IOException(e.getMessage()) {
        /**
         * {@inheritDoc}
         */
        @Override
        public Throwable getCause() {
          return e;
        }
      };
    }

  }

  private void initFile() throws IOException {
    if (currentFile == null) {
      currentFile = createChangesFile();
      this.storage.add(currentFile);
    }
    if (stream == null) {
      stream = new ObjectOutputStream(currentFile.getOutputStream());
    }
  }

  private void closeFile() throws IOException {
    if (stream != null) {
      stream.close();
      stream = null;
    }
    currentFile = null;

  }

  /**
   * Creates ChangesFile in ItemStatesStorage.
   * 
   * @return created ChangesFile
   * @throws IOException
   */
  private SimpleChangesFile createChangesFile() throws IOException {
    long id;
    synchronized (index) {
      id = index++;
    }
    File file = new File(storagePath, Long.toString(id));

    if (file.exists()) {
      throw new IOException("File already exists " + file.getAbsolutePath());
    }

    String crc = ""; // crc is ignored
    return new SimpleChangesFile(file, crc, id);
  }

  private void checkFileSize() throws IOException {
    if (currentFile.getLength() > MAX_FILE_SIZE) {
      // open new file
      closeFile();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Iterator<T> getChanges() throws IOException, ClassCastException, ClassNotFoundException {
    return new MultiFileIterator<T>(storage);
  }

  public Member getMember() {
    
    return member;
  }

  public void delete() throws IOException {
    for (ChangesFile cf : storage)
    cf.delete();
    
  }

  public int size() throws IOException, ClassCastException, ClassNotFoundException {
    Iterator<T> it = getChanges();
    int i = 0;
    while (it.hasNext()) {
      i++;
      it.next();
    }
    return i;
  }

}
