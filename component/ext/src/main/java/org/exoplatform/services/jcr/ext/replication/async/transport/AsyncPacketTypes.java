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
 * <br/>Date: 15.12.2008
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: AsyncPacketTypes.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public final class AsyncPacketTypes {

  /**
   * GET_EXPORT_CHAHGESLOG. 
   *   the pocket type for request export data
   */
  public static final int GET_EXPORT_CHAHGESLOG  = 0;

  /**
   * EXPORT_CHANGES_PACKET. 
   *   the pocket type for export data
   */
  public static final int EXPORT_CHANGES_PACKET  = 1;

  /**
   * CHANGESLOG_PACKET. 
   *   the pocket type for Changeslog
   */
  public static final int CHANGESLOG_PACKET      = 2;

  /**
   * SYNCHRONIZATION_CANCEL. 
   *   the pocket type to cancel synchronization
   */
  public static final int SYNCHRONIZATION_CANCEL = 3;

  /**
   * SYNCHRONIZATION_MERGE. 
   *   the pocket type to done synchronization merge
   */
  public static final int SYNCHRONIZATION_MERGE  = 4;

  /**
   * EXPORT_ERROR.
   *   the pocket type to error for export
   */
  public static final int EXPORT_ERROR           = 5;
}
