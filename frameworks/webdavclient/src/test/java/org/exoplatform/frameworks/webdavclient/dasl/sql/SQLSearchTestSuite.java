/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.dasl.sql;

import junit.framework.TestSuite;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavSearch;
import org.exoplatform.frameworks.webdavclient.search.DavQuery;
import org.exoplatform.frameworks.webdavclient.search.SQLQuery;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class SQLSearchTestSuite extends  TestSuite {
  
  public SQLSearchTestSuite() throws Exception {
//    addTestSuite(SQLSearchTest.class);
//    addTestSuite(SQLFullTextSearchTest.class);
//    addTestSuite(SQLSearchByProperty.class);
    
    //addTestSuite(SQLSearchByNodeType.class);
    
    {
      DavSearch davSearch = new DavSearch(TestContext.getContextAuthorized());
      davSearch.setResourcePath("/production");
      
      DavQuery davQuery = new SQLQuery("select * from nt:file");
      davSearch.setQuery(davQuery);
      
      try {
        Log.info("SEARCH STATUS: " + davSearch.execute());
      } catch (Exception exc) {
        Log.info("Unhandled exception. " + exc.getMessage(), exc);
      }
      
    }
    
  }
  
  public void testVoid() throws Exception {
  }  

}

