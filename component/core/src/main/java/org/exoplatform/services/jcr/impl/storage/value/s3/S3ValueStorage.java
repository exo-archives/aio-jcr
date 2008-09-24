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
import java.util.Properties;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.impl.util.io.S3ValueIOUtil;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;
import org.exoplatform.services.jcr.storage.value.ValueStoragePlugin;
import org.exoplatform.services.log.ExoLogger;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: S3ValueStorage.java 11907 2008-03-13 15:36:21Z ksm $
 */
public abstract class S3ValueStorage extends ValueStoragePlugin {

  protected static Log        logger                = ExoLogger.getLogger("SimpleS3ValueStorage");

  public final static String  BUCKET                = "bucket";

  public final static String  AWS_ACCESS_KEY        = "aws-access-key";

  public final static String  AWS_SECRET_ACCESS_KEY = "aws-secret-access-key";

  public final static String  S3_SWAP_DIRECTORY     = "s3-swap-directory";

  protected String            bucket;

  protected String            awsAccessKey;

  protected String            awsSecretAccessKey;

  protected File              s3SwapDirectory;

  protected final FileCleaner cleaner;

  public S3ValueStorage() {
    this.cleaner = new FileCleaner();
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.value.ValueStoragePlugin#init(java.util.Properties)
   */
  public void init(Properties props) throws IOException, RepositoryConfigurationException {
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
   * @see
   * org.exoplatform.services.jcr.storage.value.ValueStoragePlugin#checkConsistency(org.exoplatform
   * .services.jcr.storage.WorkspaceStorageConnection)
   */
  public void checkConsistency(WorkspaceStorageConnection dataConnection) {

  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.value.ValueStoragePlugin#match(java.lang.String,
   * org.exoplatform.services.jcr.datamodel.PropertyData, int)
   */
  @Override
  public boolean match(String storageId) {
    return getId().equals(storageId);
  }

}
