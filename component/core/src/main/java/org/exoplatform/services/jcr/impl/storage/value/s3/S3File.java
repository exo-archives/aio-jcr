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
package org.exoplatform.services.jcr.impl.storage.value.s3;

import java.io.File;
import java.io.IOException;

import org.exoplatform.services.jcr.impl.util.io.S3ValueIOUtil;

/**
 * An abstract representation of file on Amazon S3 
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: S3File.java 11907 2008-03-13 15:36:21Z ksm $
 */
public class S3File extends File {
  
  protected final String awsAccessKey;
  
  protected final String awsSecretAccessKey;
  
  
  public S3File(String bucket, String awsAccessKey, String awsSecretAccessKey, String key) {
    super(bucket, key);
    this.awsAccessKey = awsAccessKey;
    this.awsSecretAccessKey = awsSecretAccessKey;
  }
  
  
  /* (non-Javadoc)
   * @see java.io.File#delete()
   */
  public boolean delete() {
    try {
      return S3ValueIOUtil.deleteValue(this.getParent(), this.awsAccessKey,
          this.awsSecretAccessKey, this.getName());
    } catch (IOException ioe) {
      return false;
    }
  }
 
  
  /* (non-Javadoc)
   * @see java.io.File#isDirectory()
   */
  public boolean isDirectory() {
    return false;
  }

  
  /* (non-Javadoc)
   * @see java.io.File#exists()
   */
  public boolean exists() {
    try {
      return S3ValueIOUtil.isValueExists(this.getParent(), awsAccessKey,
          awsSecretAccessKey,  this.getName());
    } catch (IOException ioe) {
      return false;
    }
  }

}
