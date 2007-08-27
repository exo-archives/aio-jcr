package org.exoplatform.services.jcr.impl.dataflow;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.datamodel.BinaryValueData;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;

public class EditableValueData extends TransientValueData implements BinaryValueData {

  protected final FileCleaner fileCleaner;

  protected final int maxBufferSize;

  protected final File tempDirectory;
  
  protected byte[] changeBytes;
  
  protected final int maxIOBuffSize;
  
  // file used for random writing
  protected File changeFile = null;
  protected FileChannel changeChannel = null; 

  protected class SharedByteArrayOutputStream extends ByteArrayOutputStream {
    /** Return backed array object */
    byte[] buf() {
      return buf;
    }
  }
  
  public EditableValueData(byte[] bytes, int orderNumber) {
    
    super(orderNumber);
    
    this.fileCleaner = null;
    this.maxBufferSize = -1;
    this.tempDirectory = null;
    
    this.maxIOBuffSize = calcMaxIOSize();
    
    this.changeBytes = bytes;
  }
  
  public EditableValueData(File spoolFile, int orderNumber,
      FileCleaner fileCleaner, int maxBufferSize, File tempDirectory) throws IOException {
    
    super(orderNumber);
    
    this.fileCleaner = fileCleaner;
    this.maxBufferSize = maxBufferSize;
    this.tempDirectory = tempDirectory;
    
    this.maxIOBuffSize = calcMaxIOSize();
    
    this.changeBytes = null;
    this.changeFile = File.createTempFile("jcrvdedit", null, tempDirectory);
    
    log.debug("changeFile created"+this.toString());
    
    this.changeChannel = new FileOutputStream(changeFile, false).getChannel();

    FileChannel spoolCh = new FileInputStream(spoolFile).getChannel();

    this.changeChannel.transferFrom(spoolCh, 0, spoolCh.size());

    //changeCh.close();
    spoolCh.close(); 
  }
  
  protected int calcMaxIOSize() {
    return maxBufferSize < 1024 ? 1024 : maxBufferSize < (250 * 1024) ? maxBufferSize : 250 * 1024;
  }
  
  protected int calcBuffSize(long length) {
    int buffSize = (int) (length > maxIOBuffSize ? maxIOBuffSize : length / 4);
    buffSize = buffSize < 1024 ? 1024 : buffSize;  
    return buffSize; 
  }
  
  public TransientValueData createTransientCopy() throws RepositoryException {
    if (isByteArray()) {
      // bytes, make a copy of real data
      byte[] newBytes = new byte[changeBytes.length];
      System.arraycopy(changeBytes, 0, newBytes, 0, newBytes.length);
      return new TransientValueData(newBytes, orderNumber);
    } else {
      // stream, make a copy
      try {
        // force changes made to the file
        changeChannel.force(false);
        
        InputStream thisStream = getAsStream();
        TransientValueData copy = new TransientValueData(
            orderNumber, null, thisStream, null, fileCleaner, maxBufferSize, tempDirectory, true);
        copy.getSpoolFile(); // read now, till the source isn't changed
        thisStream.close();
        
        return copy;
      } catch (IOException e) {
        throw new RepositoryException("Create transient copy error. " + e, e);
      }
    } 
  }

  public byte[] getAsByteArray() throws IOException {
    log.debug("getAsByteArray" + this.toString());

    if (changeFile != null) {
      FileInputStream fs = new FileInputStream(changeFile);
      
      SharedByteArrayOutputStream bout = new SharedByteArrayOutputStream(); 
      int i= -1;
      byte[] buff = new byte[maxBufferSize];
      while ((i = fs.read(buff))>=0) {
        bout.write(buff, 0, i);
      }
      
      return bout.buf();
    }
    
    byte[] copyBytes = new byte[changeBytes.length];
    System.arraycopy(changeBytes, 0, copyBytes, 0, copyBytes.length);
    return copyBytes;
  }

  public InputStream getAsStream() throws IOException {
    log.debug("getAsStream"+this.toString());
    
    if (changeFile != null) {
      return new FileInputStream(changeFile);
    }

    return new ByteArrayInputStream(changeBytes);
  }

  public long getLength() {
    log.debug("getLength"+this.toString());
    if (changeFile != null) {
      log.debug("getLength randFile : " + changeFile.length());
      return changeFile.length();
    }
    
    return changeBytes.length;
  }

  public boolean isByteArray() {
    return changeBytes != null;
  }

  /**
   * Update with <code>length</code> bytes from the specified InputStream
   * <code>stream</code> to this value data at <code>position</code>
   * 
   * @author Karpenko
   * 
   * @param stream
   *          the data.
   * @param length
   *          the number of bytes from stream to write.
   * @param position
   *          position in file to write data
   * 
   * @throws IOException
   */

  public void update(InputStream stream, long length, long position) throws IOException {

    if (isByteArray()) {
      // merge stream content with existed bytes 
      SharedByteArrayOutputStream bout = new SharedByteArrayOutputStream();
      
      if (position > 0)
        // begin from the existed bytes
        bout.write(changeBytes, 0, (int) position);
      
      int writen = 0;
      int i= -1;
      byte[] buff = new byte[calcBuffSize(length)];
      while ((i = stream.read(buff))>=0) {
        bout.write(buff, 0, i);
        writen += i;
      }
      
      int lastWriten = (int) position + writen;
      if (lastWriten < changeBytes.length)
        // continue write content from the existed bytes
        bout.write(changeBytes, lastWriten - 1, changeBytes.length);
      
      if (changeBytes.length + length <= maxBufferSize) {
        // edit bytes
        this.changeBytes = bout.buf();
      } else {
        // switch from bytes to file/channel
        this.changeFile = File.createTempFile("jcrvdedit", null, tempDirectory);
        this.changeChannel = new FileOutputStream(changeFile, false).getChannel();
        //this.changeChannel.position(bout.size()); TODO

        ReadableByteChannel bch = Channels.newChannel(new ByteArrayInputStream(bout.buf()));
        this.changeChannel.transferFrom(bch, 0, bout.size());
        bch.close();
        
        this.changeBytes = null;
      }
    } else {
      ReadableByteChannel ch = Channels.newChannel(stream);
      
      //FileChannel fc = new FileOutputStream(changeFile, true).getChannel();
      long size = changeChannel.transferFrom(ch, position, length);
      
      // TODO
      // Forces any updates to this channel's file to be written to the storage
      // device that contains it.
      // changeChannel.force(false);
      
      //fc.close();
      ch.close();
    }
  }

  /**
   * Truncates binary value to <code> size </code>
   * 
   * @author Karpenko
   * @param size
   * @throws IOException
   */
  public void truncate(long size) throws IOException {

    if (isByteArray()) {
      // truncate bytes
      byte[] newBytes = new byte[(int) size];
      System.arraycopy(changeBytes, 0, newBytes, 0, newBytes.length);
      changeBytes = newBytes;
    } else if (size <= maxBufferSize) {
      // switch to a bytes
      ByteBuffer bb = ByteBuffer.allocate((int) size);
      changeChannel.force(false);
      changeChannel.position(0);
      changeChannel.read(bb);
      changeBytes = bb.array(); // TODO check hasArray()
      
      changeChannel.close();
      changeChannel = null;
      changeFile = null;
    } else
      // truncate file
      changeChannel.truncate(size);
  }

  /**
   * try to convert stream to byte array WARNING: Potential lack of memory due
   * to call getAsByteArray() on stream data
   * 
   * @return byte array
   */
  private byte[] randFileToByteArray() throws IOException {
    // TODO use NIO, ByteBuffer from FileChannel. 
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    byte[] buffer = new byte[0x2000];
    int len;
    int total = 0;
    FileInputStream stream = new FileInputStream(changeFile);
    while ((len = stream.read(buffer)) > 0) {
      out.write(buffer, 0, len);
      total += len;
      if (log.isDebugEnabled() && total > maxBufferSize)
        log.warn("Potential lack of memory due to call getAsByteArray() on stream data exceeded "
                + total + " bytes");
    }
    out.close();
    return out.toByteArray();
  }

  @Override
  protected void finalize() throws Throwable {

    // here is destroying randFile
    if (changeFile != null) {
      changeChannel.close();
      log.debug("delete changeChannel");
      if (!changeFile.delete()) {
        if (fileCleaner != null) {
          log.info("Could not remove file. Add to fileCleaner " + changeFile);
          fileCleaner.addFile(changeFile);
        } else {
          log.warn("Could not remove temporary file on finalize, fileCleaner not found. "
              + changeFile.getAbsolutePath());
        }
      }
    }

    log.debug(" finalize "+this.toString());
    
    super.finalize();
  }
  
}
