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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * Created by The eXo Platform SAS. <br/>Date: 19.12.2008
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: ChangesFile.java 27525 2009-01-28 00:01:58Z pnedonosko $
 */
public class RandomChangesFile implements ChangesFile {

  public static final String PREFIX                = "ChangesFile";
  public static final String SUFIX                 = "SUFIX";
  
  private static final int   OBJECT_OUT_HEADER_LEN = 4;
  
  /**
   * Check sum to file.
   */
  private final String       crc;

  /**
   * Time stamp to ChangesLog.
   */
  private final long         id;

  private final File         file;

  private RandomAccessFile   fileAccessor;

  private boolean            doTruncate            = false;
  //private FileInputStream    fileInput;

  /**
   * Create ChangesFile with file in directory.
   * 
   * @param crc
   *          constant checksum
   * @param id
   *          changes file id
   * @param directory
   *          path to directory
   */
  public RandomChangesFile(String crc, long id, File directory) throws IOException {
    this.crc = crc;
    this.id = id;

    // create file in directory
    file = new File(directory, Long.toString(this.id));
  }

  /**
   * This constructor used in RemoteExporter
   * @param crc
   * @param id
   */
  public RandomChangesFile(String crc, long id) throws IOException{
    this.crc = crc;
    this.id = id;
    
    // create temporary file
    file = File.createTempFile(PREFIX, SUFIX);
  }

  /**
   * Create ChangesFile with already formed file.
   * 
   * @param file changes file
   * @param crc checksum
   * @param timeStamp time stamp
   * @throws IOException
   */
  public RandomChangesFile(File file, String crc, long id) {
    this.crc = crc;
    this.id = id;
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
   * Return
   * 
   * @return InputStream
   * @throws IOException
   */
  public InputStream getInputStream() throws IOException {
    finishWrite();
    return new FileInputStream(file);
  }

  public OutputStream getOutputStream() throws IOException {
    return new OutputStream() {

      @Override
      public void write(int b) throws IOException {
        checkFileAccessor();
        synchronized (fileAccessor) {
          fileAccessor.write(b);
          trunc();
        }
      }

      public void write(byte b[]) throws IOException {
        checkFileAccessor();
        synchronized (fileAccessor) {
          fileAccessor.write(b);
          trunc();
        }
      }

      public void write(byte b[], int off, int len) throws IOException {
        checkFileAccessor();
        synchronized (fileAccessor) {
          fileAccessor.write(b, off, len);
          trunc();
        }
      }

      private void trunc() throws IOException {
        if (doTruncate) {
          fileAccessor.seek(file.length() - OBJECT_OUT_HEADER_LEN);
          doTruncate = false;
        }
      }

    };
  }

  /**
   * Write data to file.
   * 
   * @param data
   *          byte buffer
   * @param position
   *          to write
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
   * @throws IOException
   *           error on file accessor close.
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
   * @throws IOException
   *           error on file accessor creation.
   */
  private void checkFileAccessor() throws IOException {
    if (fileAccessor == null) {
      fileAccessor = new RandomAccessFile(file, "rw");

      if (file.length() > 0) {
        doTruncate = true;
      }
      fileAccessor.seek(file.length());
    }
  }

  /**
   * Delete file and its file-system storage.
   * 
   * @return boolean, true if delete successful.
   * @see java.io.File.delete()
   * @throws IOException
   *           on error
   */
  public boolean delete() throws IOException {
    finishWrite();
    return file.delete();
  }

  public long getId() {
    return id;
  }
  
  public String toString(){
    return file.getAbsolutePath();
  }

  public long getLength(){
    return file.length();
  }
  
}

