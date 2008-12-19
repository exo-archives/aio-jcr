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
 * @version $Id: ChangesLogFile.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class ChangesLogFile {
  
  /**
   * The canonical full path to file.
   */
  private final String filePath;
  
  /**
   * Check sum to file.
   */
  private final String crc;
  
  /**
   * Time stamp to ChangesLog.
   */
  private final long timeStamp;
  
  public ChangesLogFile(String filePath, String crc, long timeStamp) {
    this.filePath = filePath;
    this.crc = crc;
    this.timeStamp = timeStamp;
  }
  
  /**
   * getFilePath.
   *
   * @return String
   *           return the canonical full path to file.
   */
  public String getFilePath() {
    return filePath;
  }
  
  /**
   * getCRC.
   *
   * @return String
   *           return the check sum to file.
   */
  public String getCRC() {
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
