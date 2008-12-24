/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.async.storage;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 19.12.2008
 *
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a> 
 * @version $Id$
 */
public class ChangesFile {
  
  /**
   * The canonical full path to file.
   */
  private final String name;
  
  /**
   * Check sum to file.
   */
  private final String crc;
  
  /**
   * Time stamp to ChangesLog.
   */
  private final long timeStamp;
  
  public ChangesFile(String name, String crc, long timeStamp) {
    this.name = name;
    this.crc = crc;
    this.timeStamp = timeStamp;
  }
  
  /**
   * Get File name.
   *
   * @return String
   *           return file name.
   */
  public String getName() {
    return name;
  }
  
  /**
   * File checksum.
   *
   * @return String
   *           return the check sum to file.
   */
  public String getChecksum() {
    return crc;
  }
  
  /**
   * getTimeStamp.
   *
   * @return long
   *           return the time stamp to ChangesLog.
   */
  public long getTimeStamp() {
    return timeStamp;
  }
}
