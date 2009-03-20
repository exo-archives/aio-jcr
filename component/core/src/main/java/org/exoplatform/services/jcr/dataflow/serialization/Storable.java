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

import java.io.IOException;

/**
 * Created by The eXo Platform SAS. <br/>Date: 13.02.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: JCRExternalizable.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public interface Storable {

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

  /**
   * Read and set object data.
   * 
   * @param in ObjectReader.
   * @throws UnknownClassIdException If read Class ID is not expected or do not
   *           exist.
   * @throws IOException If an I/O error has occurred.
   */
  void readObject(ObjectReader in) throws UnknownClassIdException, IOException;

  /**
   * Write to stream all necessary object data.
   * 
   * @param out ObjectWriter.
   * @throws IOException If an I/O error has occurred.
   */
  void writeObject(ObjectWriter out) throws IOException;
}
