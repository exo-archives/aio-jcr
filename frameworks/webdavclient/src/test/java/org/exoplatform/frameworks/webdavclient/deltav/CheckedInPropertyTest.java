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
import org.exoplatform.frameworks.webdavclient.WebDavContext;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.commands.DavReport;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.frameworks.webdavclient.http.Log;
import org.exoplatform.frameworks.webdavclient.http.TextUtils;
import org.exoplatform.frameworks.webdavclient.properties.CheckedInProp;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class CheckedInPropertyTest extends TestCase {
  
  public void testHrefCheckedIn() throws Exception {
    Log.info("testHrefCheckedIn...");
    
    String srcPath = "/production/t e s t _ f i l e_" + System.currentTimeMillis() + ".txt";
    int versionCount = 10;

    {
      for (int i = 0; i < versionCount; i++) {
        TestUtils.createFile(srcPath, "TEST FILE CONTENT".getBytes());        
      }
    }          

    {
      DavReport davReport = new DavReport(TestContext.getContextAuthorized());
      davReport.setResourcePath(srcPath);
      davReport.setRequiredProperty(Const.DavProp.DISPLAYNAME);
      davReport.setRequiredProperty(Const.DavProp.CHECKEDIN);
      
      assertEquals(Const.HttpStatus.MULTISTATUS, davReport.execute());
      
      Multistatus multistatus = davReport.getMultistatus();      
      ArrayList<ResponseDoc> responses = multistatus.getResponses();
      
      assertEquals(versionCount, responses.size());

      for (int i = 0; i < responses.size(); i++) {
        ResponseDoc response = responses.get(i);
        
        WebDavContext context = TestContext.getContextAuthorized();

        String hrefMaked = "http://" + context.getHost();
        if (context.getPort() != 80) {
          hrefMaked += ":" + context.getPort();
        }
        
        hrefMaked += context.getServletPath() + srcPath;        
        hrefMaked += DeltaVConst.DAV_VERSIONPREFIX + (i + 1);

        
        String hrefMakedEscaped = TextUtils.Escape(hrefMaked, '%', true);
        
        String href = response.getHref();
        
        assertEquals(href, hrefMakedEscaped);
        
        CheckedInProp checkedInProp = (CheckedInProp)response.getProperty(Const.DavProp.CHECKEDIN);
        assertNotNull(checkedInProp);
        
        href = checkedInProp.getHref();
        
        assertEquals(href, hrefMakedEscaped);
      }
    }
    
    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(srcPath);
      
      davPropFind.setRequiredProperty(Const.DavProp.DISPLAYNAME);
      davPropFind.setRequiredProperty(Const.DavProp.CHECKEDIN);
      
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    }
    
    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
      davDelete.setResourcePath(srcPath);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }
        
    Log.info("done.");
  }

}
