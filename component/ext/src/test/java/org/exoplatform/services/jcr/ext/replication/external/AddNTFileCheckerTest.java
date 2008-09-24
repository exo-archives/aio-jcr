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
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id$
 */
public class AddNTFileCheckerTest extends BaseTestCaseChecker {
  public void testAddNTFile() throws Exception {

    long[] filesSize = new long[] { 12314, 652125, 5212358, 2106584, 305682 };
    String relPathArray[] = new String[filesSize.length];
    String fileNameArray[] = new String[filesSize.length];

    // add nt:file to masterMember
    randomizeMembers();
    MemberInfo masterMember = getCurrentMasterMember();

    for (int i = 0; i < filesSize.length; i++) {
      long fSize = filesSize[i];
      String relPath = createRelPath(fSize);
      relPathArray[i] = relPath;
      fileNameArray[i] = "nt_file_" + fSize + "_" + (int) (Math.random() * MAX_RANDOM_VALUE);

      String url = "http://" + masterMember.getIpAddress() + ":" + masterMember.getPort()
          + ReplicationTestService.Constants.BASE_URL + "/" + workingRepository + "/"
          + workingWorkspace + "/" + masterMember.getLogin() + "/" + masterMember.getPassword()
          + "/" + relPath + "/" + fileNameArray[i] + "/" + fSize + "/"
          + ReplicationTestService.Constants.OPERATION_PREFIX
          + ReplicationTestService.Constants.OperationType.ADD_NT_FILE;

      BasicAuthenticationHttpClient client = new BasicAuthenticationHttpClient(masterMember);
      String result = client.execute(url);
      System.out.println(url);
      System.out.println(result);

      assertEquals(result, "ok");
    }

    // check nt:file in slaveMember

    for (int i = 0; i < filesSize.length; i++) {
      long fSize = filesSize[i];
      String relPath = relPathArray[i];

      for (MemberInfo slaveMember : getCurrentSlaveMembers()) {
        String checkUrl = "http://" + slaveMember.getIpAddress() + ":" + slaveMember.getPort()
            + ReplicationTestService.Constants.BASE_URL + "/" + workingRepository + "/"
            + workingWorkspace + "/" + slaveMember.getLogin() + "/" + slaveMember.getPassword()
            + "/" + relPath + "/" + fileNameArray[i] + "/" + fSize + "/"
            + ReplicationTestService.Constants.OPERATION_PREFIX
            + ReplicationTestService.Constants.OperationType.CHECK_NT_FILE;

        BasicAuthenticationHttpClient client = new BasicAuthenticationHttpClient(slaveMember, 4000);
        String result = client.execute(checkUrl);
        System.out.println(checkUrl);
        System.out.println(result);

        assertEquals(result, "ok");
      }
    }

    // delete nt:file from masterMember

    for (int i = 0; i < filesSize.length; i++) {
      String relPath = relPathArray[i];

      String url = "http://" + masterMember.getIpAddress() + ":" + masterMember.getPort()
          + ReplicationTestService.Constants.BASE_URL + "/" + workingRepository + "/"
          + workingWorkspace + "/" + masterMember.getLogin() + "/" + masterMember.getPassword()
          + "/" + relPath + "/" + fileNameArray[i] + "/"
          + ReplicationTestService.Constants.OPERATION_PREFIX
          + ReplicationTestService.Constants.OperationType.DELETE;

      BasicAuthenticationHttpClient client = new BasicAuthenticationHttpClient(masterMember);
      String result = client.execute(url);
      System.out.println(url);
      System.out.println(result);

      assertEquals(result, "ok");
    }

    // check deleted node in slaveMember

    for (int i = 0; i < filesSize.length; i++) {
      long fSize = filesSize[i];
      String relPath = relPathArray[i];

      for (MemberInfo slaveMember : getCurrentSlaveMembers()) {
        String checkUrl = "http://" + slaveMember.getIpAddress() + ":" + slaveMember.getPort()
            + ReplicationTestService.Constants.BASE_URL + "/" + workingRepository + "/"
            + workingWorkspace + "/" + slaveMember.getLogin() + "/" + slaveMember.getPassword()
            + "/" + relPath + "/" + fileNameArray[i] + "/"
            + ReplicationTestService.Constants.OPERATION_PREFIX
            + ReplicationTestService.Constants.OperationType.CHECK_DELETE;

        BasicAuthenticationHttpClient client = new BasicAuthenticationHttpClient(slaveMember, 4000);
        String result = client.execute(checkUrl);
        System.out.println(checkUrl);
        System.out.println(result);

        assertEquals(result, "ok");
      }
    }

  }
}
