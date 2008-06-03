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
package org.exoplatform.services.jcr.config;


/**
 * Created by The eXo Platform SAS.
 * @author Gennady Azarenkov
 * @version $Id: BinarySwapEntry.java 11907 2008-03-13 15:36:21Z ksm $
 */

public class BinarySwapEntry {
  
  private static final String DEFAULT_SIZE = "200k";
  private static final String DEFAULT_DIRECTORY = System.getProperty("java.io.tmpdir");
  
  private String maxBufferSize;
  
  private String directoryPath;
  
  public BinarySwapEntry() {
    super();
  }

  public String getDirectoryPath() {
    if(directoryPath == null)
      directoryPath = DEFAULT_DIRECTORY;
    return directoryPath;

  }

  public void setDirectoryPath(String directory) {
    this.directoryPath = directory;
  }

  public String getMaxBufferSize() {
    if(maxBufferSize == null)
      maxBufferSize = DEFAULT_SIZE;
    return maxBufferSize;
  }

  public void setMaxBufferSize(String maxBufferSize) {
    this.maxBufferSize = maxBufferSize;
  }

}
