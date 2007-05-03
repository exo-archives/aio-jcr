/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.common;

import junit.framework.TestCase;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavCopy;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

/*
 * 
 * needs to test for FORBIDDEN & NOTFOUND replies
 * 
 */

public class CopyTest extends TestCase {
  
  private static final String SRC_WORKSPACE = "/production";
  private static final String DEST_WORKSPACE = "/backup";
  private static final String SRC_NAME = "/test folder source " + System.currentTimeMillis();    
  private static final String DEST_NAME = "/test folder destination " + System.currentTimeMillis();

  public void testNotAuthorized() throws Exception {
    Log.info("CopyTest:testNotAuthorized...");
    
    DavCopy davCopy = new DavCopy(TestContext.getContext());
    davCopy.setResourcePath(SRC_WORKSPACE + SRC_NAME);
    davCopy.setDestinationPath(DEST_WORKSPACE + DEST_NAME);
    assertEquals(Const.HttpStatus.AUTHNEEDED, davCopy.execute());    
    
    Log.info("done.");
  }
  
  public void testCopyToSameWorkspace() throws Exception {
    Log.info("CopyTest:testCopyToSameWorkspace...");
    
    {        
      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
      davMkCol.setResourcePath(SRC_WORKSPACE + SRC_NAME);
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    }
    
    {
      DavCopy davCopy = new DavCopy(TestContext.getContextAuthorized());
      davCopy.setResourcePath(SRC_WORKSPACE + SRC_NAME);
      davCopy.setDestinationPath(SRC_WORKSPACE + DEST_NAME);
      assertEquals(Const.HttpStatus.CREATED, davCopy.execute());
    }
    
    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
      davDelete.setResourcePath(SRC_WORKSPACE + SRC_NAME);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }
    
    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());        
      davDelete.setResourcePath(SRC_WORKSPACE + DEST_NAME);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }
    
    Log.info("done.");
  }

  public void testCopyToAnotherWorkspace() throws Exception {
    Log.info("CopyTest:testCopyToAnotherWorkspace...");
    
    {        
      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
      davMkCol.setResourcePath(SRC_WORKSPACE + SRC_NAME);
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    }

    {
      DavCopy davCopy = new DavCopy(TestContext.getContextAuthorized());
      davCopy.setResourcePath(SRC_WORKSPACE + SRC_NAME);
      davCopy.setDestinationPath(DEST_WORKSPACE + DEST_NAME);
      assertEquals(Const.HttpStatus.CREATED, davCopy.execute());
    }
    
    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
      davDelete.setResourcePath(SRC_WORKSPACE + SRC_NAME);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }
    
    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());        
      davDelete.setResourcePath(DEST_WORKSPACE + DEST_NAME);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }
    
    Log.info("done.");
  }

}
