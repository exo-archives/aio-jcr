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

import org.exoplatform.services.jcr.dataflow.ItemState;

/**
 * Created by The eXo Platform SAS. <br/>Date: 30.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class EditableItemStatesStorage<T extends ItemState> extends ItemStatesStorage<T> implements
    EditableChangesStorage<T> {

  protected final File         storagePath;

  protected ObjectOutputStream stream;

  protected ChangesFile        currentFile;
  
  private static volatile long index = 0;

  public EditableItemStatesStorage(File storagePath) {
    this.storagePath = storagePath;
  }

  /**
   * {@inheritDoc}
   */
  public void add(T change) throws IOException {
    initFile();
    stream.writeObject(change);
  }

  /**
   * {@inheritDoc}
   */
  public void addAll(ChangesStorage<T> changes) throws IOException {
    flushFile();

    for (ChangesFile cf : changes.getChangesFile())
      storage.add(cf);
  }

  private void initFile() throws IOException {
    if (currentFile == null) {
      currentFile = createFile();
      this.storage.add(currentFile);
    }
    if(stream == null){
      stream = new ObjectOutputStream(currentFile.getOutputStream());
    }
  }

  private void flushFile() throws IOException {
    if (stream != null) {
      stream.close();
      stream = null;
    }
    if (currentFile != null) {
      currentFile.finishWrite();
      currentFile = null;
    }
  }

  private ChangesFile createFile() throws IOException {
    long timestamp = System.currentTimeMillis();
    try{
      
     //TODO CHANGE ChangesFile naming system!!!!!! 
     Thread.sleep(100);
    }catch(InterruptedException e){
    }
    File file = new File(storagePath, Long.toString(index++)); //timestamp
    
    if (file.exists()){
      throw new IOException("File already exists");
    }
    String crc = ""; // crc is ignored
    return new ChangesFile(file, crc, timestamp);
  }
}
