/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.compatibility;

import java.io.File;
import java.io.FileOutputStream;

import junit.framework.TestCase;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.WebDavContext;
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.http.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class TestDavFS2 extends TestCase {
  
  //private static final WebDavContext context = new WebDavContext("192.168.0.5", 8080, "/rest/jcr/repository/production", "admin", "admin");
  
  private static final WebDavContext context = new WebDavContext("192.168.0.5", 80, "/davtest");

  public static final String TSTFOLDERNAME = "/test_folder_davfs2" + System.currentTimeMillis() + "/"; 
  
//  public void testSimple() throws Exception {    
//    Log.info("simpleTest...");
//    
//    try {
//      DavPropFind davPropFind = new DavPropFind(context);
//      davPropFind.setResourcePath("/");
//      
//      try {
//        davPropFind.setRequiredProperty("displayname");
//        davPropFind.setRequiredProperty("resourcetype");
//        davPropFind.setRequiredProperty("getcontentlength");
//        
//        int status = davPropFind.execute();
//        Log.info("STATUS: " + status);
//        
//      } catch (Exception exc) {
//        Log.info("Unhandled exception. " + exc.getMessage(), exc);
//      }
//      
//      if (davPropFind.getResponseDataBuffer() != null) {
//        String reply = new String(davPropFind.getResponseDataBuffer());
//        Log.info("REPLY: " + reply);
//        
//        String fileName = "D:\\exo\\projects\\exoprojects\\jcr\\trunk\\frameworks\\webdavclient\\tst.xml";
//        File file = new File(fileName);
//        FileOutputStream fileOut = new FileOutputStream(file);
//        fileOut.write(davPropFind.getResponseDataBuffer());
//        fileOut.close();
//      }
//     
//    } catch (Exception exc) {
//      Log.info("Unhandled exception. " + exc.getMessage());
//    }
//    
//    Log.info("Done.");
//  }
  
//  public void testLock() throws Exception {
//    Log.info("testLock...");
//    
//    try {      
//      DavLock davLock = new DavLock(context);
//      davLock.setResourcePath(TSTFOLDERNAME);
//      davLock.setDepth(0);
//      
//      int status = davLock.execute();
//      Log.info("LOCK STATUS: " + status);
//      
////    <?xml version="1.0" encoding="utf-8"?>
////    <lockinfo xmlns='DAV:'>
////     <lockscope><exclusive/></lockscope>
////    <locktype><write/></locktype><owner>admin/davfs2 1.1.2</owner>
////    </lockinfo> 
//
//      if (status == Const.HttpStatus.OK || 
//          status == Const.HttpStatus.CREATED) {
////        DavMkCol davMkCol = new DavMkCol(context);
////        
////        davMkCol.setResourcePath(TSTFOLDERNAME);
////        
////        int mkColStatus = davMkCol.execute();
////        Log.info("MKCOL STATUS: " + mkColStatus);
//        
//        DavPropFind davPropFind = new DavPropFind(context);
//        davPropFind.setResourcePath(TSTFOLDERNAME);
//        
//        davPropFind.setRequiredProperty("displayname");
//        davPropFind.setRequiredProperty("lockdiscovery");
//
//        status = davPropFind.execute();
//        Log.info("PRIOPFIND STATUS: " + status);
//        
//        String reply = new String(davPropFind.getResponseDataBuffer());
//        Log.info("PROPFIND REPLY:\r\n" + reply);
//        
//        String fileName = "D:\\exo\\projects\\exoprojects\\jcr\\trunk\\frameworks\\webdavclient\\tst.xml";
//        File file = new File(fileName);
//        FileOutputStream fileOut = new FileOutputStream(file);
//        fileOut.write(davPropFind.getResponseDataBuffer());
//        fileOut.close();        
//      }
//      
//    } catch (Exception exc) {
//      Log.info("Unhandled exception. " + exc.getMessage());
//    }
//    
//    Log.info("Done.");
//  }
  
  public void testCreateFile() throws Exception {
    Log.info("testCreateFile...");
    
    String folderName = "testfolder" + System.currentTimeMillis();
    
    {
      DavMkCol davMkCol = new DavMkCol(context);
      davMkCol.setResourcePath("/" + folderName + "/");      
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
      
      Log.info("FOLDER CREATED...");
    }
    
    {
      DavPropFind davPropFind = new DavPropFind(context);
      davPropFind.setResourcePath("/" + folderName + "/");
      
      davPropFind.setRequiredProperty("displayname");
      davPropFind.setRequiredProperty("getetag");
      davPropFind.setRequiredProperty("getcontentlength");
      davPropFind.setRequiredProperty("creationdate");
      davPropFind.setRequiredProperty("getlastmodified");
      davPropFind.setRequiredProperty("resourcetype");
      
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
      
      String reply = new String(davPropFind.getResponseDataBuffer());
      Log.info("REPLY: " + reply);
      
      String fileName = "D:\\exo\\projects\\exoprojects\\jcr\\trunk\\frameworks\\webdavclient\\tst.xml";
      File file = new File(fileName);
      FileOutputStream fileOut = new FileOutputStream(file);
      fileOut.write(davPropFind.getResponseDataBuffer());
      fileOut.close();      
    }
    
    Log.info("done.");
  }

}
