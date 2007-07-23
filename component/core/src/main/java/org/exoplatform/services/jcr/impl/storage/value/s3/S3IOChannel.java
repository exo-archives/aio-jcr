package org.exoplatform.services.jcr.impl.storage.value.s3;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.impl.util.io.S3ValueIOUtil;
import org.exoplatform.services.jcr.storage.value.ValueIOChannel;
import org.exoplatform.services.log.ExoLogger;


public abstract class S3IOChannel implements ValueIOChannel {

  protected static Log  log = ExoLogger.getLogger("S3IOChannel");

  /**
   * Bucket name. See <a href="http://amazonaws.com">amazon S3 wikipedia</a>
   */
  protected final String bucket;
  
  /**
   * AWS access key id. See <a href="http://amazonaws.com">amazon S3 wikipedia</a>
   */
  protected final String awsAccessKey;
  
  /**
   * AWS access secret key. See <a href="http://amazonaws.com">amazon S3 wikipedia</a>
   */
  protected final String awsSecretAccessKey;

  protected final FileCleaner cleaner;
  
  
  /**
   * New S3 channel
   * @param bucket the Bucket name
   * @param aws_access_key the S3 access key
   * @param aws_secret_access_key the S3 access secretkey
   * @param cleaner file cleanre
   */
  public S3IOChannel(String bucket, String aws_access_key,
      String aws_secret_access_key, FileCleaner cleaner) {
    
    this.bucket = bucket;
    this.awsAccessKey = aws_access_key;
    this.awsSecretAccessKey = aws_secret_access_key;
    this.cleaner = cleaner;
  }
   
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.value.ValueIOChannel#delete(java.lang.String)
   */
  public boolean delete(String propertyId) throws IOException {
    final String[] s3fileList = getFiles(propertyId);

    for(String s3fileName : s3fileList) {
      if(!S3ValueIOUtil.deleteValue(bucket, awsAccessKey,
          awsSecretAccessKey, s3fileName)) {
        log.warn("!!! Can't delete file " + s3fileName
            + "on Amazon S3 storage (Bucket: " + bucket + "). File added in FileCleaner list");
        cleaner.addFile(new S3File(bucket, awsAccessKey, awsSecretAccessKey, s3fileName));
      }
    }
    return true;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.value.ValueIOChannel#close()
   */
  public void close() {
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.value.ValueIOChannel#read(java.lang.String, int, int)
   */
  public ValueData read(String propertyId, int orderNumber,
      int maxBufferSize) throws IOException {
    String s3fileName = getFile(propertyId, orderNumber);
    return S3ValueIOUtil.readValue(bucket, awsAccessKey,
        awsSecretAccessKey, s3fileName, orderNumber, maxBufferSize);
  }

  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.value.ValueIOChannel#write(java.lang.String, org.exoplatform.services.jcr.datamodel.ValueData)
   */
  public String write(String propertyId, ValueData value) throws IOException {
    String s3fileName = getFile(propertyId, value.getOrderNumber());
    S3ValueIOUtil.writeValue(bucket, awsAccessKey,
        awsSecretAccessKey, s3fileName, value);
    return "/" + bucket + "/" +s3fileName;
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

}
