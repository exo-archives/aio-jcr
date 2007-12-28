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

package org.exoplatform.frameworks.webdavclient.common;

import junit.framework.TestCase;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavCopy;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.http.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

/*
 * 
 * needs to test for FORBIDDEN & NOTFOUND replies
 * 
 */

public class CopyTest extends TestCase {
  
  private static final String SRC_WORKSPACE = "/production";
  private static final String DEST_WORKSPACE = "/backup";
       
  private static String getSourceName() {
    return "/test folder source " + System.currentTimeMillis();
  }
  
  private static String getDestinationName() {
    return "/test folder destination " + System.currentTimeMillis(); 
  }

  public void testNotAuthorized() throws Exception {
    Log.info("CopyTest:testNotAuthorized...");
    
    String sourceName = getSourceName();
    String destinationName = getDestinationName();
    
    DavCopy davCopy = new DavCopy(TestContext.getContext());
    davCopy.setResourcePath(SRC_WORKSPACE + sourceName);
    davCopy.setDestinationPath(DEST_WORKSPACE + destinationName);
    assertEquals(Const.HttpStatus.AUTHNEEDED, davCopy.execute());    
    
    Log.info("done.");
  }
  
  public void testCopyToSameWorkspace() throws Exception {
    Log.info("CopyTest:testCopyToSameWorkspace...");
    
    String sourceName = getSourceName();
    String destinationName = getDestinationName();
    
    {        
      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
      davMkCol.setResourcePath(SRC_WORKSPACE + sourceName);
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    }
    
    {
      DavCopy davCopy = new DavCopy(TestContext.getContextAuthorized());
      davCopy.setResourcePath(SRC_WORKSPACE + sourceName);
      davCopy.setDestinationPath(SRC_WORKSPACE + destinationName);
      assertEquals(Const.HttpStatus.CREATED, davCopy.execute());
    }
    
    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
      davDelete.setResourcePath(SRC_WORKSPACE + sourceName);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }
    
    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());        
      davDelete.setResourcePath(SRC_WORKSPACE + destinationName);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }
    
    Log.info("done.");
  }

  public void testCopyToAnotherWorkspace() throws Exception {
    Log.info("CopyTest:testCopyToAnotherWorkspace...");

    String sourceName = getSourceName();
    String destinationName = getDestinationName();    
    
    {        
      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
      davMkCol.setResourcePath(SRC_WORKSPACE + sourceName);
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    }

    {
      DavCopy davCopy = new DavCopy(TestContext.getContextAuthorized());
      davCopy.setResourcePath(SRC_WORKSPACE + sourceName);
      davCopy.setDestinationPath(DEST_WORKSPACE + destinationName);
      assertEquals(Const.HttpStatus.CREATED, davCopy.execute());
    }
    
    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
      davDelete.setResourcePath(SRC_WORKSPACE + sourceName);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }
    
    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());        
      davDelete.setResourcePath(DEST_WORKSPACE + destinationName);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }
    
    Log.info("done.");
  }

}
