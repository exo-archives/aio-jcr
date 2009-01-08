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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.io.SpoolFile;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: ReplicableValueData.java 111 2008-11-11 11:11:11Z serg $
 */
public class ReplicableValueData extends TransientValueData {

  // private static final int BUF_SIZE = 2048; // 2 kb

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
    orderNumber = in.readInt();
    maxBufferSize = in.readInt();

    long length = in.readLong();

    if (length > maxBufferSize) {
      // store data as file
      
      SpoolFile sf = SpoolFile.createTempFile("jcrvd", null, tempDirectory);
      FileOutputStream sfout = new FileOutputStream(sf);
      
      byte[] buf = new byte[maxBufferSize];

      sf.acquire(this);
      int l=0;
      while((l=in.read(buf))!=-1){
        sfout.write(buf, 0, l );
      }
      
    } else {
      // store data as bytearray
      data = new byte[(int) length];
      for (int i = 0; i < data.length; i++)
        data[i] = in.readByte();
    }
  }
}
