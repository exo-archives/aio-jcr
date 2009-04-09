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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.storage.value.ValueOperation;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.storage.value.ValueIOChannel;
import org.exoplatform.services.log.ExoLogger;

public abstract class S3IOChannel implements ValueIOChannel {

  protected static Log                 log     = ExoLogger.getLogger("S3IOChannel");

  /**
   * Bucket name. See <a href="http://amazonaws.com">amazon S3 wikipedia</a>
   */
  protected final String               bucket;

  /**
   * AWS access key id. See <a href="http://amazonaws.com">amazon S3 wikipedia</a>
   */
  protected final String               awsAccessKey;

  /**
   * AWS access secret key. See <a href="http://amazonaws.com">amazon S3 wikipedia</a>
   */
  protected final String               awsSecretAccessKey;

  protected final File                 s3SwapDirectory;

  protected final FileCleaner          cleaner;

  protected final String               storageId;

  protected final List<ValueOperation> changes = new ArrayList<ValueOperation>();

  class DeleteOperation implements ValueOperation {

    private final String propertyId;

    DeleteOperation(String propertyId) throws IOException {
      this.propertyId = propertyId;
    }

    /**
     * {@inheritDoc}
     */
    public void commit() throws IOException {
      // perform operation on commit, SDB connection does the same
      final String[] s3fileList = getFiles(propertyId);
      for (String s3fileName : s3fileList) {
        if (!S3ValueIOUtil.deleteValue(bucket, awsAccessKey, awsSecretAccessKey, s3fileName)) {
          log.warn("!!! Can't delete file " + s3fileName + "on Amazon S3 storage (Bucket: "
              + bucket + "). File added in FileCleaner list");
          cleaner.addFile(new S3File(bucket, awsAccessKey, awsSecretAccessKey, s3fileName));
        }
      }
    }

    /**
     * {@inheritDoc}
     */
    public void rollback() throws IOException {
      // TODO we have to restore prev file
    }

    /**
     * {@inheritDoc}
     */
    public void execute() throws IOException {
      // do nothing
    }

  }

  class WriteOperation implements ValueOperation {

    private final String    propertyId;

    private final ValueData value;

    WriteOperation(String propertyId, ValueData value) throws IOException {
      this.propertyId = propertyId;
      this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    public void commit() throws IOException {
      // perform operation on commit, SDB connection does the same
      String s3fileName = getFile(propertyId, value.getOrderNumber());
      S3ValueIOUtil.writeValue(bucket, awsAccessKey, awsSecretAccessKey, s3fileName, value);
    }

    /**
     * {@inheritDoc}
     */
    public void rollback() throws IOException {
      // TODO we have to rever file on S3 to prev state or delete if it was added
    }

    /**
     * {@inheritDoc}
     */
    public void execute() throws IOException {
      // do nothing
    }

  }

  /**
   * New S3 channel.
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

  /**
   * {@inheritDoc}
   */
  public void close() {
  }

  /**
   * {@inheritDoc}
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

  /**
   * {@inheritDoc}
   */
  public void delete(String propertyId) throws IOException {
    ValueOperation o = new DeleteOperation(propertyId);
    o.execute();
    changes.add(o);
  }

  public void write(String propertyId, ValueData value) throws IOException {
    ValueOperation o = new WriteOperation(propertyId, value);
    o.execute();
    changes.add(o);
  }

  /**
   * {@inheritDoc}
   */
  public void commit() throws IOException {
    for (ValueOperation vo : changes)
      vo.commit();
  }

  /**
   * {@inheritDoc}
   */
  public void rollback() throws IOException {
    for (int p = changes.size() - 1; p >= 0; p--)
      changes.get(p).rollback();
  }

  /**
   * Creates file name by propertyId and order number.
   * 
   * @param propertyId
   *          Strign
   * @param orderNumber
   *          int
   * @return file name String
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
