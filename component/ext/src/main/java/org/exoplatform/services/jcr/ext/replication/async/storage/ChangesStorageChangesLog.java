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

import java.util.List;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.ext.replication.async.WorkspaceSynchronizerImpl.ItemStateIterableList;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 15.01.2009
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a> 
 * @version $Id$
 */
public class ChangesStorageChangesLog implements PlainChangesLog {

  private final ChangesStorage<ItemState> storage;

  private final String                    sessionId;

  public ChangesStorageChangesLog(ChangesStorage<ItemState> synchronizedChanges) {
    this.storage = synchronizedChanges;
    this.sessionId = IdGenerator.generate();
  }

  /**
   * {@inheritDoc}
   */
  public String getSessionId() {
    return sessionId;
  }

  public String dump() {
    return "Not implemented!";
  }

  public List<ItemState> getAllStates() {
    return new ItemStateIterableList<ItemState>(storage);
  }

  public int getSize() {
    throw new RuntimeException("Not implemented!");
  }

  /**
   * {@inheritDoc}
   */
  public PlainChangesLog add(ItemState state) {
    throw new RuntimeException("Not implemented!");
  }

  /**
   * {@inheritDoc}
   */
  public PlainChangesLog addAll(List<ItemState> states) {
    throw new RuntimeException("Not implemented!");
  }

  /**
   * {@inheritDoc}
   */
  public void clear() {
    throw new RuntimeException("Not implemented!");
  }

  /**
   * {@inheritDoc}
   */
  public int getEventType() {
    throw new RuntimeException("Not implemented!");
  }

}
