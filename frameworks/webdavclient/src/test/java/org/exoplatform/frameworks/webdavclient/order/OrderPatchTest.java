/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.order;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.commands.DavOrderPatch;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.commands.DavPut;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.frameworks.webdavclient.properties.DisplayNameProp;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class OrderPatchTest extends TestCase {
  
  private static String resourcePath;
  
  public void setUp() throws Exception {
    resourcePath = "/production/test folder " + System.currentTimeMillis();
    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(resourcePath);
      if (davPropFind.execute() == Const.HttpStatus.MULTISTATUS) {
        DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
        davDelete.setResourcePath(resourcePath);
        assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
      }
    }
    
    {
      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
      davMkCol.setResourcePath(resourcePath);
      davMkCol.setNodeType("webdav:folder");
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
      
      for (int i = 1; i <= 5; i++) {
        DavPut davPut = new DavPut(TestContext.getContextAuthorized());
        davPut.setResourcePath(resourcePath + "/" + i + ".txt");
        davPut.setRequestDataBuffer(("Content for " + i).getBytes());
        assertEquals(Const.HttpStatus.CREATED, davPut.execute());
      }      
    }
    
    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(resourcePath);      
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());

      Multistatus multistatus = davPropFind.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();
      for (int i = 0; i < responses.size(); i++) {
        Log.info("FILE: " + responses.get(i).getHref());
      }      
    }
    
  }
  
  protected void tearDown() throws Exception {
    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath(resourcePath);
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(resourcePath);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
  }

//  public void testOrderFirst() throws Exception {
//    Log.info("Run order first...");
//    
//    String FILENAME = "3.txt";
//
//    // R 1 2 3 4 5
//    //  ^    | 
//    //  +----+ 
//    //
//    // R 3 1 2 4 5                
//    
//    DavOrderPatch davOrderPatch = new DavOrderPatch(TestContext.getContextAuthorized());
//    davOrderPatch.setResourcePath(resourcePath);
//    
//    DOrderMember member = new DOrderMember();
//    member.setSegment(FILENAME);
//    member.setPosition(OrderConst.FIRST);
//    
//    davOrderPatch.addMember(member);
//    
//    assertEquals(Const.HttpStatus.OK, davOrderPatch.execute());
//
//    {
//      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
//      davPropFind.setResourcePath(resourcePath);
//      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
//
//      Multistatus multistatus = davPropFind.getMultistatus();
//      ArrayList<ResponseDoc> responses = multistatus.getResponses();
//
//      // R 3 1 2 4 5                
//      String []fileNames = {"3.txt", "1.txt", "2.txt", "4.txt", "5.txt"};
//      for (int i = 1; i < responses.size(); i++) {
//        DisplayNameProp property = (DisplayNameProp)responses.get(i).getProperty(Const.DavProp.DISPLAYNAME);
//        assertEquals(fileNames[i - 1], property.getDisplayName());
//      }
//      
//    }    
//    
//    Log.info("Done.");    
//  }
//  
//  public void testOrderLast() throws Exception {
//    Log.info("Run order last...");
//    
//    String FILENAME = "2.txt";
//
//    // R 1 2 3 4 5
//    //     |      ^
//    //     +------+
//    //
//    // R 1 3 4 5 2    
//    
//    DavOrderPatch davOrderPatch = new DavOrderPatch(TestContext.getContextAuthorized());
//    davOrderPatch.setResourcePath(resourcePath);
//    
//    DOrderMember member = new DOrderMember();
//    member.setSegment(FILENAME);
//    member.setPosition(OrderConst.LAST);
//    
//    davOrderPatch.addMember(member);    
//    assertEquals(Const.HttpStatus.OK, davOrderPatch.execute());
//
//    {
//      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
//      davPropFind.setResourcePath(resourcePath);
//      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
//
//      Multistatus multistatus = davPropFind.getMultistatus();
//      ArrayList<ResponseDoc> responses = multistatus.getResponses();
//
//      // R 1 3 4 5 2    
//      String []fileNames = {"1.txt", "3.txt", "4.txt", "5.txt", "2.txt"};
//      for (int i = 1; i < responses.size(); i++) {
//        DisplayNameProp property = (DisplayNameProp)responses.get(i).getProperty(Const.DavProp.DISPLAYNAME);
//        assertEquals(fileNames[i - 1], property.getDisplayName());
//      }      
//    }    
//    
//    Log.info("Done.");    
//    
//  }
//
//  public void testOrderBefore() throws Exception {
//    Log.info("Run order before...");
//    
//    String FILENAME = "2.txt";
//    String FILEPOS = "4.txt";
//
//    // R 1 2 3 4 5
//    //     |  ^
//    //     +--+
//    //
//    // R 1 3 2 4 5
//    //       ^     
//    
//    DavOrderPatch davOrderPatch = new DavOrderPatch(TestContext.getContextAuthorized());
//    davOrderPatch.setResourcePath(resourcePath);
//    
//    DOrderMember member = new DOrderMember();
//    member.setSegment(FILENAME);
//    member.setPosition(OrderConst.BEFORE, FILEPOS);
//    
//    davOrderPatch.addMember(member);    
//    assertEquals(Const.HttpStatus.OK, davOrderPatch.execute());
//
//    {
//      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
//      davPropFind.setResourcePath(resourcePath);
//      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
//
//      Thread.sleep(1000);
//      
//      Multistatus multistatus = davPropFind.getMultistatus();
//      ArrayList<ResponseDoc> responses = multistatus.getResponses();
//      for (int i = 0; i < responses.size(); i++) {
//        Log.info("HREF: " + responses.get(i).getHref());
//      }
//      
//      Thread.sleep(1000);
//
//      // R 1 3 2 4 5
//      //       ^
//      String []fileNames = {"1.txt", "3.txt", "2.txt", "4.txt", "5.txt"};
//      for (int i = 1; i < responses.size(); i++) {
//        DisplayNameProp property = (DisplayNameProp)responses.get(i).getProperty(Const.DavProp.DISPLAYNAME);
//        assertEquals(fileNames[i - 1], property.getDisplayName());
//      }
//      
//    }    
//    
//    Log.info("Done.");
//  }
//
//  public void testOrderAfter() throws Exception {
//    Log.info("Run order after...");
//
//    String FILENAME = "2.txt";
//    String FILEPOS = "4.txt";
//
//    // R 1 2 3 4 5
//    //     |    ^
//    //     +----+
//    //
//    // R 1 3 4 2 5
//    //         ^
//    
//    DavOrderPatch davOrderPatch = new DavOrderPatch(TestContext.getContextAuthorized());
//    davOrderPatch.setResourcePath(resourcePath);
//    
//    DOrderMember member = new DOrderMember();
//    member.setSegment(FILENAME);
//    member.setPosition(OrderConst.AFTER, FILEPOS);
//    
//    davOrderPatch.addMember(member);    
//    assertEquals(Const.HttpStatus.OK, davOrderPatch.execute());
//
//    {
//      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
//      davPropFind.setResourcePath(resourcePath);
//      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
//
//      Thread.sleep(1000);
//      
//      Multistatus multistatus = davPropFind.getMultistatus();
//      ArrayList<ResponseDoc> responses = multistatus.getResponses();
//      for (int i = 0; i < responses.size(); i++) {
//        Log.info("HREF: " + responses.get(i).getHref());
//      }
//      
//      Thread.sleep(1000);
//
//      // R 1 3 4 2 5
//      //         ^
//      String []fileNames = {"1.txt", "3.txt", "4.txt", "2.txt", "5.txt"};
//      for (int i = 1; i < responses.size(); i++) {
//        DisplayNameProp property = (DisplayNameProp)responses.get(i).getProperty(Const.DavProp.DISPLAYNAME);
//        assertEquals(fileNames[i - 1], property.getDisplayName());
//      }
//    }    
//    
//    Log.info("Done.");    
//  }
  
//  public void test4Before3() throws Exception {
//    Log.info("test4Before3...");
//    
//    String FILENAME = "4.txt";
//    String FILEPOS = "3.txt";
//    
//    String path = "/production/test order 4before3" + System.currentTimeMillis(); 
//    
//    String []creates = {"2.txt", "3.txt", "1.txt", "4.txt", "5.txt"};
//
//    {
//      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
//      davMkCol.setResourcePath(path);
//      davMkCol.setNodeType("webdav:folder");
//      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
//      
//      for (int i = 0; i < 5; i++) {
//        DavPut davPut = new DavPut(TestContext.getContextAuthorized());
//        davPut.setResourcePath(path + "/" + creates[i]);
//        davPut.setRequestDataBuffer(("Content for " + (i + 1)).getBytes());
//        assertEquals(Const.HttpStatus.CREATED, davPut.execute());
//      }
//    }
//    
//    DavOrderPatch davOrderPatch = new DavOrderPatch(TestContext.getContextAuthorized());
//    davOrderPatch.setResourcePath(path);
//
//    DOrderMember member = new DOrderMember();
//    member.setSegment(FILENAME);
//    member.setPosition(OrderConst.BEFORE, FILEPOS);
//    
//    davOrderPatch.addMember(member);    
//    assertEquals(Const.HttpStatus.OK, davOrderPatch.execute());
//    
//    {
//      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
//      davPropFind.setResourcePath(path);
//      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
//
//      Thread.sleep(1000);
//      
//      Multistatus multistatus = davPropFind.getMultistatus();
//      ArrayList<ResponseDoc> responses = multistatus.getResponses();
//      for (int i = 0; i < responses.size(); i++) {
//        Log.info("HREF: " + responses.get(i).getHref());
//      }
//      
//      Thread.sleep(1000);
//
//      //String []creates = {"2.txt", "3.txt", "1.txt", "4.txt", "5.txt"};
//      // R 2 4 3 1 5
//      //         ^
//      String []fileNames = {"2.txt", "4.txt", "3.txt", "1.txt", "5.txt"};
//      for (int i = 1; i < responses.size(); i++) {
//        DisplayNameProp property = (DisplayNameProp)responses.get(i).getProperty(Const.DavProp.DISPLAYNAME);
//        assertEquals(fileNames[i - 1], property.getDisplayName());
//      }
//    }    
//    
//    
//    Log.info("done.");
//  }
  
  public void testOrderMulti() throws Exception {
    // 1 2 3 4 5    1 2 3 4 5
    // 1 before 4   2 3 1 4 5
    // 4 before 3   2 4 3 1 5
    // 2 last       4 3 1 5 2
    // 5 first      5 4 3 1 2
    // 3 after 2    5 4 1 2 3
    // 5 after 1    4 1 5 2 3

    DavOrderPatch davOrderPatch = new DavOrderPatch(TestContext.getContextAuthorized());
    davOrderPatch.setResourcePath(resourcePath);

    // 1 before 4
    // R 1 2 3 4 5
    //   |    ^
    //   +----+
    //
    // R 2 3 1 4 5        
    {
      DOrderMember member = new DOrderMember();
      member.setSegment("1.txt");
      member.setPosition(OrderConst.BEFORE, "4.txt");
      davOrderPatch.addMember(member);
    }
    
    // 4 before 3
    // R 2 3 1 4 5
    //    ^    |
    //    +----+
    // R 2 4 3 1 5
    {
      DOrderMember member = new DOrderMember();
      member.setSegment("4.txt");
      member.setPosition(OrderConst.BEFORE, "3.txt");
      davOrderPatch.addMember(member);      
    }

    // 2 last
    // R 2 4 3 1 5
    //   |        ^
    //   +--------+
    // R 4 3 1 5 2
    {
      DOrderMember member = new DOrderMember();
      member.setSegment("2.txt");
      member.setPosition(OrderConst.LAST);
      davOrderPatch.addMember(member);      
    }

    // 5 first
    // R 4 3 1 5 2
    //  ^      |
    //  +------+
    // R 5 4 3 1 2
    {
      DOrderMember member = new DOrderMember();
      member.setSegment("5.txt");
      member.setPosition(OrderConst.FIRST);
      davOrderPatch.addMember(member);      
    }

    // 3 after 2
    // R 5 4 3 1 2
    //       |    ^
    //       +----+
    // R 5 4 1 2 3
    {
      DOrderMember member = new DOrderMember();
      member.setSegment("3.txt");
      member.setPosition(OrderConst.AFTER, "2.txt");
      davOrderPatch.addMember(member);      
    }

    // 5 after 1
    // R 5 4 1 2 3
    //   |    ^
    //   +----+
    // R 4 1 5 2 3
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
    
    // In multistatus
    // 1-st Response must have status 404 NOT FOUND
    // 2-nd Response must be with status 403 FORBIDDEN

    assertEquals(Const.HttpStatus.MULTISTATUS, davOrderPatch.execute());
    
    String reply = new String(davOrderPatch.getResponseDataBuffer());
    Log.info("\r\n" + reply + "\r\n");
    
    Multistatus multistatus = davOrderPatch.getMultistatus();
    ArrayList<ResponseDoc> responses = multistatus.getResponses();
    
    assertEquals(2, responses.size());
    
    Log.info("STATUS1: " + responses.get(0).getStatus());
    Log.info("STATUS2: " + responses.get(1).getStatus());
    
    
    assertEquals(Const.HttpStatus.NOTFOUND, responses.get(0).getStatus());
    assertEquals(Const.HttpStatus.FORBIDDEN, responses.get(1).getStatus());    

    // Verify folder
    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(resourcePath);
      davPropFind.setRequiredProperty(Const.DavProp.DISPLAYNAME);
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
      
      // see file names upper
      // R 4 1 5 2 3
      String []fileNames = {"4.txt", "1.txt", "5.txt", "2.txt", "3.txt"};
      Multistatus curMultistatus = davPropFind.getMultistatus();
      ArrayList<ResponseDoc> curResponses = curMultistatus.getResponses();
      for (int i = 1; i < 5; i++) {
        DisplayNameProp property = (DisplayNameProp)curResponses.get(i).getProperty(Const.DavProp.DISPLAYNAME);
        assertEquals(fileNames[i - 1], property.getDisplayName());
      }
      
    }
    
    
  }
  
  
}
