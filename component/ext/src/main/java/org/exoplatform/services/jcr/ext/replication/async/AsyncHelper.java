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
package org.exoplatform.services.jcr.ext.replication.async;

import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.ext.replication.async.storage.LocalStorageImpl;
import org.exoplatform.services.jcr.impl.Constants;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: AsyncHelper.java 111 2008-11-11 11:11:11Z $
 */
public class AsyncHelper {

  /**
   * Remote internal tag from file name.
   * 
   * @param fileName
   *          file name
   * @return file name without tag
   */
  public String removeInternalTag(String fileName) {
    return fileName.endsWith(LocalStorageImpl.INTERNAL_CHANGES_FILE_TAG)
        ? fileName.substring(0, fileName.length()
            - LocalStorageImpl.INTERNAL_CHANGES_FILE_TAG.length())
        : fileName;
  }

  /**
   * Checking if item has fixed identifier.
   * 
   * @param identifier
   *          item identifier
   * @return true if item has fixed identifier and false in other case
   */
  public boolean isFixedIdentifier(String identifier) {
    return identifier.equals(Constants.ROOT_UUID) || identifier.equals(Constants.SYSTEM_UUID)
        || identifier.equals(Constants.VERSIONSTORAGE_UUID)
        || identifier.equals(Constants.NODETYPESROOT_UUID);
  }

  /**
   * Checking if item is a lock property.
   * 
   * @param name
   *          item name
   * @return true if item is a lock property and false in other case
   */
  public boolean isLockProperty(InternalQName name) {
    return name.equals(Constants.JCR_LOCKISDEEP) || name.equals(Constants.JCR_LOCKOWNER);
  }

}
