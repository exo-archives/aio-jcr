/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.dasl.basicsearch;

import junit.framework.TestCase;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.TestUtils;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class BasicSearctTest extends TestCase {

  public void testSimpleBasicSearch() throws Exception {    
    Log.info("testSimpleBasicSearch:test...");

    String folderPath = "/production/search_test_folder_" + System.currentTimeMillis();
    
    // prepare folders
    
    TestUtils.createCollection(folderPath);    
    for (int i = 0; i < 10; i++) {
      String filePath = folderPath + "/test_file_" + i + ".txt";
      TestUtils.createFile(filePath, "TEST FILE CONTENT".getBytes());
    }    
    
    // search
    
    // clear
    
//    DavSearch davSearch = new DavSearch(TestContext.getContextAuthorized());
//    davSearch.setResourcePath("")
    
    Log.info("testSimpleBasicSearch:done.");
  }

}
