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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.exoplatform.services.log.Log;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.impl.dataflow.serialization.ObjectWriterImpl;
import org.exoplatform.services.jcr.impl.dataflow.serialization.TransactionChangesLogWriter;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id$
 */

public class RecoveryWriter extends AbstractFSAccess {

  /**
   * The apache logger.
   */
  private static Log        log             = ExoLogger.getLogger("ext.RecoveryWriter");

  /**
   * Definition the timeout to FileRemover.
   */
  private static final long REMOVER_TIMEOUT = 5 * 60 * 1000;

  /**
   * The FileCleaner will delete the temporary files.
   */
  private final FileCleaner fileCleaner;

  /**
   * The FileNameFactory will be created file names.
   */
  private FileNameFactory   fileNameFactory;

  /**
   * Definition the folder to ChangesLog.
   */
  private File              recoveryDir;

  /**
   * Definition the folder to data.
   */
  private File              recoveryDirDate;

  /**
   * The FileRemover will deleted the already delivered ChangesLogs.
   */
  private FileRemover       fileRemover;

  /**
   * RecoveryWriter constructor.
   * 
   * @param recoveryDir
   *          the recovery directory
   * @param fileNameFactory
   *          the FileNameFactory
   * @param fileCleaner
   *          the File Cleaner
   * @param ownName
   *          the own name
   * @throws IOException
   *           will be generated the IOException
   */
  public RecoveryWriter(File recoveryDir,
                        FileNameFactory fileNameFactory,
                        FileCleaner fileCleaner,
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

  /**
   * save.
   * 
   * @param confirmationChengesLog
   *          the PendingConfirmationChengesLog
   * @return String return the name of file
   * @throws IOException
   *           will be generated the IOException
   */
  public String saveDataInfo(PendingConfirmationChengesLog confirmationChengesLog) throws IOException {

    if (confirmationChengesLog.getNotConfirmationList().size() > 0) {
      
      File f = new File(confirmationChengesLog.getDataFilePath());
      
      // save info
      if (log.isDebugEnabled())
        log.debug("Write info : " + f.getAbsolutePath());

      writeNotConfirmationInfo(f, confirmationChengesLog.getNotConfirmationList());

      return f.getName();
    }
    return null;
  }
  
  /**
   * save.
   *
   * @param timeStamp
   *          the time stamp to ChangesLog
   * @param identifier
   *          the identifier to ChangesLog
   * @param tcLog
   *          the ChangersLog
   * @return String
   *           the canonical path to serialized ChangrsLog
   * @throws IOException
   *           will be generated the IOException
   */
  public String saveDataFile(Calendar timeStamp, 
                     String identifier, 
                     TransactionChangesLog tcLog) throws IOException {
    String fileName = fileNameFactory.getTimeStampName(timeStamp) + "_" + identifier;
    
    // create dir
    File dir = new File(recoveryDirDate.getCanonicalPath() + File.separator
        + fileNameFactory.getRandomSubPath());
    dir.mkdirs();
    
    File f = new File(dir.getCanonicalPath() + File.separator + File.separator + fileName);
    
    // save data
    this.save(f, (TransactionChangesLog) tcLog);
    
    return f.getCanonicalPath();
  }

  /**
   * save.
   * 
   * @param f
   *          the file to binary data
   * @param changesLog
   *          the ChangesLog
   * @return String return the name of file
   * @throws IOException
   *           will be generated the IOException
   */
  public String save(File f, TransactionChangesLog changesLog) throws IOException {
    // save data
    ObjectWriterImpl out = new ObjectWriterImpl(new FileOutputStream(f));
    TransactionChangesLogWriter wr = new TransactionChangesLogWriter();
    wr.write(out, changesLog);

    out.flush();
    out.close();
    return f.getName();
  }

  /**
   * writeNotConfirmationInfo.
   * 
   * @param dataFile
   *          the file to binary ChangesLog
   * @param participantsClusterList
   *          the list of cluster participants who was not saved the ChangesLog
   * @throws IOException
   *           will be generated the IOException
   */
  private synchronized void writeNotConfirmationInfo(File dataFile,
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

  /**
   * removeChangesLog.
   * 
   * @param identifier
   *          the identification string to ChangesLog
   * @param ownerName
   *          the owner name
   * @throws IOException
   *           will be generated the IOException
   */
  public synchronized void removeChangesLog(String identifier, String ownerName) throws IOException {
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

        saveRemoveChangesLog((new File(fileName)).getCanonicalPath());
        break;
      }

    raf.close();
  }

  /**
   * removeChangesLog.
   * 
   * @param fileNameList
   *          the list of file name
   * @param ownerName
   *          the owner name
   * @return long return the how many files was deleted
   * @throws IOException
   *           will be generated the IOException
   */
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

  /**
   * saveRemoveChangesLog. Save the file name for deleting.
   * 
   * @param filePath
   *          the file name
   * @throws IOException
   *           will be generated the IOException
   */
  public void saveRemoveChangesLog(String filePath) throws IOException {
    if (log.isDebugEnabled())
      log.debug("Seve removable changeslogs form fs : " + filePath);

    File removeDataFile = new File(recoveryDir.getAbsolutePath() + File.separator + DATA_DIR_NAME
        + File.separator + IdGenerator.generate() + REMOVED_SUFFIX);

    removeDataFile.createNewFile();

    BufferedWriter bw = new BufferedWriter(new FileWriter(removeDataFile));

    if (log.isDebugEnabled())
      log.debug("remove  : " + filePath);

    bw.write(filePath + "\n");
    bw.flush();
    bw.close();
  }

  /**
   * saveRemoveChangesLog. Save the file name to deleting for a specific members.
   * 
   * @param fileNameList
   *          the lost of file names
   * @param ownerName
   *          the owner name
   * @throws IOException
   *           will be generated the IOException
   */
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

  /**
   * removeDataFile.
   *
   * @param f
   *          File, data file.
   */
  public void removeDataFile(File f) {
    if (!f.delete())
      fileCleaner.addFile(f);
  }
}

/**
 * FileRemover. The thread will be remove ChangesLog, saved as binary file.
 */
class FileRemover extends Thread {
  /**
   * The apache logger.
   */
  private static Log          log        = ExoLogger.getLogger("ext.FileRemover");

  /**
   * Definition the constants to one second.
   */
  private static final double ONE_SECOND = 1000.0;

  /**
   * Sleep period.
   */
  private long                period;

  /**
   * Definition the folder to ChangesLog.
   */
  private File                recoveryDir;

  /**
   * The FileCleaner will delete the temporary files.
   */
  private FileCleaner         fileCleaner;

  /**
   * The FilesFilter to removable files.
   * 
   */
  class RemoveFilesFilter implements FileFilter {
    /**
     * {@inheritDoc}
     */
    public boolean accept(File pathname) {
      return pathname.getName().endsWith(AbstractFSAccess.REMOVED_SUFFIX);
    }
  }

  /**
   * FileRemover constructor.
   * 
   * @param period
   *          the sleep period
   * @param recoveryDir
   *          the recovery directory
   * @param fileCleaner
   *          the FileCleaner
   * @param ownName
   *          the own name
   */
  public FileRemover(long period, File recoveryDir, FileCleaner fileCleaner, String ownName) {
    super("FileRemover@" + ownName);
    this.period = period;
    this.recoveryDir = recoveryDir;
    this.fileCleaner = fileCleaner;

    log.info(getName() + " has been inited");
  }

  /**
   * {@inheritDoc}
   */
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

          for (String fPath : needRemoveFilesName) {
            File f = new File(fPath);
            if (f.exists() && !map.containsKey(f.getName())) {
              fileCleaner.addFile(f);

              if (log.isDebugEnabled())
                log.debug("Remove file :" + f.getCanonicalPath());
            }
          }
          
          // remove *.remove files
          if (map.size() == 0)
            for (File ff : fArray) {
              fileCleaner.addFile(ff);

              if (log.isDebugEnabled())
                log.debug("Remove file :" + ff.getCanonicalPath());
            }
        }
      } catch (IOException e) {
        log.error("FileRemover error :", e);
      } catch (InterruptedException e) {
        log.error("FileRemover error :", e);
      }
    }
  }

  /**
   * getAllRemoveFileName.
   * 
   * @param array
   *          the array of file names to deleting
   * @return ArrayList the list of deleting files
   * @throws IOException
   *           will be generated the IOException
   */
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

  /**
   * getAllPendingBinaryFilePath.
   * 
   * @return HashMap return HashMap of file names per owner.
   * @throws IOException
   *           will be generated the IOException
   */
  private HashMap<String, String> getAllPendingBinaryFilePath() throws IOException {
    HashMap<String, String> map = new HashMap<String, String>();

    for (File f : recoveryDir.listFiles())
      if (f.isFile())
        for (String filePath : getFilePathList(f))
          map.put(new File(filePath).getName(), filePath);
    return map;
  }

  /**
   * getFilePathList.
   * 
   * @param f
   *          the File with removable files
   * @return List return the list of file path
   * @throws IOException
   *           will be generated the IOException
   */
  private List<String> getFilePathList(File f) throws IOException {
    List<String> list = new ArrayList<String>();

    BufferedReader reader = new BufferedReader(new FileReader(f));

    String str;

    while ((str = reader.readLine()) != null)
      if (str.startsWith(AbstractFSAccess.PREFIX_REMOVED_DATA) == false)
        list.add(str);

    return list;
  }

  /**
   * getAllSavedBinaryFile.
   * 
   * @param recoveryDataDir
   *          The recovery data directory
   * @return List return the list of File
   * @throws IOException
   *           will be generated the IOException
   */
  private List<File> getAllSavedBinaryFile(File recoveryDataDir) throws IOException {
    ArrayList<File> list = new ArrayList<File>();

    long startTime = System.currentTimeMillis();

    getFiles(recoveryDataDir, list);

    if (log.isDebugEnabled())
      log.debug("The total time of parced : " + (System.currentTimeMillis() - startTime)
          / ONE_SECOND);
    return list;
  }

  /**
   * getFiles. The recurcive parcing the directory. Will be added to list all files from tree.
   * 
   * @param f
   *          the directory
   * @param list
   *          the list of Files.
   */
  private void getFiles(File f, List<File> list) {
    if (f.isDirectory())
      for (File subFile : f.listFiles())
        getFiles(subFile, list);
    else if (f.isFile() && !(f.getName().endsWith(AbstractFSAccess.REMOVED_SUFFIX)))
      list.add(f);
  }
}
