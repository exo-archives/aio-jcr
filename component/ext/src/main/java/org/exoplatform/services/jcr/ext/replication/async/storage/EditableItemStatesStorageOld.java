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

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.services.jcr.dataflow.ItemState;

/**
 * Created by The eXo Platform SAS. <br/>Date: 30.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class EditableItemStatesStorageOld<T extends ItemState> extends ItemStatesStorage<T> implements
    EditableChangesStorage<T> {

  /**
   * ItemStates storage direcory.
   */
  protected final File         storagePath;
  
  protected final List<ChangesFile> storage = new ArrayList<ChangesFile>();

  /**
   * Output Stream opened on current ChangesFile.
   */
  protected ObjectOutputStream stream;

  /**
   * Current ChangesFile to store changes.
   */
  protected SimpleChangesFile        currentFile;

  /**
   * Index used as unique name for ChangesFiles. Incremented each time.
   */
  private static Long          index = new Long(0);

  /**
   * Class constructor.
   * 
   * @param storagePath
   *          storage Path
   */
  public EditableItemStatesStorageOld(File storagePath, Member member) {
    super(member);
    this.storagePath = storagePath;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ChangesFile[] getChangesFile() {
    try {
      flushFile();
    } catch (IOException e) {
      // TODO
      e.printStackTrace();
    }
    
    return super.getChangesFile();
  }

  /**
   * {@inheritDoc}
   */
  public void add(T change) throws IOException {
    initFile();
    stream.writeObject(change);

    stream.flush();
  }

  /**
   * {@inheritDoc}
   */
  public void addAll(ChangesStorage<T> changes) throws IOException {
    if (changes instanceof ItemStatesStorage) {
      
      flushFile();
      
      for (ChangesFile cf : changes.getChangesFile())
        storage.add(cf);
    } else {
      initFile();
      try {
        for (Iterator<T> chi = changes.getChanges(); chi.hasNext();)
          stream.writeObject(chi.next());
        
        stream.flush();
        
        flushFile();
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

  private void flushFile() throws IOException {
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
    long timestamp;
    synchronized (index) {
      timestamp = index++;
    }
    File file = new File(storagePath, Long.toString(timestamp));

    if (file.exists()) {
      throw new IOException("File already exists " + file.getAbsolutePath());
    }

    String crc = ""; // crc is ignored
    return new SimpleChangesFile(file, crc, timestamp);
  }
}
