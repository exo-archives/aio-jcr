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

package org.exoplatform.frameworks.webdavclient.common;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.TestUtils;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.frameworks.webdavclient.http.Log;
import org.exoplatform.frameworks.webdavclient.http.TextUtils;

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
    davPropFind.setDepth(1);
    
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
