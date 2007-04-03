/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.dasl.basicsearch;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.davclient.TestContext;
import org.exoplatform.frameworks.davclient.TestUtils;
import org.exoplatform.frameworks.davclient.commands.DavSearch;
import org.exoplatform.frameworks.davclient.documents.Multistatus;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class BasicSearchTest extends TestCase {
  
  private static Log log = ExoLogger.getLogger("jcr.BasicSearchTest");
  
  public void testSimpleBasisSearch() throws Exception {
    log.info("testSimpleBasisSearch...");
    
    createStrucutre("test_");
    
    
    
    /*
    String testFolderName = "/production/test_folder_" + System.currentTimeMillis();    
    TestUtils.createCollection(testFolderName);
    */
    
    
            //----------------- query 1 ------------------------------------------ 
            DavSearch davSearch = new DavSearch(TestContext.getContextAuthorized());
            davSearch.setResourcePath("/production");
            davSearch.setXmlEnabled(false);
    		
            log.info("EXECUTE QUERY1: ");
            String query = new String(BasicSearchTestConstants.QUERY1);
            davSearch.setRequestDataBuffer(query.getBytes());    
            int status = davSearch.execute();
            log.info("DAVSEARCH STATUS: " + status);
            TestUtils.showMultistatus((Multistatus)davSearch.getMultistatus());     	

            //----------------- query 2 ------------------------------------------ 
            davSearch = new DavSearch(TestContext.getContextAuthorized());
            davSearch.setResourcePath("/production");
            davSearch.setXmlEnabled(false);
    		
            log.info("EXECUTE QUERY2: ");
            query = new String(BasicSearchTestConstants.QUERY2);
            davSearch.setRequestDataBuffer(query.getBytes());    
            status = davSearch.execute();
            log.info("DAVSEARCH STATUS: " + status);
            TestUtils.showMultistatus((Multistatus)davSearch.getMultistatus());
 
            //----------------- query 3 ------------------------------------------ 
            davSearch = new DavSearch(TestContext.getContextAuthorized());
            davSearch.setResourcePath("/production");
            davSearch.setXmlEnabled(false);
    		
            log.info("EXECUTE QUERY3: ");
            query = new String(BasicSearchTestConstants.QUERY3);
            davSearch.setRequestDataBuffer(query.getBytes());    
            status = davSearch.execute();
            log.info("DAVSEARCH STATUS: " + status);
            TestUtils.showMultistatus((Multistatus)davSearch.getMultistatus());
            
            //----------------- query 4 ------------------------------------------ 
            davSearch = new DavSearch(TestContext.getContextAuthorized());
            davSearch.setResourcePath("/production");
            davSearch.setXmlEnabled(false);
    		
            log.info("EXECUTE QUERY4: ");
            query = new String(BasicSearchTestConstants.QUERY4);
            davSearch.setRequestDataBuffer(query.getBytes());    
            status = davSearch.execute();
            log.info("DAVSEARCH STATUS: " + status);
            TestUtils.showMultistatus((Multistatus)davSearch.getMultistatus());
        
            //----------------- query 5 ------------------------------------------ 
            davSearch = new DavSearch(TestContext.getContextAuthorized());
            davSearch.setResourcePath("/production");
            davSearch.setXmlEnabled(false);
    		
            log.info("EXECUTE QUERY5: ");
            query = new String(BasicSearchTestConstants.QUERY5);
            davSearch.setRequestDataBuffer(query.getBytes());    
            status = davSearch.execute();
            log.info("DAVSEARCH STATUS: " + status);
            TestUtils.showMultistatus((Multistatus)davSearch.getMultistatus());

            //----------------- query 6 ------------------------------------------ 
            davSearch = new DavSearch(TestContext.getContextAuthorized());
            davSearch.setResourcePath("/production");
            davSearch.setXmlEnabled(false);
    		
            log.info("EXECUTE QUERY6: ");
            query = new String(BasicSearchTestConstants.QUERY6);
            davSearch.setRequestDataBuffer(query.getBytes());    
            status = davSearch.execute();
            log.info("DAVSEARCH STATUS: " + status);
            TestUtils.showMultistatus((Multistatus)davSearch.getMultistatus());

            //----------------- query 7 ------------------------------------------ 
            davSearch = new DavSearch(TestContext.getContextAuthorized());
            davSearch.setResourcePath("/production");
            davSearch.setXmlEnabled(false);
    		
            log.info("EXECUTE QUERY7: ");
            query = new String(BasicSearchTestConstants.QUERY7);
            davSearch.setRequestDataBuffer(query.getBytes());    
            status = davSearch.execute();
            log.info("DAVSEARCH STATUS: " + status);
            TestUtils.showMultistatus((Multistatus)davSearch.getMultistatus());

            //----------------- query 8 ------------------------------------------ 
            davSearch = new DavSearch(TestContext.getContextAuthorized());
            davSearch.setResourcePath("/production");
            davSearch.setXmlEnabled(false);
    		
            log.info("EXECUTE QUERY8: ");
            query = new String(BasicSearchTestConstants.QUERY8);
            davSearch.setRequestDataBuffer(query.getBytes());    
            status = davSearch.execute();
            log.info("DAVSEARCH STATUS: " + status);
            TestUtils.showMultistatus((Multistatus)davSearch.getMultistatus());

            //----------------- query 9 ------------------------------------------ 
            davSearch = new DavSearch(TestContext.getContextAuthorized());
            davSearch.setResourcePath("/production");
            davSearch.setXmlEnabled(false);
    		
            log.info("EXECUTE QUERY9: ");
            query = new String(BasicSearchTestConstants.QUERY9);
            davSearch.setRequestDataBuffer(query.getBytes());    
            status = davSearch.execute();
            log.info("DAVSEARCH STATUS: " + status);
            TestUtils.showMultistatus((Multistatus)davSearch.getMultistatus());
            
            //----------------- query 10 ------------------------------------------ 
            davSearch = new DavSearch(TestContext.getContextAuthorized());
            davSearch.setResourcePath("/production");
            davSearch.setXmlEnabled(false);
    		
            log.info("EXECUTE QUERY10: ");
            query = new String(BasicSearchTestConstants.QUERY10);
            davSearch.setRequestDataBuffer(query.getBytes());    
            status = davSearch.execute();
            log.info("DAVSEARCH STATUS: " + status);
            TestUtils.showMultistatus((Multistatus)davSearch.getMultistatus());
            
              
            //----------------- query 11 ------------------------------------------ 
            davSearch = new DavSearch(TestContext.getContextAuthorized());
            davSearch.setResourcePath("/production");
            davSearch.setXmlEnabled(false);
    		
            log.info("EXECUTE QUERY11: ");
            query = new String(BasicSearchTestConstants.QUERY11);
            davSearch.setRequestDataBuffer(query.getBytes());    
            status = davSearch.execute();
            log.info("DAVSEARCH STATUS: " + status);
            TestUtils.showMultistatus((Multistatus)davSearch.getMultistatus());
            
            //----------------- query 12 ------------------------------------------ 
            davSearch = new DavSearch(TestContext.getContextAuthorized());
            davSearch.setResourcePath("/production");
            davSearch.setXmlEnabled(false);
    		
            log.info("EXECUTE QUERY12: ");
            query = new String(BasicSearchTestConstants.QUERY12);
            davSearch.setRequestDataBuffer(query.getBytes());    
            status = davSearch.execute();
            log.info("DAVSEARCH STATUS: " + status);
            TestUtils.showMultistatus((Multistatus)davSearch.getMultistatus());
        
        deleteStrucutre("test_");    
        /*
        log.info("Execute query1: ");
        query = query_constants.query3;
        davSearch.setRequestDataBuffer(query.getBytes());    
        status = davSearch.execute();
        log.info("DAVSEARCH STATUS: " + status);
        */
        
            	
    //    TestUtils.removeResource(testFolderName);
         log.info("done.");
  }
  
  public void createStrucutre(String folderName) throws Exception 
  {
	  String testFolderName = "/production/"+folderName;
    
	  TestUtils.createCollection(testFolderName);
	  TestUtils.createCollection(testFolderName + "/1");
	  TestUtils.createCollection(testFolderName + "/2");
	  TestUtils.createCollection(testFolderName + "/3");
	  
	  String fileName = testFolderName + "/1/test_" + "1_1" + ".txt";
	  TestUtils.createFile(fileName, ("FILE CONTENT " + "1-1").getBytes());
	  fileName = testFolderName + "/2/test_" + "2_1" + ".txt";
	  TestUtils.createFile(fileName, ("FILE CONTENT " + "2-1").getBytes());
	  fileName = testFolderName + "/3/test_" + "3_1" + ".txt";
	  TestUtils.createFile(fileName, ("FILE CONTENT " + "3-1").getBytes());
	  
	  for (int i = 1; i < 10; i++)
	  {
	     fileName = testFolderName + "/test_" + i + ".txt";
	     TestUtils.createFile(fileName, ("FILE CONTENT " + i).getBytes());
	  }
	  
	  fileName = testFolderName + "/test_contains.html";
	  String file_content = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\"> \r\n"
                          + " <HTML>  \r\n"		  
                          + "  <HEAD>  \r\n"
                          + "   <TITLE>JSR 170</TITLE>  \r\n"
                          + "  </HEAD>  \r\n"
                          + "  <BODY>JSR 170</BODY>  \r\n"
                          + " </HTML>  \r\n";
	  TestUtils.createFile(fileName, (file_content).getBytes());
	  
  }
  
  public void deleteStrucutre(String folderName) throws Exception
  {
	  String testFolderName = "/production/"+folderName;
	  TestUtils.removeResource(testFolderName);
  }
}
