/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.completed.search;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.commands.DavPropFind;
import org.exoplatform.frameworks.davclient.completed.DavLocationConst;
import org.exoplatform.frameworks.davclient.documents.Multistatus;
import org.exoplatform.frameworks.davclient.documents.ResponseDoc;
import org.exoplatform.frameworks.davclient.properties.DisplayNameProp;
import org.exoplatform.frameworks.davclient.properties.SupportedQueryGrammarSetProp;
import org.exoplatform.frameworks.davclient.search.SearchConst;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class SupportedQueryGramarSetTest extends TestCase {

  private static Log log = ExoLogger.getLogger("jcr.SupportedQueryGramarSetTest");
  
  public static final String SRC_NAME = "/";
  public static final String ENABLEFOR = "production";
  //public static final String ENABLEFOR = "draft";  
  
  /*
   * supporting by workspaces
   * production+
   * other-
   */
  
  public void testSupportedQueryGrammarSetProperty() throws Exception {
    log.info("testSupportedQueryGrammarSetProperty...");
    
    DavPropFind davPropFind = new DavPropFind(DavLocationConst.getLocationAuthorized());
    davPropFind.setResourcePath(SRC_NAME);
    
    davPropFind.setRequiredProperty(Const.DavProp.DISPLAYNAME);
    davPropFind.setRequiredProperty(Const.DavProp.SUPPORTEDQUERYGRAMMARSET);
    
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    Multistatus multistatus = (Multistatus)davPropFind.getMultistatus();
    ArrayList<ResponseDoc> responses = multistatus.getResponses();
    for (int i = 0; i < responses.size(); i++) {
      ResponseDoc response = responses.get(i);
      DisplayNameProp displayNameProp = (DisplayNameProp)response.getProperty(Const.DavProp.DISPLAYNAME);
      assertNotNull(displayNameProp);
      
      SupportedQueryGrammarSetProp supportedQuery = 
          (SupportedQueryGrammarSetProp)response.getProperty(Const.DavProp.SUPPORTEDQUERYGRAMMARSET);      
      assertNotNull(supportedQuery);
      
      if (ENABLEFOR.equals(displayNameProp.getDisplayName())) {        
        assertEquals(Const.HttpStatus.OK, supportedQuery.getStatus());
        
        assertTrue(supportedQuery.isBasicSearchEnabled());
        assertTrue(supportedQuery.getGrammars().contains(SearchConst.SQL_SUPPORT));
        assertTrue(supportedQuery.getGrammars().contains(SearchConst.XPATH_SUPPORT));        
        continue;
      }
      
    }
    
    //log.info(new String(davPropFind.getResponseDataBuffer()));
    
    Thread.sleep(1000);
    
    log.info("done.");
  }
  
}
