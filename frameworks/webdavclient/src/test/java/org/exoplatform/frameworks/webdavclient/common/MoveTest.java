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
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.commands.DavMove;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class MoveTest extends TestCase {
  
  private static final String SRC_WORKSPACE = "/production";
  private static final String DEST_WORKSPACE = "/backup";
  
  private static String getSrcName() {
    return "/test folder source " + System.currentTimeMillis();
  }
  
  private static String getDestinationName() {
    return "/test folder destination " + System.currentTimeMillis(); 
  }
  
  public void testNotAuthorized() throws Exception {
    Log.info("MoveTest:testNotAuthorized...");
    
    String sourceName = getSrcName();
    String destinationName = getDestinationName();
    
    DavMove davMove = new DavMove(TestContext.getContext());
    davMove.setResourcePath(SRC_WORKSPACE + sourceName);
    davMove.setDestinationPath(DEST_WORKSPACE + destinationName);    
    assertEquals(Const.HttpStatus.AUTHNEEDED, davMove.execute());    
    
    Log.info("done.");
  }
  
  public void testMoveToSameWorkspace() throws Exception {
    Log.info("MoveTest:testMoveToSameWorkspace...");
    
    String sourceName = getSrcName();
    String destinationName = getDestinationName();
    
    {
      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
      davMkCol.setResourcePath(SRC_WORKSPACE + sourceName);
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    }
    
    {
      DavMove davMove = new DavMove(TestContext.getContextAuthorized());
      davMove.setResourcePath(SRC_WORKSPACE + sourceName);
      davMove.setDestinationPath(SRC_WORKSPACE + destinationName);
      assertEquals(Const.HttpStatus.CREATED, davMove.execute());
    }
    
    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
      davDelete.setResourcePath(SRC_WORKSPACE + destinationName);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }    
    
    Log.info("done.");
  }
  
  public void testMoveToAnotherWorkspace() throws Exception {
    Log.info("MoveTest:testMoveToAnotherWorkspace...");
    
    String sourceName = getSrcName();
    String destinationName = getDestinationName();
    
    {
      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
      davMkCol.setResourcePath(SRC_WORKSPACE + sourceName);
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    }

    {
      DavMove davMove = new DavMove(TestContext.getContextAuthorized());
      davMove.setResourcePath(SRC_WORKSPACE + sourceName);
      davMove.setDestinationPath(DEST_WORKSPACE + destinationName);
      assertEquals(Const.HttpStatus.CREATED, davMove.execute());
    }
    
    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
      davDelete.setResourcePath(DEST_WORKSPACE + destinationName);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }
    
    Log.info("done.");
  }

}
