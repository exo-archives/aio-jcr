/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.completed.search;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.commands.DavDelete;
import org.exoplatform.frameworks.davclient.commands.DavMkCol;
import org.exoplatform.frameworks.davclient.commands.DavPropFind;
import org.exoplatform.frameworks.davclient.completed.DavLocationConst;
import org.exoplatform.frameworks.davclient.documents.Multistatus;
import org.exoplatform.frameworks.davclient.documents.ResponseDoc;
import org.exoplatform.frameworks.davclient.properties.PropApi;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class SupportedMethodSetTest extends TestCase {
  
  private static Log log = ExoLogger.getLogger("jcr.SupportedMethodSetTest");
  
  public void testMupportedMethodSet() throws Exception {    
    log.info("testMupportedMethodSet...");
    
    String srcPath = "/production/test_folder_" + System.currentTimeMillis();
    
    {
      DavMkCol davMkCol = new DavMkCol(DavLocationConst.getLocationAuthorized());
      davMkCol.setResourcePath(srcPath);
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    }

    {
      DavPropFind davPropFind = new DavPropFind(DavLocationConst.getLocationAuthorized());
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
      
//      ArrayList<String> methods = ((SupportedMethodSetProp)property).getMethods();
//      for (int i = 0; i < methods.size(); i++) {
//        log.info(">>> " + methods.get(i));
//      }
    }
    
    {
      DavDelete davDelete = new DavDelete(DavLocationConst.getLocationAuthorized());
      davDelete.setResourcePath(srcPath);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }
    
    Thread.sleep(1000);
    
    log.info("done.");
  }
  
}
