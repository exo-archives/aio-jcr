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
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.frameworks.webdavclient.properties.DisplayNameProp;
import org.exoplatform.frameworks.webdavclient.properties.SupportedQueryGrammarSetProp;
import org.exoplatform.frameworks.webdavclient.search.SearchConst;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class SupportedQueryGramarSetTest extends TestCase {

  //private static Log log = ExoLogger.getLogger("jcr.SupportedQueryGramarSetTest");
  
  public static final String SRC_NAME = "/";
  public static final String ENABLEFOR = "production";
  //public static final String ENABLEFOR = "draft";  
  
  /*
   * supporting by workspaces
   * production+
   * other-
   */
  
  public void testSupportedQueryGrammarSetProperty() throws Exception {
    TestUtils.logStart();
    
    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath(SRC_NAME);
    
    davPropFind.setRequiredProperty(Const.DavProp.DISPLAYNAME);
    davPropFind.setRequiredProperty(Const.DavProp.SUPPORTEDQUERYGRAMMARSET);
    
    davPropFind.setDepth(2);
    
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    Log.info("REPLY:\r\n" + new String(davPropFind.getResponseDataBuffer()));
    TestUtils.logXML(davPropFind);
    
    Multistatus multistatus = davPropFind.getMultistatus();
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
    
    Log.info("done.");
  }
  
}
