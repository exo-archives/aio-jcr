/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.common;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavCheckIn;
import org.exoplatform.frameworks.webdavclient.commands.DavCheckOut;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.commands.DavPut;
import org.exoplatform.frameworks.webdavclient.commands.DavReport;
import org.exoplatform.frameworks.webdavclient.commands.DavVersionControl;
import org.exoplatform.frameworks.webdavclient.commands.MultistatusCommand;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class NullHrefTest extends TestCase {
  
  private static Log log = ExoLogger.getLogger("jcr.NullHrefTest");

  private void localTestHrefsForPath(String pathName, boolean isVersionReport) throws Exception {    
    MultistatusCommand command;
    
    if (isVersionReport) {
      command = new DavReport(TestContext.getContextAuthorized());
    } else {
      command = new DavPropFind(TestContext.getContextAuthorized());
    }
    
    command.setResourcePath(pathName);
    
    command.setRequiredProperty(Const.DavProp.DISPLAYNAME);
    assertEquals(Const.HttpStatus.MULTISTATUS, command.execute());

    Multistatus multistatus = (Multistatus)command.getMultistatus();
    ArrayList<ResponseDoc> responses = multistatus.getResponses();
    for (int i = 0; i < responses.size(); i++) {
      ResponseDoc response = responses.get(i);

      if ("".equals(response.getHref())) {
        fail();
      }

    }    
  }
  
  public void testNullHref_ROOT() throws Exception {    
    log.info("testNullHref_ROOT...");        
    
    localTestHrefsForPath("/", false);
    
    log.info("done.");
  }
  
  public void testNullHref_WORKSPACES() throws Exception {
    log.info("testNullHref_WORKSPACES...");
    
    localTestHrefsForPath("/production", false);
    
    log.info("done.");
  }
  
  public void testNullHref_COLLECTIONS() throws Exception {
    log.info("testNullHref_COLLECTIONS...");
    
    String srcPath = "/production/test_folder_" + System.currentTimeMillis();
    
    DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
    davMkCol.setResourcePath(srcPath);
    assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());

    localTestHrefsForPath(srcPath, false);
    
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(srcPath);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());    
    
    log.info("done.");
  }
  
  public void testNullHref_FILES() throws Exception {
    log.info("testNullHref_FILES...");
    
    String path = "/production/test_foolder_" + System.currentTimeMillis();
    
    DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
    davMkCol.setResourcePath(path);
    assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    
    for (int i = 0; i < 5; i++) {
      String srcPath = path + "/test_file_" + i + ".txt";      
      
      DavPut davPut = new DavPut(TestContext.getContextAuthorized());
      davPut.setResourcePath(srcPath);
      davPut.setRequestDataBuffer(("FILE CONTENT " + i).getBytes());
      assertEquals(Const.HttpStatus.CREATED, davPut.execute());
    }

    localTestHrefsForPath(path, false);
    
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(path);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());    
    
    log.info("done.");
  }
  
  public void testNullHref_REPORT() throws Exception {
    log.info("testNullHref_REPORT...");

    String srcPath = "/production/test_file_" + System.currentTimeMillis() + ".txt";
    
    {
      DavPut davPut = new DavPut(TestContext.getContextAuthorized());
      davPut.setResourcePath(srcPath);    
      davPut.setRequestDataBuffer("CONTENT FOR VERSION 1".getBytes());    
      assertEquals(Const.HttpStatus.CREATED, davPut.execute());
    }

    localTestHrefsForPath(srcPath, false);

    {
      DavVersionControl davVersionControl = new DavVersionControl(TestContext.getContextAuthorized());
      davVersionControl.setResourcePath(srcPath);
      assertEquals(Const.HttpStatus.OK, davVersionControl.execute());      
    }    
    
    localTestHrefsForPath(srcPath, true);
    
    {
      DavCheckIn davCheckIn = new DavCheckIn(TestContext.getContextAuthorized());
      davCheckIn.setResourcePath(srcPath);
      assertEquals(Const.HttpStatus.OK, davCheckIn.execute());      
    }
    
    localTestHrefsForPath(srcPath, true);

    {
      DavCheckOut davCheckOut = new DavCheckOut(TestContext.getContextAuthorized());
      davCheckOut.setResourcePath(srcPath);
      assertEquals(Const.HttpStatus.OK, davCheckOut.execute());      
      DavCheckIn davCheckIn = new DavCheckIn(TestContext.getContextAuthorized());
      davCheckIn.setResourcePath(srcPath);
      assertEquals(Const.HttpStatus.OK, davCheckIn.execute());      
    }

    localTestHrefsForPath(srcPath, true);    
    
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(srcPath);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());        
    
    log.info("done.");
  }
  
  public void testHrefEscaped() throws Exception {
    log.info("testHrefEscaped...");
    
    String srcPath = "/production";
    String spacedPath = srcPath + "/test folder 1 2 3 4 5";
    
    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(srcPath);    
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());      
    }
    
    {
      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
      davMkCol.setResourcePath(spacedPath);
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    }
    
    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(srcPath);
      
      davPropFind.setRequiredProperty(Const.DavProp.DISPLAYNAME);
      
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    }
    
    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
      davDelete.setResourcePath(spacedPath);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }    
    
    log.info("done.");
  }

}
