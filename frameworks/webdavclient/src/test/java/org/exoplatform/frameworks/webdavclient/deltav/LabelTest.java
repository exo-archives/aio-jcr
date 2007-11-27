/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.deltav;

import junit.framework.TestCase;

import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.TestUtils;
import org.exoplatform.frameworks.webdavclient.commands.DavLabel;
import org.exoplatform.frameworks.webdavclient.http.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class LabelTest extends TestCase {
  
  private static String sourcePath;
  private static String source;
  
  public void setUp() throws Exception {    
    sourcePath = "/production/test folder " + System.currentTimeMillis();
    source = sourcePath + "/test label file.txt";    
    TestUtils.createCollection(sourcePath);
    TestUtils.createFile(source, "FILE CONTENT1".getBytes());
    TestUtils.createFile(source, "FILE CONTENT2".getBytes());
    TestUtils.createFile(source, "FILE CONTENT3".getBytes());
  }
  
//  protected void tearDown() throws Exception {
//    TestUtils.removeResource(sourcePath);
//  }  
  
  public void testLabelNotAuthorized() throws Exception {
    Log.info("LabelTest:testLabelNotAuthorized...");
    
    try {
      DavLabel davLabel = new DavLabel(TestContext.getContextAuthorized());
      davLabel.setResourcePath(source);      
      davLabel.setLabel("my new label...");      
      int status = davLabel.execute();
      Log.info("LABELSTATUS: " + status);      
    } catch (Exception exc) {
      Log.info("Unhandled exception. " + exc.getMessage(), exc);
    }

    try {
      DavLabel davLabel = new DavLabel(TestContext.getContextAuthorized());
      davLabel.setResourcePath(source);      
      davLabel.addLabel("my new label...");      
      int status = davLabel.execute();
      Log.info("LABELSTATUS: " + status);      
    } catch (Exception exc) {
      Log.info("Unhandled exception. " + exc.getMessage(), exc);
    }

    try {
      DavLabel davLabel = new DavLabel(TestContext.getContextAuthorized());
      davLabel.setResourcePath(source);      
      davLabel.removeLabel("my new label...");      
      int status = davLabel.execute();
      Log.info("LABELSTATUS: " + status);      
    } catch (Exception exc) {
      Log.info("Unhandled exception. " + exc.getMessage(), exc);
    }
    
    
    Log.info("Done.");
  }

}

