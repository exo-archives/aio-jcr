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
package org.exoplatform.services.jcr.ext.replication.async.storage;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id$
 */
public class IncomeChangesStorage<T extends ItemState> extends ChangesLogStorage<T> implements
    MemberChangesStorage<T> {

  protected static final Log       LOG   = ExoLogger.getLogger("jcr.IncomeChangesStorage");

  /**
   * On-read cache (see getChanges()).
   */
  protected SoftReference<List<T>> cache = new SoftReference<List<T>>(null);

  /**
   * Storage owner member info.
   */
  protected final Member           member;

  public IncomeChangesStorage(ChangesStorage<T> income, Member member,FileCleaner fileCleaner, int maxBufferSize) {
    super(Arrays.asList(income.getChangesFile()), fileCleaner, maxBufferSize);
    this.member = member;
  }

  /**
   * {@inheritDoc}
   */
  public Member getMember() {
    return member;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<T> getChanges() throws IOException, ClassCastException, ClassNotFoundException {
    // cache iterator, it's fixed and unchanged collection
    List<T> list = cache.get();
    if (list == null) {
      list = new ArrayList<T>();
      for (Iterator<T> iter = super.getChanges(); iter.hasNext();)
        list.add(iter.next());

      cache = new SoftReference<List<T>>(list);
    }

    return new ReadOnlyIterator<T>(list.iterator());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void delete() throws IOException {
    // TODO
//    List<T> list = cache.get();
//    if (list != null) {
//      list.clear();
//      cache.clear();
//    }
    
    super.delete();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int size() throws IOException, ClassNotFoundException {
    List<T> list = cache.get();
    if (list != null)
      return list.size();
    else
      return super.size();
  }

}
