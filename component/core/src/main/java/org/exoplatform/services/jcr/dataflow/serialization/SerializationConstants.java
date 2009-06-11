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

import java.io.File;

/**
 * Created by The eXo Platform SAS. <br/>
 * Date: 13.02.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id$
 */
public class SerializationConstants {

  /**
   * Serialization temp dir.
   */
  public static final String TEMP_DIR                = System.getProperty("java.io.tmpdir")
                                                         + File.separator + "_jcrser.tmp";

  /**
   * TransientValueData class.
   */
  public static final int    TRANSIENT_VALUE_DATA    = 1;

  /**
   * AccessControlList class.
   */
  public static final int    ACCESS_CONTROL_LIST     = 2;

  /**
   * ItemState class.
   */
  public static final int    ITEM_STATE              = 3;

  /**
   * PlainChangesLogImpl class.
   */
  public static final int    PLAIN_CHANGES_LOG_IMPL  = 4;

  /**
   * TransactionChangesLog class.
   */
  public static final int    TRANSACTION_CHANGES_LOG = 5;

  /**
   * TransientNodeData class.
   */
  public static final int    TRANSIENT_NODE_DATA     = 7;

  /**
   * TransientPropertyData class.
   */
  public static final int    TRANSIENT_PROPERTY_DATA = 8;

  // flags

  public static final byte   NULL_DATA               = 0;

  public static final byte   NOT_NULL_DATA           = 1;

  /**
   * Serialization bytebuffer size.
   */
  public static final int    INTERNAL_BUFFER_SIZE    = 2048;
  
  static {
    try {
      File tempDirectory = new File(SerializationConstants.TEMP_DIR);
      tempDirectory.mkdirs();
    } catch (Throwable e) {
      // LOG to standard output
      System.err.println("[FATAL] " + e.getMessage());
      e.printStackTrace();
    }
  }
}
