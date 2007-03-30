/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.completed.additional;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.Const.DavProp;
import org.exoplatform.frameworks.davclient.commands.DavMkCol;
import org.exoplatform.frameworks.davclient.commands.DavPropFind;
import org.exoplatform.frameworks.davclient.commands.DavPropPatch;
import org.exoplatform.frameworks.davclient.commands.DavPut;
import org.exoplatform.frameworks.davclient.completed.DavLocationConst;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class TestMappingTable extends TestCase {
  
  private static Log log = ExoLogger.getLogger("jcr.TestMappingTable");

  public void testWebDavFile() throws Exception {
    log.info("testWebDavFile...");

    String srcFolder = "/production";
    String srcPath = srcFolder + "/test webdav file.pdf";
    
//    {
//      DavPut davPut = new DavPut(DavLocationConst.getLocationAuthorized());
//      davPut.setResourcePath(srcPath);
//      davPut.setRequestDataBuffer("TEST FILE CONTENT".getBytes());
//      
//      davPut.setNodeType("webdav:file");
//      
//      log.info("PUT REPLY: " + davPut.execute());
//    }
    
//    {
//      DavPropPatch propPatch = new DavPropPatch(DavLocationConst.getLocationAuthorized());
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
      DavPropFind davPropFind = new DavPropFind(DavLocationConst.getLocationAuthorized());
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
