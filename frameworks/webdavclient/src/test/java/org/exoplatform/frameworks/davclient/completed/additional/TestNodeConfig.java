/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.completed.additional;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.davclient.commands.DavPropFind;
import org.exoplatform.frameworks.davclient.completed.DavLocationConst;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class TestNodeConfig extends TestCase {

  private static Log log = ExoLogger.getLogger("jcr.TestNodeConfig");
  
//  public void testSimpleConfig() throws Exception {
//    
//    if (true) {
//      return;
//    }
//    
//    log.info("testSimpleConfig...");
//    
//    DavPropFind davPropFind = new DavPropFind(DavLocationConst.getLocationAuthorized());
//    davPropFind.setResourcePath("/production");
//    
//    davPropFind.setRequiredProperty(Const.DavProp.DISPLAYNAME);    
//    davPropFind.setRequiredProperty(Const.DavProp.GETCONTENTTYPE);
//    davPropFind.setRequiredProperty(Const.DavProp.GETCONTENTLENGTH);
//    davPropFind.setRequiredProperty(Const.DavProp.CREATIONDATE);
//    davPropFind.setRequiredProperty("myproperty");
//    
//    davPropFind.setRequiredProperty("dc:owner");
//    davPropFind.setRequiredProperty("dc:description");
//    
//    int status = davPropFind.execute();
//    log.info("PROPFIND STATUS: " + status);
//    
//    String reply = new String(davPropFind.getResponseDataBuffer());
//    log.info("REPLY: " + reply);
//    
//    Thread.sleep(2000);
//    
//    log.info("done.");
//  }
  
  public void testAllPropConfig() throws Exception {
    log.info("testAllPropConfig...");
    
    DavPropFind davPropFind = new DavPropFind(DavLocationConst.getLocationAuthorized());
    davPropFind.setResourcePath("/production");
    
    int status = davPropFind.execute();
    log.info("PROPFIND STATUS: " + status);
    
    String reply = new String(davPropFind.getResponseDataBuffer());
    log.info("REPLY: " + reply);
    
    Thread.sleep(2000);
    
    log.info("done.");
  }
  
}
