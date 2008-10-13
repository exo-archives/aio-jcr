/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.services.jcr.impl.storage.sdb;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 03.10.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public interface SDBConstants {

  /**
   * Item ID attribute name.
   */
  String ID                            = "ID";

  /**
   * Parent ID attribute name.
   */
  String PID                           = "PID";

  /**
   * Item name attribute name (not SimpleDB Item Name).
   */
  String NAME                          = "Name";

  /**
   * Item class (Node or Property) attribute name.
   */
  String ICLASS                        = "IClass";

  /**
   * Item data descriptor (IData) attribute name.
   */
  String IDATA                         = "IData";

  /**
   * Property data attribute name.
   */
  String DATA                          = "Data";

  /**
   * Node IClass value = 1.
   */
  String NODE_ICLASS                   = "1";

  /**
   * Property IClass value = 2.
   */
  String PROPERTY_ICLASS               = "2";

  /**
   * Item DELETED status mark for ID attribute.
   */
  String ITEM_DELETED_ID               = "D";

  /**
   * Value prefix for actual data stored in Property Data attribute.
   */
  char   VALUEPREFIX_DATA              = 'D';

  /**
   * Value prefix for storage-id stored in Property Data attribute.
   */
  char   VALUEPREFIX_STORAGEID         = 'S';

  /**
   * IData fields delimiter.
   */
  String IDATA_DELIMITER               = "|";

  /**
   * IData mixinType field key.
   */
  String IDATA_MIXINTYPE               = "NM";

  /**
   * IData ACL permission field key.
   */
  String IDATA_ACL_PERMISSION          = "AP";

  /**
   * IData ACL owner field key.
   */
  String IDATA_ACL_OWNER               = "AO";

  /**
   * SimpleDB Attribute value length maximum (1024 - 3 bytes).
   */
  long   SDB_ATTRIBUTE_VALUE_MAXLENGTH = 1021;

  /**
   * SimpleDB Maximum ItemName length (1024 bytes).
   */
  long   SDB_ITEMNAME_MAXLENGTH        = 1024;
  
  /**
   * Storage version SimpleDB Item name.
   */
  String STORAGE_VERSION_ID               = "$EXO_STORAGE_VERSION";
  
  /**
   * Storage version attribute.
   */
  String STORAGE_VERSION               = "Version";
  
  /**
   * Storage container name attribute.
   */
  String STORAGE_CONTAINER_NAME               = "Container";

}
