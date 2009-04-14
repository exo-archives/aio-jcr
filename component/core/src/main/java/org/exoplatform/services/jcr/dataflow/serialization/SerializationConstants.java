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
package org.exoplatform.services.jcr.dataflow.serialization;

/**
 * Created by The eXo Platform SAS. <br/>Date: 13.02.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: JCRExternalizable.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class SerializationConstants {

  /**
   * TransientValueData class.
   */
  public static final int TRANSIENT_VALUE_DATA    = 1;

  /**
   * AccessControlList class.
   */
  public static final int ACCESS_CONTROL_LIST     = 2;

  /**
   * ItemState class.
   */
  public static final int ITEM_STATE              = 3;

  /**
   * PlainChangesLogImpl class.
   */
  public static final int PLAIN_CHANGES_LOG_IMPL  = 4;

  /**
   * TransactionChangesLog class.
   */
  public static final int TRANSACTION_CHANGES_LOG = 5;

  /**
   * TransientNodeData class.
   */
  public static final int TRANSIENT_NODE_DATA     = 7;

  /**
   * TransientPropertyData class.
   */
  public static final int TRANSIENT_PROPERTY_DATA = 8;

  // flags

  public static final int NULL_DATA               = 0;

  public static final int NOT_NULL_DATA           = 1;

  /**
   * Serialization bytebuffer size.
   */
  public static final int INTERNAL_BUFFER_SIZE    = 2 * 1048;
}
