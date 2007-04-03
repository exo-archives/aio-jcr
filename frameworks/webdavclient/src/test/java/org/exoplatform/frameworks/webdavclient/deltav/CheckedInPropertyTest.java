/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.deltav;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.TextUtils;
import org.exoplatform.frameworks.webdavclient.WebDavContext;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.commands.DavPut;
import org.exoplatform.frameworks.webdavclient.commands.DavReport;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.frameworks.webdavclient.properties.CheckedInProp;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class CheckedInPropertyTest extends TestCase {
  
  private static Log log = ExoLogger.getLogger("jcr.CheckedInPropertyTest");
  
  public void testHrefCheckedIn() throws Exception {
    log.info("testHrefCheckedIn...");
    
    String srcPath = "/production/t e s t _ f i l e_" + System.currentTimeMillis() + ".txt";
    int versionCount = 10;

    {
      for (int i = 0; i < versionCount; i++) {
        DavPut davPut = new DavPut(TestContext.getContextAuthorized());
        davPut.setResourcePath(srcPath);
        davPut.setRequestDataBuffer("TEST FILE CONTENT".getBytes());
        
        assertEquals(Const.HttpStatus.CREATED, davPut.execute());
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
        
        log.info(">>>>> href: " + href);
        log.info(">>>>> hrefmakedescaped: " + hrefMakedEscaped);
        
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
        
    log.info("done.");
  }

}
