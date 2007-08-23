/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.storage.value.s3;

import java.io.File;
import java.io.IOException;
import org.exoplatform.services.jcr.impl.util.io.S3ValueIOUtil;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;

/**
* @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
* @version $Id: $
*/
public class SimpleS3IOChannel extends S3IOChannel {

  public SimpleS3IOChannel(String bucket, String awsAccessKey,
      String awsSecretAccessKey, File s3SwapDirectory, FileCleaner cleaner) {
    super(bucket, awsAccessKey, awsSecretAccessKey, s3SwapDirectory, cleaner);
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.storage.value.s3.S3IOChannel#getFile(java.lang.String, int)
   */
  protected String getFile(String propertyId, int orderNumber) {
    return propertyId + orderNumber;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.storage.value.s3.S3IOChannel#getFiles(java.lang.String)
   */
  protected String[] getFiles(String propertyId) {
    try {
      return S3ValueIOUtil.getBucketList(bucket, awsAccessKey,
          awsSecretAccessKey, propertyId);
    } catch (IOException ioe) {
      return null;
    }
  }

}
