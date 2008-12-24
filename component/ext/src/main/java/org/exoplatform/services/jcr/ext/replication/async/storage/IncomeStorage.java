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
   * Create empty <code>ChangesFile</code>. Will be used to save incoming changes (packets) by <code>ChangesPublisher</code>.
   *
   * @return ChangesFile
   */
  ChangesFile createChangesFile();
  
  /**
   * Add <code>ChangesFile</code> to a member (subscriber) storage.
   *
   * @param changes ChangesFile
   * @param memeber MemberAddress
   */
  void addMemeberChanges(int /*MemberAddress*/ memeber, ChangesFile changes);
  
  
  
}
