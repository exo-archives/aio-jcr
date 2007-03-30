package org.exoplatform.frameworks.ftpclient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

public class SlowStreamTest extends TestCase {

  public static final int SPEED_VALUE = 1000;
  public static final int BUFFER_SIZE = 100033;

  public static boolean INPUT_TEST_NEEDS = false;
  public static boolean OUTPUT_TEST_NEEDS = true;
  
  public void testSlowInputStream() throws Exception {    
    Log log = ExoLogger.getLogger("jcr.SlowStreamTest_testSlowInputStream");    

    if (!INPUT_TEST_NEEDS) {
      log.info("Skipped.");
      return;
    }
    
    log.info("Test...");
   
    byte []dataBytes = new byte[BUFFER_SIZE];    
    log.info("Filling buffer...");
    
    for (int i = 0; i < BUFFER_SIZE; i++) {
      dataBytes[i] = 113;
    }
    
    log.info("Buffer full.");
    
    ByteArrayInputStream byteInStream = new ByteArrayInputStream(dataBytes);
    
    FtpSlowInputStream slowInputStream = new FtpSlowInputStream(byteInStream, SPEED_VALUE);
    
    log.info("SLOW IN STREAM CREATED. test starting...");
    
    byte []buffer = new byte[4 * 1024];
    
    int allReaded = 0;
    
    long timeStart = System.currentTimeMillis();
    
    while (true) {
//      int readed = slowInputStream.read();
//      if (readed < 0) {
//        break;
//      }
      
      //int readed = slowInputStream.read(buffer);
      int readed = slowInputStream.read(buffer, 0, 5);
      if (readed < 0) {
        log.info("Read complete");
        break;
      }
      allReaded += readed;
    }
    
    log.info("AllReaded - " + allReaded);

    long timeEnd = System.currentTimeMillis();
    long allTime = timeEnd - timeStart;
    long msec = allTime % 1000;
    long sec = allTime / 1000;
    log.info(String.format("READ completed at SS:MS %d:%d", sec, msec));
    
    log.info("Complete.");    
  }
  
  public void testSlowOutputStream() throws Exception {
    Log log = ExoLogger.getLogger("jcr.SlowStreamTest_testSlowOutputStream");    

    if (!OUTPUT_TEST_NEEDS) {
      log.info("Skipped.");
      return;
    }
    
    log.info("Test...");
    
    byte []dataBytes = new byte[BUFFER_SIZE];    
    log.info("Filling buffer...");

    int c = 0;
    for (int i = 0; i < BUFFER_SIZE; i++) {
      dataBytes[i] = (byte)c;
      c++;
      if (c > 255) {
        c = 0;
      }
    }
    
    log.info("Buffer full.");
    
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    
    FtpSlowOutputStream slowOutStream = new FtpSlowOutputStream(outStream, SPEED_VALUE);
    
    long timeStart = System.currentTimeMillis();

//    for (int i = 0; i < BUFFER_SIZE; i++) {
//      slowOutStream.write(dataBytes[i]);
//    }
    
//    slowOutStream.write(1);
//    slowOutStream.write(2);
    
    slowOutStream.write(dataBytes);
    
    long timeEnd = System.currentTimeMillis();
    long allTime = timeEnd - timeStart;
    long msec = allTime % 1000;
    long sec = allTime / 1000;
    log.info(String.format("WRITE completed at SS:MS %d:%d", sec, msec));    
    
    byte []fromOut = outStream.toByteArray();
    
    for (int i = 0; i < BUFFER_SIZE; i++) {
      if (fromOut[i] != dataBytes[i]) {
        log.info("FAILURE AT [" + i + "]");
        fail();
      }
    }
    log.info("DATA GOOD !!!");
    
    log.info("Complete.");    
  }
  
}
