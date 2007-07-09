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
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class DepthTest extends TestCase {

  public void testRepositoryResource() throws Exception {
    Log.info("DepthTest:testRepositoryResource...");
    
    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath("/");
    davPropFind.setDepth(3);
    //davPropFind.setDepth(Const.DavDepth.INFINITY);

    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    Log.info("Success.");
  }
  
  public void testWorkspaceResource() throws Exception {
    Log.info("DepthTest:testWorkspaceResource...");
    
    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath("/production");
    davPropFind.setDepth(3);
    //davPropFind.setDepth(Const.DavDepth.INFINITY);
    
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    Log.info("Success.");
  }
  
  public void testJcrResource() throws Exception {
    Log.info("DepthTest:testJcrResource...");
    
    String RES = "/production/jcr_res_folder";
    
    DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
    davMkCol.setResourcePath(RES);
    assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    
    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath(RES);
    davPropFind.setDepth(Const.DavDepth.INFINITY);

    
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(RES);
    
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    
    Log.info("Success.");

  }

//  public void testDavDepth_JEREMITEST_SUCCESS() throws Exception {
//    Log.info("Test JEREMI REPOSITORY :) ...");
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
//    Log.info("STATUS - " + status);
//
//    Log.info("---------------------------------------------");
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
////    Log.info("CREATED: " + f.createNewFile());
////    FileOutputStream fous = new FileOutputStream(f);
////    fous.write(response);
////    fous.close();
//    
//    Thread.sleep(1000);
//    
//    Log.info("---------------------------------------------");    
//    
//    Log.info("Test REPOSITORY complete.");
//  }

//  public void testDavDepth_JEREMITEST_FAULIRE() throws Exception {
//    Log.info("Test JEREMI REPOSITORY :) ...");
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
//    Log.info("STATUS - " + status);
//
//    Log.info("---------------------------------------------");
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
//    Log.info("---------------------------------------------");    
//    
//    Log.info("Test REPOSITORY complete.");
//  }
  
  
//  public void testDavDepth_REPOSITORY_1() throws Exception {
//    Log.info("Test REPOSITORY...");
//    
//    DavPropFind davPropFind = new DavPropFind(TestConst.getTestServerLocationAuthorized());
//    davPropFind.setResourcePath("/");
//    davPropFind.setDepth(Const.DavDepth.INFINITY);
//
//    int status = davPropFind.execute();
//    Log.info("STATUS - " + status);
//
//    byte []resp = davPropFind.getResponseDataBuffer();
//
//    File f = new File("/eXo-response.xml");    
//    Log.info("CREATED: " + f.createNewFile());
//    FileOutputStream fous = new FileOutputStream(f);
//    fous.write(resp);
//    fous.close();
//    
//    String ss = new String(resp);
//    
//    Log.info("---------------------------------------------");
//    
//    Thread.sleep(1000);
//    System.out.println();
//    System.out.println(ss);
//    Thread.sleep(1000);
//    
//    Log.info("---------------------------------------------");
//    
//    Multistatus multistatus = (Multistatus)davPropFind.getMultistatus();
//    ArrayList<ResponseDoc> responses = multistatus.getResponses();
//    
//    for (int i = 0; i < responses.size(); i++) {
//      ResponseDoc response = responses.get(i);
//      Log.info("HREF: [" + response.getHref() + "]");
//    }
//    
//    Log.info("---------------------------------------------");
//    Log.info("RESPONSES: " + responses.size());
//    
//    Log.info("Test REPOSITORY complete.");
//  }

//  public void testDavDepth_REPOSITORY_2() throws Exception {
//    Log.info("Test REPOSITORY...");
//    
//    DavPropFind davPropFind = new DavPropFind(TestConst.getTestServerLocationAuthorized());
//    davPropFind.setResourcePath("/");
//    davPropFind.setDepth(2);
//
//    int status = davPropFind.execute();
//    Log.info("STATUS - " + status);
//
//    String ss = new String(davPropFind.getResponseDataBuffer());
//    Log.info("---------------------------------------------");
//    
//    Thread.sleep(1000);
//    System.out.println();
//    System.out.println(ss);
//    Thread.sleep(1000);
//    
//    Log.info("---------------------------------------------");    
//    
//    Log.info("Test REPOSITORY complete.");
//  }

//  public void testDavDepth_REPOSITORY_3() throws Exception {
//    Log.info("Test REPOSITORY...");
//    
//    DavPropFind davPropFind = new DavPropFind(TestConst.getTestServerLocationAuthorized());
//    davPropFind.setResourcePath("/");
//    davPropFind.setDepth(3);
//
//    int status = davPropFind.execute();
//    Log.info("STATUS - " + status);
//
//    String ss = new String(davPropFind.getResponseDataBuffer());
//    Log.info("---------------------------------------------");
//    
//    Thread.sleep(1000);
//    System.out.println();
//    System.out.println(ss);
//    Thread.sleep(1000);
//    
//    Log.info("---------------------------------------------");    
//    
//    Log.info("Test REPOSITORY complete.");
//  }
  
  
//  public void testDavDepth_WORKSPACE() throws Exception {
//    Log.info("Test WORKSPACE...");
//    
//    DavPropFind davPropFind = new DavPropFind(TestConst.getTestServerLocationAuthorized());
//    davPropFind.setResourcePath("/production");
//    davPropFind.setDepth(2);
//
//    int status = davPropFind.execute();
//    Log.info("STATUS - " + status);
//
//    String ss = new String(davPropFind.getResponseDataBuffer());
//    Log.info("---------------------------------------------");
//    
//    Thread.sleep(1000);
//    System.out.println();
//    System.out.println(ss);
//    Thread.sleep(1000);
//    
//    Log.info("---------------------------------------------");    
//    
//    Log.info("Test WORKSPACE complete.");
//  }
  
//  public void testDavDepth_JCR() throws Exception {
//    Log.info("Test WORKSPACE...");
//    
//    DavPropFind davPropFind = new DavPropFind(TestConst.getTestServerLocationAuthorized());
//    davPropFind.setResourcePath("/production/myfolder");
//    davPropFind.setDepth(2);
//
//    int status = davPropFind.execute();
//    Log.info("STATUS - " + status);
//
//    String ss = new String(davPropFind.getResponseDataBuffer());
//    Log.info("---------------------------------------------");
//    
//    Thread.sleep(1000);
//    System.out.println();
//    System.out.println(ss);
//    Thread.sleep(1000);
//    
//    Log.info("---------------------------------------------");    
//    
//    Log.info("Test WORKSPACE complete.");    
//  }
  
}
