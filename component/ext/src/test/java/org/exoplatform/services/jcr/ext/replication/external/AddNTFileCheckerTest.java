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
 * @version $Id: AddNTFileChecker.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class AddNTFileCheckerTest extends BaseTestCaseChecker {
  public void testAddNTFile() throws Exception {
    
    long[] filesSize = new long[]{12314 ,652125, 5212358, 21658425, 23549682};
    String relPathArray[] = new String[filesSize.length];
    
    //add nt:file to masterMember
    
    for (int i = 0; i < filesSize.length; i++) {
      long fSize = filesSize[i];
      String relPath = createRelPath(fSize);
      relPathArray[i] = relPath;
      
      
      String url = "http://" + masterMember.getIpAddress() + ":" 
                             + masterMember.getPort()  
                             + ReplicationTestService.Constants.BASE_URL
                             + "/" + workingRepository
                             + "/" + workingWorkspace
                             + "/" + masterMember.getLogin()
                             + "/" + masterMember.getPassword() 
                             + "/" + relPath
                             + "/" + "nt_file_" + fSize
                             + "/" + fSize + "/"
                             + ReplicationTestService.Constants.OPERATION_PREFIX
                             + ReplicationTestService.Constants.OperationType.ADD_NT_FILE;
      
      BasicAuthenticationHttpClient client = new BasicAuthenticationHttpClient(masterMember);
      String result = client.execute(url);
      System.out.println(url);
      System.out.println(result);
      
      assertEquals(result, "ok");
    }
    
    //sleep 10 seconds
    
    Thread.sleep(30000);
    
    // check nt:file in slaveMember
    
    for (int i = 0; i < filesSize.length; i++) {
      long fSize = filesSize[i];
      String relPath = relPathArray[i];
      
      for (MemberInfo slaveMember : slaveMembers) {
        String checkUrl = "http://" + slaveMember.getIpAddress() + ":" 
                               + slaveMember.getPort()  
                               + ReplicationTestService.Constants.BASE_URL
                               + "/" + workingRepository
                               + "/" + workingWorkspace 
                               + "/" + slaveMember.getLogin()
                               + "/" + slaveMember.getPassword()
                               + "/" + relPath 
                               + "/" + "nt_file_" + fSize
                               + "/" + fSize + "/"
                               + ReplicationTestService.Constants.OPERATION_PREFIX
                               + ReplicationTestService.Constants.OperationType.CHECK_NT_FILE;
        
        BasicAuthenticationHttpClient client = new BasicAuthenticationHttpClient(slaveMember);
        String result = client.execute(checkUrl);
        System.out.println(checkUrl);
        System.out.println(result);
        
        assertEquals(result, "ok");
      }
    }
    
  }
}
