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

package org.exoplatform.services.jcr.storage.value;

import java.io.IOException;
import java.util.List;

import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.storage.jdbc.ValueReference;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: ValueIOChannel.java 12843 2007-02-16 09:11:18Z peterit $
 */

public interface ValueIOChannel {
  
  /**
   * reads values
   * @param propertyId
   * @param maxBufferSize - maximum size when value will be read to memory buffer
   * @return List of ValueData
   * @throws IOException
   */
  ValueData read(String propertyId, int orderNumber, int maxBufferSize) throws IOException;
  
  /**
   * writes values
   * @param propertyId
   * @param data - list of ValueData
   * @throws IOException
   */
  void write(String propertyId, ValueData data) throws IOException;

  /**
   * deletes values
   * @param propertyId
   * @throws IOException
   */
  boolean delete(String propertyId) throws IOException;

  /**
   * closes channel 
   */
  void close();
  
  /**
   * Return this value storage id. 
   */
  String getStorageId();
}
