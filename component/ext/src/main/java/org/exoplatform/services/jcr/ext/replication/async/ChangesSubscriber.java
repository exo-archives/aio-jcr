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

import org.exoplatform.services.jcr.ext.replication.async.transport.ChangesPacket;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Store incoming changes. Takes packets on input and build changes file. When the file done
 * writes it to a <code>ChangesStorage</code>.
 * <br/>When all memebers changes will be received the storage calls <code>MergeDataManager.synchronize(ChangesStorage)</code>.
 * 
 * <br/>Date: 24.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public interface ChangesSubscriber extends SynchronizationListener {
  
  /**
   * Add packet.
   * 
   * The storage implementation will decide how to store the packet content.
   *
   * @param packet - ChangesPacket
   */
  void onChanges(ChangesPacket packet);

}
