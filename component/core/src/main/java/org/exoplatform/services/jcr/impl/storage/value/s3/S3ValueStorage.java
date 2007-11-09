/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.storage.value.s3;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;
import org.exoplatform.services.jcr.storage.value.ValueStoragePlugin;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.jcr.impl.util.io.S3ValueIOUtil;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public abstract class S3ValueStorage extends ValueStoragePlugin {

  protected static Log logger = ExoLogger.getLogger("SimpleS3ValueStorage");

  public final static String BUCKET = "bucket";

  public final static String AWS_ACCESS_KEY = "aws-access-key";

  public final static String AWS_SECRET_ACCESS_KEY = "aws-secret-access-key";
  
  public final static String S3_SWAP_DIRECTORY = "s3-swap-directory";

  protected String bucket;

  protected String awsAccessKey;
  
  protected String awsSecretAccessKey;

  protected File s3SwapDirectory;

  protected final FileCleaner cleaner;
  

  public S3ValueStorage() {
    this.cleaner = new FileCleaner();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.storage.value.ValueStoragePlugin#init(java.util.Properties)
   */
  public void init(Properties props) throws IOException,
      RepositoryConfigurationException {
    bucket = props.getProperty(BUCKET);
    awsAccessKey = props.getProperty(AWS_ACCESS_KEY);
    awsSecretAccessKey = props.getProperty(AWS_SECRET_ACCESS_KEY);
    s3SwapDirectory = new File(props.getProperty(S3_SWAP_DIRECTORY));
    
    if (!s3SwapDirectory.exists()) {
      if (s3SwapDirectory.mkdirs()) {
        logger.info("Created S3 swap directory " + s3SwapDirectory.getAbsolutePath());
      } else {
        logger.warn("Can't created S3 swap directory " + s3SwapDirectory.getAbsolutePath());
      }
    }
    S3ValueIOUtil.createBucket(bucket, awsAccessKey, awsSecretAccessKey);
  }


  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.storage.value.ValueStoragePlugin#checkConsistency(org.exoplatform.services.jcr.storage.WorkspaceStorageConnection)
   */
  public void checkConsistency(WorkspaceStorageConnection dataConnection) {

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.storage.value.ValueStoragePlugin#match(java.lang.String,
   *      org.exoplatform.services.jcr.datamodel.PropertyData, int)
   */
  @Override
  public boolean match(String storageId, PropertyData prop) {
    //return storageId.startsWith("/" + bucket);
    return getId().equals(storageId);
  }

}
