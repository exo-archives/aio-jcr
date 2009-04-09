/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.impl.storage.value.fs.operations;

import java.io.File;
import java.io.IOException;

import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.storage.value.ValueDataResourceHolder;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date: 03.04.2009
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
@Deprecated
public class WriteValueTransactionable extends ValueFileOperation {

  protected final File      file;

  protected final ValueData value;

  protected File      updatedFile;
  
  protected ValueFileLock      fileLock;

  public WriteValueTransactionable(File file,
                    ValueData value,
                    ValueDataResourceHolder resources,
                    FileCleaner cleaner,
                    File tempDir) throws IOException {
    super(resources, cleaner, tempDir);

    this.file = file;
    this.value = value;
  }

  /**
   * {@inheritDoc}
   */
  public void execute() throws IOException {
    makePerformed();
    
    fileLock = new ValueFileLock(file);
    fileLock.lock();

    if (this.file.exists()) {
      updatedFile = new File(tempDir, IdGenerator.generate() + "-" + file.getName()
                           + TEMP_FILE_EXTENSION);
      // TODO parent file good, but initial cleanup will be difficult
      //updatedFile = new File(file.getParentFile() + File.separator + file.getName()
      //    + TEMP_FILE_EXTENSION);
      writeValue(updatedFile, value);
    } else {
      updatedFile = null;
      writeValue(file, value);
    }
  }

  public void rollback() throws IOException {
    try {
      if (updatedFile != null) {
        // update
        if (updatedFile.exists()) {
          if (!updatedFile.delete()) {
            cleaner.addFile(updatedFile);
            LOG.warn("Rollback warning: Cannot delete temporary file "
                + updatedFile.getAbsolutePath() + ". File added to cleaner.");
          }
        } else
          LOG.warn("Rollback warning: File not exists " + updatedFile.getAbsolutePath());
      } else {
        // add
        if (file.exists()) {
          if (!file.delete())
            throw new IOException("Rollback error: Cannot delete file " + file.getAbsolutePath());
        } else
          LOG.warn("Rollback warning: File not exists " + file.getAbsolutePath());
      }
    } finally {
      fileLock.unlock();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void commit() throws IOException {
    try {
      if (updatedFile != null) {
        // update
        moveFile(updatedFile, file, false);
        if (!file.exists())
          throw new IOException("Commit error: Updated file not exists " + file.getAbsolutePath());
      } else {
        // add
        if (!file.exists())
          throw new IOException("Commit error: File not exists " + file.getAbsolutePath());
      }
    } finally {
      fileLock.unlock();
    }
  }

}
