/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.completed;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.commands.DavOrderPatch;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.commands.DavPut;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.frameworks.webdavclient.order.DOrderMember;
import org.exoplatform.frameworks.webdavclient.order.OrderConst;
import org.exoplatform.frameworks.webdavclient.properties.DisplayNameProp;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class OrderPatchTest extends TestCase {
  
  private static Log log = ExoLogger.getLogger("jcr.OrderPatchTest");
  
  public static final String RES_PATH = "/production/test_folder";
  
  public void setUp() throws Exception {
    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(RES_PATH);
      if (davPropFind.execute() == Const.HttpStatus.MULTISTATUS) {
        DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
        davDelete.setResourcePath(RES_PATH);
        assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
      }
    }
    
    {
      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
      davMkCol.setResourcePath(RES_PATH);
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
      
      for (int i = 1; i <= 5; i++) {
        DavPut davPut = new DavPut(TestContext.getContextAuthorized());
        davPut.setResourcePath(RES_PATH + "/" + i + ".txt");
        davPut.setRequestDataBuffer(("Content for " + i).getBytes());
        assertEquals(Const.HttpStatus.CREATED, davPut.execute());
      }      
    }
    
    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(RES_PATH);      
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());

      Multistatus multistatus = (Multistatus)davPropFind.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();
      for (int i = 0; i < responses.size(); i++) {
        log.info("FILE: " + responses.get(i).getHref());
      }      
    }
    
  }
  
  protected void tearDown() throws Exception {
    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath(RES_PATH);
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(RES_PATH);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
  }

  public void testOrderFirst() throws Exception {
    log.info("Run order first...");
    
    String FILENAME = "3.txt";
    
    DavOrderPatch davOrderPatch = new DavOrderPatch(TestContext.getContextAuthorized());
    davOrderPatch.setResourcePath(RES_PATH);
    
    DOrderMember member = new DOrderMember();
    member.setSegment(FILENAME);
    member.setPosition(OrderConst.FIRST);
    
    davOrderPatch.addMember(member);

    assertEquals(Const.HttpStatus.OK, davOrderPatch.execute());
    
    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(RES_PATH);
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());

      Multistatus multistatus = (Multistatus)davPropFind.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();

      DisplayNameProp property = (DisplayNameProp)responses.get(1).getProperty(Const.DavProp.DISPLAYNAME);
      assertEquals(FILENAME, property.getDisplayName());
    }    
    
    log.info("Done.");    
  }
  
  public void testOrderLast() throws Exception {
    log.info("Run order last...");
    
    String FILENAME = "2.txt";
    
    DavOrderPatch davOrderPatch = new DavOrderPatch(TestContext.getContextAuthorized());
    davOrderPatch.setResourcePath(RES_PATH);
    
    DOrderMember member = new DOrderMember();
    member.setSegment(FILENAME);
    member.setPosition(OrderConst.LAST);
    
    davOrderPatch.addMember(member);    
    assertEquals(Const.HttpStatus.OK, davOrderPatch.execute());

    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(RES_PATH);
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());

      Multistatus multistatus = (Multistatus)davPropFind.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();

      DisplayNameProp property = (DisplayNameProp)responses.get(5).getProperty(Const.DavProp.DISPLAYNAME);
      assertEquals(FILENAME, property.getDisplayName());
    }    
    
    log.info("Done.");    
    
  }

  public void testOrderBefore() throws Exception {
    log.info("Run order before...");
    
    String FILENAME = "2.txt";
    String FILEPOS = "4.txt";
    
    DavOrderPatch davOrderPatch = new DavOrderPatch(TestContext.getContextAuthorized());
    davOrderPatch.setResourcePath(RES_PATH);
    
    DOrderMember member = new DOrderMember();
    member.setSegment(FILENAME);
    member.setPosition(OrderConst.BEFORE, FILEPOS);
    
    davOrderPatch.addMember(member);    
    assertEquals(Const.HttpStatus.OK, davOrderPatch.execute());

    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(RES_PATH);
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());

      Thread.sleep(1000);
      
      Multistatus multistatus = (Multistatus)davPropFind.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();
      for (int i = 0; i < responses.size(); i++) {
        log.info("HREF: " + responses.get(i).getHref());
      }
      
      Thread.sleep(1000);

//      // 1 3 2 4 5
//      DisplayNameProp property = (DisplayNameProp)responses.get(3).getProperty(Const.DavProp.DISPLAYNAME);
//      assertEquals(FILENAME, property.getDisplayName());
    }    
    
    log.info("Done.");    
    
  }

  public void testOrderAfter() throws Exception {
    log.info("Run order after...");
    
    String FILENAME = "2.txt";
    String FILEPOS = "5.txt";
    
    DavOrderPatch davOrderPatch = new DavOrderPatch(TestContext.getContextAuthorized());
    davOrderPatch.setResourcePath(RES_PATH);
    
    DOrderMember member = new DOrderMember();
    member.setSegment(FILENAME);
    member.setPosition(OrderConst.AFTER, FILEPOS);
    
    davOrderPatch.addMember(member);    
    assertEquals(Const.HttpStatus.OK, davOrderPatch.execute());

    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(RES_PATH);
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());

      Thread.sleep(1000);
      
      Multistatus multistatus = (Multistatus)davPropFind.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();
      for (int i = 0; i < responses.size(); i++) {
        log.info("HREF: " + responses.get(i).getHref());
      }
      
      Thread.sleep(1000);

//      // 1 3 2 4 5
//      DisplayNameProp property = (DisplayNameProp)responses.get(3).getProperty(Const.DavProp.DISPLAYNAME);
//      assertEquals(FILENAME, property.getDisplayName());
    }    
    
    log.info("Done.");    
    
  }
  
  public void testOrderMulti() throws Exception {
    // 1 2 3 4 5    1 2 3 4 5
    // 1 before 4   2 3 1 4 5
    // 4 before 3   2 4 3 1 5
    // 2 last       4 3 1 5 2
    // 5 first      5 4 3 1 2
    // 3 after 2    5 4 1 2 3
    // 5 after 1    4 1 5 2 3

    DavOrderPatch davOrderPatch = new DavOrderPatch(TestContext.getContextAuthorized());
    davOrderPatch.setResourcePath(RES_PATH);
    
    // 1 before 4   2 3 1 4 5
    {
      DOrderMember member = new DOrderMember();
      member.setSegment("1.txt");
      member.setPosition(OrderConst.BEFORE, "4.txt");
      davOrderPatch.addMember(member);
    }
    
    // 4 before 3   2 4 3 1 5
    {
      DOrderMember member = new DOrderMember();
      member.setSegment("4.txt");
      member.setPosition(OrderConst.BEFORE, "3.txt");
      davOrderPatch.addMember(member);      
    }

    // 2 last       4 3 1 5 2
    {
      DOrderMember member = new DOrderMember();
      member.setSegment("2.txt");
      member.setPosition(OrderConst.LAST);
      davOrderPatch.addMember(member);      
    }

    // 5 first      5 4 3 1 2
    {
      DOrderMember member = new DOrderMember();
      member.setSegment("5.txt");
      member.setPosition(OrderConst.FIRST);
      davOrderPatch.addMember(member);      
    }

    // 3 after 2    5 4 1 2 3
    {
      DOrderMember member = new DOrderMember();
      member.setSegment("3.txt");
      member.setPosition(OrderConst.AFTER, "2.txt");
      davOrderPatch.addMember(member);      
    }

    // 5 after 1    4 1 5 2 3
    {      
      DOrderMember member = new DOrderMember();
      member.setSegment("5.txt");
      member.setPosition(OrderConst.AFTER, "1.txt");
      davOrderPatch.addMember(member);      
    }
    
    // try get status NOT FOUND
    {      
      DOrderMember member = new DOrderMember();
      member.setSegment("not existed file.txt");
      member.setPosition(OrderConst.FIRST);
      davOrderPatch.addMember(member);      
    }
    
    // try get FORBIDDEN
    {
      DOrderMember member = new DOrderMember();
      member.setSegment("1.txt");
      member.setPosition(OrderConst.AFTER, "not existed file.txt");
      davOrderPatch.addMember(member);            
    }

    int status = davOrderPatch.execute();
    log.info("STATUS: " + status);
    String reply = new String(davOrderPatch.getResponseDataBuffer());
    log.info("\r\n" + reply + "\r\n");
    
    if (status == Const.HttpStatus.MULTISTATUS) {
      Multistatus multistatus = (Multistatus)davOrderPatch.getMultistatus();
      
      log.info("MULTISTATUS HREFS:");
      
      ArrayList<ResponseDoc> responses = multistatus.getResponses();      
      for (int i = 0; i < responses.size(); i++) {
        log.info("HREF: " + responses.get(i).getHref());
      }
    }
    
  }
  
  
}
