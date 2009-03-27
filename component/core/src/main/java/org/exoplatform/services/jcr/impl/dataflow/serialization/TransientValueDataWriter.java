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
package org.exoplatform.services.jcr.impl.dataflow.serialization;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.exoplatform.services.jcr.dataflow.serialization.ObjectWriter;
import org.exoplatform.services.jcr.dataflow.serialization.SerializationConstants;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: TransientValueDataWriter.java 111 2008-11-11 11:11:11Z serg $
 */
public class TransientValueDataWriter {

  /**
   * Write to stream all necessary object data.
   * 
   * @param out SerializationOutputStream.
   * @throws IOException If an I/O error has occurred.
   */
  public void write(ObjectWriter out, TransientValueData vd) throws IOException {
    // write id
    out.writeInt(SerializationConstants.TRANSIENT_VALUE_DATA);

    out.writeInt(vd.getOrderNumber());
    // out.writeInt(vd.maxBufferSize);//????

    boolean isByteArray = vd.isByteArray();
    out.writeBoolean(isByteArray);

    if (isByteArray) {
      byte[] data = vd.getAsByteArray();
      int f = data.length;
      out.writeInt(f);
      out.write(data);
    } else {

      // write property id - used for reread data optimization
      String id = IdGenerator.generate();
      out.writeString(id);

      // write file content
      // TODO optimize it, use channels
      File sf = vd.getSpoolFile();
      long length = sf.length();
      out.writeLong(length);
      InputStream in = new FileInputStream(vd.getSpoolFile());
      try {
        byte[] buf = new byte[200 * 1024];
        int l = 0;
        while ((l = in.read(buf)) != -1) {
          out.write(buf, 0, l);
        }
      } finally {
        in.close();
      }
    }
  }
}
