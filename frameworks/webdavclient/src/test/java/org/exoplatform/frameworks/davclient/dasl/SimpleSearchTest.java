/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.dasl;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.TestContext;
import org.exoplatform.frameworks.davclient.commands.DavSearch;
import org.exoplatform.frameworks.davclient.documents.Multistatus;
import org.exoplatform.frameworks.davclient.documents.ResponseDoc;
import org.exoplatform.frameworks.davclient.search.SQLQuery;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class SimpleSearchTest extends TestCase {

  private static Log log = ExoLogger.getLogger("jcr.SimpleSearchTest");
  
  public void testSearchResources() throws Exception {    
    if (true) {
      return;
    }
    
    log.info("testSearchResources...");
    
    // for REPOSITORY  RESPOURCE
    {
      DavSearch davSearch = new DavSearch(TestContext.getContextAuthorized());
      davSearch.setResourcePath("/");
      
      SQLQuery query = new SQLQuery();
      query.setQuery("select * from nt:unstructured");
      davSearch.setQuery(query);
      
      int status = davSearch.execute();
      log.info("STATUS: " + status);      
    }
    
    // FOR WORKSPACE RESOURCE
    {
      DavSearch davSearch = new DavSearch(TestContext.getContextAuthorized());
      davSearch.setResourcePath("/production");
      
      SQLQuery query = new SQLQuery();
      query.setQuery("select * from nt:unstructured");
      davSearch.setQuery(query);
      
      int status = davSearch.execute();
      log.info("STATUS: " + status);      
    }
    
    // FOR NOODE RESOURCE
    {
      DavSearch davSearch = new DavSearch(TestContext.getContextAuthorized());
      davSearch.setResourcePath("/production/searchnode");
      
      SQLQuery query = new SQLQuery();
      query.setQuery("select * from nt:unstructured");
      davSearch.setQuery(query);
      
      int status = davSearch.execute();
      log.info("STATUS: " + status);      
      
    }
    
    log.info("done.");
  }
  
  public void testSearchNodeResource() throws Exception {
    if (true) {
      return;
    }
    log.info("testSearchNodeResource...");
    
    DavSearch davSearch = new DavSearch(TestContext.getContextAuthorized());
    davSearch.setResourcePath("/production/searchnode");
    
    SQLQuery query = new SQLQuery();
    query.setQuery("select * from nt:unstructured");
    davSearch.setQuery(query);
    
    int status = davSearch.execute();
    log.info("SEARCH STATUS: " + status);
    
    if (status == Const.HttpStatus.MULTISTATUS) {
      Multistatus multistatus = (Multistatus)davSearch.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();
      
      for (int i = 0; i < responses.size(); i++) {
        ResponseDoc response = responses.get(i);        
        log.info("RESPONSE: [" + response.getHref() + "]");
      }
      
    }
    
    log.info("done.");
  }
  
  public void testSearchWorkspaceResource() throws Exception {
    if (true) {
      return;
    }
    
    log.info("testSearchWorkspaceResource...");
    
    DavSearch davSearch = new DavSearch(TestContext.getContextAuthorized());
    davSearch.setResourcePath("/production");
    
    SQLQuery query = new SQLQuery();
    query.setQuery("select * from nt:file");
    davSearch.setQuery(query);
    
    int status = davSearch.execute();
    log.info("SEARCH STATUS: " + status);
    
    if (status == Const.HttpStatus.MULTISTATUS) {
      Multistatus multistatus = (Multistatus)davSearch.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();
      
      for (int i = 0; i < responses.size(); i++) {
        ResponseDoc response = responses.get(i);        
        log.info("RESPONSE: [" + response.getHref() + "]");
      }
      
    }
    
    log.info("done.");
  }
  
  public void testSearchRepository() throws Exception {
    log.info("testSearchRepository...");
    
    DavSearch davSearch = new DavSearch(TestContext.getContextAuthorized());
    davSearch.setResourcePath("/production/test");
    
    SQLQuery query = new SQLQuery();
    query.setQuery("select * from nt:base");
    davSearch.setQuery(query);
    
    int status = davSearch.execute();
    log.info("SEARCH STATUS: " + status);
    
    if (status == Const.HttpStatus.MULTISTATUS) {
      Multistatus multistatus = (Multistatus)davSearch.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();
      
      for (int i = 0; i < responses.size(); i++) {
        ResponseDoc response = responses.get(i);        
        log.info("RESPONSE: [" + response.getHref() + "]");
      }
      
    }
    
    log.info("done.");
  }
  
  public void testSimpleSearch() throws Exception {
    
    if (true) {
      return;
    }
    
    log.info("testSimpleSearch...");
    
    DavSearch davSearch = new DavSearch(TestContext.getContextAuthorized());
    davSearch.setResourcePath("/");
    
//    DavQuery query = new BasicSearchQuery();
//    davSearch.setQuery(query);
    
//    XPathQuery query = new XPathQuery();
//    
//    query.setQuery("select * from nt:file");
//    
//    davSearch.setQuery(query);
    
    SQLQuery query = new SQLQuery();
    query.setQuery("select * from nt:unstructured");
    davSearch.setQuery(query);
    
    int status = davSearch.execute();
    log.info("STATUS: " + status);
    
    Multistatus multistatus = (Multistatus)davSearch.getMultistatus();
    ArrayList<ResponseDoc> responses = multistatus.getResponses();

    log.info("---------------------------");
    for (int i = 0; i < responses.size(); i++) {
      ResponseDoc response = responses.get(i);
      log.info("HREF: [" + response.getHref() + "]");
    }
    log.info("---------------------------");
    
//    String reply = new String(davSearch.getResponseDataBuffer());
//    log.info("\r\n" + reply + "\r\n");
    
    log.info("done.");
  }
  
}
