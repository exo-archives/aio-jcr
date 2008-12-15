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
  public static final int GET_EXPORT_CHAHGESLOG                = 0;

  /**
   * EXPORT_CHANGES_FIRST_PACKET. 
   *   the pocket type for first packet to export data
   */
  public static final int EXPORT_CHANGES_FIRST_PACKET          = 1;

  /**
   * EXPORT_CHANGES_MIDDLE_PACKET. 
   *   the pocket type for middle packet to export data
   */
  public static final int EXPORT_CHANGES_MIDDLE_PACKET         = 2;

  /**
   * EXPORT_CHANGES_LAST_PACKET. 
   *   the pocket type for last packet to export data
   */
  public static final int EXPORT_CHANGES_LAST_PACKET           = 3;

  /**
   * GET_CHANGESLOG_UP_TO_DATE. 
   *   the pocket type for initialize synchronization mechanism
   */
  public static final int GET_CHANGESLOG_UP_TO_DATE            = 4;

  /**
   * NEED_TRANSFER_COUNTER. 
   *   the pocket type for information of how much ChangesLogs will be transfered
   */
  public static final int NEED_TRANSFER_COUNTER                = 5;

  /**
   * INIT_TRANSFER_CHANGES.
   */
  public static final int INIT_TRANSFER_CHANGES                = 7;

  /**
   * DONE_TRANSFER_CHANGES.
   */
  public static final int DONE_TRANSFER_CHANGES                = 8;

  /**
   * BINARY_CHANGESLOG_FIRST_PACKET. 
   *   the pocket type for first packet to binary Changeslog
   */
  public static final int BINARY_CHANGESLOG_FIRST_PACKET       = 9;

  /**
   * BINARY_CHANGESLOG_MIDDLE_PACKET. 
   *   the pocket type for middle packet to binary Changeslog
   */
  public static final int BINARY_CHANGESLOG_MIDDLE_PACKET      = 10;

  /**
   * BINARY_CHANGESLOG_LAST_PACKET. 
   *   the pocket type for last packet to binary Changeslog
   */
  public static final int BINARY_CHANGESLOG_LAST_PACKET        = 11;

  /**
   * GET_STATE_NODE.
   */
  public static final int GET_STATE_NODE                       = 12;

  /**
   * STATE_NODE.
   */
  public static final int STATE_NODE                           = 13;
  
  /**
   * BIG_PACKET_FIRST.
   *   the pocket type for first packet to binary Changeslog
   *   (using for recovery)
   */
  public static final int BIG_PACKET_FIRST                     = 14;

  /**
   * BIG_PACKET_MIDDLE.
   *   the pocket type for middle packet to binary Changeslog
   *   (using for recovery)
   */
  public static final int BIG_PACKET_MIDDLE                    = 15;

  /**
   * BIG_PACKET_LAST.
   *   the pocket type for last packet to binary Changeslog
   *   (using for recovery)
   */
  public static final int BIG_PACKET_LAST                      = 16;
}
