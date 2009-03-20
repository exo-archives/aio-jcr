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
package org.exoplatform.services.jcr.impl.dataflow;

import java.io.ByteArrayInputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.Calendar;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.dataflow.serialization.ObjectReader;
import org.exoplatform.services.jcr.dataflow.serialization.ObjectWriter;
import org.exoplatform.services.jcr.dataflow.serialization.Storable;
import org.exoplatform.services.jcr.dataflow.serialization.UnknownClassIdException;
import org.exoplatform.services.jcr.datamodel.Identifier;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.util.JCRDateFormat;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.impl.util.io.SpoolFile;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SAS.<br/>
 * 
 * @author Gennady Azarenkov
 * @version $Id: TransientValueData.java 11907 2008-03-13 15:36:21Z ksm $
 */
public class TransientValueData extends AbstractValueData implements Externalizable, Storable {

  private static final long   serialVersionUID                 = -5280857006905550884L;

  private static final String DESERIALIAED_SPOOLFILES_TEMP_DIR = "_JCRVDtemp";

  private static final int    BYTE_ARRAY_DATA                  = 1;

  private static final int    STREAM_DATA                      = 2;

  protected byte[]            data;

  protected InputStream       tmpStream;

  protected File              spoolFile;

  protected final boolean     closeTmpStream;

  /**
   * User for read(...) method
   */
  protected FileChannel       spoolChannel;

  protected FileCleaner       fileCleaner;

  protected int               maxBufferSize;

  protected File              tempDirectory;

  protected boolean           spooled                          = false;

  private final boolean       deleteSpoolFile;

  /**
   * will be used for optimization unserialization mechanism.
   */
  // private String parentPropertyDataId;
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
  protected TransientValueData(byte[] value, int orderNumber) {
    super(orderNumber);
    this.data = value;
    this.deleteSpoolFile = true;
    this.closeTmpStream = false;
  }

  /**
   * creates TransientValueData with incoming input stream. the stream will be
   * lazily spooled to file or byte array depending on maxBufferSize
   * 
   * @param orderNumber
   */
  protected TransientValueData(InputStream stream, int orderNumber) {
    super(orderNumber);
    this.tmpStream = stream;
    this.deleteSpoolFile = true;
    this.closeTmpStream = false;
  }

  public TransientValueData(int orderNumber,
                            byte[] bytes,
                            InputStream stream,
                            File spoolFile,
                            FileCleaner fileCleaner,
                            int maxBufferSize,
                            File tempDirectory,
                            boolean deleteSpoolFile) throws IOException {

    super(orderNumber);
    this.data = bytes;
    this.tmpStream = stream;
    this.closeTmpStream = true;
    this.spoolFile = spoolFile;
    this.fileCleaner = fileCleaner;
    this.maxBufferSize = maxBufferSize;
    this.tempDirectory = tempDirectory;
    this.deleteSpoolFile = deleteSpoolFile;

    if (spoolFile != null) {
      if (spoolFile instanceof SpoolFile)
        ((SpoolFile) spoolFile).acquire(this);

      if (this.tmpStream != null) {
        this.tmpStream.close();
        this.tmpStream = null; // 05.02.2009 release stream if file exists
      }

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
    this(new JCRDateFormat().serialize(value), 0);
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
    this(stringToBytes(value.getAsString()), 0);
  }

  /**
   * Constructor for Path value data
   * 
   * @param value
   */
  public TransientValueData(QPath value) {
    this(stringToBytes(value.getAsString()), 0);
  }

  /**
   * Constructor for Reference value data
   * 
   * @param value
   */
  public TransientValueData(Identifier value) {
    this(value.getString().getBytes(), 0);
  }

  /**
   * Constructor for Permission value data
   * 
   * @param value
   */
  public TransientValueData(AccessControlEntry value) {
    this(stringToBytes(value.getAsString()), 0);
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
      return new FileInputStream(spoolFile); // from spool file if
      // initialized
    } else
      throw new NullPointerException("Null Stream data ");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.datamodel.ValueData#getLength()
   */
  public long getLength() {
    spoolInputStream();

    if (data == null) {
      log.debug("getLength spoolFile : " + spoolFile.length());
      return spoolFile.length();
    } else {
      log.debug("getLength data : " + data.length);
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
    spoolInputStream();

    return data != null;
  }

  @Override
  public TransientValueData createTransientCopy() throws RepositoryException {
    if (isByteArray()) {
      // bytes, make a copy of real data
      byte[] newBytes = new byte[data.length];
      System.arraycopy(data, 0, newBytes, 0, newBytes.length);

      // be more precise if this is a binary but so small, but can be increased
      // in EditableValueData
      try {
        return new TransientValueData(orderNumber,
                                      newBytes,
                                      null,
                                      null,
                                      fileCleaner,
                                      maxBufferSize,
                                      tempDirectory,
                                      deleteSpoolFile);
      } catch (IOException e) {
        throw new RepositoryException(e);
      }
    } else {
      // spool file, i.e. shared across sessions
      return this;
    }
  }

  public EditableValueData createEditableCopy() throws RepositoryException {
    if (isByteArray()) {
      // bytes, make a copy of real data
      byte[] newBytes = new byte[data.length];
      System.arraycopy(data, 0, newBytes, 0, newBytes.length);

      try {
        return new EditableValueData(newBytes,
                                     orderNumber,
                                     fileCleaner,
                                     maxBufferSize,
                                     tempDirectory);
      } catch (IOException e) {
        throw new RepositoryException(e);
      }
    } else {
      // edited BLOB file, make a copy
      try {
        EditableValueData copy = new EditableValueData(spoolFile,
                                                       orderNumber,
                                                       fileCleaner,
                                                       maxBufferSize,
                                                       tempDirectory);
        return copy;
      } catch (FileNotFoundException e) {
        throw new RepositoryException("Create transient copy error. " + e, e);
      } catch (IOException e) {
        throw new RepositoryException("Create transient copy error. " + e, e);
      }
    }
  }

  /**
   * Read <code>length</code> bytes from the binary value at
   * <code>position</code> to the <code>stream</code>.
   * 
   * @param stream - destenation OutputStream
   * @param length - data length to be read
   * @param position - position in value data from which the read will be
   *          performed
   * @return - The number of bytes, possibly zero, that were actually
   *         transferred
   * @throws IOException
   * @throws RepositoryException
   */
  public long read(OutputStream stream, long length, long position) throws IOException {

    if (position < 0)
      throw new IOException("Position must be higher or equals 0. But given " + position);

    if (length < 0)
      throw new IOException("Length must be higher or equals 0. But given " + length);

    spoolInputStream();

    if (isByteArray()) {
      // validation
      if (position >= data.length && position > 0)
        throw new IOException("Position " + position + " out of value size " + data.length);

      if (position + length >= data.length)
        length = data.length - position;

      stream.write(data, (int) position, (int) length);

      return length;
    } else {
      if (spoolChannel == null)
        spoolChannel = new FileInputStream(spoolFile).getChannel();

      // validation
      if (position >= spoolChannel.size() && position > 0)
        throw new IOException("Position " + position + " out of value size " + spoolChannel.size());

      if (position + length >= spoolChannel.size())
        length = spoolChannel.size() - position;

      MappedByteBuffer bb = spoolChannel.map(FileChannel.MapMode.READ_ONLY, position, length);

      WritableByteChannel ch = Channels.newChannel(stream);
      ch.write(bb);
      ch.close();

      return length;
    }
  }

  /**
   * @return spool file if any
   */
  public File getSpoolFile() {
    spoolInputStream();

    return spoolFile;
  }

  /**
   * helper method to simplify operations that requires stringified data
   * 
   * @return
   * @throws IOException
   */
  public String getString() throws IOException {
    if (log.isDebugEnabled())
      log.debug("getString");

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

  /**
   * {@inheritDoc}
   */
  protected void finalize() throws Throwable {
    if (spoolChannel != null)
      spoolChannel.close();

    if (spoolFile != null) {

      if (spoolFile instanceof SpoolFile)
        ((SpoolFile) spoolFile).release(this);

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

  // ///////////////////////////////////

  private void spoolInputStream() {

    if (spooled || tmpStream == null) // already spooled
      return;

    byte[] buffer = new byte[0];
    byte[] tmpBuff = new byte[2048];
    int read = 0;
    int len = 0;
    SpoolFile sf = null;
    OutputStream sfout = null;

    try {
      while ((read = tmpStream.read(tmpBuff)) >= 0) {
        if (sfout != null) {
          // spool to temp file
          sfout.write(tmpBuff, 0, read);
          len += read;
        } else if (len + read > maxBufferSize && fileCleaner != null) {
          // threshold for keeping data in memory exceeded,
          // if have a fileCleaner create temp file and spool buffer contents.
          sf = SpoolFile.createTempFile("jcrvd", null, tempDirectory);
          sf.acquire(this);

          sfout = new FileOutputStream(sf);
          sfout.write(buffer, 0, len);
          sfout.write(tmpBuff, 0, read);
          buffer = null;
          len += read;
        } else {
          // reallocate new buffer and spool old buffer contents
          byte[] newBuffer = new byte[len + read];
          System.arraycopy(buffer, 0, newBuffer, 0, len);
          System.arraycopy(tmpBuff, 0, newBuffer, len, read);
          buffer = newBuffer;
          len += read;
        }
      }

      if (sf != null) {
        // spooled to file
        this.spoolFile = sf;
        this.data = null;
      } else {
        // ...bytes
        this.spoolFile = null;
        this.data = buffer;
      }

      this.spooled = true;
    } catch (IOException e) {
      throw new IllegalStateException(e);
    } finally {
      try {
        if (sfout != null)
          sfout.close();
      } catch (IOException e) {
        log.error("Error of spool output close.", e);
      }

      if (this.closeTmpStream)
        try {
          this.tmpStream.close();
        } catch (IOException e) {
          log.error("Error of source input close.", e);
        }
      this.tmpStream = null;
    }
  }

  /**
   * try to convert stream to byte array WARNING: Potential lack of memory due
   * to call getAsByteArray() on stream data
   * 
   * @return byte array
   */
  private byte[] fileToByteArray() throws IOException {
    FileChannel fch = new FileInputStream(spoolFile).getChannel();

    if (log.isDebugEnabled() && fch.size() > maxBufferSize)
      log.warn("Potential lack of memory due to call getAsByteArray() on stream data exceeded "
          + fch.size() + " bytes");

    try {
      ByteBuffer bb = ByteBuffer.allocate((int) fch.size());
      fch.read(bb);
      if (bb.hasArray()) {
        return bb.array();
      } else {
        // impossible code in most cases, as we use heap backed buffer
        byte[] tmpb = new byte[bb.capacity()];
        bb.get(tmpb);
        return tmpb;
      }
    } finally {
      fch.close();
    }
  }

  // ------------- Serializable

  public TransientValueData() {
    super(0);
    this.deleteSpoolFile = true;
    this.closeTmpStream = true;
  }

  public void writeExternal(ObjectOutput out) throws IOException {
    if (this.isByteArray()) {
      out.writeInt(1);
      int f = data.length;
      out.writeInt(f);
      out.write(data);
    } else {
      out.writeInt(2);
    }
    out.writeInt(orderNumber);
    out.writeInt(maxBufferSize);
  }

  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    int type = in.readInt();

    if (type == 1) {
      data = new byte[in.readInt()];
      in.readFully(data);
    }
    orderNumber = in.readInt();
    maxBufferSize = in.readInt();
  }

  public void setStream(InputStream in) {
    this.spooled = false;
    this.tmpStream = in;
  }

  /**
   * {@inheritDoc}
   */
  public void readObject(ObjectReader in) throws UnknownClassIdException, IOException {

    if (tempDirectory == null) {
      this.tempDirectory = new File(System.getProperty("java.io.tmpdir") + "/"
          + TransientValueData.DESERIALIAED_SPOOLFILES_TEMP_DIR);
      this.tempDirectory.mkdirs();
    }

    // read id
    int key;
    if ((key = in.readInt()) != Storable.TRANSIENT_VALUE_DATA) {
      throw new UnknownClassIdException("There is unexpected class [" + key + "]");
    }

    orderNumber = in.readInt();
    maxBufferSize = in.readInt();

    int type = in.readInt();

    if (type == BYTE_ARRAY_DATA) {
      data = new byte[in.readInt()];
      in.readFully(data);
    } else if (type == STREAM_DATA) {

      // read property id - used for reread data optimization
      String id = in.readString();

      // read file
      long length = in.readLong();

      SpoolFile sf = new SpoolFile(this.tempDirectory, id);

      if (sf.exists()) {
        // skip data in input stream
        if (in.skip(length) != length) {
          throw new IOException("Content isn't skipped correctly.");
        }
      } else {
        // TODO optimize it - use channels or streams
        writeToFile(in, sf, length);
      }

      sf.acquire(this);
      this.spoolFile = sf;
    }
  }

  /**
   * {@inheritDoc}
   */
  public void writeObject(ObjectWriter out) throws IOException {
    // write id
    out.writeInt(Storable.TRANSIENT_VALUE_DATA);

    out.writeInt(orderNumber);
    out.writeInt(maxBufferSize);

    if (this.isByteArray()) {
      out.writeInt(BYTE_ARRAY_DATA);
      int f = data.length;
      out.writeInt(f);
      out.write(data);
    } else {
      out.writeInt(STREAM_DATA);

      // write property id - used for reread data optimization
      String id = IdGenerator.generate();
      out.writeString(id);

      // write file content
      // TODO optimize it, use channels
      long length = this.spoolFile.length();
      out.writeLong(length);
      InputStream in = new FileInputStream(spoolFile);
      try {
        byte[] buf = new byte[200*1024];
        int l = 0;
        while ((l = in.read(buf)) != -1) {
          out.write(buf, 0, l);
        }
      } finally {
        in.close();
      }
    }
  }

  private void writeToFile(ObjectReader src, SpoolFile dest, long length) throws IOException {
    // write data to file
    FileOutputStream sfout = new FileOutputStream(dest);
    int bSize = 200*1024;
    try {
      byte[] buff = new byte[bSize];
      for (; length >= bSize; length -= bSize) {
        src.readFully(buff);
        sfout.write(buff);
      }

      if (length > 0) {
        buff = new byte[(int) length];
        src.readFully(buff);
        sfout.write(buff);
      }
    } finally {
      sfout.close();
    }
  }
}
