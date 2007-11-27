/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.common;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.TestUtils;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.commands.DavPropPatch;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.frameworks.webdavclient.http.Log;
import org.exoplatform.frameworks.webdavclient.properties.PropApi;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class PropPatchTest extends TestCase {
  
  public static final String DAV_NS = "DAV:";

  public static final String SRC_WORKSPACE = "/production";
  
  public static final String SRC_NOTEXISTWORKSPACE = "/not exist workspace"; 
  public static final String SRC_NOTEXISTPATH = SRC_WORKSPACE + "/ not exist folder " + System.currentTimeMillis();
  
  private static String getSourcePath() {
    return SRC_WORKSPACE + "/test folder " + System.currentTimeMillis(); 
  }

  public void testNotAuthorized() throws Exception {
    Log.info("PropPatchTest:testNotAuthorized...");
    
    DavPropPatch davPropPatch = new DavPropPatch(TestContext.getContext());
    davPropPatch.setResourcePath(getSourcePath());
    
    davPropPatch.setProperty(Const.DavProp.DISPLAYNAME, DAV_NS, "my display name");    
    davPropPatch.removeProperty(Const.DavProp.OWNER, DAV_NS);    

    assertEquals(Const.HttpStatus.AUTHNEEDED, davPropPatch.execute());
    
    Log.info("done.");    
  }
  
  public void testNotFound() throws Exception {
    Log.info("PropPatchTest:testNotFound...");
    
    DavPropPatch davPropPatch = new DavPropPatch(TestContext.getContextAuthorized());
    davPropPatch.setResourcePath(SRC_NOTEXISTPATH);
    
    davPropPatch.setProperty(Const.DavProp.DISPLAYNAME, DAV_NS, "my display name");    
    davPropPatch.removeProperty(Const.DavProp.OWNER, DAV_NS);
    
    assertEquals(Const.HttpStatus.NOTFOUND, davPropPatch.execute());
    
    Log.info("done.");
  }
  
  public void testForbidden() throws Exception {
    Log.info("PropPatchTest:testForbidden...");
    
    {
      DavPropPatch davPropPatch = new DavPropPatch(TestContext.getContextAuthorized());
      davPropPatch.setResourcePath("/");
      assertEquals(Const.HttpStatus.FORBIDDEN, davPropPatch.execute());
    }

    /*
     * this test patch proeprties at the Root node
     * Response must be MULTISTATUS 
     */
    {
      DavPropPatch davPropPatch = new DavPropPatch(TestContext.getContextAuthorized());
      davPropPatch.setResourcePath(SRC_WORKSPACE);
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropPatch.execute());            
      //assertEquals(Const.HttpStatus.FORBIDDEN, davPropPatch.execute());
    }
    
    Log.info("done.");
  }
  
  public void testMultistatus() throws Exception {
    Log.info("PropPatchTest:testMultistatus...");
    
    String sourcePath = getSourcePath();
    
    DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
    davMkCol.setResourcePath(sourcePath);
    assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    
    DavPropPatch davPropPatch = new DavPropPatch(TestContext.getContextAuthorized());
    davPropPatch.setResourcePath(sourcePath);    
    davPropPatch.setProperty(Const.DavProp.COMMENT, DAV_NS, "this comment");    
    davPropPatch.removeProperty(Const.DavProp.CREATIONDATE, DAV_NS);
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropPatch.execute());
    
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(sourcePath);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    
    Log.info("done.");
  }

  public void testDCPropPatch() throws Exception {
    Log.info("PropPatchTest:testDCPropPatch");

    String sourcePath = getSourcePath();
    
    String dcNameSpace = "http://purl.org/dc/elements/1.1/";
    
    String dcDescription = "dc:description";
    String dcDescriptionVal = "webdav test description";
    
    String dcRights = "dc:rights";
    String dcRightsVal = "test rights for collection";
    
    // create collection
    {
      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
      davMkCol.setResourcePath(sourcePath);
      davMkCol.setMixType("dc:elementSet");
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    }
    
    // try to retrieve dc:description and dc:rights
    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(sourcePath);
      
      davPropFind.setRequiredProperty(dcDescription, dcNameSpace);
      davPropFind.setRequiredProperty(dcRights, dcNameSpace);
      
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
      
      Multistatus multistatus = davPropFind.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();
      assertEquals(1, responses.size());
      for (int i = 0; i < responses.size(); i++) {
        ResponseDoc curResponse = responses.get(i);
        ArrayList<PropApi> properties = curResponse.getProperties();
        assertEquals(2, properties.size());
        for (int pi = 0; pi < properties.size(); pi++) {
          PropApi property = properties.get(pi);
          assertEquals(Const.HttpStatus.NOTFOUND, property.getStatus());
        }
      }      
      
    }
    
    // try to set dc:description and dc:rights
    {      
      DavPropPatch davPropPatch = new DavPropPatch(TestContext.getContextAuthorized());
      davPropPatch.setResourcePath(sourcePath);
      
      davPropPatch.setProperty(dcDescription, dcNameSpace, dcDescriptionVal);
      davPropPatch.setProperty(dcRights, dcNameSpace, dcRightsVal);
      
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropPatch.execute());
      
      Multistatus multistatus = davPropPatch.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();
      assertEquals(1, responses.size());
      for (int i = 0; i < responses.size(); i++) {
        ResponseDoc curResponse = responses.get(i);
        ArrayList<PropApi> properties = curResponse.getProperties();
        assertEquals(2, properties.size());
        for (int pi = 0; pi < properties.size(); pi++) {
          PropApi property = properties.get(pi);
          assertEquals(Const.HttpStatus.OK, property.getStatus());
        }
      }
      
    }
    
    // try to retrieve dc:description and dc:rights
    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(sourcePath);
      
      davPropFind.setRequiredProperty(dcDescription, dcNameSpace);
      davPropFind.setRequiredProperty(dcRights, dcNameSpace);
      
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());

      Multistatus multistatus = davPropFind.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();
      assertEquals(1, responses.size());
      for (int i = 0; i < responses.size(); i++) {
        ResponseDoc curResponse = responses.get(i);
        ArrayList<PropApi> properties = curResponse.getProperties();
        assertEquals(2, properties.size());
        for (int pi = 0; pi < properties.size(); pi++) {
          PropApi property = properties.get(pi);
          assertEquals(Const.HttpStatus.OK, property.getStatus());
        }
      }
    }
    
    TestUtils.removeResource(sourcePath);
    
    Log.info("done.");
  }
  
}
