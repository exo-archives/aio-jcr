/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.webdavclient.commands.DavCommand;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.commands.DavPut;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class TestUtils extends TestCase {

  private static Log log = ExoLogger.getLogger("jcr.TestUtils");
  
  public static void showMultistatus(Multistatus multistatus) {
    log.info(">>> multistatus ------------------------");
    ArrayList<ResponseDoc> responses = multistatus.getResponses();
    for (int i = 0; i < responses.size(); i++) {
      String href = responses.get(i).getHref();
      href = href.substring("http://localhost:8080/jcr-webdav/repository".length());
      log.info("HREH: [" + href + "]");
    }
    log.info(">>> ------------------------------------");
  }
  
  public static void createCollection(String path) throws Exception {
    DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
    davMkCol.setResourcePath(path);
    assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
  }
  
  public static void createFile(String path, byte []content) throws Exception {
    DavPut davPut = new DavPut(TestContext.getContextAuthorized());
    davPut.setResourcePath(path);
    davPut.setRequestDataBuffer(content);
    assertEquals(Const.HttpStatus.CREATED, davPut.execute());
  }
  
  public static void removeResource(String path) throws Exception {
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(path);    
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
  }
  
  public static void logXML(DavCommand davCommand) throws Exception {
    String xmlFileName = "D://exo/projects/exoprojects/jcr/trunk/frameworks/webdavclient/testlog.xml";
    File outFile = new File(xmlFileName);
    FileOutputStream logStream = new FileOutputStream(outFile);
    logStream.write(davCommand.getResponseDataBuffer());            
  }
  
}
