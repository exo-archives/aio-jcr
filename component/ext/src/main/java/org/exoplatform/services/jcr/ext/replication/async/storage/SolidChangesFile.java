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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * Created by The eXo Platform SAS. <br/>Date: 19.12.2008
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: ChangesFile.java 27484 2009-01-27 10:04:11Z serg $
 */
public class SolidChangesFile {

  public static final String PREFIX = "ChangesFile";

  public static final String SUFIX  = "SUFIX";

  /**
   * Check sum to file.
   */
  private final String       crc;

  /**
   * Time stamp to ChangesLog.
   */
  private final long         timeStamp;

  private final File         file;

  private RandomAccessFile   fileAccessor;

  private FileInputStream    fileInput;

  /**
   * Create ChangesFile with temporary file.
   * 
   * @param crc
   * @param timeStamp
   */
  public SolidChangesFile(String crc, long timeStamp) throws IOException {
    this.crc = crc;
    this.timeStamp = timeStamp;

    // create temporary file
    file = File.createTempFile(PREFIX, SUFIX);
  }

  /**
   * Create ChangesFile with file in directory.
   * 
   * @param crc constant checksum
   * @param timeStamp time stamp
   * @param directory path to directory
   */
  public SolidChangesFile(String crc, long timeStamp, String directory) throws IOException {
    this.crc = crc;
    this.timeStamp = timeStamp;

    // create file in directory
    File dir = new File(directory);
    file = new File(dir, Long.toString(this.timeStamp));
  }

  /**
   * Create ChangesFile with already formed file.
   * 
   * @param file changes file
   * @param crc checksum
   * @param timeStamp time stamp
   * @throws IOException
   */
  public SolidChangesFile(File file, String crc, long timeStamp) {
    this.crc = crc;
    this.timeStamp = timeStamp;
    this.file = file;
  }

  /**
   * File checksum.
   * 
   * @return String return the check sum to file.
   */
  public String getChecksum() {
    return crc;
  }

  /**
   * getTimeStamp.
   * 
   * @return long return the time stamp to ChangesLog.
   */
  public long getTimeStamp() {
    return timeStamp;
  }

  /**
   * Return
   * 
   * @return InputStream
   * @throws IOException
   */
  public InputStream getDataStream() throws IOException {
    finishWrite();

    if (fileInput != null)
      fileInput.close();

    return fileInput = new FileInputStream(file);
  }

  public OutputStream getOutputStream() throws IOException {
    return new OutputStream() {

      @Override
      public void write(int b) throws IOException {
        checkFileAccessor();
        synchronized (fileAccessor) {
          fileAccessor.write(b);
        }
      }

      public void write(byte b[]) throws IOException {
        checkFileAccessor();
        synchronized (fileAccessor) {
          fileAccessor.write(b);
        }
      }

      public void write(byte b[], int off, int len) throws IOException {
        checkFileAccessor();
        synchronized (fileAccessor) {
          fileAccessor.write(b, off, len);
        }
      }
    };
  }

  /**
   * Write data to file.
   * 
   * @param data byte buffer
   * @param position to write
   * @throws IOException
   */
  public void writeData(byte[] data, long position) throws IOException {
    checkFileAccessor();
    synchronized (fileAccessor) {
      fileAccessor.seek(position);
      fileAccessor.write(data);
    }
  }

  /**
   * Say internal writer that file write stopped.
   * 
   * @throws IOException error on file accessor close.
   */
  public void finishWrite() throws IOException {
    if (fileAccessor != null) {
      // close writer
      fileAccessor.close();
      fileAccessor = null;
    }
  }

  /**
   * Check is file accessor created. Create if not.
   * 
   * @throws IOException error on file accessor creation.
   */
  private void checkFileAccessor() throws IOException {
    if (fileAccessor == null) {
      fileAccessor = new RandomAccessFile(file, "rwd");
      fileAccessor.seek(file.length());
    }
  }

  /**
   * Delete file and its file-system storage.
   * 
   * @return boolean, true if delete successful.
   * @see java.io.File.delete()
   * @throws IOException on error
   */
  public boolean delete() throws IOException {
    finishWrite();

    if (fileInput != null)
      fileInput.close();

    return file.delete();
  }

  /*
  public boolean moveTo(File dir) throws IOException {
    File dest = new File(dir, Long.toString(getTimeStamp()));
    return file.renameTo(dest);
  }*/

  public String getPath() {
    return file.getAbsolutePath();
  }

  public long length() {
    
    return 0;
  }

}
