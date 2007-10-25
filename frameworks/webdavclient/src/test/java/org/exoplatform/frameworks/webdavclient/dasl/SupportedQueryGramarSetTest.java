/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.dasl;

import junit.framework.TestCase;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.TestUtils;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class SupportedQueryGramarSetTest extends TestCase {

  public static final String SRC_NAME = "/production";
  
  /*
   * supporting by workspaces
   */
  
  public void testSupportedQueryGrammarSetProperty() throws Exception {
    TestUtils.logStart();
    
    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath(SRC_NAME);
    
    davPropFind.setRequiredProperty(Const.DavProp.DISPLAYNAME);
    davPropFind.setRequiredProperty(Const.DavProp.SUPPORTEDQUERYGRAMMARSET);
    
    davPropFind.setDepth(1);
    
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());

    
//    Multistatus multistatus = davPropFind.getMultistatus();
//    ArrayList<ResponseDoc> responses = multistatus.getResponses();
//    for (int i = 0; i < responses.size(); i++) {
//      ResponseDoc response = responses.get(i);
//      DisplayNameProp displayNameProp = (DisplayNameProp)response.getProperty(Const.DavProp.DISPLAYNAME);
//      assertNotNull(displayNameProp);
//      
//      SupportedQueryGrammarSetProp supportedQuery = 
//          (SupportedQueryGrammarSetProp)response.getProperty(Const.DavProp.SUPPORTEDQUERYGRAMMARSET);      
//      assertNotNull(supportedQuery);
//      
//      if (ENABLEFOR.equals(displayNameProp.getDisplayName())) {        
//        assertEquals(Const.HttpStatus.OK, supportedQuery.getStatus());
//        
//        assertTrue(supportedQuery.isBasicSearchEnabled());
//        assertTrue(supportedQuery.getGrammars().contains(SearchConst.SQL_SUPPORT));
//        assertTrue(supportedQuery.getGrammars().contains(SearchConst.XPATH_SUPPORT));        
//        continue;
//      }
//      
//    }
    
    Thread.sleep(1000);
    
    Log.info("done.");
  }
  
}
