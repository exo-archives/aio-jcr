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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.ByteArrayPersistedValueData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CleanableFileStreamValueData;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.impl.util.io.SwapFile;
import org.exoplatform.services.log.ExoLogger;

import com.amazon.s3.AWSAuthConnection;
import com.amazon.s3.GetResponse;
import com.amazon.s3.ListBucketResponse;
import com.amazon.s3.ListEntry;
import com.amazon.s3.Response;
import com.amazon.s3.S3Object;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class S3ValueIOUtil {

  /**
   * Logger.
   */
  private static final Log LOG = ExoLogger.getLogger("jcr.S3ValueIOUtil");

  public static ValueData readValue(String bucket,
                                    String awsAccessKey,
                                    String awsSecretAccessKey,
                                    String s3fielName,
                                    int orderNum,
                                    int maxBufferSize,
                                    File swapDir,
                                    FileCleaner cleaner) throws IOException {

    AWSAuthConnection conn = new AWSAuthConnection(awsAccessKey, awsSecretAccessKey);

    GetResponse resp = conn.get(bucket, s3fielName, null);
    int responseCode = resp.connection.getResponseCode();
    if (responseCode != HttpURLConnection.HTTP_OK) {
      throw new IOException("Filed read data from S3 storage. HTTP status " + responseCode);
    }
    if (LOG.isDebugEnabled())
      LOG.debug("Read from S3: STATUS = " + responseCode);

    int size = resp.connection.getContentLength();
    InputStream in = resp.connection.getInputStream();

    if (size > maxBufferSize) {
      SwapFile swapFile = SwapFile.get(swapDir, s3fielName + orderNum);
      if (!swapFile.isSpooled()) {
        FileOutputStream fout = new FileOutputStream(swapFile);
        try {
          // NIO work...
          FileChannel fch = fout.getChannel();
          ReadableByteChannel inch = Channels.newChannel(in);

          long actualSize = fch.transferFrom(inch, 0, size);

          // int rd = -1;
          // byte[] buff = new byte[4096];
          // while ((rd = in.read(buff)) != -1) {
          // fout.write(buff, 0, rd);
          // }
          // fout.flush();
        } finally {
          fout.close();
        }
      } else
        return new CleanableFileStreamValueData(swapFile, orderNum, cleaner);
    }

    int rd = -1;
    byte[] buff = new byte[4096];
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    while ((rd = in.read(buff)) != -1)
      out.write(buff, 0, rd);

    if (LOG.isDebugEnabled())
      LOG.debug("Value created as ByteArrayPersistedValueData");

    return new ByteArrayPersistedValueData(out.toByteArray(), orderNum);
  }

  public static boolean isValueExists(String bucket,
                                      String awsAccessKey,
                                      String awsSecretAccessKey,
                                      String s3fielName) throws IOException {

    AWSAuthConnection conn = new AWSAuthConnection(awsAccessKey, awsSecretAccessKey);

    GetResponse resp = conn.get(bucket, s3fielName, null);
    int responseCode = resp.connection.getResponseCode();

    if (LOG.isDebugEnabled())
      LOG.info("Read from S3: STATUS = " + responseCode);

    if (responseCode != HttpURLConnection.HTTP_OK) {
      return false;
    }
    return true;
  }

  public static void createBucket(String bucket, String awsAccessKey, String awsSecretAccessKey) throws IOException {

    AWSAuthConnection conn = new AWSAuthConnection(awsAccessKey, awsSecretAccessKey);
    Response resp = conn.createBucket(bucket, null);
    int responseCode = resp.connection.getResponseCode();
    if (responseCode != HttpURLConnection.HTTP_OK) {
      throw new IOException("Can't create BUCKET on S3 storage. HTTP status " + responseCode);
    }
    if (LOG.isDebugEnabled())
      LOG.debug("Create bucket on S3: STATUS = " + responseCode);
  }

  public static void writeValue(String bucket,
                                String awsAccessKey,
                                String awsSecretAccessKey,
                                String key,
                                ValueData value) throws IOException {

    AWSAuthConnection conn = new AWSAuthConnection(awsAccessKey, awsSecretAccessKey);
    InputStream valueStream = (value.isByteArray())
        ? new ByteArrayInputStream(value.getAsByteArray())
        : value.getAsStream();
    Response resp = conn.put(bucket, key, new S3Object(valueStream, null), null);
    int responseCode = resp.connection.getResponseCode();
    if (responseCode != HttpURLConnection.HTTP_OK) {
      throw new IOException("Filed PUT data to S3 storage. HTTP status " + responseCode);
    }

    if (LOG.isDebugEnabled())
      LOG.info("Write to S3: STATUS = " + responseCode);
  }

  public static boolean deleteValue(String bucket,
                                    String awsAccessKey,
                                    String awsSecretAccessKey,
                                    String key) throws IOException {

    AWSAuthConnection conn = new AWSAuthConnection(awsAccessKey, awsSecretAccessKey);
    Response resp = conn.delete(bucket, key, null);
    int responseCode = resp.connection.getResponseCode();
    if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
      return false;
    }
    if (LOG.isDebugEnabled())
      LOG.info("Delete from S3: STATUS = " + responseCode);
    return true;
  }

  public static String[] getBucketList(String bucket,
                                       String awsAccessKey,
                                       String awsSecretAccessKey,
                                       String prefix) throws IOException {

    AWSAuthConnection conn = new AWSAuthConnection(awsAccessKey, awsSecretAccessKey);
    ListBucketResponse resp = conn.listBucket(bucket, prefix, null, null, null);
    int responseCode = resp.connection.getResponseCode();

    if (LOG.isDebugEnabled())
      LOG.info("Get list of bucket from S3: STATUS = " + responseCode);

    List<ListEntry> entries = resp.entries;
    String[] keys = new String[entries.size()];
    int i = 0;
    for (ListEntry l : entries) {
      keys[i] = l.key;
      i++;
    }
    return keys;
  }

}
