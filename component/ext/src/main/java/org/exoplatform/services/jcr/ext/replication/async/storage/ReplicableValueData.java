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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Calendar;

import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.datamodel.Identifier;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.JCRDateFormat;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.impl.util.io.SpoolFile;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: ReplicableValueData.java 111 2008-11-11 11:11:11Z serg $
 */
public class ReplicableValueData extends TransientValueData {

  private static final int DEF_MAX_BUF_SIZE = 2048; // 2kb

  public ReplicableValueData(int orderNumber,
                             byte[] bytes,
                             InputStream stream,
                             File spoolFile,
                             FileCleaner fileCleaner,
                             int maxBufferSize,
                             File tempDirectory,
                             boolean deleteSpoolFile) throws IOException {

    super(orderNumber,
          bytes,
          stream,
          spoolFile,
          fileCleaner,
          maxBufferSize,
          tempDirectory,
          deleteSpoolFile);

  }

  public ReplicableValueData(InputStream stream) {
    super(stream);
  }

  /**
   * Constructor for String value data
   * 
   * @param value
   */
  public ReplicableValueData(String value) {
    super(value);
  }

  /**
   * Constructor for boolean value data
   * 
   * @param value
   */
  public ReplicableValueData(boolean value) {
    super(value);
  }

  /**
   * Constructor for Calendar value data
   * 
   * @param value
   */
  public ReplicableValueData(Calendar value) {
    super(value);
  }

  /**
   * Constructor for double value data
   * 
   * @param value
   */
  public ReplicableValueData(double value) {
    super(value);
  }

  /**
   * Constructor for long value data
   * 
   * @param value
   */
  public ReplicableValueData(long value) {
    super(value);
  }

  /**
   * Constructor for Name value data
   * 
   * @param value
   */
  public ReplicableValueData(InternalQName value) {
    super(value);
  }

  /**
   * Constructor for Path value data
   * 
   * @param value
   */
  public ReplicableValueData(QPath value) {
    super(value);
  }

  /**
   * Constructor for Reference value data
   * 
   * @param value
   */
  public ReplicableValueData(Identifier value) {
    super(value);
  }

  /**
   * Constructor for Permission value data
   * 
   * @param value
   */
  public ReplicableValueData(AccessControlEntry value) {
    super(value);
  }

  public ReplicableValueData() {
    super();
  }

  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeInt(orderNumber);
    out.writeInt(maxBufferSize);

    // write data
    if (this.isByteArray()) {
      long f = data.length;
      out.writeLong(f);
      out.write(data);
    } else {
      long length = this.spoolFile.length();
      out.writeLong(length);
      InputStream in = new FileInputStream(spoolFile);
      byte[] buf = new byte[length > maxBufferSize ? maxBufferSize : (int) length];
      int l = 0;
      while ((l = in.read(buf)) != -1) {
        out.write(buf, 0, l);
      }
    }
  }

  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    this.orderNumber = in.readInt();
    this.maxBufferSize = in.readInt();

    long length = in.readLong();

    if (length > maxBufferSize) {
      // store data as file

      // TODO where store spool file
      SpoolFile sf = SpoolFile.createTempFile("jcrvd", null, tempDirectory);
      FileOutputStream sfout = new FileOutputStream(sf);

      byte[] buf = new byte[DEF_MAX_BUF_SIZE];

      sf.acquire(this);

      //int l = 0;
      for (; length >= DEF_MAX_BUF_SIZE; length -= DEF_MAX_BUF_SIZE) {
        in.readFully(buf);
        sfout.write(buf);
      }

      if (length > 0) {
        buf = new byte[(int)length];
        in.readFully(buf);
        sfout.write(buf);
      }
      
      sfout.close();

      this.spoolFile = sf;

    } else {
      // store data as bytearray
      this.data = new byte[(int) length];
      in.readFully(data);
    }
  }
}
