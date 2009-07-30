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
package org.exoplatform.services.jcr.impl.dataflow.persistent;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.impl.util.io.SwapFile;

/**
 * Created by The eXo Platform SAS. Implementation of FileStream ValueData secures deleting file in
 * object finalization
 * 
 * @author Gennady Azarenkov
 * @version $Id: CleanableFileStreamValueData.java 11907 2008-03-13 15:36:21Z ksm $
 */

public class CleanableFileStreamValueData extends FileStreamPersistedValueData {

  protected final FileCleaner cleaner;

  public CleanableFileStreamValueData(SwapFile file, int orderNumber, FileCleaner cleaner) throws FileNotFoundException {
    super(file, orderNumber);
    this.cleaner = cleaner;

    // aquire this file
    file.acquire(this);
  }

  /**
   * {@inheritDoc}
   */
  protected void finalize() throws Throwable {
    try {
      // release file
      ((SwapFile) file).release(this);

      if (!file.delete()) {
        cleaner.addFile(file);

        if (log.isDebugEnabled())
          log.debug("Ñould not remove temporary file on finalize: inUse="
              + (((SwapFile) file).inUse()) + ", " + file.getAbsolutePath());
      }
    } finally {
      super.finalize();
    }
  }

  /**
   * {@inheritDoc}
   */
  public TransientValueData createTransientCopy() throws RepositoryException {
    try {
      return new TransientValueData(orderNumber, null, null, file, cleaner, -1, null, true); // was
      // false
    } catch (IOException e) {
      throw new RepositoryException(e);
    }
  }

}
