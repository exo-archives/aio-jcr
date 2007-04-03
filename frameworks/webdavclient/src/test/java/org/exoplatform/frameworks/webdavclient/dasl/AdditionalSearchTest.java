/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.dasl;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavSearch;
import org.exoplatform.frameworks.webdavclient.search.BasicSearchQuery;
import org.exoplatform.frameworks.webdavclient.search.basicsearch.AndCondition;
import org.exoplatform.frameworks.webdavclient.search.basicsearch.BasicSearchCondition;
import org.exoplatform.frameworks.webdavclient.search.basicsearch.EqualCondition;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class AdditionalSearchTest extends TestCase {

  public static Log log = ExoLogger.getLogger("jcr.AdditionalSearchTest");
  
  public void testBasicSearchBuilder() throws Exception {
    
    
    log.info("testBasicSearchBuilder...");
    
//    {
//      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
//      davPropFind.setResourcePath("/");
//      
//      davPropFind.setRequiredProperty(Const.DavProp.DISPLAYNAME);
//      
//      log.info("PROPFIND STATUS: " + davPropFind.execute());
//      
//      log.info("REPLY: " + new String(davPropFind.getResponseDataBuffer()));
//    }
    
    try {

      {
        DavSearch davSearch = new DavSearch(TestContext.getContextAuthorized());
        davSearch.setResourcePath("/production");
        
        BasicSearchQuery query = new BasicSearchQuery();
        
        query.setRequiredProperty(Const.DavProp.DISPLAYNAME);
        query.setRequiredProperty(Const.DavProp.RESOURCETYPE);
        query.setRequiredProperty(Const.DavProp.GETCONTENTLENGTH);
        
        //query.setRequiredProperty("dc:creator");
        //query.setRequiredProperty("dc:description");
        
        query.setFrom("/test1/", 5);

        BasicSearchCondition condition = new AndCondition(
            //new NotCondition(new EqualCondition(Const.DavProp.DISPLAYNAME, "testfile")),
            //new NotCondition(new EqualCondition(Const.DavProp.GETCONTENTLENGTH, "1000"))
            new EqualCondition("jcr:primaryType", "text/xml"),
            new EqualCondition("jcr:mimeType", "text/xml")
            );
        query.setCondition(condition);
        
        davSearch.setQuery(query);
        
        log.info("SEARCH STATUS: " + davSearch.execute());
        log.info("REPLY: " + new String(davSearch.getResponseDataBuffer()));
      }
      
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
    }    
    
    Thread.sleep(1000);
    
    log.info("done.");    
  }
  
//  public void testSQLExtended() throws Exception {    
//    log.info("testSQLExtended...");
//    
//    try {
//      {
//        DavSearch davSearch = new DavSearch(TestContext.getContextAuthorized());
//        davSearch.setResourcePath("/production");
//        
//        SQLQuery query = new SQLQuery();
//        query.setQuery("select * from nt:file");
//        davSearch.setQuery(query);
//        
//        int status = davSearch.execute();
//        log.info("SEARCH STAUS: " + status);
//        
//        String reply = new String(davSearch.getResponseDataBuffer());
//        log.info("REPLY: " + reply);
//      }
//      
//    } catch (Exception exc) {
//      log.info("Unhandled exception. " + exc.getMessage(), exc);
//    }        
//    log.info("done.");
//  }
  
}
