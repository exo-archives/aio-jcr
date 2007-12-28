/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

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

