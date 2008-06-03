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

import java.util.ArrayList;

import junit.framework.TestCase;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.TestUtils;
import org.exoplatform.frameworks.webdavclient.commands.DavCheckIn;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.commands.DavVersionControl;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.frameworks.webdavclient.http.Log;
import org.exoplatform.frameworks.webdavclient.http.TextUtils;
import org.exoplatform.frameworks.webdavclient.properties.CheckedInProp;
import org.exoplatform.frameworks.webdavclient.properties.CheckedOutProp;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class CheckInTest extends TestCase {

  public static final String SRC_NOTEXIST = "/production/not exist folder " + System.currentTimeMillis();
  
  private static String sourcePath;
  
  private static String sourceName;
  
  public void setUp() throws Exception {    
    sourcePath = "/production/test folder " + System.currentTimeMillis();
    sourceName = sourcePath + "/test version file.txt";
    
    TestUtils.createCollection(sourcePath);
    TestUtils.createFile(sourceName, "FILE CONTENT".getBytes());
  }
  
  protected void tearDown() throws Exception {
    TestUtils.removeResource(sourcePath);
  }

  public void testNotAuthorized() throws Exception {
    Log.info("testNotAuthorized...");

    DavCheckIn davCheckIn = new DavCheckIn(TestContext.getContext());
    davCheckIn.setResourcePath(sourceName);    
    assertEquals(Const.HttpStatus.AUTHNEEDED, davCheckIn.execute());    
    
    Log.info("done.");
  }
  
  public void testNotFound() throws Exception {
    Log.info("testNotFound...");
    
    DavCheckIn davCheckIn = new DavCheckIn(TestContext.getContextAuthorized());
    davCheckIn.setResourcePath(SRC_NOTEXIST);    
    assertEquals(Const.HttpStatus.NOTFOUND, davCheckIn.execute());
    
    Log.info("done.");
  }
  
  public void testConflict() throws Exception {
    Log.info("testForbidden...");

    DavCheckIn davCheckIn = new DavCheckIn(TestContext.getContextAuthorized());
    davCheckIn.setResourcePath(sourcePath);
    assertEquals(Const.HttpStatus.CONFLICT, davCheckIn.execute());
    
    Log.info("done.");
  }
  
  public void testOk() throws Exception {
    Log.info("testOk...");
    
    DavVersionControl davVersionControl = new DavVersionControl(TestContext.getContextAuthorized());
    davVersionControl.setResourcePath(sourceName);
    assertEquals(Const.HttpStatus.OK, davVersionControl.execute());

    DavCheckIn davCheckIn = new DavCheckIn(TestContext.getContextAuthorized());
    davCheckIn.setResourcePath(sourceName);
    assertEquals(Const.HttpStatus.OK, davCheckIn.execute());
    
    /*
     * check if this node is checkedIn and checkedOut
     */
    
    DavPropFind davProPFind = new DavPropFind(TestContext.getContextAuthorized());
    davProPFind.setResourcePath(sourceName);
    
    davProPFind.setRequiredProperty(Const.DavProp.CHECKEDIN);
    davProPFind.setRequiredProperty(Const.DavProp.CHECKEDOUT);
    
    assertEquals(Const.HttpStatus.MULTISTATUS, davProPFind.execute());
    
    Multistatus multistatus = davProPFind.getMultistatus();
    ArrayList<ResponseDoc> responses = multistatus.getResponses();
    assertEquals(1, responses.size());
    
    ResponseDoc response = responses.get(0);
    
    CheckedInProp checkedInProperty = (CheckedInProp)response.getProperty(Const.DavProp.CHECKEDIN);
    assertNotNull(checkedInProperty);
    assertEquals(Const.HttpStatus.OK, checkedInProperty.getStatus());
    
    CheckedOutProp checkedOutProperty = (CheckedOutProp)response.getProperty(Const.DavProp.CHECKEDOUT);
    assertNotNull(checkedOutProperty);
    assertEquals(Const.HttpStatus.NOTFOUND, checkedOutProperty.getStatus());
    
    String checkedInHref = checkedInProperty.getHref();
    
    String hrefMustBe = TestContext.getContextAuthorized().getServerPrefix() + sourceName + "?version=1";
    hrefMustBe = TextUtils.Escape(hrefMustBe, '%', true);
    
    assertEquals(hrefMustBe, checkedInHref);
    
    Log.info("done.");    
  }  
  
}
