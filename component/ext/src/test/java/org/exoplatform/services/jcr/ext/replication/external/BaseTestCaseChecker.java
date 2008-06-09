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
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a> 
 * @version $Id: ReplicationTestCaseChecker.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class BaseTestCaseChecker extends TestCase {
  protected final String workingRepository = "repository";
  protected final String workingWorkspace = "backup";
  protected final MemberInfo masterMember = new MemberInfo("192.168.0.15", 8080, "admin", "admin");

  protected final MemberInfo[] slaveMembers = new MemberInfo[] { 
      new MemberInfo("192.168.0.15", 8080, "admin", "admin"),
      new MemberInfo("192.168.0.15", 8080, "admin", "admin") };
  
  protected String createRelPath(long fSize) {
    String alphabet = "abcdefghijklmnopqrstuvwxyz";
    String relPath = "";
    long pathDepth = fSize % 7;
    
    for (long i = 0; i < pathDepth; i++) {
      int index1 = (int)(Math.random() * 1000) % alphabet.length();
      int index2 = (int)(Math.random() * 1000) % alphabet.length();
      String s = alphabet.substring(index1, index1+1) + alphabet.substring(index2, index2+1);
      relPath +=("::" + s); 
    }
    
    return relPath;
  }
}

