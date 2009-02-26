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
package org.exoplatform.services.jcr.ext.backup.server;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.apache.ws.commons.util.Base64;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.backup.BackupChain;
import org.exoplatform.services.jcr.ext.backup.BackupConfig;
import org.exoplatform.services.jcr.ext.backup.BackupJob;
import org.exoplatform.services.jcr.ext.backup.BackupManager;
import org.exoplatform.services.jcr.impl.core.SessionImpl;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 26.02.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: TestBackupServer.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class TestBackupServer extends BaseStandaloneTest {

  public void testRestore() throws Exception {

    ExoContainer container = ExoContainerContext.getCurrentContainer();

    BackupManager backManager = (BackupManager) container.getComponentInstanceOfType(BackupManager.class);

    BackupServer backupServer = new BackupServer(repositoryService, backManager);

    // create backup the workspace ws1
    File backDir = new File("target/backup/ws1");
    backDir.mkdirs();

    BackupConfig config = new BackupConfig();
    config.setRepository(repository.getName());
    config.setWorkspace("ws1");
    config.setBuckupType(BackupManager.FULL_BACKUP_ONLY);

    config.setBackupDir(backDir);

    backManager.startBackup(config);

    BackupChain bch = backManager.findBackup(repository.getName(), "ws1");

    // wait till full backup will be stopped
    while (bch.getFullBackupState() != BackupJob.FINISHED) {
      Thread.yield();
      Thread.sleep(50);
    }

    // stop fullBackup
    backManager.stopBackup(bch);

    // restore
    String path = bch.getLogFilePath();
    backupServer.restore("db1", "db77", "root", "exo", getPath(path), getWEntry(getStreamConfig()));
    
   
    // check workspace 'ws77'
    assertNotNull(repository.login(credentials, "ws77"));
  }

  private String getPath(String path) throws UnsupportedEncodingException {
    return Base64.encode(path.getBytes("UTF-8"), 0, (int) path.getBytes("UTF-8").length, 0, "");
  }

  private String getWEntry(InputStream config) throws IOException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();

    byte[] b = new byte[1024];
    int len = 0;
    while ((len = config.read(b)) != -1) {
      bout.write(b, 0, len);
    }
    config.close();
    byte[] cb = bout.toByteArray();
    bout.close();

    String conf = Base64.encode(cb, 0, cb.length, 0, "");

    conf = conf.replace("+", "char_pluse");

    return conf;
  }

  private InputStream getStreamConfig() throws FileNotFoundException {
    String containerConf = getClass().getResource("/conf/standalone/exo-jcr-config_for_TestBackupServer.xml").getPath();
    return new FileInputStream(containerConf);
  }
}
