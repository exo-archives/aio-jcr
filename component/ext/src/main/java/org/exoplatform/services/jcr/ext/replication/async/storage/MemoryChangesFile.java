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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 
 *
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a> 
 * @version $Id: MemoryChangesFile.java 111 2008-11-11 11:11:11Z serg $
 */
public class MemoryChangesFile implements ChangesFile {

  private byte[] buf; 
  
  private byte[] crc;
  
  private long id;
  MemoryChangesFile(byte[] crc, long id, byte[]buf){
    this.buf = buf;
    this.crc = crc;
    this.id = id;
  }
  
  public boolean delete() throws IOException {
    buf = null;
    return true;
  }

  public byte[] getChecksum() {
    return crc;
  }

  public long getId() {
    return id;
  }

  public InputStream getInputStream() throws IOException {
    return new ByteArrayInputStream(buf);
  }

  public long getLength() {
    return buf.length;
  }

  public void validate() throws InvalidChecksumException {
    //do nothing
  }

}
