/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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

package org.exoplatform.services.jcr.webdav.command.lock;

import java.util.ArrayList;

import javax.jcr.Node;

import org.exoplatform.services.jcr.webdav.Depth;
import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.command.LockCommand;
import org.exoplatform.services.jcr.webdav.command.UnLockCommand;
import org.exoplatform.services.jcr.webdav.lock.NullResourceLocksHolder;
import org.exoplatform.services.rest.Response;

/**
 * Created by The eXo Platform SAS.
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class TestUnLock extends LockTest {
  
  protected Node unLockNode;
  
  public void setUp() throws Exception {
    super.setUp();
    if(unLockNode == null) {
      unLockNode = lockNode.addNode("testUnLockNode", "nt:unstructured");
      session.save();
    }
  }   
  
  public void testUnLock() throws Exception {
    String path = unLockNode.getPath();
    NullResourceLocksHolder lockHolder = new NullResourceLocksHolder();
    
    assertEquals(WebDavStatus.NO_CONTENT, new UnLockCommand(lockHolder).unLock(session, path, null).getStatus());    
    
    assertEquals(WebDavStatus.OK, new LockCommand(lockHolder).lock(session, path, null, new Depth(null), "Infinity").getStatus());     
    
    assertEquals(true, unLockNode.isLocked());
    
    String lockToken = unLockNode.getLock().getLockToken();
    session.removeLockToken(lockToken);
    
    assertEquals(WebDavStatus.LOCKED, new UnLockCommand(lockHolder).unLock(session, path, null).getStatus());
    
    session.addLockToken(lockToken);
    ArrayList<String> tokens = new ArrayList<String>();
    tokens.add(lockToken);
    
    assertEquals(WebDavStatus.NO_CONTENT, new UnLockCommand(lockHolder).unLock(session, path, tokens).getStatus());
  }
  
  public void testNullResourceUnLock() throws Exception {
    String path = unLockNode.getPath() + "/somenotexistednode";
    
    NullResourceLocksHolder lockHolder = new NullResourceLocksHolder();
    
    assertEquals(WebDavStatus.NOT_FOUND, new UnLockCommand(lockHolder).unLock(session, path, null).getStatus());

    Response response = new LockCommand(lockHolder).lock(session, path, null, new Depth(null), "Infinity");
    assertEquals(WebDavStatus.OK, response.getStatus());

    String lockToken = response.getResponseHeaders().get("Lock-Token");
    
    lockToken = lockToken.substring(1, lockToken.length() - 1);
    
    assertEquals(WebDavStatus.LOCKED, new UnLockCommand(lockHolder).unLock(session, path, null).getStatus()); 

    ArrayList<String> tokens = new ArrayList<String>();
    tokens.add(lockToken);
    
    assertEquals(WebDavStatus.NO_CONTENT, new UnLockCommand(lockHolder).unLock(session, path, tokens).getStatus());
    
  }

}
