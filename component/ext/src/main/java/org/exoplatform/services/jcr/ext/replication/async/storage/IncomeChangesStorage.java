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

import java.util.Arrays;

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

  /**
   * Storage owner member info.
   */
  protected final Member            member;

  public IncomeChangesStorage(ChangesStorage<T> income, Member member) {
    super(Arrays.asList(income.getChangesFile()));
    this.member = member;
  }

  /**
   * {@inheritDoc}
   */
  public Member getMember() {
    return member;
  }
}

