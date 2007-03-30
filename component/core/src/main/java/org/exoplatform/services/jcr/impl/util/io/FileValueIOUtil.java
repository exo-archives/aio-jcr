/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.ByteArrayPersistedValueData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.FileStreamPersistedValueData;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: FileValueIOUtil.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class FileValueIOUtil {
  
  public static ValueData readValue(File file, int orderNum, int maxBufferSize, boolean temp) throws IOException {

    FileInputStream is = new FileInputStream(file);
    FileChannel channel = is.getChannel();
    try {
      int size = (int) channel.size();
      
      if (size > maxBufferSize) {
        return new FileStreamPersistedValueData(file, orderNum, temp);
      } else {
        ByteBuffer buf = ByteBuffer.allocate(size);
        int numRead = channel.read(buf);
        byte[] arr = new byte[numRead]; // buf.position()
        buf.rewind();
        buf.get(arr);
        return new ByteArrayPersistedValueData(arr, orderNum);
      }
    } finally {
      channel.close();
      is.close();
    }
  }

  public static void writeValue(File file, ValueData value)  throws IOException {
    
    FileOutputStream out = new FileOutputStream(file);
    if (value.isByteArray()) {
      byte[] buff = value.getAsByteArray();
      out.write(buff);
    } else {
      byte[] buffer = new byte[0x2000];
      int len;
      InputStream in = value.getAsStream();
      while ((len = in.read(buffer)) > 0) {
        out.write(buffer, 0, len);
      }
    }
    out.close();

  }

}
