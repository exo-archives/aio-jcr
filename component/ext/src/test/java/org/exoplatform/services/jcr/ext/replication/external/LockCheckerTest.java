/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.external;

import org.exoplatform.services.jcr.ext.replication.test.ReplicationTestService;

/**
 * Created by The eXo Platform SAS
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a> 
 * @version $Id: LockCheckerTest.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class LockCheckerTest extends BaseTestCaseChecker {
  public void testLock() throws Exception {
    String relPathArray[] = new String[5];
    
    //set lock to masterMember
    
    for (int i = 0; i < relPathArray.length; i++) {
      int rendomValue = (int)(Math.random() * 1000);
      String relPath = createRelPath(rendomValue) + "::" + "locked_node"+ rendomValue;
      relPathArray[i] = relPath;
      
      String url = "http://" + masterMember.getIpAddress() + ":" 
                             + masterMember.getPort()  
                             + ReplicationTestService.Constants.BASE_URL
                             + "/" + workingRepository
                             + "/" + workingWorkspace
                             + "/" + masterMember.getLogin()
                             + "/" + masterMember.getPassword() 
                             + "/" + relPath + "/"
                             + ReplicationTestService.Constants.OPERATION_PREFIX
                             + ReplicationTestService.Constants.OperationType.SET_LOCK;
      
      BasicAuthenticationHttpClient client = new BasicAuthenticationHttpClient(masterMember);
      String result = client.execute(url);
      System.out.println(url);
      System.out.println(result);
      
      assertEquals(result, "ok");
    }
    
    // check relock in slaveMember
    
    for (int i = 0; i < relPathArray.length; i++) {
      String relPath = relPathArray[i];
      
      for (MemberInfo slaveMember : slaveMembers) {
        String checkUrl = "http://" + masterMember.getIpAddress() + ":" 
                                    + masterMember.getPort()  
                                    + ReplicationTestService.Constants.BASE_URL
                                    + "/" + workingRepository
                                    + "/" + workingWorkspace
                                    + "/" + masterMember.getLogin()
                                    + "/" + masterMember.getPassword() 
                                    + "/" + relPath + "/"
                                    + ReplicationTestService.Constants.OPERATION_PREFIX
                                    + ReplicationTestService.Constants.OperationType.SET_LOCK;
        
        BasicAuthenticationHttpClient client = new BasicAuthenticationHttpClient(slaveMember);
        String result = client.execute(checkUrl);
        System.out.println(checkUrl);
        System.out.println(result);
        
        assertEquals(result, "fail");
      }
    }
    
    // check lock in slaveMember
    
    for (int i = 0; i < relPathArray.length; i++) {
      String relPath = relPathArray[i];
      
      for (MemberInfo slaveMember : slaveMembers) {
        String checkUrl = "http://" + masterMember.getIpAddress() + ":" 
                                    + masterMember.getPort()  
                                    + ReplicationTestService.Constants.BASE_URL
                                    + "/" + workingRepository
                                    + "/" + workingWorkspace
                                    + "/" + masterMember.getLogin()
                                    + "/" + masterMember.getPassword() 
                                    + "/" + relPath + "/"
                                    + ReplicationTestService.Constants.OPERATION_PREFIX
                                    + ReplicationTestService.Constants.OperationType.CECK_LOCK;
        
        BasicAuthenticationHttpClient client = new BasicAuthenticationHttpClient(slaveMember);
        String result = client.execute(checkUrl);
        System.out.println(checkUrl);
        System.out.println(result);
        
        assertEquals(result, "ok");
      }
    }
  }
}
