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

package org.exoplatform.frameworks.webdavclient.dasl.sql;

import junit.framework.TestSuite;

import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavSearch;
import org.exoplatform.frameworks.webdavclient.http.Log;
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

