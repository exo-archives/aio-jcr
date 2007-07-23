/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.storage.value.s3;

import java.io.File;
import java.io.IOException;

import org.exoplatform.services.jcr.impl.util.io.S3ValueIOUtil;

/**
 * An abstract representation of file on Amazon S3 
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
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
