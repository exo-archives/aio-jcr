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
package org.exoplatform.services.jcr.ext.backup;

import java.io.File;
import java.util.Calendar;

import junit.framework.TestCase;

import org.exoplatform.services.jcr.ext.backup.impl.fs.FileNameProducer;

/**
 * Created by The eXo Platform SARL
 * Author : Alex Reshetnyak
 *          alex.reshetnyak@exoplatform.com.ua
 *          reshetnyak.alex@exoplatform.com.ua
 * Nov 20, 2007  
 */
public class TestFileNameProduser extends TestCase {
  FileNameProducer nameProducer;
  File tempDir;
  String backupsetName;

  public void testGetNextName() throws Exception {
    tempDir = new File ("target" + File.separator + "temp" + File.separator + "fileProduser");
    tempDir.mkdirs();
    
    backupsetName = String.valueOf(System.currentTimeMillis());
    
    nextName(true);
    nextName(false);
    nextName(false);
    nextName(false);
    nextName(false);
    nextName(false);
    
    assertEquals(1, 1);
  }
  
  private void nextName(boolean isFullBackup) throws InterruptedException {
//    nameProducer = new FileNameProducer("reposytory", "production", tempDir.getAbsolutePath(),  isFullBackup);
    Thread.sleep(100);
    nameProducer = new FileNameProducer(backupsetName, tempDir.getAbsolutePath(), Calendar.getInstance(), isFullBackup);
    System.out.println(nameProducer.getNextFile().getName());
  }
}
