/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.dataflow;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Calendar;

import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.Uuid;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.util.DateFormatHelper;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;

/**
 * Created by The eXo Platform SARL .<br/> 
 * 
 * 
 * @author Gennady Azarenkov
 * @version $Id$
 */

public class TransientValueData extends AbstractValueData implements Externalizable {
//  , Cloneable {

  protected byte[]      data;

  protected InputStream tmpStream;
  
  protected InputStream lockStream;

  protected File        spoolFile;

  protected FileCleaner fileCleaner;

  protected int         maxBufferSize;

  protected File        tempDirectory;
  
  protected boolean spooled = false;
  private final boolean deleteSpoolFile;
  
  static protected byte[] stringToBytes(final String value) {
    try {
      return value.getBytes(Constants.DEFAULT_ENCODING);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("FATAL ERROR Charset " + Constants.DEFAULT_ENCODING
          + " is not supported!");
    }
  }

  /**
   * creates TransientValueData with incoming byte array
   * 
   * @param value
   * @param orderNumber
   */
  private TransientValueData(byte[] value, int orderNumber) {
    super(orderNumber);
    this.data = value;
    this.deleteSpoolFile = true;

  }

  /**
   * creates TransientValueData with incoming input stream. the stream will be
   * lazily spooled to file or byte array depending on maxBufferSize
   * 
   * @param orderNumber
   */
  private TransientValueData(InputStream stream, int orderNumber) {
    super(orderNumber);
    this.tmpStream = stream;
    this.deleteSpoolFile = true;
  }
  
  
  public TransientValueData(int orderNumber, byte[] bytes, InputStream stream, 
      File spoolFile, FileCleaner fileCleaner, int maxBufferSize,
      File tempDirectory,boolean deleteSpoolFile ) {
    super(orderNumber);
    this.data = bytes;
    this.tmpStream = stream;
    this.spoolFile = spoolFile;
    this.fileCleaner = fileCleaner;
    this.maxBufferSize = maxBufferSize;
    this.tempDirectory = tempDirectory;
    this.deleteSpoolFile = deleteSpoolFile;
    if (spoolFile != null){
      this.spooled = true;
    }
  }

  
  public TransientValueData(InputStream stream) {
    this(stream, 0);
  }

  /**
   * Constructor for String value data
   * 
   * @param value
   */
  public TransientValueData(String value) {
    this(stringToBytes(value), 0);
  }

  /**
   * Constructor for boolean value data
   * 
   * @param value
   */
  public TransientValueData(boolean value) {
    this(Boolean.valueOf(value).toString().getBytes(), 0);
  }

  /**
   * Constructor for Calendar value data
   * 
   * @param value
   */
  public TransientValueData(Calendar value) {
    this(new DateFormatHelper().serialize(value), 0);
  }

  /**
   * Constructor for double value data
   * 
   * @param value
   */
  public TransientValueData(double value) {
    this(Double.valueOf(value).toString().getBytes(), 0);
  }

  /**
   * Constructor for long value data
   * 
   * @param value
   */
  public TransientValueData(long value) {
    this(Long.valueOf(value).toString().getBytes(), 0);
  }

  /**
   * Constructor for Name value data
   * 
   * @param value
   */
  public TransientValueData(InternalQName value) {
    this(value.getAsString().getBytes(), 0);
  }

  /**
   * Constructor for Path value data
   * 
   * @param value
   */
  public TransientValueData(QPath value) {
    this(value.getAsString().getBytes(), 0);
  }

  /**
   * Constructor for Reference value data
   * 
   * @param value
   */
  public TransientValueData(Uuid value) {
    this(value.getString().getBytes(), 0);
  }

  /**
   * Constructor for Permission value data
   * 
   * @param value
   */
  public TransientValueData(AccessControlEntry value) {
    this(value.getAsString().getBytes(), 0);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.datamodel.ValueData#getAsByteArray()
   */
  public byte[] getAsByteArray() throws IOException {
    spoolInputStream();
    if (data != null) {
      byte[] bytes = new byte[data.length];
      System.arraycopy(data, 0, bytes, 0, data.length);
      return bytes;
    } else {
      return fileToByteArray();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.datamodel.ValueData#getAsStream()
   */
  public InputStream getAsStream() throws IOException {
    spoolInputStream();
    if (data != null) {
      return new ByteArrayInputStream(data); // from bytes
    } else if (spoolFile != null) {
      return new FileInputStream(spoolFile); // from spool file if initialized
    } else
      throw new NullPointerException("Null Stream data ");

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.datamodel.ValueData#getLength()
   */
  public long getLength() {
    try {
      spoolInputStream();
    } catch (IOException e) {
      e.printStackTrace();
      return -1;
    }
    if (data == null) {
      return spoolFile.length();
    } else {
      return data.length;
    }
  }

  /*
   * returns true if this data is spooled to byte array, false otherwise (to
   * file)
   * 
   * @see org.exoplatform.services.jcr.datamodel.ValueData#isByteArray()
   */
  public boolean isByteArray() {
    try {
      spoolInputStream();
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    return data != null;
  }
  
  @Override
  public TransientValueData createTransientCopy() {
    
    // do we need that?
    //byte[] newBytes = null;
    if (isByteArray()) {
      // make a copy of real data
      byte[] newBytes = new byte[data.length];  
      System.arraycopy(data, 0, newBytes, 0, newBytes.length);
      return new TransientValueData(newBytes, orderNumber);
    } else {
      return this;
    }
    ///////

//    return new TransientValueData(orderNumber, null, tmpStream, 
//        spoolFile, fileCleaner, maxBufferSize,
//        tempDirectory);
  }


  /**
   * @return spool file if any
   */
  public File getSpoolFile() {
    try {
      spoolInputStream();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
    return spoolFile;
  }

  /**
   * helper method to simplify operations that requires stringified data
   * 
   * @return
   * @throws IOException
   */
  public String getString() throws IOException {
    return new String(getAsByteArray(), Constants.DEFAULT_ENCODING);
  }

  // ///////////////////////////////////
  /**
   * make sense for stream storage only
   * 
   * @param cleaner
   */
  public void setFileCleaner(FileCleaner cleaner) {
    this.fileCleaner = cleaner;
  }

  /**
   * @param tempDirectory
   */
  public void setTempDirectory(File tempDirectory) {
    this.tempDirectory = tempDirectory;
  }

  /**
   * @param maxBufferSize
   */
  public void setMaxBufferSize(int maxBufferSize) {
    this.maxBufferSize = maxBufferSize;
  }

  protected void finalize() throws Throwable {
    if (spoolFile != null) {
      if (lockStream != null) {
        lockStream.close();
        lockStream = null;
      }
      if (deleteSpoolFile && spoolFile.exists()) {
        if (!spoolFile.delete())
          if (fileCleaner != null) {
            log.info("Could not remove file. Add to fileCleaner " + spoolFile);
            fileCleaner.addFile(spoolFile);
          } else {
            log.warn("Could not remove temporary file on finalize " + spoolFile.getAbsolutePath());
          }
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }

    if (obj instanceof TransientValueData) {

      TransientValueData other = (TransientValueData) obj;
      if (isByteArray() != other.isByteArray())
        return false;
      try {
        if (isByteArray()) {
          return Arrays.equals(getAsByteArray(), other.getAsByteArray());
        } else
          return getSpoolFile().equals(other.getSpoolFile());
      } catch (IOException e) {
        e.printStackTrace();
        return false;
      }
    }
    return false;
  }

  private boolean toBufferOnly() {
    return fileCleaner == null;
  }


  // ///////////////////////////////////

  private void spoolInputStream() throws IOException {

    if (spooled || tmpStream == null) // already spooled
      return;

    this.spoolFile = File.createTempFile("jcrvd", null, tempDirectory);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    FileOutputStream fos = new FileOutputStream(spoolFile);

    byte[] buffer = new byte[0x2000];
    int len;
    long total = 0;
    try {
      while ((len = tmpStream.read(buffer)) > 0) {
        if (!toBufferOnly())
          fos.write(buffer, 0, len);
        total += len;
        if (total < maxBufferSize || toBufferOnly()) {
          baos.write(buffer, 0, len);
        } else {
          baos = null;
        }
      }
      fos.close();
      if (baos != null) { // spool to byte array
        this.spoolFile.delete();
        this.spoolFile = null;
        this.data = baos.toByteArray();
        baos.close();
      } else { // spool to file
        //this.fileCleaner.addFile(spoolFile);
        this.data = null;
      }
      this.tmpStream = null;
      spooled = true;
      
      //------------------------
//      // TODO
//      if (baos == null) {
//        tmpStream = new FileInputStream(spoolFile);
//      }
      //------------------------
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
  public void lock(){
     if(lockStream == null && spoolFile !=null){
       try {
        lockStream = new FileInputStream(spoolFile);
      } catch (FileNotFoundException e) {
      }
     }
  }
  /**
   * try to convert stream to byte array WARNING: Potential lack of memory due
   * to call getAsByteArray() on stream data
   * 
   * @return byte array
   */
  private byte[] fileToByteArray() throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    byte[] buffer = new byte[0x2000];
    int len;
    int total = 0;
    FileInputStream stream = new FileInputStream(spoolFile);
    while ((len = stream.read(buffer)) > 0) {
      out.write(buffer, 0, len);
      total += len;
      if (total > maxBufferSize)
        log.warn("Potential lack of memory due to call getAsByteArray() on stream data exceeded "
            + total + " bytes");
    }
    out.close();
    return out.toByteArray();
  }

  
  // ------------- Serializable

  public TransientValueData() {
    super(0);
    this.deleteSpoolFile = true;
  }
  
  public void writeExternal(ObjectOutput out) throws IOException {
//    System.out.println("-->TransientValueData--> writeExternal(ObjectOutput out)");
    if (this.isByteArray()) {
//      write 1 - ByteArray
//      write 2 - InputStream
      out.writeInt(1);
      int f = data.length; 
      out.writeInt(f);
      out.write(data);
//      out.writeInt(orderNumber);
//      out.writeInt(maxBufferSize);
    } else {
      out.writeInt(2);
//      out.writeInt(maxBufferSize);
    }
    out.writeInt(orderNumber);
    out.writeInt(maxBufferSize);
  }

  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
//    System.out.println("-->TransientValueData--> readExternal(ObjectInput in)");
    int type = in.readInt();
    //type == 1 --> ByteArray
    //type == 2 --> InputStream
    
    if (type == 1) {
      data = new byte[in.readInt()];
      for (int i = 0; i < data.length; i++) 
        data[i] = in.readByte();
    }
    orderNumber = in.readInt();
    maxBufferSize = in.readInt();
  }
  
  public void setStream(InputStream in ){
    this.tmpStream = in;
  }
  
}
