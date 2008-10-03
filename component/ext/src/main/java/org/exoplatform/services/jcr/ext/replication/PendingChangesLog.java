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
package org.exoplatform.services.jcr.ext.replication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id$
 */

public class PendingChangesLog {
  private static Log log = ExoLogger.getLogger("ext.PendingChangesLog");

  public final class Type {
    public static final int CHANGESLOG_WITHOUT_STREAM = 1;

    public static final int CHANGESLOG_WITH_STREAM    = 2;

    private Type() {
    }
  }

  private static final int                       SLEEP_TIME = 5;

  private TransactionChangesLog                  itemDataChangesLog;

  private List<InputStream>                      listInputStream;

  private List<RandomAccessFile>                 listRandomAccessFile;

  private int                                    containerType;

  private List<FixupStream>                      listFixupStream;

  private HashMap<FixupStream, RandomAccessFile> mapFixupStream;

  private List<File>                             listFile;

  private String                                 identifier;

  private FileCleaner                            fileCleaner;

  private byte[]                                 data;

  public PendingChangesLog(TransactionChangesLog itemDataChangesLog, FileCleaner fileCleaner) throws IOException {
    this.itemDataChangesLog = itemDataChangesLog;
    listInputStream = new ArrayList<InputStream>();
    listFixupStream = new ArrayList<FixupStream>();
    containerType = analysisItemDataChangesLog();
    listFile = new ArrayList<File>();
    identifier = IdGenerator.generate();
    this.fileCleaner = fileCleaner;
  }

  public PendingChangesLog(TransactionChangesLog itemDataChangesLog,
                           String identifier,
                           int type,
                           FileCleaner fileCleaner) throws IOException {
    this.itemDataChangesLog = itemDataChangesLog;
    listInputStream = new ArrayList<InputStream>();
    listFixupStream = new ArrayList<FixupStream>();
    listRandomAccessFile = new ArrayList<RandomAccessFile>();
    listFile = new ArrayList<File>();
    this.identifier = identifier;
    containerType = type;
    this.fileCleaner = fileCleaner;
  }

  public PendingChangesLog(String identifier, int dataLength) {
    this.identifier = identifier;
    data = new byte[dataLength];
  }

  public PendingChangesLog(TransactionChangesLog transactionChangesLog,
                           List<FixupStream> listFixupStreams,
                           List<File> listFiles,
                           FileCleaner fileCleaner) {
    this.itemDataChangesLog = transactionChangesLog;
    this.listFixupStream = listFixupStreams;
    this.listFile = listFiles;
    this.fileCleaner = fileCleaner;
  }

  public void putData(int offset, byte[] tempData) {
    for (int i = 0; i < tempData.length; i++)
      data[i + offset] = tempData[i];
  }

  public byte[] getData() {
    return data;
  }

  public TransactionChangesLog getItemDataChangesLog() {
    return itemDataChangesLog;
  }

  public List<InputStream> getInputStreams() {
    return listInputStream;
  }

  public List<RandomAccessFile> getListRandomAccessFiles() {
    return listRandomAccessFile;
  }

  public List<File> getListFile() {
    return listFile;
  }

  public List<FixupStream> getFixupStreams() {
    return listFixupStream;
  }

  private int analysisItemDataChangesLog() throws IOException {
    int itemDataChangesLogType = PendingChangesLog.Type.CHANGESLOG_WITHOUT_STREAM;

    List<ItemState> listItemState = itemDataChangesLog.getAllStates();

    for (int i = 0; i < listItemState.size(); i++) {
      ItemState itemState = listItemState.get(i);
      ItemData itemData = itemState.getData();

      if (itemData instanceof TransientPropertyData) {
        TransientPropertyData propertyData = (TransientPropertyData) itemData;
        if ((propertyData.getValues() != null))
          for (int j = 0; j < propertyData.getValues().size(); j++)
            if (!(propertyData.getValues().get(j).isByteArray())) {
              listFixupStream.add(new FixupStream(i, j));

              // TODO
              InputStream inputStream;
              if (itemState.isDeleted())
                inputStream = new ByteArrayInputStream("".getBytes());
              else
                inputStream = propertyData.getValues().get(j).getAsStream();

              listInputStream.add(inputStream);
              itemDataChangesLogType = PendingChangesLog.Type.CHANGESLOG_WITH_STREAM;
            }

      }

      if (itemData instanceof TransientNodeData) {
        TransientNodeData propertyData = (TransientNodeData) itemData;
      }

    }

    return itemDataChangesLogType;
  }

  public int getConteinerType() {
    return containerType;
  }

  public String getIdentifier() {
    return identifier;
  }

  public static byte[] getAsByteArray(TransactionChangesLog dataChangesLog) throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(os);
    oos.writeObject(dataChangesLog);

    byte[] bArray = os.toByteArray();
    return bArray;
  }

  public static TransactionChangesLog getAsItemDataChangesLog(byte[] byteArray) throws IOException,
                                                                               ClassNotFoundException {
    ByteArrayInputStream is = new ByteArrayInputStream(byteArray);
    ObjectInputStream ois = new ObjectInputStream(is);
    TransactionChangesLog objRead = (TransactionChangesLog) ois.readObject();

    return objRead;
  }

  public RandomAccessFile getRandomAccessFile(FixupStream fs) throws IOException {
    int i = 0;
    try {
      for (i = 0; i < listFixupStream.size(); i++)
        if (this.listFixupStream.get(i).compare(fs))
          return listRandomAccessFile.get(i);
    } catch (IndexOutOfBoundsException e) {
      try {
        Thread.sleep(SLEEP_TIME);
        return listRandomAccessFile.get(i);
      } catch (InterruptedException ie) {
        log.error("The interrupted exceptio : ", ie);
      } catch (IndexOutOfBoundsException ioobe) {
        if (log.isDebugEnabled()) {
          log.info("listFixupStream.size() == " + listFixupStream.size());
          log.info("listRandomAccessFile.size() == " + listRandomAccessFile.size());
          log.info(" i == " + i);
        }
        synchronized (this) {
          if (listFile.size() > i) {
            listFile.remove(i);
          }
          listFixupStream.remove(i);

          addNewStream(fs);

          getRandomAccessFile(fs);
        }
      }
    }
    return null;
  }

  public void addNewStream(FixupStream fs) throws IOException {
    this.getFixupStreams().add(fs);

    File f = File.createTempFile("tempFile" + IdGenerator.generate(), ".tmp");

    this.getListFile().add(f);
    this.getListRandomAccessFiles().add(new RandomAccessFile(f, "rw"));

  }

  public void restore() throws IOException {
    for (int i = 0; i < this.listFixupStream.size(); i++) {
      List<ItemState> listItemState = itemDataChangesLog.getAllStates();
      ItemState itemState = listItemState.get(listFixupStream.get(i).getItemSateId());
      ItemData itemData = itemState.getData();

      TransientPropertyData propertyData = (TransientPropertyData) itemData;
      TransientValueData transientValueData = (TransientValueData) (propertyData.getValues().get(listFixupStream.get(i)
                                                                                                                .getValueDataId()));
      transientValueData.setStream(new FileInputStream(listFile.get(i)));
      transientValueData.setFileCleaner(fileCleaner);
      transientValueData.isByteArray();
    }

    if (listRandomAccessFile != null)
      for (int i = 0; i < listRandomAccessFile.size(); i++)
        listRandomAccessFile.get(i).close();

    for (int i = 0; i < listFile.size(); i++)
      fileCleaner.addFile(listFile.get(i));

  }

}
