/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.dasl;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.TestUtils;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.frameworks.webdavclient.properties.PropApi;
import org.exoplatform.frameworks.webdavclient.properties.SupportedMethodSetProp;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class SupportedMethodSetTest extends TestCase {
    
  public void testSupportedMethodSet() throws Exception {    
    TestUtils.logStart();
    
    String srcPath = "/production/test_folder_" + System.currentTimeMillis();
    
    {
      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
      davMkCol.setResourcePath(srcPath);
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    }

    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(srcPath);
      
      davPropFind.setRequiredProperty(Const.DavProp.DISPLAYNAME);
      davPropFind.setRequiredProperty(Const.DavProp.VERSIONNAME);
      davPropFind.setRequiredProperty(Const.DavProp.SUPPORTEDMETHODSET);
      
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
      
      Multistatus multistatus = davPropFind.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();
      
      assertEquals(1, responses.size());
      
      ResponseDoc response = responses.get(0);      
      
      PropApi property = response.getProperty(Const.DavProp.SUPPORTEDMETHODSET);      
      assertNotNull(property);
      
      assertEquals(Const.HttpStatus.OK, property.getStatus());
      
      org.exoplatform.frameworks.httpclient.Log.info("REPLY:\r\n" + new String(davPropFind.getResponseDataBuffer()));
      
      ArrayList<String> methods = ((SupportedMethodSetProp)property).getMethods();
      for (int i = 0; i < methods.size(); i++) {
        Log.info(">>> " + methods.get(i));
      }
    }
    
    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
      davDelete.setResourcePath(srcPath);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }
    
    Thread.sleep(1000);
    
    Log.info("done.");
  }
  
}
