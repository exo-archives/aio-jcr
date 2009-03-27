/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.async.storage;

import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.AbstractValueData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.impl.util.io.SpoolFile;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: ReplicableValueData.java 111 2008-11-11 11:11:11Z serg $
 */
@Deprecated public class ReplicableValueData extends AbstractValueData implements Externalizable {

  protected static final Log                  LOG              = ExoLogger.getLogger("jcr.LocalStorageImpl");

  public static final String                  FILE_PREFIX      = "jcrrvd";

  private static final int                    DEF_MAX_BUF_SIZE = 20480;

  /**
   * Flag, if true BLOB data will be readed to spool file in readExternal, if false - empty virtual
   * file will be used.
   */
  protected static final ThreadLocal<Boolean> readBlobData     = new ThreadLocal<Boolean>();

  protected static FileCleaner                serviceCleaner;

  private final FileCleaner                   cleaner;

  private SpoolFile                           spoolFile;

  private boolean                             localFile        = false;

  public static void initFileCleaner(FileCleaner cleaner) {
    serviceCleaner = cleaner;
  }

  public static void setReadBlobData(boolean flag) {
    readBlobData.set(flag);
  }

  //
  // public static boolean isReadBlobData() {
  // final Boolean res = readBlobData.get();
  // return res != null ? res.booleanValue() : false;
  // }

  // TODO
  // public ReplicableValueData(byte[] data, int order) {
  // super(order);
  // this.spoolFile = null;
  // this.data = data;
  // this.cleaner = null;
  // }

  /**
   * ReplicableValueData constructor. Used on read-side, i.e. on Subscriber (local and remote)
   */
  public ReplicableValueData() {
    super(0);
    this.cleaner = serviceCleaner;
  }

  /**
   * 
   * ReplicableValueData constructor. Used in Local storage (creation side).
   * 
   * @param file
   *          SpoolFile
   * @param orderNumber
   *          int
   * @param cleaner
   *          FileCleaner
   * @throws IOException
   *           if I/O error occurs
   */
  public ReplicableValueData(SpoolFile file, int orderNumber, FileCleaner cleaner) throws IOException {
    super(orderNumber);
    this.spoolFile = file;

    this.cleaner = cleaner;

    this.spoolFile.acquire(this);
  }

  public SpoolFile getSpoolFile() {
    return spoolFile;
  }

  // /**
  // * Creates Replicable value data from InputStream.
  // *
  // * @param inputStream
  // * data
  // * @param orderNumber
  // * @throws IOException
  // */
  // public ReplicableValueData(InputStream inputStream, int orderNumber, FileCleaner cleaner)
  // throws IOException {
  // super(orderNumber);
  //
  // // this.cleaner = cleaner;
  //
  // // create spool file
  // byte[] tmpBuff = new byte[2048];
  // int read = 0;
  // int len = 0;
  // SpoolFile sf = null;
  // sf = new SpoolFile(File.createTempFile(FILE_PREFIX, null).getAbsolutePath());
  // sf.acquire(this);
  //
  // try {
  // OutputStream sfout = new FileOutputStream(sf);
  // try {
  // while ((read = inputStream.read(tmpBuff)) >= 0) {
  // // spool to temp file
  // sfout.write(tmpBuff, 0, read);
  // len += read;
  // }
  // } finally {
  // sfout.close();
  // }
  // } finally {
  // inputStream.close();
  // }
  //
  // // spooled to file
  // this.spoolFile = sf;
  // // this.data = null;
  // }

  /**
   * {@inheritDoc}
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeInt(orderNumber);

    out.writeBoolean(localFile);

    if (localFile) {
      // write file path
      byte[] cpath = spoolFile.getCanonicalPath().getBytes(Constants.DEFAULT_ENCODING);
      out.writeLong(cpath.length);
      out.write(cpath);
    } else {
      // write file content
      long length = this.spoolFile.length();
      out.writeLong(length);
      InputStream in = new FileInputStream(spoolFile);
      try {
        byte[] buf = new byte[length > DEF_MAX_BUF_SIZE ? DEF_MAX_BUF_SIZE : (int) length];
        int l = 0;
        while ((l = in.read(buf)) != -1) {
          out.write(buf, 0, l);
        }
      } finally {
        in.close();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    orderNumber = in.readInt();

    localFile = in.readBoolean();

    long length = in.readLong();

    // if (length >= DEF_MAX_BUF_SIZE) {
    // store data as file

    // ask if we have to read large data from Obj stream to a spool file
    // Boolean read = readBlobData.get();
    // if (read != null && read.booleanValue()) {

    if (localFile) {
      byte[] cpath = new byte[(int) length];
      in.readFully(cpath);

      SpoolFile sf = new SpoolFile(new String(cpath, Constants.DEFAULT_ENCODING));
      sf.acquire(this);
      this.spoolFile = sf;
    } else {
      SpoolFile sf = new SpoolFile(File.createTempFile(FILE_PREFIX, null).getAbsolutePath());
      FileOutputStream sfout = new FileOutputStream(sf);
      try {
        byte[] buff = new byte[DEF_MAX_BUF_SIZE];

        sf.acquire(this);

        // int l = 0;
        for (; length >= DEF_MAX_BUF_SIZE; length -= DEF_MAX_BUF_SIZE) {
          in.readFully(buff);
          sfout.write(buff);
        }

        if (length > 0) {
          buff = new byte[(int) length];
          in.readFully(buff);
          sfout.write(buff);
        }
      } finally {
        sfout.close();
      }

      this.spoolFile = sf;
      this.localFile = true;
    }

    // } else {
    // // store data as bytearray
    // byte[] buff = new byte[(int) length];
    // in.readFully(buff);
    //
    // this.spoolFile = null;
    // }
  }

  /**
   * {@inheritDoc}
   */
  public byte[] getAsByteArray() throws IOException {

    throw new IOException("This is Stream data.");

    // if (data != null) {
    // return data;
    // } else if (spoolFile != null) {
    //      
    // if (spoolFile.exists()) {
    // FileChannel fch = new FileInputStream(spoolFile).getChannel();
    //  
    // if (LOG.isDebugEnabled() && fch.size() > DEF_MAX_BUF_SIZE)
    // LOG.warn("Potential lack of memory due to call getAsByteArray() on stream data exceeded "
    // + fch.size() + " bytes");
    //  
    // try {
    // ByteBuffer bb = ByteBuffer.allocate((int) fch.size());
    // fch.read(bb);
    // if (bb.hasArray()) {
    // return bb.array();
    // } else {
    // // impossible code in most cases, as we use heap backed buffer
    // byte[] tmpb = new byte[bb.capacity()];
    // bb.get(tmpb);
    // return tmpb;
    // }
    // } finally {
    // fch.close();
    // }
    // } else
    // throw new IOException("Empty Stream data. Check readBlobData value.");
    // } else
    // throw new NullPointerException("Null Stream data ");
  }

  /**
   * {@inheritDoc}
   */
  public InputStream getAsStream() throws IOException {
    // if (data != null) {
    // return new ByteArrayInputStream(data);
    // } else if (spoolFile != null) {
    // if (spoolFile.exists())
    // return new FileInputStream(spoolFile);
    // else
    // throw new IOException("Empty Stream data. Check readBlobData value.");
    // } else
    // throw new NullPointerException("Null Stream data ");

    if (spoolFile.exists())
      return new FileInputStream(spoolFile);
    else
      throw new IOException("Empty Stream data. Check readBlobData value.");
  }

  /**
   * {@inheritDoc}
   */
  public long getLength() {
    return spoolFile.length();
  }

  /**
   * {@inheritDoc}
   */
  public boolean isByteArray() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TransientValueData createTransientCopy() throws RepositoryException {
    throw new RepositoryException("Not supported");

    // try {
    // spoolFile.release(this);
    // return new TransientValueData(orderNumber, null, null, spoolFile, null, -1, null, true);
    // } catch (IOException e) {
    // throw new RepositoryException(e);
    // }

    // try {
    // // array
    // if (isByteArray()) {
    // // bytes, make a copy of real data
    // // byte[] newBytes = new byte[data.length];
    // // System.arraycopy(data, 0, newBytes, 0, newBytes.length);
    // return new TransientValueData(orderNumber, data, null, null, null, -1, null, true);
    // } else {
    // // file
    // return new TransientValueData(orderNumber, null, null, spoolFile, null, -1, null, false);
    // }
    // } catch (IOException e) {
    // throw new RepositoryException(e);
    // }
  }

  /**
   * {@inheritDoc}
   */
  protected void finalize() throws Throwable {
    // same code as in TransientValueData (05.02.2009)
    if (spoolFile != null) {
      if (!spoolFile.inUse() && spoolFile.exists()) {
        if (!spoolFile.delete())
          if (cleaner != null) {
            log.info("Could not remove file. Add to fileCleaner " + spoolFile);
            cleaner.addFile(spoolFile);
          } else {
            log.warn("Could not remove temporary file on finalize " + spoolFile.getAbsolutePath());
          }
      }
    }
  }
}
