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
 * @author Gennady Azarenkov
 * @version $Id$
 */

public class ByteArrayPersistedValueData extends AbstractValueData {
  

  protected byte[] data;

  public ByteArrayPersistedValueData(byte[] data, int orderNumber) {
    super(orderNumber);
    this.data = data;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.AbstractValueData#getAsStream()
   */
  public InputStream getAsStream() throws IOException {
    return new ByteArrayInputStream(data);
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.AbstractValueData#getAsByteArray()
   */
  public byte[] getAsByteArray() throws IllegalStateException {
    return data;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.AbstractValueData#getLength()
   */
  public long getLength() {
    return data.length;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.AbstractValueData#isByteArray()
   */
  public boolean isByteArray() {
    return true;
  }

  
  @Override
  public TransientValueData createTransientCopy() throws RepositoryException {
    try {
      return new TransientValueData(orderNumber, data, 
          null, null, null, -1, null, false);
    } catch (IOException e) {
      throw new RepositoryException(e);
    }
  }

}
