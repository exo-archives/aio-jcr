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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.ext.replication.FixupStream;
import org.exoplatform.services.jcr.ext.replication.PendingChangesLog;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a> 
 * @version $Id: RecoveryWriter.java 16481 2008-06-26 13:23:34Z rainf0x $
 */
public class RecoveryWriter extends AbstractFSAccess {

  protected static Log      log = ExoLogger.getLogger("ext.RecoveryWriter");
  
  private final long        REMOVER_TIMEOUT = 2* 60 * 60 * 1000; // 2 hours

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

    fileRemover = new FileRemover(REMOVER_TIMEOUT, recoveryDir, fileCleaner, ownName);
    fileRemover.start();
  }

  public String save(PendingConfirmationChengesLog confirmationChengesLog)
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
      if (log.isDebugEnabled())
        log.debug("Write info : " + f.getAbsolutePath());

      writeNotConfirmationInfo(f, confirmationChengesLog.getNotConfirmationList());

      return f.getName();
    }
    return null;
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
    File metaDataFile = new File(recoveryDir.getAbsolutePath() + File.separator + ownerName);

    RandomAccessFile raf = new RandomAccessFile(metaDataFile, "rw");

    String fileName;

    while ((fileName = raf.readLine()) != null)
      if (fileName.indexOf(identifier) != -1) {
        raf.seek(raf.getFilePointer() - (fileName.length() + 1));

        String s = new String(fileName);
        s = s.replaceAll(".", PREFIX_CHAR);

        raf.writeBytes(s);

        if (log.isDebugEnabled()) {
          log.debug("remove metadata : " + fileName);
          log.debug("remove changes log form fs : " + identifier);
        }

        saveRemoveChangesLog((new File(fileName)).getName());
        break;
      }

    raf.close();
  }

  public long removeChangesLog(List<String> fileNameList, String ownerName) throws IOException {
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

          if (log.isDebugEnabled()) {
            log.debug("remove metadata : " + fName);
            log.debug("remove changeslogs form fs : " + fileNameList.size());
          }
        }
      }
    raf.close();

    saveRemoveChangesLog(fileNameList, ownerName);

    return removeCounter;
  }

  public void saveRemoveChangesLog(String fileName) throws IOException {
    if (log.isDebugEnabled())
      log.debug("Seve removable changeslogs form fs : " + fileName);

    File removeDataFile = new File(recoveryDir.getAbsolutePath() + File.separator + DATA_DIR_NAME
        + File.separator + IdGenerator.generate() + REMOVED_SUFFIX);

    removeDataFile.createNewFile();

    BufferedWriter bw = new BufferedWriter(new FileWriter(removeDataFile));

    if (log.isDebugEnabled())
      log.debug("remove  : " + fileName);

    bw.write(fileName + "\n");
    bw.flush();
    bw.close();
  }

  private void saveRemoveChangesLog(List<String> fileNameList, String ownerName) throws IOException {
    if (log.isDebugEnabled())
      log.debug("Seve removable changeslogs form fs : " + fileNameList.size());

    File removeDataFile = new File(recoveryDir.getAbsolutePath() + File.separator + DATA_DIR_NAME
        + File.separator + IdGenerator.generate() + REMOVED_SUFFIX);

    removeDataFile.createNewFile();

    BufferedWriter bw = new BufferedWriter(new FileWriter(removeDataFile));

    for (String fileName : fileNameList) {
      if (log.isDebugEnabled())
        log.debug("remove  : " + fileName);

      bw.write(fileName + "\n");
    }

    bw.flush();
    bw.close();
  }
}

// The thread will be remove ChangesLog, saved as binary file.
class FileRemover extends Thread {
  protected static Log log = ExoLogger.getLogger("ext.FileRemover");

  private long         period;

  private File         recoveryDir;

  private FileCleaner  fileCleaner;

  class RemoveFilesFilter implements FileFilter {
    public boolean accept(File pathname) {
      return pathname.getName().endsWith(AbstractFSAccess.REMOVED_SUFFIX);
    }
  }

  public FileRemover(long period, File recoveryDir, FileCleaner fileCleaner, String ownName) {
    super("FileRemover@" + ownName);
    this.period = period;
    this.recoveryDir = recoveryDir;
    this.fileCleaner = fileCleaner;

    log.info(getName() + " has been inited");
  }

  @Override
  public void run() {
    while (true) {
      try {
        Thread.yield();
        Thread.sleep(period);

        File recoveryDataDir = new File(recoveryDir.getCanonicalPath() + File.separator
            + RecoveryWriter.DATA_DIR_NAME);

        File[] fArray = recoveryDataDir.listFiles(new RemoveFilesFilter());

        if (fArray.length > 0) {
          ArrayList<String> needRemoveFilesName = getAllRemoveFileName(fArray);

          HashMap<String, String> map = getAllPendingBinaryFilePath();

          List<File> savedBinaryFileList = getAllSavedBinaryFile(recoveryDataDir);
          
          for (File f : savedBinaryFileList)
            if (needRemoveFilesName.contains(f.getName()) && !map.containsKey(f.getName())) {
              fileCleaner.addFile(f);
              
              if (log.isDebugEnabled())
                log.debug("Remove file :" + f.getCanonicalPath());
            }
          
          // remove *.remove files  
          if (savedBinaryFileList.size() == 0) 
            for (File f : fArray) {
              fileCleaner.addFile(f);
              
              if (log.isDebugEnabled())
                log.debug("Remove file :" + f.getCanonicalPath());
            }
          
        }
      } catch (IOException e) {
        log.error("FileRemover error :", e);
      } catch (InterruptedException e) {
        log.error("FileRemover error :", e);
      }
    }
  }

  private ArrayList<String> getAllRemoveFileName(File[] array) throws IOException {
    ArrayList<String> fileNameList = new ArrayList<String>();

    for (File file : array)
      if (file.isFile()) {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String sFileName;

        while ((sFileName = br.readLine()) != null) {
          fileNameList.add(sFileName);

          if (log.isDebugEnabled())
            log.debug("Need remove file : " + sFileName);
        }
      }

    return fileNameList;
  }

  private HashMap<String, String> getAllPendingBinaryFilePath() throws IOException {
    HashMap<String, String> map = new HashMap<String, String>();

    for (File f : recoveryDir.listFiles())
      if (f.isFile())
        for (String filePath : getFilePathList(f))
          map.put(new File(filePath).getName(), filePath);
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

    if (log.isDebugEnabled())
      log.debug("The total time of parced : " + (System.currentTimeMillis() - startTime) / 1000.0);
    return list;
  }

  private void getFiles(File f, List<File> list) {
    if (f.isDirectory())
      for (File subFile : f.listFiles())
        getFiles(subFile, list);
    else if (f.isFile() && !(f.getName().endsWith(AbstractFSAccess.REMOVED_SUFFIX)))
      list.add(f);
  }
}
