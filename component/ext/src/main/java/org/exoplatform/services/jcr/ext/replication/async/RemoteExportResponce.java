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
package org.exoplatform.services.jcr.ext.replication.async;

import org.exoplatform.services.jcr.ext.replication.async.storage.Member;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 12.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class RemoteExportResponce {

  public static final int FIRST  = 0;

  public static final int MIDDLE = 1;

  public static final int LAST   = 2;

  private final int       type;

  private final byte[]    buffer;

  private final long      offset;

  private final byte[]    crc;

  private final long      timeStamp;

  private final Member    member;

  RemoteExportResponce(Member member,
                       int type,
                       byte[] crc,
                       long timeStamp,
                       byte[] buffer,
                       long offset) {
    this.member = member;
    this.type = type;
    this.crc = crc;
    this.timeStamp = timeStamp;
    this.buffer = buffer;
    this.offset = offset;
  }

  /**
   * @return the member
   */
  public Member getMember() {
    return member;
  }

  public int getType() {
    return type;
  }

  public byte[] getCRC() {
    return crc;
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public byte[] getBuffer() {
    return buffer;
  }

  public long getOffset() {
    return offset;
  }
}
