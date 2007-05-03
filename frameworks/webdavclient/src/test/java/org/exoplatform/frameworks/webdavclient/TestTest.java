/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient;

import java.io.File;
import java.io.FileOutputStream;

import junit.framework.TestCase;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class TestTest extends TestCase {

  public void testSimplePropFind() throws Exception {
    Log.info("TestTest:testSimplePropFind");
    
    //WebDavContext context = new WebDavContext("192.168.0.5", 8080, "/ecm/repository", "exoadmin", "exo@ecm");
    WebDavContext context = new WebDavContext("192.168.0.5", 8080, "/ecm/repository", "gavrik", "vetal");
    
    DavPropFind davProPfind = new DavPropFind(context);
    //DavPropFind davProPfind = new DavPropFind(TestContext.getContextAuthorized());
    
    davProPfind.setResourcePath("/");
    
//    davProPfind.setRequiredProperty(Const.DavProp.DISPLAYNAME);
//    davProPfind.setRequiredProperty(Const.DavProp.RESOURCETYPE);
//    davProPfind.setRequiredProperty(Const.DavProp.GETCONTENTLENGTH);
//    davProPfind.setRequiredProperty(Const.DavProp.GETCONTENTTYPE);
    
    int status = davProPfind.execute();
    Log.info("STATUS: " + status);
    
    Log.info("REPLY: " + new String(davProPfind.getResponseDataBuffer()));
    
    String outFileName = "D://exo/projects/exoprojects/jcr/trunk/frameworks/webdavclient/testlog.xml";
    File outFile = new File(outFileName);
    FileOutputStream logStream = new FileOutputStream(outFile);
    logStream.write(davProPfind.getResponseDataBuffer());        
    
    Log.info("done.");
  }
  
}

