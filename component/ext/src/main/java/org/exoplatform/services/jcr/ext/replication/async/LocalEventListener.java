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
package org.exoplatform.services.jcr.ext.replication.async;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date: 25.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public interface LocalEventListener extends SynchronizationEventListener {

  /**
   * Fire synchronization done (stop local system).
   * <ul>
   * <li>Publisher will stop data send.</li>
   * <li>LocalStorage will run storage rotation.</li>
   * <li>Subscriber will stop messages receive, stop merge if was started.</li>
   * <li>IncomeStorage will run storage clean.</li>
   * <li>AsyncInitializer will close channel.</li>
   * </ul>
   * 
   */
  void onStop();
}
