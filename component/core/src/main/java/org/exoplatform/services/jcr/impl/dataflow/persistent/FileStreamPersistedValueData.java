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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.impl.dataflow.AbstractValueData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author Gennady Azarenkov
 * @version $Id: FileStreamPersistedValueData.java 11907 2008-03-13 15:36:21Z ksm $
 */

public class FileStreamPersistedValueData extends AbstractValueData {

  protected final File    file;

  protected final boolean temp;

  public FileStreamPersistedValueData(File file, int orderNumber, boolean temp) {
    super(orderNumber);
    this.file = file;
    this.temp = temp;
  }

  /**
   * @see org.exoplatform.services.jcr.datamodel.AbstractValueData#getAsStream()
   */
  public InputStream getAsStream() throws IOException {
    return new FileInputStream(file);
  }

  /**
   * @see org.exoplatform.services.jcr.datamodel.AbstractValueData#getAsByteArray()
   */
  public byte[] getAsByteArray() throws IllegalStateException {
    throw new IllegalStateException("It is illegal to call on FileStreamPersistedValueData due to potential lack of memory");
  }

  /**
   * @see org.exoplatform.services.jcr.datamodel.AbstractValueData#getLength()
   */
  public long getLength() {
    return file.length();
  }

  /**
   * @see org.exoplatform.services.jcr.datamodel.AbstractValueData#isByteArray()
   */
  public boolean isByteArray() {
    return false;
  }

  @Override
  public TransientValueData createTransientCopy() throws RepositoryException {
    try {
      return new TransientValueData(orderNumber, null, null, file, null, -1, null, false);
    } catch (IOException e) {
      throw new RepositoryException(e);
    }
  }

  // TODO cleanup
//  protected void finalize() throws Throwable {
//    try {
//      if (temp && !file.delete())
//        log.warn("FilePersistedValueData could not remove temporary file on finalize "
//            + file.getAbsolutePath());
//    } finally {
//      super.finalize();
//    }
//  }
}
