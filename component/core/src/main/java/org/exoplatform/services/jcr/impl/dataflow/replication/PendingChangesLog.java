/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.dataflow.replication;

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
import java.util.List;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.util.UUIDGenerator;

/**
 * Created by The eXo Platform SARL Author : Alex Reshetnyak
 * alex.reshetnyak@exoplatform.com.ua 24.11.2006
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id$
 */

public class PendingChangesLog {
  public class Type {
    public static final int ItemDataChangesLog_without_Streams = 1;

    public static final int ItemDataChangesLog_with_Streams    = 2;
  }

  private TransactionChangesLog  itemDataChangesLog;

  private List<InputStream>      listInputStream;

  private List<RandomAccessFile> listRandomAccessFile;

  private int                    containerType;

  private List<FixupStream>      listFixupStream;

  private List<File>             listFile;

  private String                 identifier;

  private FileCleaner            fileCleaner;
  
  private byte[]                 data;

  public PendingChangesLog(TransactionChangesLog itemDataChangesLog_,
      FileCleaner fileCleaner) throws IOException {
    itemDataChangesLog = itemDataChangesLog_;
    listInputStream = new ArrayList<InputStream>();
    listFixupStream = new ArrayList<FixupStream>();
    containerType = analysisItemDataChangesLog(); 
      //analysisItemDataChangesLog();
    listFile = new ArrayList<File>();
    identifier = UUIDGenerator.generate();
    this.fileCleaner = fileCleaner;
  }

  public PendingChangesLog(TransactionChangesLog itemDataChangesLog_, 
      String identifier, int type, FileCleaner fileCleaner)
      throws IOException {
    itemDataChangesLog = itemDataChangesLog_;
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
  
  public void putData(int offset, byte[] tempData){
    for (int i = 0; i < tempData.length; i++) 
      data[i+offset] = tempData[i];
  }
  
  public byte[] getData(){
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

  private int analysisItemDataChangesLog_TO_DO() throws IOException {
    int itemDataChangesLogType = PendingChangesLog.Type.ItemDataChangesLog_without_Streams;

    List<ItemState> listItemState = itemDataChangesLog.getAllStates();

    for (int i = 0; i < listItemState.size(); i++) {
      ItemState itemState = listItemState.get(i);
      ItemData itemData = itemState.getData();

      if (itemData instanceof TransientPropertyData) {
        TransientPropertyData propertyData = (TransientPropertyData) itemData;
        if ((propertyData.getValues() != null)) 
          for (int j = 0; j < propertyData.getValues().size(); j++)
            if ((propertyData.getValues().get(j).getAsByteArray().length >= (200*1024))) {
              listFixupStream.add(new FixupStream(i, j));
              InputStream inputStream = new ByteArrayInputStream(propertyData.getValues().get(j).getAsByteArray());
              listInputStream.add(inputStream);
              itemDataChangesLogType = PendingChangesLog.Type.ItemDataChangesLog_with_Streams;
            }
      }
      
      if (itemData instanceof TransientNodeData) {
        TransientNodeData propertyData = (TransientNodeData) itemData;
      }
    }
    return itemDataChangesLogType;
  }
  
  private int analysisItemDataChangesLog() throws IOException {
    int itemDataChangesLogType = PendingChangesLog.Type.ItemDataChangesLog_without_Streams;

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
              itemDataChangesLogType = PendingChangesLog.Type.ItemDataChangesLog_with_Streams;
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

  public RandomAccessFile getRandomAccessFile(FixupStream fs) {
    for (int i = 0; i < listFixupStream.size(); i++)
      if (this.listFixupStream.get(i).compare(fs))
        return listRandomAccessFile.get(i);
    return null;
  }

  public void restore() throws Exception {
//    fileCleaner = new FileCleaner(30030);
    for (int i = 0; i < this.listFixupStream.size(); i++) {
      List<ItemState> listItemState = itemDataChangesLog.getAllStates();
      ItemState itemState = listItemState.get(listFixupStream.get(i).getItemSateId());
      ItemData itemData = itemState.getData();

      TransientPropertyData propertyData = (TransientPropertyData) itemData;
      TransientValueData transientValueData = (TransientValueData) (propertyData.getValues()
          .get(listFixupStream.get(i).getValueDataId()));
      transientValueData.setStream(new FileInputStream(listFile.get(i)));
      transientValueData.setFileCleaner(fileCleaner);
      transientValueData.isByteArray();
    }

    for (int i = 0; i < listFile.size(); i++) 
      fileCleaner.addFile(listFile.get(i));
      
  }

}