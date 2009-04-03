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
import java.util.Comparator;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: ChangesFileComparator.java 111 2008-11-11 11:11:11Z serg $
 */
public class ChangesFileComparator<F extends File> implements Comparator<F> {

  public int compare(F o1, F o2) {

    // long first = Long.parseLong(o1.getName());
    // long second = Long.parseLong(o2.getName());
    // if (first < second) {
    // return -1;
    // } else if (first == second) {
    // return 0;
    // } else {
    // return 1;
    // }

    String fileName1 = o1.getName().endsWith(LocalStorageImpl.INTERNAL_CHANGES_FILE_TAG)
        ? o1.getName().substring(0,
                                 o1.getName().length()
                                     - LocalStorageImpl.INTERNAL_CHANGES_FILE_TAG.length())
        : o1.getName();

    String fileName2 = o2.getName().endsWith(LocalStorageImpl.INTERNAL_CHANGES_FILE_TAG)
        ? o2.getName().substring(0,
                                 o2.getName().length()
                                     - LocalStorageImpl.INTERNAL_CHANGES_FILE_TAG.length())
        : o2.getName();

    return (int) (Long.parseLong(fileName1) - Long.parseLong(fileName2));
  }
}
