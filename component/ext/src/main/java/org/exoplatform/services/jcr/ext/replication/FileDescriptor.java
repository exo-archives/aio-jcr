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
package org.exoplatform.services.jcr.ext.replication;

import java.io.File;
import java.io.RandomAccessFile;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id$
 */

public class FileDescriptor implements Comparable<FileDescriptor> {
  /**
   * file. The File object to file.
   */
  private File             file;

  /**
   * randomAccessFile. The RandomAccessFile object to file.
   */
  private RandomAccessFile randomAccessFile;

  /**
   * systemId. The system identification String.
   */
  private final String     systemId;

  /**
   * FileDescriptor constructor.
   * 
   * @param f
   *          the File object
   * @param raf
   *          the RandomAccessFile object
   * @param systemId
   *          The system identification String
   */
  public FileDescriptor(File f, RandomAccessFile raf, String systemId) {
    this.file = f;
    this.randomAccessFile = raf;
    this.systemId = systemId;
  }

  /**
   * getFile.
   * 
   * @return File return the File object
   */
  public File getFile() {
    return file;
  }

  /**
   * getRandomAccessFile.
   * 
   * @return RandomAccessFile return the RandomAccessFile object
   */
  public RandomAccessFile getRandomAccessFile() {
    return randomAccessFile;
  }

  /**
   * getSystemId.
   * 
   * @return String return the system identification String
   */
  public String getSystemId() {
    return systemId;
  }

  /**
   * {@inheritDoc}
   */
  public int compareTo(FileDescriptor o) {
    return file.getName().compareTo(o.getFile().getName());
  }
}
