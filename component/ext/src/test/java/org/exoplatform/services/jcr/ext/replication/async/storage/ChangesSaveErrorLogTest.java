/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.async.storage;

import java.io.File;

import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.replication.async.ChangesSaveErrorLog;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 27.01.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: ChangesSaveErrorLogTest.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class ChangesSaveErrorLogTest extends BaseStandaloneTest {

  public void testErrorsSave() throws Exception {

    File storage = new File("../target/temp/storage/" + System.currentTimeMillis());
    storage.mkdirs();
    
    ChangesSaveErrorLog errorLog = new ChangesSaveErrorLog(storage.getAbsolutePath(),
                                                           repository.getName(),
                                                           session.getWorkspace().getName());
    Exception e1 = new Exception("Exception #1");
    Exception e2 = new Exception("Exception #2");
    Exception e3 = new Exception("Exception #3");
    
    
    errorLog.reportError(e1);
    errorLog.reportError(e2);
    errorLog.reportError(e3);
    
    //check
    
    ChangesSaveErrorLog destErrorLog = new ChangesSaveErrorLog(storage.getAbsolutePath(),
                                                           repository.getName(),
                                                           session.getWorkspace().getName());
    
    String[] errors = destErrorLog.getErrors();
    
    assertEquals(3, errors.length);
    
    assertEquals(e1.getMessage(), errors[0]);
    assertEquals(e2.getMessage(), errors[1]);
    assertEquals(e3.getMessage(), errors[2]);
    
    assertNotNull(destErrorLog.getErrorLog());
  }
}
