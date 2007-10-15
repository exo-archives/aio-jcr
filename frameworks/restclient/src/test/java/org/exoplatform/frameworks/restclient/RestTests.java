/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.restclient;

import junit.framework.TestSuite;

import org.exoplatform.frameworks.restclient.organization.UserTests;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class RestTests extends TestSuite {
  
  public static final String HOST = "192.168.0.5";
  public static final int PORT = 8080;
  public static final String SERVLET = "/rest";
  
  public static RestContext context = new RestContext(HOST, PORT, SERVLET);
  
  public RestTests() {
    addTestSuite(UserTests.class);
  }
  
  public void testSimple() {
  }
  
//  public void testSimpleGetUsers() {
//    
//    try {
//      GetCommand getCommand = new GetCommand(context, null);
//      getCommand.setSrcPath("/organization/users");
//      
//      int status = getCommand.execute();
//      Log.info("STATUS: " + status);
//      
//      Log.info("REPLY: " + new String(getCommand.getResponseBytes()));
//      
//    } catch (Exception exc) {
//      Log.info("Unhandled exception. ", exc);
//    }
//        
//  }
  
//  public void testRest() {
//    
//    if (true) {
//      return;
//    }
//    
//    try {      
//      Log.info("public void testRest()");
//      
//      {
//        OrganizationTemplate template = 
//            new OrganizationTemplate(OrganizationTemplate.TEMPLATE_USER);
//        
//        template.setPropertyValue(OrganizationTemplate.XML_USERID, "jdoe");
//        template.setPropertyValue(OrganizationTemplate.XML_FIRSTNAME, "John");
//        template.setPropertyValue(OrganizationTemplate.XML_LASTNAME, "Doe");
//        template.setPropertyValue(OrganizationTemplate.XML_EMAIL, "jdoe@exoplatform.com");
//        template.setPropertyValue(OrganizationTemplate.XML_PROFILE, "http://localhost:8080/rest/organization/profile/", true);
//        template.setPropertyValue(OrganizationTemplate.XML_MEMBERSHIP, "http://www.exoplatform.com /rest/organization/memberships/user/jdoe", true);
//              
//        GetCommand getCommand = new GetCommand(context, template);      
//        int status = getCommand.execute();
//        Log.info("STATUS: " + status);
//      }
//      
//      {
//        OrganizationTemplate template =
//            new OrganizationTemplate(OrganizationTemplate.TEMPLATE_GROUP);
//        
//        template.setPropertyValue(OrganizationTemplate.XML_GROUPID, "admin");
//        template.setPropertyValue(OrganizationTemplate.XML_LABEL, "Administrators");
//        template.setPropertyValue(OrganizationTemplate.XML_DESCRIPTION, "Administrators");
//        template.setPropertyValue(OrganizationTemplate.XML_MEMBERSHIP, "http://www.exoplatform.com /rest/organization/memberships/group/admin", true);
//        
//        GetCommand getCommand = new GetCommand(context, template);
//        int status = getCommand.execute();
//        Log.info("STATUS: " + status);
//      }
//      
//      {
//        OrganizationTemplate template =
//            new OrganizationTemplate(OrganizationTemplate.TEMPLATE_MEMBERSHIPTYPE);
//        
//        template.setPropertyValue(OrganizationTemplate.XML_MEMBERSHIPTYPEID, "member");
//        template.setPropertyValue(OrganizationTemplate.XML_DESCRIPTION, "Member");
//
//        GetCommand getCommand = new GetCommand(context, template);      
//        int status = getCommand.execute();
//        Log.info("STATUS: " + status);
//      }
//      
//      {
//        OrganizationTemplate template =
//            new OrganizationTemplate(OrganizationTemplate.TEMPLATE_MEMBERSHIP);
//        
//        template.setPropertyValue(OrganizationTemplate.XML_MEMBERSHIPID, "1234");
//        template.setPropertyValue(OrganizationTemplate.XML_USER, "http://www.exoplatform.com /rest/organization/user/jdoe", true);
//        template.setPropertyValue(OrganizationTemplate.XML_GROUP, "http://www.exoplatform.com /rest/organization/group/admin", true);
//        template.setPropertyValue(OrganizationTemplate.XML_MEMBERSHIPTYPE, "http://www.exoplatform.com /rest/organization/membership-type/member", true);
//
//        GetCommand getCommand = new GetCommand(context, template);      
//        int status = getCommand.execute();
//        Log.info("STATUS: " + status);      
//      }
//      
//    } catch (Exception exc) {
//      Log.info("Unhandled exception.", exc);
//    }
//    
//  }
  
//  public void testRestList() {
//    
//    if (true) {
//      return;
//    }
//    
//    try {
//      
//      OrganizationTemplateList templateList = 
//        new OrganizationTemplateList(OrganizationTemplateList.LIST_USER);
//
//      {
//        OrganizationTemplate user =
//          new OrganizationTemplate(OrganizationTemplate.TEMPLATE_USER);
//
//        user.setPropertyValue(OrganizationTemplate.XML_USERID, "user1");
//        user.setPropertyValue(OrganizationTemplate.XML_FIRSTNAME, "user 1 name");
//        user.setPropertyValue(OrganizationTemplate.XML_LASTNAME, "user 1 last name");
//        user.setPropertyValue(OrganizationTemplate.XML_EMAIL, "user1@exoplatform.com");
//        user.setPropertyValue(OrganizationTemplate.XML_PROFILE, "http://localhost:8080/rest/organization/profile/", true);
//        user.setPropertyValue(OrganizationTemplate.XML_MEMBERSHIP, "http://www.exoplatform.com /rest/organization/memberships/user/user1", true);
//        
//        templateList.addTemplate(user);
//      }
//      
//      {
//        OrganizationTemplate user =
//          new OrganizationTemplate(OrganizationTemplate.TEMPLATE_USER);
//
//        user.setPropertyValue(OrganizationTemplate.XML_USERID, "user2");
//        user.setPropertyValue(OrganizationTemplate.XML_FIRSTNAME, "user 2 name");
//        user.setPropertyValue(OrganizationTemplate.XML_LASTNAME, "user 2 last name");
//        user.setPropertyValue(OrganizationTemplate.XML_EMAIL, "user2@exoplatform.com");
//        user.setPropertyValue(OrganizationTemplate.XML_PROFILE, "http://localhost:8080/rest/organization/profile/", true);
//        user.setPropertyValue(OrganizationTemplate.XML_MEMBERSHIP, "http://www.exoplatform.com /rest/organization/memberships/user/user2", true);
//        
//        templateList.addTemplate(user);        
//      }
//      
//      GetCommand getCommand = new GetCommand(context, templateList);
//      getCommand.setSrcPath("/");
//      int status = getCommand.execute();
//      Log.info("GET STATUS: " + status);
//      
//    } catch (Exception exc) {
//      Log.info("Unhandled exception", exc);
//    }
//  }
  
}

