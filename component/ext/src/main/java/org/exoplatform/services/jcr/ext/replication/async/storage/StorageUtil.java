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
package org.exoplatform.services.jcr.ext.replication.async.storage;

import java.io.File;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: StorageUtil.java 111 2008-11-11 11:11:11Z serg $
 */
public class StorageUtil {

  /**
   * Delete directory content and directory if <code>onlyContent</code> is
   * <code>true</code>.
   * 
   * @param directory to delete;
   * @param onlyContent delete only content if true or content and
   *          directory-container in other case;
   * @return true if all content deleted. Else if one or more file cant delete.
   */
  public static boolean deleteDirectory(File directory, boolean onlyContent) {
    boolean isOK = true;
    File[] subfiles = directory.listFiles();
    for (File f : subfiles) {
      if (f.isDirectory()) {
        isOK &= deleteDirectory(f, false);
      } else {
        isOK &= f.delete();
      }
    }

    if (!onlyContent) {
      isOK &= directory.delete();
    }
    return isOK;
  }

}
