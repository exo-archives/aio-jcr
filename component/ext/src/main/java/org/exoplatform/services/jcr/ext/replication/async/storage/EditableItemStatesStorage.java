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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;
import org.exoplatform.services.jcr.impl.Constants;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 30.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class EditableItemStatesStorage<T extends ItemState> extends ItemStatesStorage<T> implements
    EditableChangesStorage<T> {
  
  public static final String        PREFIX  = "FSPERF";

  public static final String        SUFFIX  = "FSsuf";

  protected final File          storagePath;
  protected ObjectOutputStream      stream;
  
  protected File                    currentFile;

  protected final MessageDigest digest;

  public EditableItemStatesStorage(File storagePath) {
    this.storagePath = storagePath;

    // TODO do we need CRC here?
    MessageDigest d;
    try {
      d = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      // TODO;
      d = null;
    }

    this.digest = d;
  }
    
  /**
   * {@inheritDoc}
   */
  public void add(T change) throws IOException {
    initFile();
    this.stream.writeObject(change);
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
      currentFile = File.createTempFile(PREFIX, SUFFIX, storagePath);

      if (digest != null) {
        digest.reset();

        stream = new ObjectOutputStream(new DigestOutputStream(new FileOutputStream(currentFile),
                                                               digest));
      } else
        stream = new ObjectOutputStream(new FileOutputStream(currentFile));
    }
  }

  private void flushFile() throws IOException {
    stream.close();

    String crc;
    if (digest != null)
      crc = new String(digest.digest(), Constants.DEFAULT_ENCODING);
    else
      crc = "";

    this.storage.add(new ChangesFile(currentFile, crc, System.currentTimeMillis()));

    stream = null;
    currentFile = null;
  }
}
