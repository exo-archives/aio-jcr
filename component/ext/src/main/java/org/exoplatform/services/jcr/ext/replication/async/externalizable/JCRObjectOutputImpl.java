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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.exoplatform.services.jcr.util.jcrexternalizable.JCRExternalizable;
import org.exoplatform.services.jcr.util.jcrexternalizable.JCRObjectOutput;
import org.exoplatform.services.jcr.util.jcrexternalizable.UnknownClassIdException;

/**
 * Created by The eXo Platform SAS. <br/>Date: 13.02.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: JCRObjectOutputImpl.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class JCRObjectOutputImpl implements JCRObjectOutput {

  private final OutputStream out;

  public JCRObjectOutputImpl(OutputStream out) {
    this.out = new BufferedOutputStream(out, 1024*2);
  }

  public void close() throws IOException {
    flush();
    out.close();
  }

  public void flush() throws IOException {
    out.flush();
  }

  public void write(byte[] b) throws IOException {
    out.write(b);
  }

  public void writeBoolean(boolean v) throws IOException {
    out.write(v ? 1 : 0);
  }

  public void writeInt(int v) throws IOException {
    out.write((v >>> 24) & 0xFF);
    out.write((v >>> 16) & 0xFF);
    out.write((v >>> 8) & 0xFF);
    out.write((v >>> 0) & 0xFF);
  }

  public void writeLong(long v) throws IOException {

    byte[] writeBuffer = new byte[8];
    writeBuffer[0] = (byte) (v >>> 56);
    writeBuffer[1] = (byte) (v >>> 48);
    writeBuffer[2] = (byte) (v >>> 40);
    writeBuffer[3] = (byte) (v >>> 32);
    writeBuffer[4] = (byte) (v >>> 24);
    writeBuffer[5] = (byte) (v >>> 16);
    writeBuffer[6] = (byte) (v >>> 8);
    writeBuffer[7] = (byte) (v >>> 0);
    out.write(writeBuffer, 0, 8);
  }

  public void writeObject(JCRExternalizable obj) throws IOException, UnknownClassIdException {
    writeInt(JCRExternlizableFactory.getObjectId(obj));
    obj.writeExternal(this);
  }

}
