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
import java.util.List;

import org.exoplatform.services.jcr.dataflow.ItemState;


/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Stores all members (subscribers) changes. Can returns changes for a requested memeber.
 * 
 * <br/>Date: 24.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: IncomeStorage.java 111 2008-11-11 11:11:11Z pnedonosko $
 */
public interface IncomeStorage {

  /**
   * Create empty <code>ChangesFile</code>. Will be used to save incoming changes (packets) by
   * <code>ChangesSubscriber</code>. This method guaranties order of files in the storage. Files
   * will be stored in order of creation.
   * @param crc
   *          String
   * @param timeStamp
   *          long
   * 
   * @return ChangesFile
   */
  RandomChangesFile createChangesFile(String crc, long timeStamp, Member member) throws IOException;

  /**
   * Add <code>ChangesFile</code> to a member (subscriber) storage.
   * 
   * @param changes
   *          ChangesFile
   * @param member
   *          Member
   */
  void addMemberChanges(Member member, ChangesFile changes) throws IOException;

  /**
   * getChanges.
   * 
   * @return
   */
  List<MemberChangesStorage<ItemState>> getChanges() throws IOException;
  
  void clean() throws IOException;

}
