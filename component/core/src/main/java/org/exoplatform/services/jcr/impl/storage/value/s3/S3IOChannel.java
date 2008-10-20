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

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.storage.value.ValueIOChannel;
import org.exoplatform.services.log.ExoLogger;

public abstract class S3IOChannel implements ValueIOChannel {

  protected static Log        log = ExoLogger.getLogger("S3IOChannel");

  /**
   * Bucket name. See <a href="http://amazonaws.com">amazon S3 wikipedia</a>
   */
  protected final String      bucket;

  /**
   * AWS access key id. See <a href="http://amazonaws.com">amazon S3 wikipedia</a>
   */
  protected final String      awsAccessKey;

  /**
   * AWS access secret key. See <a href="http://amazonaws.com">amazon S3 wikipedia</a>
   */
  protected final String      awsSecretAccessKey;

  protected final File        s3SwapDirectory;

  protected final FileCleaner cleaner;

  protected final String      storageId;

  /**
   * New S3 channel
   * 
   * @param bucket
   *          the Bucket name
   * @param aws_access_key
   *          the S3 access key
   * @param aws_secret_access_key
   *          the S3 access secretkey
   * @param cleaner
   *          file cleanre
   */
  public S3IOChannel(String bucket,
                     String awsAccessKey,
                     String awsSecretAccessKey,
                     File s3SwapDirectory,
                     FileCleaner cleaner,
                     String storageId) {

    this.bucket = bucket;
    this.awsAccessKey = awsAccessKey;
    this.awsSecretAccessKey = awsSecretAccessKey;
    this.s3SwapDirectory = s3SwapDirectory;
    this.cleaner = cleaner;
    this.storageId = storageId;
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.value.ValueIOChannel#delete(java.lang.String)
   */
  public boolean delete(String propertyId) throws IOException {
    final String[] s3fileList = getFiles(propertyId);

    for (String s3fileName : s3fileList) {
      if (!S3ValueIOUtil.deleteValue(bucket, awsAccessKey, awsSecretAccessKey, s3fileName)) {
        log.warn("!!! Can't delete file " + s3fileName + "on Amazon S3 storage (Bucket: " + bucket
            + "). File added in FileCleaner list");
        cleaner.addFile(new S3File(bucket, awsAccessKey, awsSecretAccessKey, s3fileName));
      }
    }
    return true;
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.value.ValueIOChannel#close()
   */
  public void close() {
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.value.ValueIOChannel#read(java.lang.String, int, int)
   */
  public ValueData read(String propertyId, int orderNumber, int maxBufferSize) throws IOException {
    String s3fileName = getFile(propertyId, orderNumber);
    return S3ValueIOUtil.readValue(bucket,
                                   awsAccessKey,
                                   awsSecretAccessKey,
                                   s3fileName,
                                   orderNumber,
                                   maxBufferSize,
                                   s3SwapDirectory,
                                   cleaner);
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.value.ValueIOChannel#write(java.lang.String,
   * org.exoplatform.services.jcr.datamodel.ValueData)
   */
  public void write(String propertyId, ValueData value) throws IOException {
    String s3fileName = getFile(propertyId, value.getOrderNumber());
    S3ValueIOUtil.writeValue(bucket, awsAccessKey, awsSecretAccessKey, s3fileName, value);
    // return "/" + bucket + "/" +s3fileName;
  }

  /**
   * creates file name by propertyId and order number
   * 
   * @param propertyId
   * @param orderNumber
   * @return file name
   */
  protected abstract String getFile(String propertyId, int orderNumber);

  /**
   * creates file names list by propertyId
   * 
   * @param propertyId
   * @return array of file names
   */
  protected abstract String[] getFiles(String propertyId);

  public String getStorageId() {
    return storageId;
  }
}
