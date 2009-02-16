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
public class VersionCheckerTest extends BaseTestCaseChecker {
  private static int    MANY_TEST          = 5;

  private static String relPathArray[]     = new String[MANY_TEST];

  private static String baseVersionValue[] = new String[MANY_TEST];

  private static String versionValue_1[]   = new String[MANY_TEST];

  private static String versionValue_2[]   = new String[MANY_TEST];

  public void testCreateVersionNode() throws Exception {
    randomizeMembers();
    MemberInfo masterMember = getCurrentMasterMember();

    // create version node in masterMember

    for (int i = 0; i < relPathArray.length; i++) {
      int rendomValue = (int) (Math.random() * MAX_RANDOM_VALUE);
      String relPath = createRelPath(rendomValue) + "::" + "version_node" + rendomValue;
      relPathArray[i] = relPath;
      baseVersionValue[i] = "base_version_value" + (int) (Math.random() * MAX_RANDOM_VALUE);

      String url = "http://" + masterMember.getIpAddress() + ":" + masterMember.getPort()
          + ReplicationTestService.Constants.BASE_URL + "/" + workingRepository + "/"
          + workingWorkspace + "/" + masterMember.getLogin() + "/" + masterMember.getPassword()
          + "/" + relPath + "/" + baseVersionValue[i] + "/"
          + ReplicationTestService.Constants.OperationType.ADD_VERSIONODE;

      BasicAuthenticationHttpClient client = new BasicAuthenticationHttpClient(masterMember);
      String result = client.execute(url);
      System.out.println(url);
      System.out.println(result);

      assertEquals(result, "ok");
    }

    // check version value in slaveMember

    for (int i = 0; i < relPathArray.length; i++) {
      String relPath = relPathArray[i];

      for (MemberInfo slaveMember : getCurrentSlaveMembers()) {
        String checkUrl = "http://" + slaveMember.getIpAddress() + ":" + slaveMember.getPort()
            + ReplicationTestService.Constants.BASE_URL + "/" + workingRepository + "/"
            + workingWorkspace + "/" + slaveMember.getLogin() + "/" + slaveMember.getPassword()
            + "/" + relPath + "/" + baseVersionValue[i] + "/"
            + ReplicationTestService.Constants.OperationType.CHECK_VERSION_NODE;

        BasicAuthenticationHttpClient client = new BasicAuthenticationHttpClient(slaveMember, 500);
        String result = client.execute(checkUrl);
        System.out.println(checkUrl);
        System.out.println(result);

        assertEquals(result, "ok");
      }
    }
  }

  public void testAddNewVersionValue() throws Exception {
    randomizeMembers();
    MemberInfo masterMember = getCurrentMasterMember();
    // create version node in masterMember

    for (int i = 0; i < relPathArray.length; i++) {
      versionValue_1[i] = "version_value_1_" + (int) (Math.random() * MAX_RANDOM_VALUE);

      String url = "http://" + masterMember.getIpAddress() + ":" + masterMember.getPort()
          + ReplicationTestService.Constants.BASE_URL + "/" + workingRepository + "/"
          + workingWorkspace + "/" + masterMember.getLogin() + "/" + masterMember.getPassword()
          + "/" + relPathArray[i] + "/" + versionValue_1[i] + "/"
          + ReplicationTestService.Constants.OperationType.ADD_NEW_VERSION;

      BasicAuthenticationHttpClient client = new BasicAuthenticationHttpClient(masterMember);
      String result = client.execute(url);
      System.out.println(url);
      System.out.println(result);

      assertEquals(result, "ok");
    }

    // check version value in slaveMember

    for (int i = 0; i < relPathArray.length; i++) {
      String relPath = relPathArray[i];

      for (MemberInfo slaveMember : getCurrentSlaveMembers()) {
        String checkUrl = "http://" + slaveMember.getIpAddress() + ":" + slaveMember.getPort()
            + ReplicationTestService.Constants.BASE_URL + "/" + workingRepository + "/"
            + workingWorkspace + "/" + slaveMember.getLogin() + "/" + slaveMember.getPassword()
            + "/" + relPath + "/" + versionValue_1[i] + "/"
            + ReplicationTestService.Constants.OperationType.CHECK_VERSION_NODE;

        BasicAuthenticationHttpClient client = new BasicAuthenticationHttpClient(slaveMember, 500);
        String result = client.execute(checkUrl);
        System.out.println(checkUrl);
        System.out.println(result);

        assertEquals(result, "ok");
      }
    }
  }

  public void testAddNewVersionValue2() throws Exception {
    randomizeMembers();
    MemberInfo masterMember = getCurrentMasterMember();

    // create version node in masterMember

    for (int i = 0; i < relPathArray.length; i++) {
      versionValue_2[i] = "version_value_2_" + (int) (Math.random() * MAX_RANDOM_VALUE);

      String url = "http://" + masterMember.getIpAddress() + ":" + masterMember.getPort()
          + ReplicationTestService.Constants.BASE_URL + "/" + workingRepository + "/"
          + workingWorkspace + "/" + masterMember.getLogin() + "/" + masterMember.getPassword()
          + "/" + relPathArray[i] + "/" + versionValue_2[i] + "/"
          + ReplicationTestService.Constants.OperationType.ADD_NEW_VERSION;

      BasicAuthenticationHttpClient client = new BasicAuthenticationHttpClient(masterMember);
      String result = client.execute(url);
      System.out.println(url);
      System.out.println(result);

      assertEquals(result, "ok");
    }

    // check version value in slaveMember

    for (int i = 0; i < relPathArray.length; i++) {
      String relPath = relPathArray[i];

      for (MemberInfo slaveMember : getCurrentSlaveMembers()) {
        String checkUrl = "http://" + slaveMember.getIpAddress() + ":" + slaveMember.getPort()
            + ReplicationTestService.Constants.BASE_URL + "/" + workingRepository + "/"
            + workingWorkspace + "/" + slaveMember.getLogin() + "/" + slaveMember.getPassword()
            + "/" + relPath + "/" + versionValue_2[i] + "/"
            + ReplicationTestService.Constants.OperationType.CHECK_VERSION_NODE;

        BasicAuthenticationHttpClient client = new BasicAuthenticationHttpClient(slaveMember, 500);
        String result = client.execute(checkUrl);
        System.out.println(checkUrl);
        System.out.println(result);

        assertEquals(result, "ok");
      }
    }
  }

  public void testRestorePreviousVersion() throws Exception {
    randomizeMembers();
    MemberInfo masterMember = getCurrentMasterMember();

    // create version node in masterMember

    for (int i = 0; i < relPathArray.length; i++) {
      String url = "http://" + masterMember.getIpAddress() + ":" + masterMember.getPort()
          + ReplicationTestService.Constants.BASE_URL + "/" + workingRepository + "/"
          + workingWorkspace + "/" + masterMember.getLogin() + "/" + masterMember.getPassword()
          + "/" + relPathArray[i] + "/" + ReplicationTestService.Constants.OperationType.RESTORE_RPEVIOUS_VERSION;
      BasicAuthenticationHttpClient client = new BasicAuthenticationHttpClient(masterMember);
      String result = client.execute(url);
      System.out.println(url);
      System.out.println(result);

      assertEquals(result, "ok");
    }

    // check version value in slaveMember

    for (int i = 0; i < relPathArray.length; i++) {
      String relPath = relPathArray[i];

      for (MemberInfo slaveMember : getCurrentSlaveMembers()) {
        String checkUrl = "http://" + slaveMember.getIpAddress() + ":" + slaveMember.getPort()
            + ReplicationTestService.Constants.BASE_URL + "/" + workingRepository + "/"
            + workingWorkspace + "/" + slaveMember.getLogin() + "/" + slaveMember.getPassword()
            + "/" + relPath + "/" + versionValue_1[i] + "/"
            + ReplicationTestService.Constants.OperationType.CHECK_VERSION_NODE;

        BasicAuthenticationHttpClient client = new BasicAuthenticationHttpClient(slaveMember, 500);
        String result = client.execute(checkUrl);
        System.out.println(checkUrl);
        System.out.println(result);

        assertEquals(result, "ok");
      }
    }
  }

  public void testRestoreBaseVersion() throws Exception {
    randomizeMembers();
    MemberInfo masterMember = getCurrentMasterMember();

    // create version node in masterMember

    for (int i = 0; i < relPathArray.length; i++) {
      String url = "http://" + masterMember.getIpAddress() + ":" + masterMember.getPort()
          + ReplicationTestService.Constants.BASE_URL + "/" + workingRepository + "/"
          + workingWorkspace + "/" + masterMember.getLogin() + "/" + masterMember.getPassword()
          + "/" + relPathArray[i] + "/" + ReplicationTestService.Constants.OperationType.RESTORE_BASE_VERSION;

      BasicAuthenticationHttpClient client = new BasicAuthenticationHttpClient(masterMember);
      String result = client.execute(url);
      System.out.println(url);
      System.out.println(result);

      assertEquals(result, "ok");
    }

    // check version value in slaveMember

    for (int i = 0; i < relPathArray.length; i++) {
      String relPath = relPathArray[i];

      for (MemberInfo slaveMember : getCurrentSlaveMembers()) {
        String checkUrl = "http://" + slaveMember.getIpAddress() + ":" + slaveMember.getPort()
            + ReplicationTestService.Constants.BASE_URL + "/" + workingRepository + "/"
            + workingWorkspace + "/" + slaveMember.getLogin() + "/" + slaveMember.getPassword()
            + "/" + relPath + "/" + baseVersionValue[i] + "/"
            + ReplicationTestService.Constants.OperationType.CHECK_VERSION_NODE;

        BasicAuthenticationHttpClient client = new BasicAuthenticationHttpClient(slaveMember, 500);
        String result = client.execute(checkUrl);
        System.out.println(checkUrl);
        System.out.println(result);

        assertEquals(result, "ok");
      }
    }
  }
}
