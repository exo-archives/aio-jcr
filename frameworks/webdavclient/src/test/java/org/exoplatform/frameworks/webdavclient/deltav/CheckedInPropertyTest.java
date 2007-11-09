/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.deltav;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.httpclient.TextUtils;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.TestUtils;
import org.exoplatform.frameworks.webdavclient.WebDavContext;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.commands.DavReport;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
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
