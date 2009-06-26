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
package org.exoplatform.services.jcr.aws.storage.value.s3;

import java.io.File;
import java.io.IOException;

import org.exoplatform.services.jcr.impl.util.io.FileCleaner;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class SimpleS3IOChannel extends S3IOChannel {

  /**
   * SimpleS3IOChannel constructor.
   * 
   * @param bucket
   *          S3 bucket name
   * @param awsAccessKey
   *          AccessKey
   * @param awsSecretAccessKey
   *          SecretAccessKey
   * @param s3SwapDirectory
   *          SwapDirectory
   * @param cleaner
   *          FileCleaner
   * @param storageId
   *          JCR storage ID.
   */
  public SimpleS3IOChannel(String bucket,
                           String awsAccessKey,
                           String awsSecretAccessKey,
                           File s3SwapDirectory,
                           FileCleaner cleaner,
                           String storageId) {
    super(bucket, awsAccessKey, awsSecretAccessKey, s3SwapDirectory, cleaner, storageId);
  }

  /**
   * {@inheritDoc}
   */
  protected String getFile(String propertyId, int orderNumber) {
    return propertyId + orderNumber;
  }

  /**
   * {@inheritDoc}
   */
  protected String[] getFiles(String propertyId) {
    try {
      return S3ValueIOUtil.getBucketList(bucket, awsAccessKey, awsSecretAccessKey, propertyId);
    } catch (IOException ioe) {
      return null;
    }
  }

}
