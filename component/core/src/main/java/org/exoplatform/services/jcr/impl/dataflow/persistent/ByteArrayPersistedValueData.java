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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.impl.dataflow.AbstractValueData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author Gennady Azarenkov
 * @version $Id: ByteArrayPersistedValueData.java 11907 2008-03-13 15:36:21Z ksm $
 */
public class ByteArrayPersistedValueData extends AbstractValueData {

  protected byte[] data;

  /**
   * ByteArrayPersistedValueData constructor.
   * 
   * @param data
   *          byte[]
   * @param orderNumber
   *          int
   */
  public ByteArrayPersistedValueData(byte[] data, int orderNumber) {
    super(orderNumber);
    this.data = data;
  }

  /**
   * {@inheritDoc}
   */
  public InputStream getAsStream() throws IOException {
    return new ByteArrayInputStream(data);
  }

  /**
   * {@inheritDoc}
   */
  public byte[] getAsByteArray() {
    return data;
  }

  /**
   * {@inheritDoc}
   */
  public long getLength() {
    return data.length;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isByteArray() {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TransientValueData createTransientCopy() throws RepositoryException {
    try {
      return new TransientValueData(orderNumber, data, null, null, null, -1, null, false);
    } catch (IOException e) {
      throw new RepositoryException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public boolean isTransient() {
    return false;
  }

}
