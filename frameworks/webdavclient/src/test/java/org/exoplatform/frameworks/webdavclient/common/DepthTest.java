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
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class DepthTest extends TestCase {

  private static Log log = ExoLogger.getLogger(Const.DAV_PREFIX + "DavDepthTest");
  
  public void test_REPOSITORY_RES() throws Exception {
    log.info("Run...");
    
    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath("/");
    davPropFind.setDepth(Const.DavDepth.INFINITY);

    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    byte []data = davPropFind.getResponseDataBuffer();
    log.info(">>> " + new String(data));
    
    log.info("Success.");
  }
  
  public void test_WORKSPACE_RES() throws Exception {
    log.info("Run...");
    
    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath("/production");
    davPropFind.setDepth(Const.DavDepth.INFINITY);
    
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    byte []data = davPropFind.getResponseDataBuffer();
    log.info(">>> " + new String(data));
    
    log.info("Success.");
  }
  
  public void test_JCR_RES() throws Exception {
    log.info("Run...");
    
    String RES = "/production/jcr_res_folder";
    
    DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
    davMkCol.setResourcePath(RES);
    assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    
    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath(RES);
    davPropFind.setDepth(Const.DavDepth.INFINITY);

    
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    byte []data = davPropFind.getResponseDataBuffer();
    log.info(">>> " + new String(data));
    
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(RES);
    
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    
    log.info("Success.");

  }

//  public void testDavDepth_JEREMITEST_SUCCESS() throws Exception {
//    log.info("Test JEREMI REPOSITORY :) ...");
//    
//    ServerLocation location = new ServerLocation();
//    location.setHost("jeremi.info");
//    location.setPort(8080);
//    location.setServletPath("/pengyou/repository/default");
//    
//    location.setUserId("test");
//    location.setUserPass("test");
//    
//    DavPropFind davPropFind = new DavPropFind(location);
//    davPropFind.setResourcePath("/");
//    davPropFind.setDepth(1);
//
//    int status = davPropFind.execute();
//    log.info("STATUS - " + status);
//
//    log.info("---------------------------------------------");
//    
//    Thread.sleep(1000);
//    System.out.println();
//    
//    // formatter out
//    
//    byte []response = davPropFind.getResponseDataBuffer();
//    
//    String ss = new String(response);
//    System.out.println(ss);
//    
////    File f = new File("/jeremi.xml");    
////    log.info("CREATED: " + f.createNewFile());
////    FileOutputStream fous = new FileOutputStream(f);
////    fous.write(response);
////    fous.close();
//    
//    Thread.sleep(1000);
//    
//    log.info("---------------------------------------------");    
//    
//    log.info("Test REPOSITORY complete.");
//  }

//  public void testDavDepth_JEREMITEST_FAULIRE() throws Exception {
//    log.info("Test JEREMI REPOSITORY :) ...");
//    
//    ServerLocation location = new ServerLocation();
//    location.setHost("jeremi.info");
//    location.setPort(8080);
//    location.setServletPath("/pengyou/repository/default");
//    
//    location.setUserId("test");
//    location.setUserPass("test");
//    
//    DavPropFind davPropFind = new DavPropFind(location);
//    davPropFind.setResourcePath("/");
//    davPropFind.setDepth(2);        // <<<<<<<<<<<<<<<<<<<<<<<<
//
//    int status = davPropFind.execute();
//    log.info("STATUS - " + status);
//
//    log.info("---------------------------------------------");
//    
//    Thread.sleep(1000);
//    System.out.println();
//    
//    byte []response = davPropFind.getResponseDataBuffer();    
//    String ss = new String(response);
//    System.out.println(ss);
//    
//    Thread.sleep(1000);
//    
//    log.info("---------------------------------------------");    
//    
//    log.info("Test REPOSITORY complete.");
//  }
  
  
//  public void testDavDepth_REPOSITORY_1() throws Exception {
//    log.info("Test REPOSITORY...");
//    
//    DavPropFind davPropFind = new DavPropFind(TestConst.getTestServerLocationAuthorized());
//    davPropFind.setResourcePath("/");
//    davPropFind.setDepth(Const.DavDepth.INFINITY);
//
//    int status = davPropFind.execute();
//    log.info("STATUS - " + status);
//
//    byte []resp = davPropFind.getResponseDataBuffer();
//
//    File f = new File("/eXo-response.xml");    
//    log.info("CREATED: " + f.createNewFile());
//    FileOutputStream fous = new FileOutputStream(f);
//    fous.write(resp);
//    fous.close();
//    
//    String ss = new String(resp);
//    
//    log.info("---------------------------------------------");
//    
//    Thread.sleep(1000);
//    System.out.println();
//    System.out.println(ss);
//    Thread.sleep(1000);
//    
//    log.info("---------------------------------------------");
//    
//    Multistatus multistatus = (Multistatus)davPropFind.getMultistatus();
//    ArrayList<ResponseDoc> responses = multistatus.getResponses();
//    
//    for (int i = 0; i < responses.size(); i++) {
//      ResponseDoc response = responses.get(i);
//      log.info("HREF: [" + response.getHref() + "]");
//    }
//    
//    log.info("---------------------------------------------");
//    log.info("RESPONSES: " + responses.size());
//    
//    log.info("Test REPOSITORY complete.");
//  }

//  public void testDavDepth_REPOSITORY_2() throws Exception {
//    log.info("Test REPOSITORY...");
//    
//    DavPropFind davPropFind = new DavPropFind(TestConst.getTestServerLocationAuthorized());
//    davPropFind.setResourcePath("/");
//    davPropFind.setDepth(2);
//
//    int status = davPropFind.execute();
//    log.info("STATUS - " + status);
//
//    String ss = new String(davPropFind.getResponseDataBuffer());
//    log.info("---------------------------------------------");
//    
//    Thread.sleep(1000);
//    System.out.println();
//    System.out.println(ss);
//    Thread.sleep(1000);
//    
//    log.info("---------------------------------------------");    
//    
//    log.info("Test REPOSITORY complete.");
//  }

//  public void testDavDepth_REPOSITORY_3() throws Exception {
//    log.info("Test REPOSITORY...");
//    
//    DavPropFind davPropFind = new DavPropFind(TestConst.getTestServerLocationAuthorized());
//    davPropFind.setResourcePath("/");
//    davPropFind.setDepth(3);
//
//    int status = davPropFind.execute();
//    log.info("STATUS - " + status);
//
//    String ss = new String(davPropFind.getResponseDataBuffer());
//    log.info("---------------------------------------------");
//    
//    Thread.sleep(1000);
//    System.out.println();
//    System.out.println(ss);
//    Thread.sleep(1000);
//    
//    log.info("---------------------------------------------");    
//    
//    log.info("Test REPOSITORY complete.");
//  }
  
  
//  public void testDavDepth_WORKSPACE() throws Exception {
//    log.info("Test WORKSPACE...");
//    
//    DavPropFind davPropFind = new DavPropFind(TestConst.getTestServerLocationAuthorized());
//    davPropFind.setResourcePath("/production");
//    davPropFind.setDepth(2);
//
//    int status = davPropFind.execute();
//    log.info("STATUS - " + status);
//
//    String ss = new String(davPropFind.getResponseDataBuffer());
//    log.info("---------------------------------------------");
//    
//    Thread.sleep(1000);
//    System.out.println();
//    System.out.println(ss);
//    Thread.sleep(1000);
//    
//    log.info("---------------------------------------------");    
//    
//    log.info("Test WORKSPACE complete.");
//  }
  
//  public void testDavDepth_JCR() throws Exception {
//    log.info("Test WORKSPACE...");
//    
//    DavPropFind davPropFind = new DavPropFind(TestConst.getTestServerLocationAuthorized());
//    davPropFind.setResourcePath("/production/myfolder");
//    davPropFind.setDepth(2);
//
//    int status = davPropFind.execute();
//    log.info("STATUS - " + status);
//
//    String ss = new String(davPropFind.getResponseDataBuffer());
//    log.info("---------------------------------------------");
//    
//    Thread.sleep(1000);
//    System.out.println();
//    System.out.println(ss);
//    Thread.sleep(1000);
//    
//    log.info("---------------------------------------------");    
//    
//    log.info("Test WORKSPACE complete.");    
//  }
  
}
