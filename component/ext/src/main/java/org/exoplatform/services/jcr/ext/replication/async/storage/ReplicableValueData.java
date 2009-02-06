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

import java.io.ByteArrayInputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
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
public class ReplicableValueData extends AbstractValueData implements Externalizable {

  protected static final Log LOG              = ExoLogger.getLogger("jcr.LocalStorageImpl");

  public static final String FILE_PREFIX      = "repValDat";

  private static final int   DEF_MAX_BUF_SIZE = 20480;                                      // 20kb

  // private final ResourcesHolder resHolder;

  private FileCleaner        cleaner;

  private byte[]             data;

  private File               spoolFile;

  // TODO
  // public ReplicableValueData(File file, int order) throws IOException {
  // super(order);
  // this.spoolFile = file;
  // if (spoolFile != null) {
  // if (spoolFile instanceof SpoolFile)
  // ((SpoolFile) spoolFile).acquire(this);
  // }
  // this.data = null;
  // }

  public ReplicableValueData(byte[] data, int order) {
    super(order);
    this.spoolFile = null;
    this.data = data;
    this.cleaner = null;
  }

  /**
   * ReplicableValueData constructor.
   * 
   */
  public ReplicableValueData() {
    super(0);
    this.cleaner = null;
  }

  /**
   * Creates Replicable value data from InputStream.
   * 
   * @param inputStream
   *          data
   * @param orderNumber
   * @throws IOException
   */
  public ReplicableValueData(InputStream inputStream, int orderNumber, FileCleaner cleaner) throws IOException {
    super(orderNumber);

    this.cleaner = cleaner;

    // create spool file
    byte[] tmpBuff = new byte[2048];
    int read = 0;
    int len = 0;
    SpoolFile sf = null;
    sf = new SpoolFile(File.createTempFile(FILE_PREFIX, null).getAbsolutePath());
    sf.acquire(this);

    try {
      OutputStream sfout = new FileOutputStream(sf);
      try {
        while ((read = inputStream.read(tmpBuff)) >= 0) {
          // spool to temp file
          sfout.write(tmpBuff, 0, read);
          len += read;
        }
      } finally {
        sfout.close();
      }
    } finally {
      inputStream.close();
    }

    // spooled to file
    this.spoolFile = sf;
    this.data = null;
  }

  /**
   * {@inheritDoc}
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeInt(orderNumber);

    // write data
    if (this.isByteArray()) {
      long f = data.length;
      out.writeLong(f);
      out.write(data);
    } else {
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
    this.orderNumber = in.readInt();

    long length = in.readLong();

    if (length > DEF_MAX_BUF_SIZE) {
      // store data as file

      SpoolFile sf = new SpoolFile(File.createTempFile(FILE_PREFIX, null).getAbsolutePath());
      FileOutputStream sfout = new FileOutputStream(sf);
      try {
        byte[] buf = new byte[DEF_MAX_BUF_SIZE];

        sf.acquire(this);

        // int l = 0;
        for (; length >= DEF_MAX_BUF_SIZE; length -= DEF_MAX_BUF_SIZE) {
          in.readFully(buf);
          sfout.write(buf);
        }

        if (length > 0) {
          buf = new byte[(int) length];
          in.readFully(buf);
          sfout.write(buf);
        }
      } finally {
        sfout.close();
      }

      this.spoolFile = sf;
    } else {
      // store data as bytearray
      this.data = new byte[(int) length];
      in.readFully(data);
    }
  }

  /**
   * {@inheritDoc}
   */
  public byte[] getAsByteArray() throws IllegalStateException, IOException {
    if (data != null) {
      return data;
    } else if (spoolFile != null) {
      FileChannel fch = new FileInputStream(spoolFile).getChannel();

      if (LOG.isDebugEnabled() && fch.size() > DEF_MAX_BUF_SIZE)
        LOG.warn("Potential lack of memory due to call getAsByteArray() on stream data exceeded "
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
    } else
      throw new NullPointerException("Null Stream data ");
  }

  /**
   * {@inheritDoc}
   */
  public InputStream getAsStream() throws IOException {
    if (data != null) {
      return new ByteArrayInputStream(data);
    } else if (spoolFile != null && spoolFile.exists()) {
      return new FileInputStream(spoolFile);
    } else
      throw new NullPointerException("Null Stream data ");
  }

  /**
   * {@inheritDoc}
   */
  public long getLength() {
    return (isByteArray() ? data.length : spoolFile.length());
  }

  /**
   * {@inheritDoc}
   */
  public boolean isByteArray() {
    return (data != null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TransientValueData createTransientCopy() throws RepositoryException {
    try {
      // array
      if (isByteArray()) {
        // bytes, make a copy of real data
        byte[] newBytes = new byte[data.length];
        System.arraycopy(data, 0, newBytes, 0, newBytes.length);
        return new TransientValueData(orderNumber, newBytes, null, null, null, -1, null, false);
      } else {
        // file
        return new TransientValueData(orderNumber, null, null, spoolFile, null, -1, null, false);
      }
    } catch (IOException e) {
      throw new RepositoryException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  protected void finalize() throws Throwable {
    // same code as in TransientValueData (05.02.2009)
    if (spoolFile != null) {
      if (spoolFile instanceof SpoolFile)
        ((SpoolFile) spoolFile).release(this);

      if (spoolFile.exists()) {
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
