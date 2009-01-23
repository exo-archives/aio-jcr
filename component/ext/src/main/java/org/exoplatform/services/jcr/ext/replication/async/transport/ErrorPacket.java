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
 * This packet contains Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: ErrorPacket.java 111 2008-11-11 11:11:11Z serg $
 */
public class ErrorPacket extends MessagePacket {

  private String errorMessage;

  public ErrorPacket(int type, String message, int priority) {
    super(type, priority);
    this.errorMessage = message;
  }

  /**
   * ErrorPacket constructor.
   * 
   */
  ErrorPacket() {
    super();
  }

  public String getErrorMessage() {
    return this.errorMessage;
  }

  /**
   * {@inheritDoc}
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);

    if (errorMessage != null) {
      byte[] b = errorMessage.getBytes(Constants.DEFAULT_ENCODING);
      out.writeInt(NOT_NULL_VALUE);
      out.writeInt(b.length);
      out.write(b);
    } else {
      out.writeInt(NULL_VALUE);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    super.readExternal(in);

    if (in.readInt() == NOT_NULL_VALUE) {
      byte[] buf = new byte[in.readInt()];
      in.readFully(buf);
      errorMessage = new String(buf, Constants.DEFAULT_ENCODING);
    } else {
      errorMessage = null;
    }
  }
}
