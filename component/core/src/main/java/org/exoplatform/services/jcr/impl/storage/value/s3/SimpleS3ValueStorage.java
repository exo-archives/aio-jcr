/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.storage.value.s3;

import java.io.IOException;

import org.exoplatform.services.jcr.storage.value.ValueIOChannel;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class SimpleS3ValueStorage extends S3ValueStorage {
  
  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.storage.value.ValueStoragePlugin#openIOChannel()
   */
  public ValueIOChannel openIOChannel() throws IOException {
    return new SimpleS3IOChannel(bucket, awsAccessKey,
        this.awsSecretAccessKey, cleaner);
  }

}
