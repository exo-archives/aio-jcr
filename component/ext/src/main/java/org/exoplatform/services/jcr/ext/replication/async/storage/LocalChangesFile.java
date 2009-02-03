/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;


/**
 * Created by The eXo Platform SAS.
 * 
 * DON'T USE IT!
 * 
 * <br/>Date: 
 *
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a> 
 * @version $Id: SimpleChangesFile.java 111 2008-11-11 11:11:11Z serg $
 */
@Deprecated
public class LocalChangesFile implements ChangesFile{

  protected static final Log LOG                   = ExoLogger.getLogger("ext.LocalChangesFile");
  
  /**
   * Check sum to file.
   */
  private final String       crc;

  /**
   * Time stamp to ChangesLog.
   */
  private final long         id;

  private final File         file;
  
  public LocalChangesFile(File file, String crc, long id){
    this.crc =crc;
    this.id = id;
    this.file = file;
  }
  
  public boolean delete() throws IOException {
    return file.delete();
  }

  public String getChecksum() {
    return crc;
  }

  public long getId() {
    return id;
  }

  public InputStream getInputStream() throws IOException {
    return new FileInputStream(file);
  }
  
  public OutputStream getOutputStream() throws IOException{
    return new FileOutputStream(file, file.exists()) {

      final int HEADER_LEN = 4;
      
      final boolean isNew = file.length() <= 0;
      
      int written = 0; 
      
      @Override
      public void write(int b) throws IOException {
        LOG.info("write - b=" + b);
        if (isNew || written >= HEADER_LEN)
          super.write(b);
        else 
          written++;
      }

      public void write(byte b[]) throws IOException {
        LOG.info("write - b[]=" + b + " (" + b.length + ")");
        write(b, 0, b.length);
      }

      public void write(byte b[], int off, int len) throws IOException {
        LOG.info("write - b[]=" + b + " (" + b.length + "), off=" + off + ", len=" + len);
        
        if (isNew || written >= HEADER_LEN)
          super.write(b, off, len);
        else {
          int toskip = len - (HEADER_LEN - written);
          int newlen = len - toskip;
          if (toskip > 0)
            super.write(b, off + toskip, newlen);
          
          written += newlen;
        }
      }
      
    };
  }

  public String toString(){
    return file.getAbsolutePath();
  }

  public long getLength() {
    return file.length();
  }

}
