/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.webdavclient;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavGet;
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.commands.DavOptions;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.commands.DavPut;
import org.exoplatform.services.log.ExoLogger;
import junit.framework.TestCase;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class LargePutGetTest extends TestCase {

  private static Log log = ExoLogger.getLogger("jcr.LargePutGetTest");
  
  public static final String TEST_WORKSPACE = "/production";
  public static final String TEST_FOLDER = TEST_WORKSPACE + "/large_test_folder_" + System.currentTimeMillis();
  public static final String TEST_FILE = TEST_FOLDER + "/large_file.datka";
  
  public static final int FILE_SIZE = 5 * 1024 * 1024;
  
  public void testLargePutGet() throws Exception {
    log.info("Test...");

    WebDavContext context = TestContext.getContextAuthorized();

    DavOptions davOptions = new DavOptions(context);
    assertEquals(Const.HttpStatus.OK, davOptions.execute());
    
    DavPropFind davPropFind = new DavPropFind(context);
    davPropFind.setResourcePath(TEST_WORKSPACE);
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    DavMkCol davMkCol = new DavMkCol(context);
    davMkCol.setResourcePath(TEST_FOLDER);
    assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    
    davPropFind = new DavPropFind(context);
    davPropFind.setResourcePath(TEST_FOLDER);
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    {
      byte []data = new byte[FILE_SIZE];
      int mm = 0;
      for (int i = 0; i < FILE_SIZE; i++) {
        data[i] = (byte)mm;
        mm++;
        if (mm > 255) {
          mm = 0;
        }
      }
      
      DavPut davPut = new DavPut(context);
      davPut.setResourcePath(TEST_FILE);
      davPut.setRequestDataBuffer(data);
      
      assertEquals(Const.HttpStatus.CREATED, davPut.execute());      
    }
    
    {
      DavGet davGet = new DavGet(context);
      davGet.setResourcePath(TEST_FILE);
      
      assertEquals(Const.HttpStatus.OK, davGet.execute());      
      
      byte []data = davGet.getResponseDataBuffer();
      int mm = 0;
      for (int i = 0; i < FILE_SIZE; i++) {
        if (data[i] != (byte)mm) {
          log.info("Data not equals!");
          break;
        }
        mm++;
        if (mm > 255) {
          mm = 0;
        }
      }
      
    }
    
    {
      DavDelete davDelete = new DavDelete(context);
      davDelete.setResourcePath(TEST_FILE);
     
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
      
      davDelete = new DavDelete(context);
      davDelete.setResourcePath(TEST_FOLDER);
      
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());      
    }
    
    log.info("Complete.");
  }
  
}
