/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.dasl.basicsearch;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Z Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class BasicSearchTestConstants extends TestCase {
	
 //DAV:allprop check
 //SELECT * FROM nt:base WHERE  jcr:path LIKE '/test_'
 public static final String QUERY1 =
                 "<D:searchrequest xmlns:D=\"DAV:\" xmlns:jcr=\"jcr\">"
               + "<D:basicsearch>"
               +   "<D:select>"
               +     "<D:allprop/>"
               +   "</D:select>"
               +   "<D:from>"
               +     "<D:scope>"
               +      "<D:href>/test_</D:href>"
               +      "<D:depth>100</D:depth>"
               +     "</D:scope>"
               +   "</D:from>"
               + "</D:basicsearch>"
               + "</D:searchrequest>";
 
 //SELECT absence check (when the request contains only ignored DAV properties)
 //SELECT * FROM nt:base WHERE  jcr:path LIKE '/test_/%'
 public static final String QUERY2 =
	   "<D:searchrequest xmlns:D=\"DAV:\" xmlns:jcr=\"jcr\">"
     + "<D:basicsearch>"      
     +   "<D:select>"
     +     "<D:prop>"
     +       "<D:n1 />"
     +       "<D:n2 />"
     +     "</D:prop>"
     +   "</D:select>"
     +   "<D:from>"
     +     "<D:scope>"
     +      "<D:href>/test_/%</D:href>"
     +      "<D:depth>100</D:depth>"
     +     "</D:scope>"
     +   "</D:from>"
     + "</D:basicsearch>"
     + "</D:searchrequest>";
 
 //LIKE check, DAV:where absence check (infinity depth)
 //SELECT * FROM nt:base WHERE  jcr:path LIKE '/test_/%'
 public static final String QUERY3 =
	   "<D:searchrequest xmlns:D=\"DAV:\" xmlns:jcr=\"jcr\">"
     + "<D:basicsearch>"      
     +   "<D:select>"
     +     "<D:allprop/>"
     +   "</D:select>"
     +   "<D:from>"
     +     "<D:scope>"
     +      "<D:href>/test_/%</D:href>"
     +      "<D:depth>100</D:depth>"
     +     "</D:scope>"
     +   "</D:from>"
     + "</D:basicsearch>"
     + "</D:searchrequest>";

 //LIKE and NOT LIKE
 //SELECT * FROM nt:base WHERE NOT jcr:path LIKE '/test_/%/%' AND jcr:path LIKE '/test_/%'
 public static final String QUERY4 =
	   "<D:searchrequest xmlns:D=\"DAV:\" xmlns:jcr=\"jcr\">"
     + "<D:basicsearch>"      
     +   "<D:select>"
     +     "<D:allprop/>"
     +   "</D:select>"
     +   "<D:from>"
     +     "<D:scope>"
     +      "<D:href>/test_/%</D:href>"
     +      "<D:depth>100</D:depth>"
     +     "</D:scope>"
     +   "</D:from>"
     +   "<D:where>"
     +      "<D:not>"
     +         "<D:like>"
     +          "<D:prop>"
     +           "<jcr:path />"
     +          "</D:prop>"
     +         "<D:literal>"
     +           "/test_/%/%"
     +         "</D:literal>"
     +         "</D:like>"
     +     "</D:not>"
     +   "</D:where>"
     + "</D:basicsearch>"
     + "</D:searchrequest>";
 
 //Select only nodes from test folder which ends with jcr:content
 //SELECT * FROM nt:base WHERE  jcr:path LIKE '/test_/%/jcr:content'
 public static final String QUERY5 =
	   "<A:searchrequest xmlns:A=\"DAV:\" xmlns:jcr=\"jcr\">"
     + "<A:basicsearch>"      
     +   "<A:select>"
     +     "<A:allprop/>"
     +   "</A:select>"
     +   "<A:from>"
     +     "<A:scope>"
     +      "<A:href>/test_/%/jcr:content</A:href>"
     +      "<A:depth>100</A:depth>"
     +     "</A:scope>"
     +   "</A:from>"
     + "</A:basicsearch>"
     + "</A:searchrequest>";
  
 //EQ check (select nodes only with nt:file type)
 //SELECT * FROM nt:base WHERE jcr:primaryType = 'nt:file' AND jcr:path LIKE '/test_/%'
 public static final String QUERY6 =
	  "<D:searchrequest xmlns:D=\"DAV:\" xmlns:jcr=\"jcr\">"
      + "<D:basicsearch>"      
      +   "<D:select>"
      +     "<D:allprop/>"
      +   "</D:select>"
      +   "<D:from>"
      +     "<D:scope>"
      +      "<D:href>/test_/%</D:href>"
      +      "<D:depth>100</D:depth>"
      +     "</D:scope>"
      +   "</D:from>"
      +   "<D:where>"
      +       "<D:eq>"
      +          "<D:prop>"
      +           "<jcr:primaryType />"
      +          "</D:prop>"
      +         "<D:literal>"
      +           "nt:file"
      +         "</D:literal>"
      +       "</D:eq>"
      +   "</D:where>"
      + "</D:basicsearch>"
      + "</D:searchrequest>";
  
 //GT or GTE check
 //SELECT * FROM nt:base WHERE jcr:uuid > '[08d211cbc0a800040178f2b22b9eae38]'
 //AND jcr:path LIKE '/test_/%'
 public static final String QUERY7 = 
    "<D:searchrequest xmlns:D=\"DAV:\" xmlns:jcr=\"jcr\">"
  + "<D:basicsearch>"      
  +   "<D:select>"
  +     "<D:allprop/>"
  +   "</D:select>"
  +   "<D:from>"
  +     "<D:scope>"
  +      "<D:href>/test_/%</D:href>"
  +      "<D:depth>100</D:depth>"
  +     "</D:scope>"
  +   "</D:from>"
  +   "<D:where>"
  +       "<D:gt>"
  +          "<D:prop>"
  +           "<jcr:uuid />"
  +          "</D:prop>"
  +         "<D:literal>"
  +           "[08d211cbc0a800040178f2b22b9eae38]"
  +         "</D:literal>"
  +       "</D:gt>"
  +   "</D:where>"
  + "</D:basicsearch>"
  + "</D:searchrequest>";
   
 //LT or LTE
 //SELECT * FROM nt:base WHERE jcr:uuid < '[08d211cbc0a800040178f2b22b9eae38]'
 //AND jcr:path LIKE '/test_/%'
 public static final String QUERY8 = 
	    "<D:searchrequest xmlns:D=\"DAV:\" xmlns:jcr=\"jcr\">"
	  + "<D:basicsearch>"      
	  +   "<D:select>"
	  +     "<D:allprop/>"
	  +   "</D:select>"
	  +   "<D:from>"
	  +     "<D:scope>"
	  +      "<D:href>/test_/%</D:href>"
	  +      "<D:depth>100</D:depth>"
	  +     "</D:scope>"
	  +   "</D:from>"
	  +   "<D:where>"
	  +       "<D:lt>"
	  +          "<D:prop>"
	  +           "<jcr:uuid />"
	  +          "</D:prop>"
	  +         "<D:literal>"
	  +           "[08d211cbc0a800040178f2b22b9eae38]"
	  +         "</D:literal>"
	  +       "</D:lt>"
	  +   "</D:where>"
	  + "</D:basicsearch>"
	  + "</D:searchrequest>";
  
 //AND and OR nesting
 //SELECT * FROM nt:base WHERE jcr:uuid > '[08d211cbc0a800040178f2b22b9eae38]'
 //OR jcr:primaryType = 'nt:file' AND jcr:uuid > '[08d211cbc0a800040178f2b22b9eae38]'
 //OR jcr:primaryType = 'nt:folder' AND jcr:path LIKE '/test_/%'
 public static final String QUERY9 =
        "<D:searchrequest xmlns:D=\"DAV:\" xmlns:jcr=\"jcr\">"
      + "<D:basicsearch>"      
      +   "<D:select>"
      +     "<D:allprop/>"
      +   "</D:select>"
      +   "<D:from>"
      +     "<D:scope>"
      +      "<D:href>/test_/%</D:href>"
      +      "<D:depth>100</D:depth>"
      +     "</D:scope>"
      +   "</D:from>"
      +   "<D:where>"
      +     "<D:and>"
  
      +     "<D:or>"
      +       "<D:gt>"
      +          "<D:prop>"
      +           "<jcr:uuid />"
      +          "</D:prop>"
      +         "<D:literal>"
      +           "[08d211cbc0a800040178f2b22b9eae38]"
      +         "</D:literal>"
      +       "</D:gt>"
      +       "<D:eq>"
      +          "<D:prop>"
      +           "<jcr:primaryType />"
      +          "</D:prop>"
      +         "<D:literal>"
      +           "nt:file"
      +         "</D:literal>"
      +       "</D:eq>"
      +     "</D:or>"
  
      +     "<D:or>"
      +       "<D:gt>"
      +          "<D:prop>"
      +           "<jcr:uuid />"
      +          "</D:prop>"
      +         "<D:literal>"
      +           "[08d211cbc0a800040178f2b22b9eae38]"
      +         "</D:literal>"
      +       "</D:gt>"
      +       "<D:eq>"
      +          "<D:prop>"
      +           "<jcr:primaryType />"
      +          "</D:prop>"
      +         "<D:literal>"
      +           "nt:folder"
      +         "</D:literal>"
      +       "</D:eq>"
      +     "</D:or>"
      
      +    "</D:and>"
      +   "</D:where>"
      + "</D:basicsearch>"
      + "</D:searchrequest>";
 
   //ORDERBY check by ascending
   //SELECT * FROM nt:base WHERE jcr:primaryType = 'nt:file' AND jcr:path LIKE '/test_/%'
   //ORDER BY jcr:created ASC
   public static final String QUERY10 =
        "<D:searchrequest xmlns:D=\"DAV:\" xmlns:jcr=\"jcr\">"
      + "<D:basicsearch>"      
      +   "<D:select>"
      +     "<D:allprop/>"
      +   "</D:select>"
      +   "<D:from>"
      +     "<D:scope>"
      +      "<D:href>/test_/%</D:href>"
      +      "<D:depth>100</D:depth>"
      +     "</D:scope>"
      +   "</D:from>"
      +   "<D:where>"
      +       "<D:eq>"
      +          "<D:prop>"
      +           "<jcr:primaryType />"
      +          "</D:prop>"
      +         "<D:literal>"
      +           "nt:file"
      +         "</D:literal>"
      +       "</D:eq>"
      +   "</D:where>"
      +   "<D:orderby>"
      +     "<D:order>"
      +       "<D:prop>"
      +		    "<jcr:created />"
      +		  "</D:prop>"
      +       "<D:ascending />"
      +     "</D:order>"
      +   "</D:orderby>"
      + "</D:basicsearch>"
      + "</D:searchrequest>";
   
   //ORDERBY check by descending
   //SELECT * FROM nt:base WHERE jcr:primaryType = 'nt:file' AND jcr:path LIKE '/test_/%'
   //ORDER BY jcr:created DESC
   public static final String QUERY11 =
       "<D:searchrequest xmlns:D=\"DAV:\" xmlns:jcr=\"jcr\">"
     + "<D:basicsearch>"      
     +   "<D:select>"
     +     "<D:allprop/>"
     +   "</D:select>"
     +   "<D:from>"
     +     "<D:scope>"
     +      "<D:href>/test_/%</D:href>"
     +      "<D:depth>100</D:depth>"
     +     "</D:scope>"
     +   "</D:from>"
     +   "<D:where>"
     +       "<D:eq>"
     +          "<D:prop>"
     +           "<jcr:primaryType />"
     +          "</D:prop>"
     +         "<D:literal>"
     +           "nt:file"
     +         "</D:literal>"
     +       "</D:eq>"
     +   "</D:where>"
     +   "<D:orderby>"
     +     "<D:order>"
     +       "<D:prop>"
     +		    "<jcr:created />"
     +		  "</D:prop>"
     +       "<D:descending />"
     +     "</D:order>"
     +   "</D:orderby>"
     + "</D:basicsearch>"
     + "</D:searchrequest>";
   
   //CONTAINS check
   public static final String QUERY12 =
       "<D:searchrequest xmlns:D=\"DAV:\" xmlns:jcr=\"jcr\">"
     + "<D:basicsearch>"      
     +   "<D:select>"
     +     "<D:allprop/>"
     +   "</D:select>"
     +   "<D:from>"
     +     "<D:scope>"
     +      "<D:href>/test_/%</D:href>"
     +      "<D:depth>100</D:depth>"
     +     "</D:scope>"
     +   "</D:from>"
     +   "<D:where>"
     +     "<D:contains>JSR</D:contains>"
     +   "</D:where>"
     + "</D:basicsearch>"
     + "</D:searchrequest>";
   
  private static Log log = ExoLogger.getLogger("jcr.BasicSearchTest");

  public void testSimpleBasisSearch() throws Exception {
    log.info("testSimpleBasisSearch...");

    }
  }
