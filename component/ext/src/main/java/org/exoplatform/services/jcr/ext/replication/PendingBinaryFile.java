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
  private static Log                                       log = ExoLogger.getLogger("ext.PendingBinaryFile");

  private HashMap<String, HashMap<String, FileDescriptor>> mapFilePerOwner;

  private long                                             needTransferCounter;

  private long                                             successfulTransferCounter;

  private long                                             removedOldChangesLogCounter;

  private boolean                                          isSuccessfulTransfer;

  private boolean                                          isSuccessfulSave;

  public PendingBinaryFile() {
    mapFilePerOwner = new HashMap<String, HashMap<String, FileDescriptor>>();
    needTransferCounter = 0;
    removedOldChangesLogCounter = 0;
    successfulTransferCounter = 0;
    isSuccessfulTransfer = false;
    isSuccessfulSave = false;
  }

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

  public RandomAccessFile getRandomAccessFile(String ownName, String fileName) throws Exception {

    HashMap<String, FileDescriptor> fileMap = mapFilePerOwner.get(ownName);

    // try {
    return fileMap.get(fileName).getRandomAccessFile();
    /*} catch (NullPointerException e) {
      try {
      
      return fileMap.get(fileName).getRandomAccessFile();
      } catch (NullPointerException ex) {
        throw new Exception("Can't finded the RandomAccessFile to '" + fileName + "'", ex);
      }
    }*/
  }

  public FileDescriptor getFileDescriptor(String ownName, String fileName) throws IOException {
    if (mapFilePerOwner.containsKey(ownName)) {
      HashMap<String, FileDescriptor> fileMap = mapFilePerOwner.get(ownName);
      return fileMap.get(fileName);
    } else {
      this.addBinaryFile(ownName, fileName, "");
      return getFileDescriptor(ownName, fileName);
    }
  }

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

  public List<String> getFileNameList() {
    ArrayList<String> list = new ArrayList<String>();

    for (String ownerName : mapFilePerOwner.keySet()) {
      HashMap<String, FileDescriptor> fileMap = mapFilePerOwner.get(ownerName);

      for (String fileName : fileMap.keySet())
        list.add(fileName);
    }

    return list;
  }

  /*
   * class FileDescriptor implements Comparable<FileDescriptor> { private File
   * file; private RandomAccessFile randomAccessFile; private final String
   * systemId; public FileDescriptor(File f, RandomAccessFile raf, String
   * systemId) { this.file = f; this.randomAccessFile = raf; this.systemId =
   * systemId; } public File getFile() { return file; } public RandomAccessFile
   * getRandomAccessFile() { return randomAccessFile; } public String
   * getSystemId() { return systemId; } public int compareTo(FileDescriptor o) {
   * return file.getName().compareTo(o.getFile().getName()); } }
   */

  public long getNeedTransferCounter() {
    return needTransferCounter;
  }

  public void setNeedTransferCounter(long needTransferCounter) {
    this.needTransferCounter = needTransferCounter;
  }

  public long getRemovedOldChangesLogCounter() {
    return removedOldChangesLogCounter;
  }

  public void setRemovedOldChangesLogCounter(long needRemoveOldChangesLogCounter) {
    this.removedOldChangesLogCounter = needRemoveOldChangesLogCounter;
  }

  public boolean isAllOldChangesLogsRemoved() {
    return (needTransferCounter == removedOldChangesLogCounter ? true : false);
  }

  public long getSuccessfulTransferCounter() {
    return successfulTransferCounter;
  }

  public void setSuccessfulTransferCounter(long successfulTransferCounter) {
    this.successfulTransferCounter = successfulTransferCounter;
  }

  public boolean isSuccessfulTransfer() {
    return isSuccessfulTransfer;
  }

  public void addToSuccessfulTransferCounter(long c) {
    successfulTransferCounter += c;

    isSuccessfulTransfer = (needTransferCounter == successfulTransferCounter ? true : false);
  }

  public boolean isSuccessfulSave() {
    return isSuccessfulSave;
  }

  public void setSuccessfulSave(boolean isSuccessfulSave) {
    this.isSuccessfulSave = isSuccessfulSave;
  }
}
