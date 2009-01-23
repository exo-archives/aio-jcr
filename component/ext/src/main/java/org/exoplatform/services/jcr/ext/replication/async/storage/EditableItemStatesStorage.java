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
public class EditableItemStatesStorage<T extends ItemState> extends ItemStatesStorage<T> implements EditableChangesStorage<T> {

  /**
   * ItemStates storage direcory.
   */
  protected final File         storagePath;

  /**
   * Output Stream opened on current ChangesFile.
   */
  protected ObjectOutputStream stream;

  /**
   * Current ChangesFile to store changes.
   */
  protected ChangesFile        currentFile;

  /**
   * Index used as unique name for ChangesFiles. Incremented each time.
   */
  private static Long          index = new Long(0);

  /**
   * Class constructor.
   * 
   * @param storagePath storage Path
   */
  public EditableItemStatesStorage(File storagePath, Member member) {
    super(member);
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
    if (currentFile != null) {
      currentFile.finishWrite();
      currentFile = null;
    }
  }

  /**
   * Creates ChangesFile in ItemStatesStorage.
   * 
   * @return created ChangesFile
   * @throws IOException
   */
  private ChangesFile createChangesFile() throws IOException {
    long timestamp = 0;
    synchronized (index) {
      timestamp = getNextFileId();
    }
    File file = new File(storagePath, Long.toString(timestamp));

    if (file.exists()) {
      throw new IOException("File already exists");
    }

    String crc = ""; // crc is ignored
    return new ChangesFile(file, crc, timestamp);
  }

  private long getNextFileId() {
    long fileId = 0;
    synchronized (index) {
      fileId = index++;
    }
    return fileId;
  }

}
