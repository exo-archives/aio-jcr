/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.frameworks.webdavclient.dasl;

import junit.framework.TestCase;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.TestUtils;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.http.Log;

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
