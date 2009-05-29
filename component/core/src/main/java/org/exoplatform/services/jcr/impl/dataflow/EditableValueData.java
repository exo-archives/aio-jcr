package org.exoplatform.services.jcr.impl.dataflow;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.impl.util.io.FileCleaner;

public class EditableValueData extends TransientValueData {

  protected final int maxIOBuffSize;

  public EditableValueData(byte[] bytes,
                           int orderNumber,
                           FileCleaner fileCleaner,
                           int maxBufferSize,
                           File tempDirectory) throws IOException {

    // send bytes to super.<init>
    super(orderNumber, bytes, null, null, fileCleaner, maxBufferSize, tempDirectory, true);

    this.maxIOBuffSize = calcMaxIOSize();

    this.spooled = true;
  }

  public EditableValueData(File spoolFile,
                           int orderNumber,
                           FileCleaner fileCleaner,
                           int maxBufferSize,
                           File tempDirectory,
                           boolean deleteSpoolFile) throws IOException {

    // don't send any data there (no stream, no bytes)
    super(orderNumber, null, null, null, fileCleaner, maxBufferSize, tempDirectory, deleteSpoolFile);

    this.maxIOBuffSize = calcMaxIOSize();

    File sf = null;
    FileChannel sch = null;
    try {
      sf = File.createTempFile("jcrvdedit", null, tempDirectory);

      sch = new RandomAccessFile(sf, "rw").getChannel();

      FileChannel sourceCh = new FileInputStream(spoolFile).getChannel();
      try {
        sch.transferFrom(sourceCh, 0, sourceCh.size());
      } finally {
        sourceCh.close();
      }
    } catch (final IOException e) {
      try {
        sch.close();
        sf.delete();
      } catch (Exception e1) {
      }
      throw new IOException("init error " + e.getMessage()) {
        @Override
        public Throwable getCause() {
          return e;
        }
      };
    }

    this.data = null;

    this.spoolFile = sf;
    this.spoolChannel = sch;

    this.spooled = true;
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
      byte[] newBytes = new byte[data.length];
      System.arraycopy(data, 0, newBytes, 0, newBytes.length);
      return new TransientValueData(newBytes, orderNumber);
    } else {
      // stream, make a copy
      try {
        // force changes made to the file
        spoolChannel.force(false);

        InputStream thisStream = getAsStream();
        TransientValueData copy = new TransientValueData(orderNumber,
                                                         null,
                                                         thisStream,
                                                         null,
                                                         fileCleaner,
                                                         maxBufferSize,
                                                         tempDirectory,
                                                         true);
        copy.getSpoolFile(); // read now, till the source isn't changed
        thisStream.close();

        return copy;
      } catch (IOException e) {
        throw new RepositoryException("Create transient copy error. " + e, e);
      }
    }
  }

  /**
   * Update with <code>length</code> bytes from the specified <code>stream</code> to this value data
   * at <code>position</code>.
   * 
   * If <code>position</code> is lower 0 the IOException exception will be thrown.
   * 
   * If <code>position</code> is higher of current Value length the Value length will be increased
   * before to <code>position</code> size and <code>length</code> bytes will be added after the
   * <code>position</code>.
   * 
   * @param stream
   *          the data.
   * @param length
   *          the number of bytes from buffer to write.
   * @param position
   *          position in file to write data
   * 
   * @throws IOException
   */
  public void update(InputStream stream, long length, long position) throws IOException {

    if (position < 0)
      throw new IOException("Position must be higher or equals 0. But given " + position);

    if (length < 0)
      throw new IOException("Length must be higher or equals 0. But given " + length);

    if (isByteArray()) {
      // edit bytes
      // ...check length
      long updateSize = position + length;

      long newSize = updateSize > data.length ? updateSize : data.length;
      if ((newSize <= maxBufferSize && newSize <= Integer.MAX_VALUE) || maxBufferSize <= 0
          || tempDirectory == null) {
        // bytes
        byte[] newBytes = new byte[(int) newSize];

        int newIndex = 0; // first pos to write

        if ((newIndex = (int) position) > 0) {
          // begin from the existed bytes
          System.arraycopy(data, 0, newBytes, 0, newIndex < data.length ? newIndex : data.length);
        }

        // write new data
        int i = -1;
        boolean doRead = true;
        byte[] buff = new byte[calcBuffSize(length)];
        while (doRead && (i = stream.read(buff)) >= 0) {
          if (newIndex + i > newBytes.length) {
            // given length reached
            i = newBytes.length - newIndex;
            doRead = false;
          }
          System.arraycopy(buff, 0, newBytes, newIndex, i);
          newIndex += i;
        }

        if (newIndex < data.length)
          // write the rest of existed data
          System.arraycopy(data, newIndex, newBytes, newIndex, data.length - newIndex);

        this.data = newBytes;

        this.spoolFile = null;
        this.spoolChannel = null;

      } else {

        // switch from bytes to file/channel
        File chf = null;
        FileChannel chch = null;
        long newIndex = 0; // first pos to write

        try {
          chf = File.createTempFile("jcrvdedit", null, tempDirectory);
          chch = new RandomAccessFile(chf, "rw").getChannel();

          // allocate the space for whole file
          MappedByteBuffer bb = chch.map(FileChannel.MapMode.READ_WRITE, position + length, 0);
          bb.force();
          bb = null;

          ReadableByteChannel bch = Channels.newChannel(new ByteArrayInputStream(this.data));

          if ((newIndex = (int) position) > 0) {
            // begin from the existed bytes
            chch.transferFrom(bch, 0, newIndex < data.length ? newIndex : data.length);
            bch.close();
          }

          // write update data
          ReadableByteChannel sch = Channels.newChannel(stream);
          chch.transferFrom(sch, newIndex, length);
          sch.close();
          newIndex += length;

          if (newIndex < data.length)
            // write the rest of existed data
            chch.transferFrom(bch, newIndex, data.length - newIndex);

          bch.close();
        } catch (final IOException e) {
          try {
            chch.close();
            chf.delete();
          } catch (Exception e1) {
          }
          throw new IOException("update error " + e.getMessage()) {
            @Override
            public Throwable getCause() {
              return e;
            }
          };
        }
        this.spoolFile = chf;
        this.spoolChannel = chch;
        this.data = null;
      }
    } else {
      MappedByteBuffer bb = spoolChannel.map(FileChannel.MapMode.READ_WRITE, position, length);

      ReadableByteChannel ch = Channels.newChannel(stream);
      ch.read(bb);
      ch.close();

      bb.force();
    }
  }

  /**
   * Set length of the Value in bytes to the specified <code>size</code>.
   * 
   * If <code>size</code> is lower 0 the IOException exception will be thrown.
   * 
   * This operation can be used both for extend and for truncate the Value size.
   * 
   * This method used internally in update operation in case of extending the size to the given
   * position.
   * 
   * @param size
   * @throws IOException
   */
  public void setLength(long size) throws IOException {

    if (size < 0)
      throw new IOException("Size must be higher or equals 0. But given " + size);

    if (isByteArray()) {
      if (size < maxBufferSize || maxBufferSize <= 0 || tempDirectory == null) {
        // use bytes
        byte[] newBytes = new byte[(int) size];
        System.arraycopy(data, 0, newBytes, 0, (data.length < newBytes.length)
            ? data.length
            : newBytes.length);
        this.data = newBytes;
      } else {
        // switch from bytes to file/channel
        File chf = null;
        FileChannel chch = null;
        try {
          chf = File.createTempFile("jcrvdedit", null, tempDirectory);
          chch = new RandomAccessFile(chf, "rw").getChannel();

          ReadableByteChannel bch = Channels.newChannel(new ByteArrayInputStream(this.data));
          chch.transferFrom(bch, 0, this.data.length); // get all
          bch.close();

          if (chch.size() < size) {
            // extend length
            MappedByteBuffer bb = chch.map(FileChannel.MapMode.READ_WRITE, size, 0);
            bb.force();
          }
        } catch (final IOException e) {
          try {
            chch.close();
            chf.delete();
          } catch (Exception e1) {
          }
          throw new IOException("setLength(" + size + ") error. " + e.getMessage()) {
            @Override
            public Throwable getCause() {
              return e;
            }
          };
        }
        this.spoolFile = chf;
        this.spoolChannel = chch;
        this.data = null;
      }
    } else if (size < maxBufferSize) {
      // switch to bytes
      ByteBuffer bb = ByteBuffer.allocate((int) size);
      spoolChannel.force(false);
      spoolChannel.position(0);
      spoolChannel.read(bb);

      byte[] tmpb = null;

      if (bb.hasArray()) {
        tmpb = bb.array();
      } else {
        // impossible code in most cases, as we use heap backed buffer
        tmpb = new byte[bb.capacity()];
        bb.get(tmpb);
      }

      spoolChannel.close();

      // delete file
      if (!spoolFile.delete()) {
        if (fileCleaner != null) {
          log.info("Could not remove file. Add to fileCleaner " + spoolFile);
          fileCleaner.addFile(spoolFile);
        } else {
          log.warn("Could not remove temporary file on switch to bytes, fileCleaner not found. "
              + spoolFile.getAbsolutePath());
        }
      }

      data = tmpb;
      spoolChannel = null;
      spoolFile = null;
    } else {
      if (spoolChannel.size() < size) {
        // extend file
        MappedByteBuffer bb = spoolChannel.map(FileChannel.MapMode.READ_WRITE, size, 0);
        bb.force();
      } else {
        // truncate file
        spoolChannel.truncate(size);
      }
    }
  }
}
