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

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: SimpleChangesFile.java 111 2008-11-11 11:11:11Z serg $
 */
public class SimpleChangesFile implements ChangesFile {

  /**
   * Check sum to file.
   */
  protected final String crc;

  /**
   * Time stamp to ChangesLog.
   */
  protected final long   id;

  protected final File   file;
  
  protected final ResourcesHolder resHolder;

  public SimpleChangesFile(File file, String crc, long id, ResourcesHolder resHolder) {
    this.crc = crc;
    this.id = id;
    this.file = file;
    this.resHolder = resHolder;
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
    InputStream is = new FileInputStream(file);
    this.resHolder.add(is);
    return is;
  }

  public String toString() {
    return file.getAbsolutePath();
  }

  public long getLength() {
    return file.length();
  }

}
