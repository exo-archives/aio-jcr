/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.dasl;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.davclient.TestContext;
import org.exoplatform.frameworks.davclient.TestUtils;
import org.exoplatform.frameworks.davclient.commands.DavSearch;
import org.exoplatform.frameworks.davclient.documents.Multistatus;
import org.exoplatform.frameworks.davclient.search.XPathQuery;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class XPathSearchTest extends TestCase {
  
  private static Log log = ExoLogger.getLogger("jcr.XPathSearchTest");
  
  public void testSimpleXPathSearch() throws Exception {
    log.info("testSimpleXPathSearch...");
    
    String testFolderName = "/production/xpath_test_folder_" + System.currentTimeMillis();    
    TestUtils.createCollection(testFolderName);
    
    for (int i = 0; i < 10; i++) {
      String fileName = testFolderName + "/test_file_" + i + ".txt";
      TestUtils.createFile(fileName, ("FILE CONTENT " + i).getBytes());
    }
    
    {
      DavSearch davSearch = new DavSearch(TestContext.getContextAuthorized());
      davSearch.setResourcePath(testFolderName);
      
      XPathQuery query = new XPathQuery();
      query.setQuery("//element(*, nt:file)");
      
      davSearch.setQuery(query);
      
      int status = davSearch.execute();
      log.info("SEARCH STATUS: " + status);
      
      TestUtils.showMultistatus((Multistatus)davSearch.getMultistatus());
    }
    
    
    TestUtils.removeResource(testFolderName);
    
    log.info("done.");
  }

}
