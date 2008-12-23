/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.async.transport;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.exoplatform.services.jcr.impl.Constants;

/**
 * Created by The eXo Platform SAS
 * Author : Karpenko Sergiy
 *          karpenko.sergiy@gmail.com
 * 23 Ãðó 2008  
 */
public class GetExportPacket extends MessagePacket {
  
  private String nodeId;

  public GetExportPacket(String nodeId) {
    super(AsyncPacketTypes.GET_EXPORT_CHAHGESLOG);
    this.nodeId = nodeId;
  }

  public String getNodeId(){
    return nodeId;
  }
  
  /**
   * {@inheritDoc}
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
   
    if (nodeId != null) {
      byte[] buf = nodeId.getBytes(Constants.DEFAULT_ENCODING);
      out.writeInt(NOT_NULL_VALUE);
      out.writeInt(buf.length);
      out.write(buf);
    } else 
      out.writeInt(NULL_VALUE);
  }

  /**
   * {@inheritDoc}
   */
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
   super.readExternal(in);
   
    if (in.readInt() == NOT_NULL_VALUE) {
      int bufSize = in.readInt();
      byte[] buf = new byte[bufSize];
      in.readFully(buf);
      nodeId = new String(buf, Constants.DEFAULT_ENCODING);
    } else
      nodeId = null;
  }

}
