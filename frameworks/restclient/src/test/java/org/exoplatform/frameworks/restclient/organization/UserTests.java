/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.restclient.organization;

import junit.framework.TestCase;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.restclient.RestContext;
import org.exoplatform.frameworks.restclient.RestStatus;
import org.exoplatform.frameworks.restclient.command.GetCommand;
import org.exoplatform.frameworks.restclient.command.PutCommand;
import org.exoplatform.frameworks.restclient.common.template.Template;
import org.exoplatform.frameworks.restclient.organization.template.OrganizationTemplate;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class UserTests extends TestCase {
  
  private static RestContext context = new RestContext("192.168.0.5", 8080, "/rest");
  
  public static final String ORGANIZATION_PATH = "/organization";
  public static final String USERS_PATH = ORGANIZATION_PATH + "/users";
  
  public void testCreateUser() {
    Log.info("public void testCreateUser()");
    
    try {
      //String testUserId = "test1232";
      String testUserId = "gavrik_id";
      
      String testUserPassword = "test2pass";
      String testUserFirstName = "test 2 name";
      String testUserLastName = "test 2 last name";
      String testUserEMail = "gavrik-vetal-sp@ukr.net";      
      
      // CREATE NEW USER
//      {
//        OrganizationTemplate userTemplate =
//          new OrganizationTemplate(OrganizationTemplate.TEMPLATE_USER);
//        
//        userTemplate.setPropertyValue(OrganizationTemplate.XML_USERID, testUserId);
//        userTemplate.setPropertyValue(OrganizationTemplate.XML_USERPASSWORD, testUserPassword);
//        userTemplate.setPropertyValue(OrganizationTemplate.XML_FIRSTNAME, testUserFirstName);
//        userTemplate.setPropertyValue(OrganizationTemplate.XML_LASTNAME, testUserLastName);
//        userTemplate.setPropertyValue(OrganizationTemplate.XML_EMAIL, testUserEMail);
//
//        PutCommand putCommand = new PutCommand(context, userTemplate);
//        putCommand.setSrcPath(USERS_PATH);
//        
//        assertEquals(RestStatus.CREATED, putCommand.execute());
//        
//        Log.info("NEW USER CREATED!!!!!!!!!!!");
//      }
      
      // TEST IT
      {
        GetCommand getCommand = new GetCommand(context, null);
        getCommand.setSrcPath(USERS_PATH + "/" + testUserId);
        
        //assertEquals(RestStatus.OK, getCommand.execute());
        int status = getCommand.execute();
        Log.info("GET STATUS: " + status);
        
        if (status == RestStatus.OK) {
          Log.info("NEW USER VERIFIED!!!!!!");
          Template userTemplate = getCommand.getResponseTemplate();
        }
        
        
      }
      
      // DELETE THIS USER
      {
        
      }
      
      // TEST IT FOR NOT FOUND
      {
        
      }
      
      
    } catch (Exception exc) {
      Log.info("Unhandled exception. ", exc);
    }
    
    Log.info("done.");
  }

}

