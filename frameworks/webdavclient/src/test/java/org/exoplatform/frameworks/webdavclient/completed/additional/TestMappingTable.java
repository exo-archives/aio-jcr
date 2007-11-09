/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.completed.additional;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class TestMappingTable extends TestCase {
  
  private static Log log = ExoLogger.getLogger("jcr.TestMappingTable");

  public void testWebDavFile() throws Exception {
    log.info("testWebDavFile...");

    String srcFolder = "/production";
    String srcPath = srcFolder + "/test webdav file.pdf";
    
//    {
//      DavPut davPut = new DavPut(TestContext.getContextAuthorized());
//      davPut.setResourcePath(srcPath);
//      davPut.setRequestDataBuffer("TEST FILE CONTENT".getBytes());
//      
//      davPut.setNodeType("webdav:file");
//      
//      log.info("PUT REPLY: " + davPut.execute());
//    }
    
//    {
//      DavPropPatch propPatch = new DavPropPatch(TestContext.getContextAuthorized());
//      propPatch.setResourcePath(srcPath);
//      
////      propPatch.setProperty("dc:description", "owner test MMMMMMMMMMMMMMMM");
////      propPatch.setProperty("dc:description", "owner test ((((((((((((((");
////      propPatch.setProperty("webdav:creator", "owner !!!!!!!!!!!!!!!!! 0000000000000000");
//      
////      propPatch.setProperty("jcr:mimeType", "aplication/pdf/xz");
//      
//      propPatch.setProperty("dc:creator", "gavrik-vetal -------------");
//      
//      log.info("proppatch status: " + propPatch.execute());
//      log.info("reply: " + new String(propPatch.getResponseDataBuffer()));
//    }      
    
    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(srcPath);
      
      davPropFind.setRequiredProperty(Const.DavProp.DISPLAYNAME);
      
//      davPropFind.setRequiredProperty("dc:description");
      davPropFind.setRequiredProperty("dc:creator");
//      davPropFind.setRequiredProperty("jcr:mimeType");
      davPropFind.setRequiredProperty("webdav:creator");
      
      log.info("status: " + davPropFind.execute());
      
      String reply = new String(davPropFind.getResponseDataBuffer());
      log.info("reply: " + reply);
    }
    
    log.info("done.");
  }
  
}
