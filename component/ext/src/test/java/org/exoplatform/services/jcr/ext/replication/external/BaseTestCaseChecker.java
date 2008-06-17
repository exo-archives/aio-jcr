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

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: ReplicationTestCaseChecker.java 111 2008-11-11 11:11:11Z
 *          rainf0x $
 */
public class BaseTestCaseChecker extends TestCase {
  protected static int MAX_RANDOM_VALUE = 1000000;
  protected final String workingRepository = "repository";

  protected final String workingWorkspace  = "draft";

  private MemberInfo[]   members           = new MemberInfo[] {
      new MemberInfo("192.168.0.5", 8080, "root", "exo"),
      new MemberInfo("192.168.0.5", 8090, "root", "exo"),
      new MemberInfo("192.168.0.15", 8080, "root", "exo"),
      new MemberInfo("192.168.0.135", 8080, "root", "exo")};

  private MemberInfo     masterMember;

  private MemberInfo[]   slaveMembers;

  protected String createRelPath(long fSize) {
    String alphabet = "abcdefghijklmnopqrstuvwxyz";
    String relPath = "";
    long pathDepth = (fSize % 7) + 5;

    for (long i = 0; i < pathDepth; i++) {
      int index1 = (int) (Math.random() * 1000) % alphabet.length();
      int index2 = (int) (Math.random() * 1000) % alphabet.length();
      String s = alphabet.substring(index1, index1 + 1) + alphabet.substring(index2, index2 + 1);
      //s+=(int) (Math.random() * 100000);
      
      relPath += ("::" + s);
    }

    return relPath;
  }
  
  public MemberInfo getCurrentMasterMember() {
    return masterMember;
  }

  public MemberInfo[] getCurrentSlaveMembers() {
    return slaveMembers;
  }
  
  public void randomizeMembers() {
    int masterIndex = (int)(Math.random() * 1000) % members.length;
    
    masterMember = members[masterIndex];
    
    slaveMembers = new MemberInfo[members.length-1];

    int slaveMembersIndex = 0;
    
    for (int i = 0; i < members.length; i++) 
      if (i != masterIndex)
        slaveMembers[slaveMembersIndex++] = members[i];
  }
}
