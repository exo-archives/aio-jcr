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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.lock.Lock;

import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.impl.core.SessionImpl;

/**
 * Created by The eXo Platform SAS Author : Peter Nedonosko peter.nedonosko@exoplatform.com.ua
 * 05.12.2007
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: TestBackupManager.java 760 2008-02-07 15:08:07Z pnedonosko $
 */
public class TestBackupManager extends AbstractBackupTestCase {

  public void testFullBackupRestore() throws Exception {
    // backup
    File backDir = new File("target/backup/ws1");
    backDir.mkdirs();

    BackupConfig config = new BackupConfig();
    config.setRepository(repository.getName());
    config.setWorkspace("ws1");
    config.setBackupType(BackupManager.FULL_BACKUP_ONLY);

    config.setBackupDir(backDir);

    backup.startBackup(config);

    BackupChain bch = backup.findBackup(repository.getName(), "ws1");

    // wait till full backup will be stopped
    while (bch.getFullBackupState() != BackupJob.FINISHED) {
      Thread.yield();
      Thread.sleep(50);
    }

    // stop fullBackup

    if (bch != null)
      backup.stopBackup(bch);
    else
      fail("Can't get fullBackup chain");

    // restore
    RepositoryEntry re = (RepositoryEntry) ws1Session.getContainer()
                                                     .getComponentInstanceOfType(RepositoryEntry.class);
    WorkspaceEntry ws1back = makeWorkspaceEntry("ws1back", "jdbcjcr_backup_only_use_1");

    repository.configWorkspace(ws1back);

    // BackupChainLog bchLog = new BackupChainLog(backDir, rconfig);
    File backLog = new File(bch.getLogFilePath());
    if (backLog.exists()) {
      BackupChainLog bchLog = new BackupChainLog(backLog);

      backup.restore(bchLog, re.getName(), ws1back);

      // check
      SessionImpl back1 = null;
      try {
        back1 = (SessionImpl) repository.login(credentials, "ws1back");
        Node ws1backTestRoot = back1.getRootNode().getNode("backupTest");
        assertEquals("Restored content should be same",
                     "property-5",
                     ws1backTestRoot.getNode("node_5").getProperty("exo:data").getString());
      } catch (Exception e) {
        e.printStackTrace();
        fail(e.getMessage());
      } finally {
        if (back1 != null)
          back1.logout();
      }
    } else
      fail("There are no backup files in " + backDir.getAbsolutePath());
  }

  public void testIncrementalBackupRestore() throws Exception {
    // full backup & incremental
    File backDir = new File("target/backup/ws1.incr");
    backDir.mkdirs();

    BackupConfig config = new BackupConfig();
    config.setRepository(repository.getName());
    config.setWorkspace("ws1");
    config.setBackupType(BackupManager.FULL_AND_INCREMENTAL);

    config.setBackupDir(backDir);

    backup.startBackup(config);

    BackupChain bch = backup.findBackup(repository.getName(), "ws1");

    // wait till full backup will be stopped
    while (bch.getFullBackupState() != BackupJob.FINISHED) {
      Thread.yield();
      Thread.sleep(50);
    }

    // add some changes which will be logged in incremental log
    ws1TestRoot.getNode("node_3").remove();
    ws1TestRoot.getNode("node_4").remove();
    ws1TestRoot.getNode("node_5").remove();
    ws1TestRoot.addNode("node #3").setProperty("exo:data", "property #3");
    ws1TestRoot.addNode("node #5").setProperty("exo:extraData", "property #5");

    ws1TestRoot.save(); // log here via listener

    // stop all
    if (bch != null)
      backup.stopBackup(bch);
    else
      fail("Can't get fullBackup chain");

    // restore
    RepositoryEntry re = (RepositoryEntry) ws1Session.getContainer()
                                                     .getComponentInstanceOfType(RepositoryEntry.class);
    WorkspaceEntry ws1back = makeWorkspaceEntry("ws1back.incr", "jdbcjcr_backup_only_use_2");

    repository.configWorkspace(ws1back);

    File backLog = new File(bch.getLogFilePath());
    if (backLog.exists()) {
      BackupChainLog bchLog = new BackupChainLog(backLog);
      backup.restore(bchLog, re.getName(), ws1back);

      // check
      SessionImpl back1 = null;
      try {
        back1 = (SessionImpl) repository.login(credentials, ws1back.getName());
        Node ws1backTestRoot = back1.getRootNode().getNode("backupTest");
        assertFalse("Node should be removed", ws1backTestRoot.hasNode("node_3"));
        assertFalse("Node should be removed", ws1backTestRoot.hasNode("node_4"));
        assertFalse("Node should be removed", ws1backTestRoot.hasNode("node_5"));

        assertEquals("Restored content should be same",
                     "property #3",
                     ws1backTestRoot.getNode("node #3").getProperty("exo:data").getString());
        assertEquals("Restored content should be same",
                     "property #5",
                     ws1backTestRoot.getNode("node #5").getProperty("exo:extraData").getString());

        assertFalse("Proeprty should be removed", ws1backTestRoot.getNode("node #5")
                                                                 .hasProperty("exo:data"));
      } catch (Exception e) {
        e.printStackTrace();
        fail(e.getMessage());
      } finally {
        if (back1 != null)
          back1.logout();
      }
    } else
      fail("There are no backup files in " + backDir.getAbsolutePath());
  }

  /**
   * With BLOBs, locks, copy and move
   * 
   * @throws Exception
   */
  public void testIncrementalBackupRestore2() throws Exception {
    // full backup with BLOBs & incremental with BLOBs

    // BLOBs for full
    File tempf = createBLOBTempFile("testIncrementalBackupRestore2-", 5 * 1024); // 5M
    tempf.deleteOnExit();
    ws1TestRoot.addNode("node_101").setProperty("exo:data", new FileInputStream(tempf));
    ws1TestRoot.addNode("node_102").setProperty("exo:extraData", new FileInputStream(tempf));

    File backDir = new File("target/backup/ws1.incr2");
    backDir.mkdirs();

    BackupConfig config = new BackupConfig();
    config.setRepository(repository.getName());
    config.setWorkspace("ws1");
    config.setBackupType(BackupManager.FULL_AND_INCREMENTAL);

    config.setBackupDir(backDir);

    backup.startBackup(config);

    BackupChain bch = backup.findBackup(repository.getName(), "ws1");

    // wait till full backup will be stopped
    while (bch.getFullBackupState() != BackupJob.FINISHED) {
      Thread.yield();
      Thread.sleep(50);
    }

    // add some changes which will be logged in incremental log
    ws1TestRoot.addNode("node #53").setProperty("exo:extraData", "property #53");
    ws1TestRoot.save(); // log here via listener

    // BLOBs for incr
    ws1TestRoot.getNode("node_1").setProperty("exo:extraData", new FileInputStream(tempf));
    ws1TestRoot.getNode("node_5").setProperty("exo:data", new FileInputStream(tempf));

    ws1TestRoot.addNode("node_101").setProperty("exo:data", new FileInputStream(tempf));
    ws1TestRoot.addNode("node_102").setProperty("exo:data", new FileInputStream(tempf));
    ws1TestRoot.save(); // log here via listener

    ws1TestRoot.getNode("node_2").setProperty("exo:data", (InputStream) null); // remove property
    ws1TestRoot.getNode("node_3").setProperty("exo:data",
                                              new ByteArrayInputStream("aaa".getBytes())); // set
    // aaa
    // bytes
    ws1TestRoot.getNode("node_4").remove(); // (*)
    ws1TestRoot.save(); // log here via listener

    ws1TestRoot.getNode("node_5").addMixin("mix:lockable");
    ws1TestRoot.save(); // log here via listener
    Lock n107lock = ws1TestRoot.getNode("node_5").lock(true, false);
    ws1TestRoot.getSession().move(ws1TestRoot.getNode("node #53").getPath(),
                                  ws1TestRoot.getNode("node_5").getPath() + "/node #53");
    ws1TestRoot.save(); // log here via listener

    ws1TestRoot.getNode("node_6").addMixin("mix:referenceable");
    String id6 = ws1TestRoot.getNode("node_6").getUUID();
    ws1TestRoot.save(); // log here via listener

    // before(*), log here via listener
    ws1TestRoot.getSession().getWorkspace().move(ws1TestRoot.getNode("node_6").getPath(),
                                                 ws1TestRoot.getPath() + "/node_4"); // in place of
    // 4 removed

    // stop all
    if (bch != null)
      backup.stopBackup(bch);
    else
      fail("Can't get fullBackup chain");

    // restore
    RepositoryEntry re = (RepositoryEntry) ws1Session.getContainer()
                                                     .getComponentInstanceOfType(RepositoryEntry.class);
    WorkspaceEntry ws1back = makeWorkspaceEntry("ws1back.incr2", "jdbcjcr_backup_only_use_3");

    repository.configWorkspace(ws1back);

    File backLog = new File(bch.getLogFilePath());
    if (backLog.exists()) {
      BackupChainLog bchLog = new BackupChainLog(backLog);
      backup.restore(bchLog, re.getName(), ws1back);

      // check
      SessionImpl back1 = null;
      try {
        back1 = (SessionImpl) repository.login(credentials, ws1back.getName());
        Node ws1backTestRoot = back1.getRootNode().getNode("backupTest");

        assertTrue("Node should exists", ws1backTestRoot.getNode("node_5").hasNode("node #53"));
        assertTrue("Property should exists", ws1backTestRoot.getNode("node_5")
                                                            .hasProperty("node #53/exo:extraData"));

        assertTrue("Node should exists", ws1backTestRoot.hasNode("node_7"));
        assertTrue("Property should exists", ws1backTestRoot.hasProperty("node_5/exo:data"));
        assertTrue("Property should exists", ws1backTestRoot.hasProperty("node_1/exo:extraData"));
        assertTrue("Node should exists", ws1backTestRoot.hasNode("node_102"));

        compareStream(new FileInputStream(tempf), ws1backTestRoot.getNode("node_5")
                                                                 .getProperty("exo:data")
                                                                 .getStream());
        compareStream(new FileInputStream(tempf), ws1backTestRoot.getNode("node_1")
                                                                 .getProperty("exo:extraData")
                                                                 .getStream());

        assertFalse("Property should be removed", ws1backTestRoot.getNode("node_2")
                                                                 .hasProperty("exo:data"));

        compareStream(new ByteArrayInputStream("aaa".getBytes()),
                      ws1backTestRoot.getNode("node_3").getProperty("exo:data").getStream());

        assertTrue("Node should be mix:lockable ", ws1backTestRoot.getNode("node_5")
                                                                  .isNodeType("mix:lockable"));
        assertFalse("Node should be not locked ", ws1backTestRoot.getNode("node_5").isLocked());

        assertEquals("Node should be mix:referenceable and UUID should be " + id6,
                     id6,
                     ws1backTestRoot.getNode("node_4").getUUID());
      } catch (Exception e) {
        e.printStackTrace();
        fail(e.getMessage());
      } finally {
        if (back1 != null)
          back1.logout();
      }
    } else
      fail("There are no backup files in " + backDir.getAbsolutePath());
  }
}
