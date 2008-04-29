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
package org.exoplatform.services.jcr.ext.replication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.dataflow.ChangesLogIterator;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.impl.WorkspaceContainer;
import org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspacePersistentDataManager;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;

/**
 * Created by The eXo Platform SAS Author : Alex Reshetnyak
 * alex.reshetnyak@exoplatform.com.ua 24.04.2008
 */
public class TestBinaryChangesLogRestore extends BaseStandaloneTest {
  public void testRestoreAndSave() throws Exception {
    session.getRootNode().addNode("exo:registry", "exo:registry");
    session.save();
    
    WorkspaceContainer wContainer = (WorkspaceContainer) (repository.getSystemSession()
        .getContainer());

    WorkspacePersistentDataManager workspacePersistentDataManager = (WorkspacePersistentDataManager) wContainer
        .getComponentInstanceOfType(WorkspacePersistentDataManager.class);

    ClassLoader cl = Thread.currentThread().getContextClassLoader();

    URL url1 = cl
        .getResource("replication/binary_changesLog/1/20080424_174857_355_811f877f7f00000100cc54366aca65b8.xls");

    URL url2 = cl
        .getResource("replication/binary_changesLog/2/20080424_174922_824_811feafd7f0000010014a97b3beb8f1c.xls");

    URL url3 = cl
        .getResource("replication/binary_changesLog/3/20080424_174946_968_8120494d7f00000100cc543626f5400a.xls");

    FileCleaner fileCleaner = new FileCleaner();

    TransactionChangesLog changesLog_1 = readExternal(new ObjectInputStream(url1.openStream()),
        fileCleaner);

    TransactionChangesLog changesLog_2 = readExternal(new ObjectInputStream(url2.openStream()),
        fileCleaner);

    TransactionChangesLog changesLog_3 = readExternal(new ObjectInputStream(url3.openStream()),
        fileCleaner);

    // save to jcr
    try {
      log.info("Save ChangesLog # 1 :");
      dump(changesLog_1);
      workspacePersistentDataManager.save(changesLog_1);
      log.info("After # 1 :");
      printRootNode();

      log.info("Save ChangesLog # 2 :");
      dump(changesLog_2);
      workspacePersistentDataManager.save(changesLog_2);
      log.info("After # 2 :");
      printRootNode();

      log.info("Save ChangesLog # 3 :");
      dump(changesLog_3);
      workspacePersistentDataManager.save(changesLog_3);
      log.info("After # 3 :");
      printRootNode();
    } catch (Exception e) {
      log.info("Exeption :");
      printRootNode();
      
      
      log.error("Fail : ", e);
      fail();
    } 
    
    
  }

  private TransactionChangesLog readExternal(ObjectInputStream in, FileCleaner fileCleaner)
      throws IOException, ClassNotFoundException {
    int changesLogType = in.readInt();

    TransactionChangesLog transactionChangesLog = null;

    if (changesLogType == PendingChangesLog.Type.ItemDataChangesLog_with_Streams) {

      // read ChangesLog
      transactionChangesLog = (TransactionChangesLog) in.readObject();

      // read FixupStream count
      int iFixupStream = in.readInt();

      ArrayList<FixupStream> listFixupStreams = new ArrayList<FixupStream>();

      for (int i = 0; i < iFixupStream; i++)
        listFixupStreams.add((FixupStream) in.readObject());

      // read stream data
      int iStreamCount = in.readInt();
      ArrayList<File> listFiles = new ArrayList<File>();

      for (int i = 0; i < iStreamCount; i++) {

        // read file size
        long fileSize = in.readLong();

        // read content file
        File contentFile = getAsFile(in, fileSize);
        listFiles.add(contentFile);
      }

      PendingChangesLog pendingChangesLog = new PendingChangesLog(transactionChangesLog,
          listFixupStreams, listFiles, fileCleaner);

      pendingChangesLog.restore();

      TransactionChangesLog log = pendingChangesLog.getItemDataChangesLog();

    } else if (changesLogType == PendingChangesLog.Type.ItemDataChangesLog_without_Streams) {
      transactionChangesLog = (TransactionChangesLog) in.readObject();
    }

    return transactionChangesLog;
  }

  protected File getAsFile(ObjectInputStream ois, long fileSize) throws IOException {
    int bufferSize = 1024 * 8;
    byte[] buf = new byte[bufferSize];

    File tempFile = File.createTempFile("" + System.currentTimeMillis(), "" + System.nanoTime());
    FileOutputStream fos = new FileOutputStream(tempFile);
    long readBytes = fileSize;

    while (readBytes > 0) {
      if (readBytes >= bufferSize) {
        ois.readFully(buf);
        fos.write(buf);
      } else if (readBytes < bufferSize) {
        ois.readFully(buf, 0, (int) readBytes);
        fos.write(buf, 0, (int) readBytes);
      }
      readBytes -= bufferSize;
    }

    fos.flush();
    fos.close();

    return tempFile;
  }

  private void dump(TransactionChangesLog changesLog) {
    ChangesLogIterator logIterator = changesLog.getLogIterator();

    while (logIterator.hasNextLog()) {
      PlainChangesLog pcl = logIterator.nextLog();
      log.info(pcl.dump());
    }
  }
  
  private void printRootNode() throws RepositoryException {
    NodeIterator ni = session.getRootNode().getNodes();
    while (ni.hasNext()) {
      Node cNode = ni.nextNode();
      log.info(cNode.getName() + ":" + cNode.getIndex());
    }
  }
}
