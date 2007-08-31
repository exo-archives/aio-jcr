package org.exoplatform.services.jcr.impl.dataflow;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;

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

  public EditableValueData(byte[] bytes, int orderNumber, 
      FileCleaner fileCleaner, int maxBufferSize, File tempDirectory) {
    
    super(orderNumber);
    
    this.fileCleaner = fileCleaner;
    this.maxBufferSize = maxBufferSize;
    this.tempDirectory = tempDirectory;
    
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
    
    //this.changeChannel = new FileOutputStream(changeFile, false).getChannel();
    this.changeChannel = new RandomAccessFile(changeFile, "rw").getChannel();

    FileChannel spoolCh = new FileInputStream(spoolFile).getChannel();

    try {
      this.changeChannel.transferFrom(spoolCh, 0, spoolCh.size());
    } finally {
      //changeCh.close();
      spoolCh.close();
    }
  }
  
  protected int calcMaxIOSize() {
    return maxBufferSize < 1024 ? 1024 : maxBufferSize < (250 * 1024) ? maxBufferSize : 250 * 1024;
  }
  
  protected int calcBuffSize(long length) {
    int buffSize = (int) (length > maxIOBuffSize ? maxIOBuffSize : length / 4);
    buffSize = buffSize < 1024 ? 256 : buffSize;  
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

    if (changeFile != null)
      return fileToBytes(changeFile);
    
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
    if (changeFile != null) {
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
      // validation
      if (position >= changeBytes.length)
        this.setLength(position);
        //throw new IOException("Position out of range " + position + ". Current length " + changeBytes.length + " bytes. Use setLength() to extend value size.");  
        
      // merge stream content with existed bytes 
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      
      if (position > 0)
        // begin from the existed bytes
        bout.write(changeBytes, 0, (int) position);
      
      // TODO check the new size before and use file if the size will be greater the maxBufferSize
      int writen = 0;
      int i= -1;
      byte[] buff = new byte[calcBuffSize(length)];
      while ((i = stream.read(buff))>=0) {
        bout.write(buff, 0, i);
        writen += i;
      }
      
      int lastWriten = (int) position + writen;
      if (lastWriten < changeBytes.length)
        // TODO continue write content from the existed bytes 
        bout.write(changeBytes, lastWriten, changeBytes.length - lastWriten);
      
      buff = bout.toByteArray();
      //long newLength = changeBytes.length - position + length;
      
      if (buff.length <= maxBufferSize || maxBufferSize <= 0 || tempDirectory == null) {
        // edit bytes
        this.changeBytes = buff;
        
        this.changeFile = null;
        this.changeChannel = null;
      } else {
        // switch from bytes to file/channel
        this.changeFile = File.createTempFile("jcrvdedit", null, tempDirectory);
        
        //this.changeChannel = new FileOutputStream(changeFile, false).getChannel();
        this.changeChannel = new RandomAccessFile(changeFile, "rw").getChannel();

        ReadableByteChannel bch = Channels.newChannel(new ByteArrayInputStream(buff));
        this.changeChannel.transferFrom(bch, 0, buff.length);
        
        bch.close();
        
        this.changeBytes = null;
      }
    } else {
      // validation
      if (position >= changeChannel.size())
        // The exception for the contract only. Actualy we can do that with changeChannel.map()  
        //throw new IOException("Position out of range " + position + ". Current length " + changeChannel.size() + " bytes. Use setLength() to extend value size.");  
        this.setLength(position);
        
        
      // TODO test position + lebgth > current file size, i.e. extend size case
      MappedByteBuffer bb = changeChannel.map(FileChannel.MapMode.READ_WRITE, position, length);
        
      ReadableByteChannel ch = Channels.newChannel(stream);
      ch.read(bb);
      ch.close();
      
      bb.force();
      //long size = changeChannel.position(position + length).transferFrom(ch, position, length);
    }
  }
  
  /**
   * Set length to the binary value to <code> size </code>
   * 
   * @param size
   * @throws IOException
   */
  public void setLength(long size) throws IOException {
   
    if (isByteArray()) {    
      if (size < maxBufferSize || maxBufferSize <= 0 || tempDirectory == null) {
        // use bytes
        byte[] newBytes = new byte[(int) size];
        System.arraycopy(changeBytes, 0, newBytes, 0,
            (changeBytes.length < newBytes.length) ? changeBytes.length
                : newBytes.length);
        this.changeBytes = newBytes;
      } else {
        // switch from bytes to file/channel
        File chf = null;
        FileChannel cch = null;
        try {
          chf = File.createTempFile("jcrvdedit", null, tempDirectory);
          cch = new RandomAccessFile(chf, "rw").getChannel();
  
          ReadableByteChannel bch = Channels.newChannel(new ByteArrayInputStream(this.changeBytes));
          cch.transferFrom(bch, 0, this.changeBytes.length); // get all
          bch.close();
          
          if (cch.size() < size) {
            // extend length
            MappedByteBuffer bb = cch.map(FileChannel.MapMode.READ_WRITE, size, 0);
            bb.force();
          }
        } catch(final IOException e) {
          try {
            cch.close();
            chf.delete();
          } catch (Exception e1) {}
          throw new IOException("setLength(" + size + ") error. " + e.getMessage()) {
            @Override
            public Throwable getCause() {
              return e;
            }
          };
        }
        this.changeFile = chf; 
        this.changeChannel = cch; 
        this.changeBytes = null;
      } 
    } else if (size < maxBufferSize) {
      // truncate file, switch to a bytes
      ByteBuffer bb = ByteBuffer.allocate((int) size);
      changeChannel.force(false);
      changeChannel.position(0);
      changeChannel.read(bb);
      changeBytes = bb.array(); // TODO check hasArray()
      
      changeChannel.close();
      changeChannel = null;
      
      // delete file
      if (!changeFile.delete()) {
        if (fileCleaner != null) {
          log.info("Could not remove file. Add to fileCleaner " + changeFile);
          fileCleaner.addFile(changeFile);
        } else {
          log.warn("Could not remove temporary file on switch to bytes, fileCleaner not found. "
              + changeFile.getAbsolutePath());
        }
      }
      changeFile = null;
    } else {
      if (changeChannel.size() < size) {
        // extend file
        MappedByteBuffer bb = changeChannel.map(FileChannel.MapMode.READ_WRITE, size, 0);
        bb.force();        
      } else {
        // truncate file
        changeChannel.truncate(size);
      }
    }
  }

  private byte[] fileToBytes(File file) throws IOException {
    FileInputStream fs = new FileInputStream(file);
    
    //ByteArrayOutputStream bout = new ByteArrayOutputStream(); 
    int i= -1;
    byte[] bytes = new byte[(int) file.length()];
    int bi = 0;
    byte[] buff = new byte[maxBufferSize];
    while ((i = fs.read(buff))>=0) {
      for (int bbi = 0; bbi < i; bbi++)
        bytes[bi++] = buff[bbi];
      //bout.write(buff, 0, i);
    }
    
    return bytes;
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
