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

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date: 26.12.2008
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: CancelPacket.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class CancelPacket extends MessagePacket {

  public CancelPacket(int type, int transmitterPriority) {
    super(type, transmitterPriority);
  }

  /**
   * CancelPacket constructor.
   * 
   */
  public CancelPacket() {
    super();
  }

}
