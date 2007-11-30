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

package org.exoplatform.services.jcr.datamodel;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: ValueData.java 12843 2007-02-16 09:11:18Z peterit $
 */

public interface ValueData {
  
  /**
   * set number of this value (values should be ordered)
   */
  void setOrderNumber(int orderNum);
  
  /**
   * @return number of this value (values should be ordered)
   */
  int getOrderNumber();
  
  /**
   * @return true if data rendered as byte array, false otherwise
   */
  boolean isByteArray();
  
  /**
   * @return this value data as array of bytes
   * @throws IllegalStateException 
   */
  byte[] getAsByteArray() throws IllegalStateException, IOException;
  
  /**
   * renders this value data as stream of bytes
   * NOTE: client is responsible for closing this stream, else IllegalStateException occurs in close()! 
   * @return this value data as stream of bytes
   * @throws IOException
   */
  InputStream getAsStream() throws IOException;
  
  
  /**
   * Return this data length in bytes
   */
  long getLength();
  

}
