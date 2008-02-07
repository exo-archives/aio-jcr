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
package org.exoplatform.services.jcr.ext.backup.impl.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.dataflow.ChangesLogIterator;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.ext.backup.BackupConfig;
import org.exoplatform.services.jcr.ext.backup.impl.AbstractIncrementalBackupJob;
import org.exoplatform.services.jcr.ext.replication.FixupStream;
import org.exoplatform.services.jcr.ext.replication.PendingChangesLog;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL Author : Alex Reshetnyak
 * alex.reshetnyak@exoplatform.com.ua Nov 20, 2007
 */
public class IncrementalBackupJob extends AbstractIncrementalBackupJob {

  protected static Log       log = ExoLogger.getLogger("ext.IncrementalBackupJob");

  private ObjectOutputStream oosFileData;

  private FileCleaner        fileCleaner;
  
  public IncrementalBackupJob() {
    fileCleaner = new FileCleaner(10000);
  }
  
  public void init(ManageableRepository repository, String workspaceName, BackupConfig config, Calendar timeStamp) {
    this.repository = repository;
    this.workspaceName = workspaceName;
    this.config = config;
    this.timeStamp = timeStamp;
    
    try {
      url = createStorage();
    } catch (FileNotFoundException e) {
      log.error("Incremental backup initialization failed ", e);
      notifyError("Incremental backup initialization failed ", e);
    } catch (IOException e) {
      log.error("Incremental backup initialization failed ", e);
      notifyError("Incremental backup initialization failed ", e);
    }
  }  
  
  public void stop() {
    state = FINISHED;
    log.info("Stop requested " + getStorageURL().getPath());
    
    notifyListeners();
  }

  @Override
  protected URL createStorage() throws FileNotFoundException, IOException {
    FileNameProducer fnp = new FileNameProducer(config.getRepository(), config.getWorkspace(),
        config.getBackupDir().getAbsolutePath(), super.timeStamp, false);
    
    File backupFileData = fnp.getNextFile();

    oosFileData = new ObjectOutputStream(new FileOutputStream(backupFileData));
    return new URL("file:" + backupFileData.getAbsoluteFile());
  }

  @Override
  protected void save(ItemStateChangesLog persistentLog) throws IOException {

    TransactionChangesLog changesLog = (TransactionChangesLog) persistentLog;

    if (changesLog != null && changesLog.getSystemId() == null && !isSessionNull(changesLog)) {
      changesLog.setSystemId(IdGenerator.generate());
      long start = System.currentTimeMillis();

      writeExternal(oosFileData, changesLog, fileCleaner);
      
      long total = System.currentTimeMillis() - start;

      if(log.isDebugEnabled())
        log.debug("Time : " + total + " ms" + "    Itemstates count : "
          + changesLog.getAllStates().size());
    }
  }

  public void writeExternal(ObjectOutputStream out, TransactionChangesLog changesLog,
      FileCleaner fileCleaner) throws IOException {

    PendingChangesLog pendingChangesLog = new PendingChangesLog(changesLog, fileCleaner);

    if (pendingChangesLog.getConteinerType() == PendingChangesLog.Type.ItemDataChangesLog_with_Streams) {

      out.writeInt(PendingChangesLog.Type.ItemDataChangesLog_with_Streams);
      out.writeObject(changesLog);

      // Write FixupStream
      List<FixupStream> listfs = pendingChangesLog.getFixupStreams();
      out.writeInt(listfs.size());

      for (int i = 0; i < listfs.size(); i++)
        out.writeObject(listfs.get(i));

      // write stream data
      List<InputStream> listInputList = pendingChangesLog.getInputStreams();

      // write file count
      out.writeInt(listInputList.size());

      for (int i = 0; i < listInputList.size(); i++) {
        File tempFile = getAsFile(listInputList.get(i));
        FileInputStream fis = new FileInputStream(tempFile);

        // write file size
        out.writeLong(tempFile.length());

        // write file content
        writeContent(fis, out);

        fis.close();
        fileCleaner.addFile(tempFile);
      }

      // restore changes log worlds

    } else {
      out.writeInt(PendingChangesLog.Type.ItemDataChangesLog_without_Streams);
      out.writeObject(changesLog);
    }

    out.flush();
  }

  private File getAsFile(InputStream is) throws IOException {
    byte[] buf = new byte[1024 * 20];

    File tempFile = File.createTempFile("" + System.currentTimeMillis(), "" + System.nanoTime());
    FileOutputStream fos = new FileOutputStream(tempFile);
    int len;

    while ((len = is.read(buf)) > 0)
      fos.write(buf, 0, len);

    fos.flush();
    fos.close();

    return tempFile;
  }

  private void writeContent(InputStream is, ObjectOutputStream oos) throws IOException {
    byte[] buf = new byte[1024 * 8];
    int len;
    
    int size = 0;

    while ((len = is.read(buf)) > 0) {
      oos.write(buf, 0, len);
      size+=len;
    }
    
    oos.flush();
  }

  private boolean isSessionNull(TransactionChangesLog changesLog) {
    boolean isSessionNull = false;

    ChangesLogIterator logIterator = changesLog.getLogIterator();
    while (logIterator.hasNextLog())
      if (logIterator.nextLog().getSessionId() == null) {
        isSessionNull = true;
        break;
      }

    return isSessionNull;
  }
  
}
