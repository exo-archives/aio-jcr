/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.storage.value.s3;

import java.io.IOException;
import org.exoplatform.services.jcr.impl.util.io.S3ValueIOUtil;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;

/**
* @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
* @version $Id: $
*/
public class SimpleS3IOChannel extends S3IOChannel {

  public SimpleS3IOChannel(String bucket, String aws_access_key,
      String aws_secret_access_key, FileCleaner cleaner) {
    super(bucket, aws_access_key, aws_secret_access_key, cleaner);
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
