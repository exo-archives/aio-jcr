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
 * @version $Id$
 */
public class FileStreamPersistedValueData extends AbstractValueData {

  protected final File file;

  public FileStreamPersistedValueData(File file, int orderNumber) {
    super(orderNumber);
    this.file = file;
  }

  /**
   * {@inheritDoc}
   */
  public InputStream getAsStream() throws IOException {
    return new FileInputStream(file);
  }

  /**
   * {@inheritDoc}
   */
  public byte[] getAsByteArray() throws IllegalStateException {
    throw new IllegalStateException("It is illegal to call on FileStreamPersistedValueData due to potential lack of memory");
  }

  /**
   * {@inheritDoc}
   */
  public long getLength() {
    return file.length();
  }

  /**
   * {@inheritDoc}
   */
  public boolean isByteArray() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TransientValueData createTransientCopy() throws RepositoryException {
    try {
      return new TransientValueData(orderNumber, null, null, file, null, -1, null, false);
    } catch (IOException e) {
      throw new RepositoryException(e);
    }
  }
}
