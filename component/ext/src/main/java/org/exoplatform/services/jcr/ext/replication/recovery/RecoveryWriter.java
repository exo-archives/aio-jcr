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
package org.exoplatform.services.jcr.ext.replication.recovery;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.ext.replication.FixupStream;
import org.exoplatform.services.jcr.ext.replication.PendingChangesLog;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS Author : Alex Reshetnyak
 * alex.reshetnyak@exoplatform.com.ua 26.03.2008
 */
public class RecoveryWriter extends AbstractFSAccess {

  protected static Log      log = ExoLogger.getLogger("ext.RecoveryWriter");

  private final FileCleaner fileCleaner;

  private FileNameFactory   fileNameFactory;

  private File              recoveryDir;

  private File              recoveryDirDate;

  private FileRemover       fileRemover;

  public RecoveryWriter(File recoveryDir, FileNameFactory fileNameFactory, FileCleaner fileCleaner,
      String ownName) throws IOException {
    this.fileCleaner = fileCleaner;
    this.recoveryDir = recoveryDir;
    this.fileNameFactory = fileNameFactory;

    recoveryDirDate = new File(this.recoveryDir.getCanonicalPath() + File.separator + DATA_DIR_NAME);
    if (!recoveryDirDate.exists())
      recoveryDirDate.mkdirs();

    fileRemover = new FileRemover(10 * 60 * 1000, recoveryDir, fileCleaner, ownName);
    fileRemover.start();
  }

  public void save(PendingConfirmationChengesLog confirmationChengesLog)
      throws FileNotFoundException, IOException {

    if (confirmationChengesLog.getNotConfirmationList().size() > 0) {
      String fileName = fileNameFactory.getTimeStampName(confirmationChengesLog.getTimeStamp())
          + "_" + confirmationChengesLog.getIdentifier();

      // create dir
      File dir = new File(recoveryDirDate.getCanonicalPath() + File.separator
          + fileNameFactory.getRandomSubPath());
      dir.mkdirs();

      File f = new File(dir.getCanonicalPath() + File.separator + File.separator + fileName);

      // save data
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(f));

      writeExternal(objectOutputStream, (TransactionChangesLog) confirmationChengesLog
          .getChangesLog());

      objectOutputStream.flush();
      objectOutputStream.close();

      // save info
      log.info("Write info : " + f.getAbsolutePath());
      writeNotConfirmationInfo(f, confirmationChengesLog.getNotConfirmationList());
    }
  }

  synchronized private void writeNotConfirmationInfo(File dataFile,
      List<String> participantsClusterList) throws IOException {
    for (String name : participantsClusterList) {
      File metaDataFile = new File(recoveryDir.getCanonicalPath() + File.separator + name);

      if (!metaDataFile.exists())
        metaDataFile.createNewFile();

      RandomAccessFile raf = new RandomAccessFile(metaDataFile, "rw");
      raf.seek(metaDataFile.length());
      raf.write((dataFile.getCanonicalPath() + "\n").getBytes());

      raf.close();
    }
  }

  private void writeExternal(ObjectOutputStream out, TransactionChangesLog changesLog)
      throws IOException {

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

  private void writeContent(InputStream is, ObjectOutputStream oos) throws IOException {
    byte[] buf = new byte[BUFFER_1KB * 8];
    int len;

    int size = 0;

    while ((len = is.read(buf)) > 0) {
      oos.write(buf, 0, len);
      size += len;
    }

    oos.flush();
  }

  synchronized public void removeChangesLog(String identifier, String ownerName) throws IOException {
    log.info("remove changes log form fs : " + identifier);
    File metaDataFile = new File(recoveryDir.getAbsolutePath() + File.separator + ownerName);

    RandomAccessFile raf = new RandomAccessFile(metaDataFile, "rw");

    String fileName;
    while ((fileName = raf.readLine()) != null)
      if (fileName.indexOf(identifier) != -1) {
        raf.seek(raf.getFilePointer() - (fileName.length() + 1));

        String s = new String(fileName);
        s = s.replaceAll(".", PREFIX_CHAR);

        raf.writeBytes(s);
        log.info("remove metadata : " + fileName);
        break;
      }

    raf.close();

    // TODO
    // if (fileName != null) {
    // File contentFiel = new File(fileName);
    //      
    // if (contentFiel.exists()) {
    // fileCleaner.addFile(contentFiel);
    // log.info("remove : " + fileName);
    // } else {
    // throw new FileNotFoundException("Can't find : " + fileName);
    // }
    // }
  }

  public long removeChangesLog(List<String> fileNameList, String ownerName) throws IOException {
    log.info("remove changeslogs form fs : " + fileNameList.size());

    long removeCounter = 0;

    File metaDataFile = new File(recoveryDir.getAbsolutePath() + File.separator + ownerName);

    RandomAccessFile raf = new RandomAccessFile(metaDataFile, "rw");

    HashMap<String, String> fileNameMap = new HashMap<String, String>();

    for (String fileName : fileNameList)
      fileNameMap.put(fileName, fileName);

    String fName;
    while ((fName = raf.readLine()) != null)
      if (fName.startsWith(PREFIX_REMOVED_DATA) == false) {
        File f = new File(fName);

        if (fileNameMap.containsKey(f.getName())) {
          raf.seek(raf.getFilePointer() - (fName.length() + 1));

          String s = new String(fName);
          s = s.replaceAll(".", PREFIX_CHAR);

          raf.writeBytes(s);

          removeCounter++;
          log.info("remove metadata : " + fName);
        }
      }
    raf.close();

    return removeCounter;
  }
}

// The thread will be remove ChangesLog, saved as binary file.
class FileRemover extends Thread {
  protected static Log log = ExoLogger.getLogger("ext.FileRemover");

  private long         period;

  private File         recoveryDir;

  private FileCleaner  filecCleaner;

  public FileRemover(long period, File recoveryDir, FileCleaner fileCleaner, String ownName) {
    super("FileRemover@" + ownName);
    this.period = period;
    this.recoveryDir = recoveryDir;
    this.filecCleaner = fileCleaner;

    log.info(getName() + " has been inited");
  }

  @Override
  public void run() {
    while (true) {
      try {
        Thread.yield();
        Thread.sleep(period);

        HashMap<String, String> map = getAllPendingBinaryFilePath();

        File recoveryDataDir = new File(recoveryDir.getCanonicalPath() + File.separator
            + RecoveryWriter.DATA_DIR_NAME);

        for (File f : getAllSavedBinaryFile(recoveryDataDir))
          if (!map.containsKey(f.getCanonicalPath())) {
            log.info("Remove file :" + f.getCanonicalPath());
            filecCleaner.addFile(f);
          }

      } catch (IOException e) {
        log.error("FileRemover error :", e);
      } catch (InterruptedException e) {
        log.error("FileRemover error :", e);
      }
    }
  }

  private HashMap<String, String> getAllPendingBinaryFilePath() throws IOException {
    HashMap<String, String> map = new HashMap<String, String>();

    for (File f : recoveryDir.listFiles())
      if (f.isFile())
        for (String filePath : getFilePathList(f))
          map.put(filePath, filePath);

    return map;
  }

  private List<String> getFilePathList(File f) throws IOException {
    List<String> list = new ArrayList<String>();

    BufferedReader reader = new BufferedReader(new FileReader(f));

    String str;

    while ((str = reader.readLine()) != null)
      if (str.startsWith(AbstractFSAccess.PREFIX_REMOVED_DATA) == false)
        list.add(str);

    return list;
  }

  private List<File> getAllSavedBinaryFile(File recoveryDataDir) throws IOException {
    ArrayList<File> list = new ArrayList<File>();

    long startTime = System.currentTimeMillis();
    
    getFiles(recoveryDataDir, list);
    
    System.out.println("The total time of parced : " + (System.currentTimeMillis() - startTime) / 1000.0);
    return list;
  }

  private void getFiles(File f, List<File> list) {
    if (f.isDirectory())
      for (File subFile : f.listFiles())
        getFiles(subFile, list);
    else if (f.isFile())
      list.add(f);
  }
}
