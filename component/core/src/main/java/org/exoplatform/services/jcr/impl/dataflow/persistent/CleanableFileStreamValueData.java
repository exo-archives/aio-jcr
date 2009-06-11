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

  /**
   * CleanableFileStreamValueData  constructor.
   *
   * @param file SwapFile
   * @param orderNumber int 
   * @param cleaner FileCleaner
   */
  public CleanableFileStreamValueData(SwapFile file, int orderNumber, FileCleaner cleaner) {
    super(file, orderNumber);
    this.cleaner = cleaner;
  }

  /**
   * {@inheritDoc}
   */
  protected void finalize() throws Throwable {
    try {
      if (!file.delete()) {
        cleaner.addFile(file);

        log.warn("CleanableFileStreamValueData: could not remove temporary file on finalize "
            + file.getAbsolutePath());
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
      // TODO cleanup
      //return new TransientValueData(orderNumber, null, null, file, cleaner, -1, null, false);
      return new FileStreamTransientValueData(file, orderNumber);
    } catch (IOException e) {
      throw new RepositoryException(e);
    }
  }
}
