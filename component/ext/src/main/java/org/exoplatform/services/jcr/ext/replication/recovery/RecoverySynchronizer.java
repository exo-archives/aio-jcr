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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ChangesLogIterator;
import org.exoplatform.services.jcr.dataflow.ItemDataKeeper;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.ext.replication.AbstractWorkspaceDataReceiver;
import org.exoplatform.services.jcr.ext.replication.ChannelManager;
import org.exoplatform.services.jcr.ext.replication.FixupStream;
import org.exoplatform.services.jcr.ext.replication.Packet;
import org.exoplatform.services.jcr.ext.replication.ReplicationException;
import org.exoplatform.services.jcr.ext.replication.recovery.PendingBinaryFile.FileDescriptor;
import org.exoplatform.services.jcr.impl.storage.JCRInvalidItemStateException;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.jgroups.Message;
import org.jgroups.blocks.GroupRequest;
import org.jgroups.blocks.MessageDispatcher;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id$
 */
public class RecoverySynchronizer {
  protected static Log                       log = ExoLogger.getLogger("ext.RecoverySynchronizer");

  private File                               recoveryDir;

  private FileNameFactory                    fileNameFactory;

  private FileCleaner                        fileCleaner;

  private ChannelManager                     channelManager;

  private String                             ownName;

  private String                             systemId;

  private RecoveryReader                     recoveryReader;

  private RecoveryWriter                     recoveryWriter;

  private HashMap<String, PendingBinaryFile> mapPendingBinaryFile;

  private ItemDataKeeper                     dataKeeper;

  private List<String>                       initedParticipantsClusterList;

  private List<String>                       successfulSynchronizedList;

  public RecoverySynchronizer(File recoveryDir, FileNameFactory fileNameFactory,
      FileCleaner fileCleaner, ChannelManager channelManager, String ownName,
      RecoveryWriter recoveryWriter, String systemId) {
    this.recoveryDir = recoveryDir;
    this.fileNameFactory = fileNameFactory;
    this.fileCleaner = fileCleaner;
    this.channelManager = channelManager;
    this.ownName = ownName;
    this.systemId = systemId;

    recoveryReader = new RecoveryReader(fileCleaner, recoveryDir);
    this.recoveryWriter = recoveryWriter;
    mapPendingBinaryFile = new HashMap<String, PendingBinaryFile>();

    successfulSynchronizedList = new ArrayList<String>();
    initedParticipantsClusterList = new ArrayList<String>();
  }

  public void synchronizRepository() {
    try {
      Packet packet = new Packet(Packet.PacketType.GET_ChangesLog_up_to_DATE, IdGenerator
          .generate(), ownName, Calendar.getInstance());
      channelManager.sendPacket(packet);
    } catch (Exception e) {
      log.error("Synchronization error", e);
    }
  }

  private void send(Packet packet) throws Exception {
    byte[] buffer = Packet.getAsByteArray(packet);

    if (buffer.length <= Packet.MAX_PACKET_SIZE) {
      channelManager.send(buffer);
    } else
      channelManager.sendBigPacket(buffer, packet);
  }

  public int processingPacket(Packet packet, int status) throws Exception {
    PendingBinaryFile container;
    int stat = status;

    switch (packet.getPacketType()) {

    case Packet.PacketType.GET_ChangesLog_up_to_DATE:
      sendChangesLogUpDate(packet.getTimeStamp(), packet.getOwnerName(), packet.getIdentifier());
      break;

    case Packet.PacketType.BinaryFile_First_Packet:
      if (mapPendingBinaryFile.containsKey(packet.getIdentifier()) == false)
        mapPendingBinaryFile.put(packet.getIdentifier(), new PendingBinaryFile());

      container = mapPendingBinaryFile.get(packet.getIdentifier());
      container.addBinaryFile(packet.getOwnerName(), packet.getFileName(), packet.getSystemId());
      break;

    case Packet.PacketType.BinaryFile_Middle_Packet:
      if (mapPendingBinaryFile.containsKey(packet.getIdentifier())) {
        container = mapPendingBinaryFile.get(packet.getIdentifier());

        RandomAccessFile randomAccessFile = container.getRandomAccessFile(packet.getOwnerName(),
            packet.getFileName());

        if (randomAccessFile != null) {
          if (log.isDebugEnabled())
            log.info("Offset : BinaryFile_Middle_Packet :" + packet.getOffset());

          randomAccessFile.seek(packet.getOffset());
          randomAccessFile.write(packet.getByteArray());
        } else
          log.warn("Can't find the RandomAccessFile : \n" + "owner - \t" + packet.getOwnerName()
              + "\nfile name - \t" + packet.getFileName());
      }
      break;

    case Packet.PacketType.BinaryFile_Last_Packet:
      if (mapPendingBinaryFile.containsKey(packet.getIdentifier())) {
        container = mapPendingBinaryFile.get(packet.getIdentifier());

        RandomAccessFile randomAccessFile = container.getRandomAccessFile(packet.getOwnerName(),
            packet.getFileName());

        if (randomAccessFile != null) {
          if (log.isDebugEnabled())
            log.info("Offset : BinaryFile_Last_Packet :" + packet.getOffset());

          randomAccessFile.seek(packet.getOffset());
          randomAccessFile.write(packet.getByteArray());
          randomAccessFile.close();

          if (log.isDebugEnabled())
            log.debug("Last packet of file has been received : " + packet.getFileName());
        } else
          log.warn("Can't find the RandomAccessFile : \n" + "owner - \t" + packet.getOwnerName()
              + "\nfile name - \t" + packet.getFileName());
      }
      break;

    case Packet.PacketType.ALL_BinaryFile_transferred_OK:
      if (mapPendingBinaryFile.containsKey(packet.getIdentifier())) {
        PendingBinaryFile pbf = mapPendingBinaryFile.get(packet.getIdentifier());
        pbf.addToSuccessfulTransferCounter(packet.getSize());

        if (pbf.isSuccessfulTransfer()) {
          if (log.isDebugEnabled())
            log.debug("The signal ALL_BinaryFile_transferred_OK has been received  from "
                + packet.getOwnerName());

          List<FileDescriptor> fileDescriptorList = pbf.getSortedFilesDescriptorList();

          if (log.isDebugEnabled())
            log.info("fileDescriptorList.size() == pbf.getNeedTransferCounter() : "
                + fileDescriptorList.size() + "== " + pbf.getNeedTransferCounter());

          if (fileDescriptorList.size() == pbf.getNeedTransferCounter()) {
            for (FileDescriptor fileDescriptor : fileDescriptorList) {
              try {
                TransactionChangesLog transactionChangesLog = recoveryReader
                    .getChangesLog(fileDescriptor.getFile().getAbsolutePath());

                transactionChangesLog.setSystemId(fileDescriptor.getSystemId());

                Calendar cLogTime = fileNameFactory.getDateFromFileName(fileDescriptor.getFile()
                    .getName());

                if (log.isDebugEnabled()) {
                  log.debug("Save to JCR : " + fileDescriptor.getFile().getAbsolutePath());
                  log.debug("SystemID : " + transactionChangesLog.getSystemId());
                  log.debug("list size : " + fileDescriptorList.size());
                }

                // dump log
                if (log.isDebugEnabled()) {
                  ChangesLogIterator logIterator = transactionChangesLog.getLogIterator();
                  while (logIterator.hasNextLog()) {
                    PlainChangesLog pcl = logIterator.nextLog();
                    log.debug(pcl.dump());
                  }
                }

                saveChangesLog(dataKeeper, transactionChangesLog, cLogTime);

                if (log.isDebugEnabled()) {
                  log.debug("After save message: the owner systemId --> "
                      + transactionChangesLog.getSystemId());
                  log.debug("After save message: --> " + systemId);
                }

              } catch (Exception e) {
                log.error("Can't save to JCR ", e);
              }
            }

            // Send file name list
            List<String> fileNameList = mapPendingBinaryFile.get(packet.getIdentifier())
                .getFileNameList();

            Packet packetFileNameList = new Packet(Packet.PacketType.ALL_ChangesLog_saved_OK,
                packet.getIdentifier(), ownName, fileNameList);
            send(packetFileNameList);

            log.info("The " + fileDescriptorList.size() + " changeslogs were received and saved");

          } else if (log.isDebugEnabled()) {
            log.debug("Do not start save : " + fileDescriptorList.size() + " of "
                + pbf.getNeedTransferCounter());
          }
        }
      }
      break;

    case Packet.PacketType.ALL_ChangesLog_saved_OK:
      long removeCounter = recoveryWriter.removeChangesLog(packet.getFileNameList(), packet
          .getOwnerName());

      if (log.isDebugEnabled())
        log.debug("Remove from file system : " + removeCounter);

      Packet removedOldChangesLogPacket = new Packet(
          Packet.PacketType.REMOVED_OLD_CHANGESLOG_COUNTER, packet.getIdentifier(), ownName);
      removedOldChangesLogPacket.setSize(removeCounter);
      channelManager.sendPacket(removedOldChangesLogPacket);

      break;

    case Packet.PacketType.REMOVED_OLD_CHANGESLOG_COUNTER:
      if (mapPendingBinaryFile.containsKey(packet.getIdentifier()) == true) {
        PendingBinaryFile pbf = mapPendingBinaryFile.get(packet.getIdentifier());
        pbf.setRemovedOldChangesLogCounter(pbf.getRemovedOldChangesLogCounter() + packet.getSize());

        if (pbf.isAllOldChangesLogsRemoved()) {

          // remove temporary files
          for (FileDescriptor fd : pbf.getSortedFilesDescriptorList())
            fileCleaner.addFile(fd.getFile());

          // remove PendingBinaryFile
          mapPendingBinaryFile.remove(packet.getIdentifier());

          // next iteration
          if (log.isDebugEnabled())
            log.debug("Next iteration of recovery ...");

          synchronizRepository();
        }
      } else
        log.warn("Can not find the PendingBinaryFile whith id: " + packet.getIdentifier());
      break;

    case Packet.PacketType.NEED_TRANSFER_COUNTER:
      if (mapPendingBinaryFile.containsKey(packet.getIdentifier()) == false)
        mapPendingBinaryFile.put(packet.getIdentifier(), new PendingBinaryFile());

      PendingBinaryFile pbf = mapPendingBinaryFile.get(packet.getIdentifier());
      pbf.setNeedTransferCounter(pbf.getNeedTransferCounter() + packet.getSize());

      if (log.isDebugEnabled())
        log.debug("NeedTransferCounter : " + pbf.getNeedTransferCounter());
      break;

    case Packet.PacketType.SYNCHRONIZED_OK:
      if (successfulSynchronizedList.contains(packet.getOwnerName()) == false)
        successfulSynchronizedList.add(packet.getOwnerName());

      if (successfulSynchronizedList.size() == initedParticipantsClusterList.size()) {
        stat = AbstractWorkspaceDataReceiver.NORMAL_MODE;
      }
      break;
    }

    return stat;
  }

  private void sendChangesLogUpDate(Calendar timeStamp, String ownerName, String identifier) {
    try {
      if (log.isDebugEnabled())
        log.debug("+++ sendChangesLogUpDate() +++ : "
            + Calendar.getInstance().getTime().toGMTString());

      List<String> filePathList = recoveryReader.getFilePathList(timeStamp, ownerName);

      Packet needTransferCounter = new Packet(Packet.PacketType.NEED_TRANSFER_COUNTER, identifier,
          ownName);
      needTransferCounter.setSize(filePathList.size());
      channelManager.sendPacket(needTransferCounter);

      if (filePathList.size() > 0) {
        for (String filePath : filePathList) {
          channelManager.sendBinaryFile(filePath, ownerName, identifier, systemId);
        }

        Packet endPocket = new Packet(Packet.PacketType.ALL_BinaryFile_transferred_OK, identifier);
        endPocket.setOwnName(ownerName);
        endPocket.setSize(filePathList.size());
        channelManager.sendPacket(endPocket);

      } else {
        Packet synchronizedOKPacket = new Packet(Packet.PacketType.SYNCHRONIZED_OK, IdGenerator
            .generate(), ownerName);
        channelManager.sendPacket(synchronizedOKPacket);
      }

    } catch (Exception e) {
      log.error("ChangesLogs was send with error", e);
    }
  }

  public void setDataKeeper(ItemDataKeeper dataKeeper) {
    this.dataKeeper = dataKeeper;
  }

  public void updateInitedParticipantsClusterList(Collection<? extends String> list) {
    initedParticipantsClusterList = new ArrayList<String>(list);
  }

  private void saveChangesLog(ItemDataKeeper dataManager, TransactionChangesLog changesLog,
      Calendar cLogTime) throws ReplicationException {
    try {
      try {
        dataManager.save(changesLog);
      } catch (JCRInvalidItemStateException e) {
        TransactionChangesLog normalizeChangesLog = getNormalizedChangesLog(e.getIdentifier(), e
            .getState(), changesLog);
        if (normalizeChangesLog != null)
          saveChangesLog(dataManager, normalizeChangesLog, cLogTime);
      }
    } catch (Throwable t) {
      throw new ReplicationException("Save error. Log time " + cLogTime.getTime(), t);
    }
  }

  private TransactionChangesLog getNormalizedChangesLog(String collisionID, int state,
      TransactionChangesLog changesLog) {
    ItemState citem = changesLog.getItemState(collisionID);

    if (citem != null) {

      TransactionChangesLog result = new TransactionChangesLog();
      result.setSystemId(changesLog.getSystemId());

      ChangesLogIterator cli = changesLog.getLogIterator();
      while (cli.hasNextLog()) {
        ArrayList<ItemState> normalized = new ArrayList<ItemState>();
        PlainChangesLog next = cli.nextLog();
        for (ItemState change : next.getAllStates()) {
          if (state == change.getState()) {
            ItemData item = change.getData();
            // targeted state
            if (citem.isNode()) {
              // Node... by ID and desc path
              if (!item.getIdentifier().equals(collisionID)
                  && !item.getQPath().isDescendantOf(citem.getData().getQPath(), false))
                normalized.add(change);
            } else if (!item.getIdentifier().equals(collisionID)) {
              // Property... by ID
              normalized.add(change);
            }
          } else
            // another state
            normalized.add(change);
        }

        PlainChangesLog plog = new PlainChangesLogImpl(normalized, next.getSessionId(), next
            .getEventType());
        result.addLog(plog);
      }

      return result;
    }

    return null;
  }
}

class PendingBinaryFile {
  private static Log                                       log = ExoLogger
                                                                   .getLogger("ext.PendingBinaryFile");

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

  public RandomAccessFile getRandomAccessFile(String ownName, String fileName) {

    HashMap<String, FileDescriptor> fileMap = mapFilePerOwner.get(ownName);

    return fileMap.get(fileName).getRandomAccessFile();
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

  class FileDescriptor implements Comparable<FileDescriptor> {
    private File             file;

    private RandomAccessFile randomAccessFile;

    private final String     systemId;

    public FileDescriptor(File f, RandomAccessFile raf, String systemId) {
      this.file = f;
      this.randomAccessFile = raf;
      this.systemId = systemId;
    }

    public File getFile() {
      return file;
    }

    public RandomAccessFile getRandomAccessFile() {
      return randomAccessFile;
    }

    public String getSystemId() {
      return systemId;
    }

    public int compareTo(FileDescriptor o) {
      return file.getName().compareTo(o.getFile().getName());
    }
  }

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

class Counter {
  int count = 0;

  public int inc() {
    return ++count;
  }

  public void clear() {
    count = 0;
  }

  public int getValue() {
    return count;
  }
}