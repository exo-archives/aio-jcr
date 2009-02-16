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
package org.exoplatform.services.jcr.ext.replication.async.externalizable;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.exoplatform.services.jcr.util.jcrexternalizable.JCRExternalizable;
import org.exoplatform.services.jcr.util.jcrexternalizable.JCRObjectInput;
import org.exoplatform.services.jcr.util.jcrexternalizable.UnknownClassIdException;

/**
 * Created by The eXo Platform SAS. <br/>Date: 13.02.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: JCRObjectInputImpl.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class JCRObjectInputImpl implements JCRObjectInput {

  private final InputStream in;

  public JCRObjectInputImpl(InputStream in) {
    this.in = new BufferedInputStream(in, 1024*2);
  }

  public void close() throws IOException {
    in.close();
  }

  public boolean readBoolean() throws IOException {
    return (in.read() == 1 ? true : false);
  }

  public void readFully(byte[] b) throws IOException {
    in.read(b);
  }

  public int readInt() throws IOException {
    int ch1 = in.read();
    int ch2 = in.read();
    int ch3 = in.read();
    int ch4 = in.read();
    if ((ch1 | ch2 | ch3 | ch4) < 0)
      throw new EOFException();
    return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
  }

  public long readLong() throws IOException {
    byte[] readBuffer = new byte[8];

    readFully(readBuffer);
    return (((long) readBuffer[0] << 56) + ((long) (readBuffer[1] & 255) << 48)
        + ((long) (readBuffer[2] & 255) << 40) + ((long) (readBuffer[3] & 255) << 32)
        + ((long) (readBuffer[4] & 255) << 24) + ((readBuffer[5] & 255) << 16)
        + ((readBuffer[6] & 255) << 8) + ((readBuffer[7] & 255) << 0));
  }

  public JCRExternalizable readObject() throws UnknownClassIdException, IOException {
    int type = readInt();
    JCRExternalizable objectInstants = JCRExternlizableFactory.getObjectInstanse(type);
    objectInstants.readExternal(this);

    return objectInstants;

  }
}
