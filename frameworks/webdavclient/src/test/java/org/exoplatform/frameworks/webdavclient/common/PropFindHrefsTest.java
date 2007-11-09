/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.common;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.httpclient.TextUtils;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.TestUtils;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class PropFindHrefsTest extends TestCase {
  
  public static final int FOLDERS = 5; 
  
  private static String sourceName = "";
  
  public void setUp() throws Exception {
    sourceName = "/production/test folder " + System.currentTimeMillis();
    TestUtils.createCollection(sourceName);
  }
  
  protected void tearDown() throws Exception {
    TestUtils.removeResource(sourceName);
  }
  
  public void testPropFindHrefs() throws Exception {
    Log.info("PropFindTest:testPropFindHrefs");
    
    for (int i = 0; i < FOLDERS; i++) {
      String curFolderName = sourceName + "/test sub folder " + i;      
      TestUtils.createCollection(curFolderName);
    }
    
    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    
    davPropFind.setResourcePath(sourceName);
    
    davPropFind.setRequiredProperty(Const.DavProp.DISPLAYNAME);
    davPropFind.setRequiredProperty(Const.DavProp.RESOURCETYPE);
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());

    Multistatus multistatus = davPropFind.getMultistatus();
    ArrayList<ResponseDoc> responses = multistatus.getResponses();
    
    assertEquals(FOLDERS + 1, responses.size());

    ResponseDoc rootResponse = responses.get(0);
    
    String hrefMustBe = TestContext.getContext().getServerPrefix() + sourceName;
    hrefMustBe = TextUtils.Escape(hrefMustBe, '%', true);

    assertEquals(hrefMustBe, rootResponse.getHref());
    
    for (int i = 0; i < FOLDERS; i++) {
      ResponseDoc response = responses.get(i + 1);
      
      String responseHref = response.getHref();
      
      hrefMustBe = TestContext.getContext().getServerPrefix() + sourceName + "/test sub folder " + i;
      hrefMustBe = TextUtils.Escape(hrefMustBe, '%', true);
      
      assertEquals(hrefMustBe, responseHref);
    }
    
    Log.info("Done.");
  }

}
