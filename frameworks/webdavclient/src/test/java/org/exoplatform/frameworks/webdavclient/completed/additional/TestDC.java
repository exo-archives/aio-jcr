/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.completed.additional;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.commands.DavReport;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class TestDC extends TestCase {
  
  private static Log log = ExoLogger.getLogger("jcr.TestDC");
  
  public void testDCMultivalue() throws Exception {
    log.info("testDCMultivalue...");
    
    //String srcFolder = "/production/test folder " + System.currentTimeMillis();
    //String srcPath = srcFolder + "/test file.txt";

    String srcFolder = "/production";
    String srcPath = srcFolder + "/webdav.pdf";    
    
//    {
//      DavMkCol mkCol = new DavMkCol(TestContext.getContextAuthorized());
//      mkCol.setResourcePath(srcFolder);
//      
//      //mkCol.setMixType("dc:elementSet");
//      
//      log.info("mkcol status:" + mkCol.execute());      
//    }
//    
//    {
//      DavPut davPut = new DavPut(TestContext.getContextAuthorized());
//      davPut.setResourcePath(srcPath);      
//      //davPut.setMixType("dc:elementSet");
//      
//      davPut.setRequestDataBuffer("FILE CONTENT".getBytes());
//      
//      davPut.execute();
//    }

//    {
//      DavCheckOut davCheckOut = new DavCheckOut(TestContext.getContextAuthorized());
//      davCheckOut.setResourcePath(srcPath);
//      log.info("VERSION-CONTROL STATUS: " + davCheckOut.execute());      
//    }    
    
//    {
//      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
//      davPropFind.setResourcePath(srcFolder);
//      
//      davPropFind.setRequiredProperty(Const.DavProp.DISPLAYNAME);
//      davPropFind.setRequiredProperty(Const.DavProp.RESOURCETYPE);
//      davPropFind.setRequiredProperty(Const.DavProp.GETCONTENTLENGTH);
//      davPropFind.setRequiredProperty(Const.DavProp.CREATORDISPLAYNAME);
//      
//      
////      davPropFind.setResourcePath(srcPath);
////      davPropFind.setRequiredProperty("dc:description");
////      davPropFind.setRequiredProperty("dc:creator");
////      davPropFind.setRequiredProperty("jcr:mimeType");
//      
//      log.info("status: " + davPropFind.execute());
//      
//      String reply = new String(davPropFind.getResponseDataBuffer());
//      log.info("reply: " + reply);
//    }

//    {
//      DavPropPatch propPatch = new DavPropPatch(TestContext.getContextAuthorized());
//      propPatch.setResourcePath(srcPath);
//      
//      propPatch.setProperty("dc:description", "owner test MMMMMMMMMMMMMMMM");
//      propPatch.setProperty("dc:description", "owner test ((((((((((((((");
//      propPatch.setProperty("dc:description", "owner test 0000000000000000");
//      
//      propPatch.setProperty("jcr:mimeType", "aplication/pdf/xz");
//      
//      propPatch.setProperty("dc:creator", "gavrik-vetal AAAAAAAAAAAAAAAA");
//      
//      log.info("proppatch status: " + propPatch.execute());
//      log.info("reply: " + new String(propPatch.getResponseDataBuffer()));
//    }  

//    {
//      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
//      davPropFind.setResourcePath(srcPath);
//      
//      davPropFind.setRequiredProperty("dc:description");
//      davPropFind.setRequiredProperty("dc:creator");
//      davPropFind.setRequiredProperty("jcr:mimeType");
//      
//      log.info("status: " + davPropFind.execute());
//      
//      String reply = new String(davPropFind.getResponseDataBuffer());
//      log.info("reply: " + reply);
//    }

//    {
//      DavCheckIn davCheckIn = new DavCheckIn(TestContext.getContextAuthorized());
//      davCheckIn.setResourcePath(srcPath);
//      log.info("VERSION-CONTROL STATUS: " + davCheckIn.execute());            
//    }
    
    
//    {
//      DavVersionControl davVersionControl = new DavVersionControl(TestContext.getContextAuthorized());
//      davVersionControl.setResourcePath(srcPath);
//      log.info("VERSION-CONTROL STATUS: " + davVersionControl.execute());
//    }
//    
//    {
//      DavCheckIn davCheckIn = new DavCheckIn(TestContext.getContextAuthorized());
//      davCheckIn.setResourcePath(srcPath);
//      log.info("VERSION-CONTROL STATUS: " + davCheckIn.execute());      
//    }
//
//    {
//      DavCheckOut davCheckOut = new DavCheckOut(TestContext.getContextAuthorized());
//      davCheckOut.setResourcePath(srcPath);
//      log.info("VERSION-CONTROL STATUS: " + davCheckOut.execute());      
//    }    
//
//    {
//      DavCheckIn davCheckIn = new DavCheckIn(TestContext.getContextAuthorized());
//      davCheckIn.setResourcePath(srcPath);
//      log.info("VERSION-CONTROL STATUS: " + davCheckIn.execute());      
//    }
    
      {
        DavReport davReport = new DavReport(TestContext.getContextAuthorized());
        davReport.setResourcePath(srcPath);

        davReport.setRequiredProperty(Const.DavProp.DISPLAYNAME);
        davReport.setRequiredProperty(Const.DavProp.RESOURCETYPE);

//      davPropFind.setRequiredProperty(Const.DavProp.DISPLAYNAME);
//      davPropFind.setRequiredProperty(Const.DavProp.RESOURCETYPE);
//      davPropFind.setRequiredProperty(Const.DavProp.GETCONTENTLENGTH);
        davReport.setRequiredProperty(Const.DavProp.CREATORDISPLAYNAME);
        
        davReport.setRequiredProperty("dc:description");
        davReport.setRequiredProperty("dc:creator");
        davReport.setRequiredProperty("dc:contributor");
        davReport.setRequiredProperty("jcr:mimeType");      
        
        log.info("Davreport: " + davReport.execute());      
        log.info("reply: " + new String(davReport.getResponseDataBuffer()));
      }
    
//    {
//      DavPropPatch propPatch = new DavPropPatch(TestContext.getContextAuthorized());
//      propPatch.setResourcePath(srcPath);
//      
//      propPatch.removeProperty("dc:creator");
//      
//      log.info("proppatch status: " + propPatch.execute());
//      log.info("reply: " + new String(propPatch.getResponseDataBuffer()));
//    }  
//
//    {
//      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
//      davPropFind.setResourcePath(srcPath);
//      
//      davPropFind.setRequiredProperty("dc:description");
//      davPropFind.setRequiredProperty("dc:creator");
//      davPropFind.setRequiredProperty("jcr:mimeType");
//      
//      log.info("status: " + davPropFind.execute());
//      
//      String reply = new String(davPropFind.getResponseDataBuffer());
//      log.info("reply: " + reply);
//    }
    
      Thread.sleep(2000);
      
    log.info("done.");
  }
  
  public void testDC_PROPFIND() throws Exception {
    if (true) {
      return;
    }
    log.info("testDC_PROPFIND...");
    
    String srcFolder = "/production/test_folder_" + System.currentTimeMillis();
    String srcName = srcFolder + "test_file.txt";  

//    DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
//    davMkCol.setResourcePath(srcFolder);   
//    log.info("mkcol status: " + davMkCol.execute());

//    File file = new File("D:/exo/projects/v2.x/exo-jcr/frameworks/davclient/src/presets/test_file.doc ");
//    FileInputStream inS = new FileInputStream(file);
//    
//    log.info("INS AVAILABLE: " + inS.available());
//    
//    byte []buffer = new byte[inS.available()];
//    int readed = inS.read(buffer);
//    
//    DavPut davPut = new DavPut(TestContext.getContextAuthorized());
//    davPut.setResourcePath(srcName);    
//    davPut.setRequestDataBuffer(buffer);
//    log.info("put status: " + davPut.execute());

//    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
//    davPropFind.setResourcePath(srcName);    
//    int status = davPropFind.execute();
//    log.info("propfind status: " + status);
//    
//    String reply = new String(davPropFind.getResponseDataBuffer());
//    log.info(reply);
    
    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath("/production/test/");
    //davPropFind.setResourcePath("/production/test/jsr170-1.0.pdf");
    
    davPropFind.setRequiredProperty("dc:contributor");
    davPropFind.setRequiredProperty("dc:creator");
    davPropFind.setRequiredProperty("dc:date");
    davPropFind.setRequiredProperty("dc:publisher");
    davPropFind.setRequiredProperty("dc:title");
    
    davPropFind.setRequiredProperty(Const.DavProp.DISPLAYNAME);
    davPropFind.setRequiredProperty(Const.DavProp.GETCONTENTLENGTH);
    
    davPropFind.setRequiredProperty("contributor123");
    davPropFind.setRequiredProperty(Const.DavProp.GETCONTENTTYPE);
    
    davPropFind.setRequiredProperty("jcr:uuid");
    
//dc:contributor
//dc:creator
//dc:date
//dc:publisher
//dc:title
//jcr:data
//jcr:lastModified
//jcr:mimeType    
    
    int status = davPropFind.execute();
    log.info("STATUS: " + status);
    String reply = new String(davPropFind.getResponseDataBuffer());
    log.info(reply);
    
    Thread.sleep(1000);
    
//    if (status == Const.HttpStatus.MULTISTATUS){
//      Multistatus multisra
//    }
    
    log.info("done.");    
  }

}
