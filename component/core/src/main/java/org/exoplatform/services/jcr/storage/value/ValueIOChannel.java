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

import org.exoplatform.services.jcr.datamodel.ValueData;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: ValueIOChannel.java 14100 2008-05-12 10:53:47Z gazarenkov $
 */

public interface ValueIOChannel {

  /**
   * Read Property value.
   * 
   * @param propertyId
   *          - Property ID
   * @param maxBufferSize
   *          - maximum size when value will be read to memory buffer
   * @return ValueData
   * @throws IOException
   *           if error occurs
   */
  ValueData read(String propertyId, int orderNumber, int maxBufferSize) throws IOException;

  /**
   * Add or update Property value.
   * 
   * @param propertyId
   *          - Property ID
   * @param data
   *          - ValueData
   * @throws IOException
   *           if error occurs
   */
  void write(String propertyId, ValueData data) throws IOException;

  /**
   * Delete Property all values.
   * 
   * @param propertyId
   *          - Property ID
   * @throws IOException
   *           if error occurs
   */
  void delete(String propertyId) throws IOException;

  /**
   * Closes channel.
   */
  void close();

  /**
   * Return this value storage id.
   */
  String getStorageId();

  /**
   * Commit channel changes.
   * 
   * @throws IOException
   *           if error occurs
   */
  void commit() throws IOException;

  /**
   * Rollback channel changes.
   * 
   * @throws IOException
   *           if error occurs
   */
  void rollback() throws IOException;
}
