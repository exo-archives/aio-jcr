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

import java.io.IOException;

import org.exoplatform.services.jcr.ext.replication.async.storage.Member;
import org.exoplatform.services.jcr.ext.replication.async.storage.RandomChangesFile;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: IncomeDataContext.java 111 2008-11-11 11:11:11Z serg $
 */
public class IncomeDataContext {
  private final RandomChangesFile changesFile;

  private final Member            member;

  private final long              totalPackets;

  private long                    savedPackets;

  /**
   * Constructor.
   * 
   * @param changesFile
   *          file to store income changes;
   * @param member
   *          owner of income data;
   * @param totalPackets
   *          total packets count
   */
  public IncomeDataContext(RandomChangesFile changesFile, Member member, long totalPackets) {
    this.changesFile = changesFile;
    this.member = member;
    this.totalPackets = totalPackets;
    this.savedPackets = 0;
  }

  public RandomChangesFile getChangesFile() {
    return changesFile;
  }

  public Member getMember() {
    return member;
  }

  public void writeData(byte[] buf, long offset) throws IOException {
    changesFile.writeData(buf, offset);
    savedPackets++;
    if (savedPackets == totalPackets)
      changesFile.finishWrite();
  }

  public boolean isFinished() {
    return (savedPackets == totalPackets);
  }

}
