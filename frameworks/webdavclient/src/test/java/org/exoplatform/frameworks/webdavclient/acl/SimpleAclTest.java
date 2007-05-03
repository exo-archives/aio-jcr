/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.acl;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class SimpleAclTest extends TestCase {
  
  private static Log log = ExoLogger.getLogger("jcr.SimpleAclTest");
  
  public void testSimpleAcl() throws Exception {    
    log.info("testSimpleAcl");
    
    String srcName = "/production/testacl";
    
    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath(srcName);
    
    davPropFind.setRequiredProperty(Const.DavProp.DISPLAYNAME);
    davPropFind.setRequiredProperty(Const.DavProp.CURRENT_USER_PRIVILEGE_SET);
    davPropFind.setRequiredProperty(Const.DavProp.SUPPORTED_PRIVILEGE_SET);
    
    log.info("STATUS: " + davPropFind.execute());
    log.info("REPLY:\r\n" + new String(davPropFind.getResponseDataBuffer()));
    
    log.info("done.");    
  }

}
