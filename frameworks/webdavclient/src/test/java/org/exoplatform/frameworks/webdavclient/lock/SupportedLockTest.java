/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.lock;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.TestUtils;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.frameworks.webdavclient.http.Log;
import org.exoplatform.frameworks.webdavclient.properties.PropApi;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class SupportedLockTest extends TestCase {

  public static String sourcePath;
  
  public void setUp() throws Exception {
    sourcePath = "/production/test folder for supportedlock " + System.currentTimeMillis();    
    TestUtils.createCollection(sourcePath);
  }
  
  protected void tearDown() throws Exception {
    TestUtils.removeResource(sourcePath);
  }
  
  public void testSupportedLock() throws Exception {
    Log.info("Process...");
        
    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath(sourcePath);
    davPropFind.setDepth(0);
    
    davPropFind.setRequiredProperty(Const.DavProp.SUPPORTEDLOCK);
    
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    Multistatus multistatus = davPropFind.getMultistatus();
    ArrayList<ResponseDoc> responses = multistatus.getResponses();
    
    assertEquals(1, responses.size());
    
    ResponseDoc response = responses.get(0);
    
    PropApi supportedLockProperty = response.getProperty(Const.DavProp.SUPPORTEDLOCK);
    assertNotNull(supportedLockProperty);
    
    assertEquals(Const.HttpStatus.OK, supportedLockProperty.getStatus());
    
    Log.info("Done");
  }
  
}
