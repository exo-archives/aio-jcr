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
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: FileDescriptor.java 111 2008-11-11 11:11:11Z rainf0x $
 */

public class ChangesFile implements Comparable<ChangesFile> {
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
   * Total packet count;
   */
  private final long       totalPacketCount;

  private long             count = 0;

  /**
   * FileDescriptor constructor.
   * 
   * @param f the File object
   * @param systemId The system identification String
   */
  public ChangesFile(File f, String systemId, long totalPacketCount) {
    this.file = f;
    this.systemId = systemId;
    this.totalPacketCount = totalPacketCount;
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
  public int compareTo(ChangesFile o) {
    return file.getName().compareTo(o.getFile().getName());
  }

  /**
   * Write data to file by offset.
   * 
   * @param offset - offset in file to store data.
   * @param data - byte[].
   * @throws IOException if IO exception occurs.
   */
  public synchronized  void write(long offset, byte[] data) throws IOException {
    if (randomAccessFile == null) {
      randomAccessFile = new RandomAccessFile(file, "rw");
      if (file.length() != 0) {
        System.out.println(" ERROR!!!!  ------------------------ file size is" + file.length());
      }
      // randomAccessFile.seek(file.length());
    }

    randomAccessFile.seek(offset);
    randomAccessFile.write(data);

   // System.out.println(" WRITE " + file.getName() + " off=" + offset + " len=" + data.length
    //    + " c=" + count + " tc=" + totalPacketCount);
    count++;
    if (isStored()) {
      randomAccessFile.close();
    }

  }

  /**
   * Is file complete.
   * 
   * @return True if file completely written.
   */
  public boolean isStored() {
    return (count == totalPacketCount);
  }

}
