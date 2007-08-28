/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.common;

import junit.framework.TestCase;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavGet;
import org.exoplatform.frameworks.webdavclient.commands.DavPut;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class PutTest extends TestCase {
  
  private static final String SRC_WORKSPACE = "/production";
  
  private static final String FILE_CONTENT = "TEST FILE CONTENT...";
  
  private static String getSrcName() {
    return "/test_file_test_" + System.currentTimeMillis() + ".txt";
    //return "/test file test " + System.currentTimeMillis() + ".txt";
  }
  
  public void testNotAuthorized() throws Exception {
    Log.info("PutTest:testNotAuthorized...");
    
    DavPut davPut = new DavPut(TestContext.getContext());
    davPut.setResourcePath(SRC_WORKSPACE + getSrcName());
    davPut.setRequestDataBuffer(FILE_CONTENT.getBytes());    
    assertEquals(Const.HttpStatus.AUTHNEEDED, davPut.execute());
    
    Log.info("done.");
  }
  
  public void testCreated() throws Exception {
    Log.info("PutTest:testCreated...");
    
    String sourceName = getSrcName();

    DavPut davPut = new DavPut(TestContext.getContextAuthorized());
    davPut.setResourcePath(SRC_WORKSPACE + sourceName);
    davPut.setRequestDataBuffer(FILE_CONTENT.getBytes());
    
    assertEquals(Const.HttpStatus.CREATED, davPut.execute());
    
    // verify for content...
    DavGet davGet = new DavGet(TestContext.getContextAuthorized());
    davGet.setResourcePath(SRC_WORKSPACE + sourceName);
    assertEquals(Const.HttpStatus.OK, davGet.execute());
    
    byte []dataRemote = davGet.getResponseDataBuffer();
    byte []dataContent = FILE_CONTENT.getBytes();

    if (dataRemote.length != dataContent.length) {
      fail();
    }
    
    for (int i = 0; i < dataRemote.length; i++) {
      if (dataRemote[i] != dataContent[i]) {
        fail();
      }
    }
    
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(SRC_WORKSPACE + sourceName);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    
    Log.info("done.");
  }

}
