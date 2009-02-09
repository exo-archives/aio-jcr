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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id$
 */
public class IncomeChangesStorage<T extends ItemState> extends ChangesLogStorage<T> implements
    MemberChangesStorage<T> {

  protected static final Log        LOG = ExoLogger.getLogger("jcr.IncomeChangesStorage");

  protected final List<T> cache = new ArrayList<T>(); 
  
  /**
   * Storage owner member info.
   */
  protected final Member            member;

  public IncomeChangesStorage(ChangesStorage<T> income, Member member) {
    super(Arrays.asList(income.getChangesFile()));
    this.member = member;
  }

  private void preload() throws ClassCastException, IOException, ClassNotFoundException {
    for (Iterator<T> iter = super.getChanges(); iter.hasNext();)
      cache.add(iter.next());
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
    if (cache.size() <= 0)
      preload();

    final Iterator<T> iter = cache.iterator();
    
    return new Iterator<T>() {

      /**
       * {@inheritDoc}
       */
      public boolean hasNext() {
        return iter.hasNext();
      }

      /**
       * {@inheritDoc}
       */
      public T next() {
        return iter.next();
      }

      /**
       * {@inheritDoc}
       */
      public void remove() {
        throw new RuntimeException("Not implemented");
      }
    };
  }
  
  
}

