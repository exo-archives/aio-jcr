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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: PendingBinaryFile.java 111 2008-11-11 11:11:11Z rainf0x $
 */

public class PendingBinaryFile {
  /**
   * The apache logger.
   */
  private static Log                                       log = ExoLogger.getLogger("ext.PendingBinaryFile");

  /**
   * The map for FileDesctiptor per owner. 
   */
  private HashMap<String, HashMap<String, FileDescriptor>> mapFilePerOwner;

  /**
   * Need transfer counter.
   */
  private long                                             needTransferCounter;

  /**
   * Successful transfer counter.
   */
  private long                                             successfulTransferCounter;

  /**
   * Removed old ChangesLog counter.
   */
  private long                                             removedOldChangesLogCounter;

  /**
   * Successful transfer 'flag'.
   */
  private boolean                                          isSuccessfulTransfer;

  /**
   * Successful save 'flag'.
   */
  private boolean                                          isSuccessfulSave;

  /**
   * PendingBinaryFile  constructor.
   */
  public PendingBinaryFile() {
    mapFilePerOwner = new HashMap<String, HashMap<String, FileDescriptor>>();
    needTransferCounter = 0;
    removedOldChangesLogCounter = 0;
    successfulTransferCounter = 0;
    isSuccessfulTransfer = false;
    isSuccessfulSave = false;
  }

  /**
   * addBinaryFile.
   *
   * @param ownerName
   *          owner name
   * @param fileName
   *          name of file
   * @param systemId
   *          String of system identification 
   * @throws IOException
   *           will be generated IOException 
   */
  public void addBinaryFile(String ownerName, String fileName, String systemId) throws IOException {
    File f = File.createTempFile(fileName, "");
    RandomAccessFile binaryFile = new RandomAccessFile(f, "rw");

    FileDescriptor fileDescriptor = new FileDescriptor(f, binaryFile, systemId);

    HashMap<String, FileDescriptor> fileMap;

    if (mapFilePerOwner.containsKey(ownerName) == false) {
      fileMap = new HashMap<String, FileDescriptor>();
      mapFilePerOwner.put(ownerName, fileMap);
    } else {
      fileMap = mapFilePerOwner.get(ownerName);
    }

    fileMap.put(fileName, fileDescriptor);
  }

  /**
   * getRandomAccessFile.
   *
   * @param ownName
   *          owner name
   * @param fileName
   *          name of file
   * @return RandomAccessFile
   *           the RandomAccessFile
   * @throws Exception
   *           will be generated Exception
   */
  public RandomAccessFile getRandomAccessFile(String ownName, String fileName) throws Exception {
    HashMap<String, FileDescriptor> fileMap = mapFilePerOwner.get(ownName);

    return fileMap.get(fileName).getRandomAccessFile();
  }

  /**
   * getFileDescriptor.
   *
   * @param ownName
   *          owner name
   * @param fileName
   *          name of file
   * @return FileDescriptor
   *           return the FileDescriptor 
   * @throws IOException
   *           will be generated IOException
   */
  public FileDescriptor getFileDescriptor(String ownName, String fileName) throws IOException {
    if (mapFilePerOwner.containsKey(ownName)) {
      HashMap<String, FileDescriptor> fileMap = mapFilePerOwner.get(ownName);
      return fileMap.get(fileName);
    } else {
      this.addBinaryFile(ownName, fileName, "");
      return getFileDescriptor(ownName, fileName);
    }
  }

  /**
   * getSortedFilesDescriptorList.
   *
   * @return List
   *           return the list of FileDescriptors
   */
  public List<FileDescriptor> getSortedFilesDescriptorList() {

    ArrayList<FileDescriptor> fileDescriptorhList = new ArrayList<FileDescriptor>();

    for (String ownerName : mapFilePerOwner.keySet()) {
      HashMap<String, FileDescriptor> fileMap = mapFilePerOwner.get(ownerName);

      fileDescriptorhList.addAll(fileMap.values());
    }

    if (log.isDebugEnabled())
      log.debug("getSortedFilePath() : " + fileDescriptorhList.size());

    Collections.sort(fileDescriptorhList);

    if (log.isDebugEnabled()) {
      log.debug("\n\nList has been sorted :\n");
      for (FileDescriptor fd : fileDescriptorhList)
        log.debug(fd.getFile().getAbsolutePath());
    }

    return fileDescriptorhList;
  }

  /**
   * getFileNameList.
   *
   * @return List
   *           return the list of names of files
   */
  public List<String> getFileNameList() {
    ArrayList<String> list = new ArrayList<String>();

    for (String ownerName : mapFilePerOwner.keySet()) {
      HashMap<String, FileDescriptor> fileMap = mapFilePerOwner.get(ownerName);

      for (String fileName : fileMap.keySet())
        list.add(fileName);
    }

    return list;
  }

  /**
   * getNeedTransferCounter.
   *
   * @return long 
   *           return the needTransferCounter
   */
  public long getNeedTransferCounter() {
    return needTransferCounter;
  }

  /**
   * setNeedTransferCounter.
   *
   * @param needTransferCounter
   *          set the needTransferCounter 
   */
  public void setNeedTransferCounter(long needTransferCounter) {
    this.needTransferCounter = needTransferCounter;
  }

  /**
   * getRemovedOldChangesLogCounter.
   *
   * @return long
   *           return the removedOldChangesLogCounter
   */
  public long getRemovedOldChangesLogCounter() {
    return removedOldChangesLogCounter;
  }

  /**
   * setRemovedOldChangesLogCounter.
   *
   * @param needRemoveOldChangesLogCounter
   *          set the removedOldChangesLogCounter
   */
  public void setRemovedOldChangesLogCounter(long needRemoveOldChangesLogCounter) {
    this.removedOldChangesLogCounter = needRemoveOldChangesLogCounter;
  }

  /**
   * isAllOldChangesLogsRemoved.
   *
   * @return boolean
   *           return 'true' if  all old ChangesLogs was removed 
   */
  public boolean isAllOldChangesLogsRemoved() {
    return (needTransferCounter == removedOldChangesLogCounter ? true : false);
  }

  /**
   * getSuccessfulTransferCounter.
   *
   * @return long
   *          return the successfulTransferCounter
   */ 
  public long getSuccessfulTransferCounter() {
    return successfulTransferCounter;
  }

  /**
   * setSuccessfulTransferCounter.
   *
   * @param successfulTransferCounter
   *          set the successfulTransferCounter
   */
  public void setSuccessfulTransferCounter(long successfulTransferCounter) {
    this.successfulTransferCounter = successfulTransferCounter;
  }

  /**
   * isSuccessfulTransfer.
   *
   * @return boolean
   *           return 'true' if is successful transfer
   */
  public boolean isSuccessfulTransfer() {
    return isSuccessfulTransfer;
  }

  /**
   * addToSuccessfulTransferCounter.
   *
   * @param count
   *          add the 'count' to successfulTransferCounter
   *         
   */
  public void addToSuccessfulTransferCounter(long count) {
    successfulTransferCounter += count;

    isSuccessfulTransfer = (needTransferCounter == successfulTransferCounter ? true : false);
  }

  /**
   * isSuccessfulSave.
   *
   * @return boolean
   *           return the 'true' if successful save
   */
  public boolean isSuccessfulSave() {
    return isSuccessfulSave;
  }

  /**
   * setSuccessfulSave.
   *
   * @param successfulSave
   *           set the isSuccessfulSave
   */
  public void setSuccessfulSave(boolean successfulSave) {
    this.isSuccessfulSave = successfulSave;
  }
}
