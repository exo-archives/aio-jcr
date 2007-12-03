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

package org.exoplatform.services.webdav;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.common.response.RangedInputStream;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class RangedStreamTest extends TestCase {
  
  private static Log log = ExoLogger.getLogger("jcr.RangedStreamTest");
  
  private static byte []getBytes() {
    byte []bytes = new byte[256];
    
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = (byte)i;
    }
    
    return bytes;
  }
  
  public void testRangedStream() throws Exception {
    log.info("testRangedStream...");
    
    byte []bytes = getBytes();
    
    ByteArrayInputStream inStream = new ByteArrayInputStream(bytes);
    
    {
      RangedInputStream rangedStream = new RangedInputStream(inStream, 50, 100);
      ByteArrayOutputStream testOut = new ByteArrayOutputStream();
      while (true) {
        int readed = rangedStream.read();
        if (readed < 0) {
          break;
        }
        
        testOut.write(readed);
      }
      
      byte []testBytes = testOut.toByteArray();
      assertEquals(51, testBytes.length);
      
      for (int i = 0; i < testBytes.length; i++) {
        assertEquals(bytes[50 + i], testBytes[i]);
      }
    }

    log.info("done.");
  }
  
  public void testRangedStreamByBuff() throws Exception {
    log.info("testRangedStreamByBuff");
    
    byte []bytes = getBytes();
    
    ByteArrayInputStream inStream = new ByteArrayInputStream(bytes);
    
    RangedInputStream rangedStream = new RangedInputStream(inStream, 50, 70);
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    
    byte []buff = new byte[4096];
    
    while (true) {
      int readed = rangedStream.read(buff);
      if (readed < 0) {
        break;
      }
      outStream.write(buff, 0, readed);
    }
    
    byte []testBytes = outStream.toByteArray();
    assertEquals(21, testBytes.length);
    
    for (int i = 0; i < testBytes.length; i++) {
      assertEquals(bytes[50 + i], testBytes[i]);
    }
    
    log.info("done.");
  }
  
  public void testRangeStreamByBuffAndSize() throws Exception {
    log.info("testRangeStreamByBuffAndSize");
    
    byte []bytes = getBytes();
    
    ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
    
    RangedInputStream rangedStream = new RangedInputStream(inputStream, 0, 50);
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    
    byte []buff = new byte[10];
    
    while (true) {
      int readed = rangedStream.read(buff, 0, 7);
      if (readed < 0) {
        break;
      }
      outStream.write(buff, 0, readed);
    }
    
    byte []testBytes = outStream.toByteArray();
    assertEquals(/*186*/51, testBytes.length);
    
    for (int i = 0; i < testBytes.length; i++) {
      assertEquals(bytes[i + 0], testBytes[i]);
    }
    
    log.info("done.");
  }

}
