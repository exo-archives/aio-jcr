/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.common;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.commands.DavMove;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class MoveTest extends TestCase {
  
  private static Log log = ExoLogger.getLogger("jcr.MoveTest");

  private static final String SRC_WORKSPACE = "/production";
  private static final String DEST_WORKSPACE = "/backup";
  private static final String SRC_NAME = "/test folder source " + System.currentTimeMillis();    
  private static final String DEST_NAME = "/test folder destination " + System.currentTimeMillis();  
  
  public void testNotAuthorized() throws Exception {
    log.info("testNotAuthorized...");
    
    DavMove davMove = new DavMove(TestContext.getContext());
    davMove.setResourcePath(SRC_WORKSPACE + SRC_NAME);
    davMove.setDestinationPath(DEST_WORKSPACE + DEST_NAME);    
    assertEquals(Const.HttpStatus.AUTHNEEDED, davMove.execute());    
    
    log.info("done.");
  }
  
  public void testMoveToSameWorkspace() throws Exception {
    log.info("testMoveToSameWorkspace...");
    
    {
      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
      davMkCol.setResourcePath(SRC_WORKSPACE + SRC_NAME);
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    }
    
    {
      DavMove davMove = new DavMove(TestContext.getContextAuthorized());
      davMove.setResourcePath(SRC_WORKSPACE + SRC_NAME);
      davMove.setDestinationPath(SRC_WORKSPACE + DEST_NAME);
      assertEquals(Const.HttpStatus.CREATED, davMove.execute());
    }
    
    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
      davDelete.setResourcePath(SRC_WORKSPACE + DEST_NAME);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }    
    
    log.info("done.");
  }
  
  public void testMoveToAnotherWorkspace() throws Exception {
    log.info("testMoveToAnotherWorkspace...");
    
    {
      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
      davMkCol.setResourcePath(SRC_WORKSPACE + SRC_NAME);
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    }

    {
      DavMove davMove = new DavMove(TestContext.getContextAuthorized());
      davMove.setResourcePath(SRC_WORKSPACE + SRC_NAME);
      davMove.setDestinationPath(DEST_WORKSPACE + DEST_NAME);
      assertEquals(Const.HttpStatus.CREATED, davMove.execute());
    }
    
    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
      davDelete.setResourcePath(DEST_WORKSPACE + DEST_NAME);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }
    
    log.info("done.");
  }

}
