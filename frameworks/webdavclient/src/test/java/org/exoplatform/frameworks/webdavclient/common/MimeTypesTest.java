/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.common;


import junit.framework.TestCase;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.TestUtils;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class MimeTypesTest extends TestCase {
  
  public void testWordMimeTypes() {
    Log.info("MimeTypesTest:testWordMimeTypes");
    
    try {
      // retrieve mimetype of file
      {
        String srcPath = "/production/test";
        
        DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
        davPropFind.setResourcePath(srcPath);
        
        davPropFind.setRequiredProperty(Const.DavProp.DISPLAYNAME);
        davPropFind.setRequiredProperty(Const.DavProp.RESOURCETYPE);
        davPropFind.setRequiredProperty(Const.DavProp.CREATIONDATE);
        davPropFind.setRequiredProperty(Const.DavProp.ISFOLDER);
        davPropFind.setRequiredProperty("jcr:mimeType");
        
        int status = davPropFind.execute();
        Log.info("STATUS: " + status);
        Log.info("REPLY: \r\n" + new String(davPropFind.getResponseDataBuffer()));
        TestUtils.logXML(davPropFind);
      }
      
    } catch (Exception exc) {
      Log.info("Unhandled exception. ", exc);
    }    
    
    Log.info("MimeTypesTest:done");
  }

//  public void testWebDavGetXML() {
//    Log.info("Test...");
//    
//    try {
//      
//      DavGet davGet = new DavGet(TestContext.getContextAuthorized());
//      davGet.setResourcePath("/production/1");
//      
//      int status = davGet.execute();
//      Log.info("STATUS: " + status);
//      
//      Log.info("REPLY: \r\n" + new String(davGet.getResponseDataBuffer()));
//      TestUtils.logXML(davGet);
//    } catch (Exception exc) {
//      Log.info("Unhandled exception. ", exc);
//    }
//    
//    Log.info("done.");
//  }
  
}

