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

import org.exoplatform.services.jcr.ext.replication.async.AsyncHelper;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: ChangesFileComparator.java 111 2008-11-11 11:11:11Z serg $
 */
public class ChangesFileComparator<F extends File> implements Comparator<F> {

  /**
   * Helper.
   */
  private final AsyncHelper asyncHelper;

  /**
   * ChangesFileComparator constructor.
   */
  public ChangesFileComparator() {
    this.asyncHelper = new AsyncHelper();
  }

  public int compare(F o1, F o2) {
    return (int) (Long.parseLong(asyncHelper.removeInternalTag(o1.getName())) - Long.parseLong(asyncHelper.removeInternalTag(o2.getName())));
  }
}
