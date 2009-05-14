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

import org.exoplatform.services.jcr.dataflow.ItemState;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date: 29.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: EditableChangesStorage.java 111 2008-11-11 11:11:11Z pnedonosko $
 */
public interface EditableChangesStorage<T extends ItemState> extends MemberChangesStorage<T> {

  /**
   * Add change.
   * 
   * @param change
   *          ItemState
   */
  void add(T change) throws IOException;

  /**
   * Add all changes.
   * 
   * @param changes
   *          Iterator of ItemState
   */
  void addAll(ChangesStorage<T> changes) throws IOException;

}
