/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.completed;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.commands.DavDelete;
import org.exoplatform.frameworks.davclient.commands.DavGet;
import org.exoplatform.frameworks.davclient.commands.DavPut;
import org.exoplatform.services.log.ExoLogger;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class PutTest extends TestCase {
  
  private static Log log = ExoLogger.getLogger("jcr.PutTest");
  
  private static final String SRC_WORKSPACE = "/production";
  private static final String SRC_NAME = "/test file test " + System.currentTimeMillis() + ".txt";
  
  private static final String FILE_CONTENT = "TEST FILE CONTENT...";
  
  public void testNotAuthorized() throws Exception {
    log.info("testNotAuthorized...");
    
    DavPut davPut = new DavPut(DavLocationConst.getLocation());
    davPut.setResourcePath(SRC_WORKSPACE + SRC_NAME);
    davPut.setRequestDataBuffer(FILE_CONTENT.getBytes());    
    assertEquals(Const.HttpStatus.AUTHNEEDED, davPut.execute());
    
    log.info("done.");
  }
  
  public void testCreated() throws Exception {
    log.info("testCreated...");

    DavPut davPut = new DavPut(DavLocationConst.getLocationAuthorized());
    davPut.setResourcePath(SRC_WORKSPACE + SRC_NAME);
    davPut.setRequestDataBuffer(FILE_CONTENT.getBytes());
    
    assertEquals(Const.HttpStatus.CREATED, davPut.execute());
    
    // verify for content...
    DavGet davGet = new DavGet(DavLocationConst.getLocationAuthorized());
    davGet.setResourcePath(SRC_WORKSPACE + SRC_NAME);
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
    
    DavDelete davDelete = new DavDelete(DavLocationConst.getLocationAuthorized());
    davDelete.setResourcePath(SRC_WORKSPACE + SRC_NAME);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    
    log.info("done.");
  }

}
