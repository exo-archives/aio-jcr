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

import java.util.List;

import org.exoplatform.services.jcr.ext.replication.async.transport.Member;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 25.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public interface RemoteEventListener extends SynchronizationEventListener {

  /**
   * On sycnhronization merge done event action. <br/>
   * <ul>
   * <li>Publisher will not send more changes.</li>
   * <li>Subscriber will start the merge of received changes on last active member done.</li>
   * </ul>
   * 
   * @param member Member 
   */
  void onMerge(Member member);

  /**
   * Fire memebers disconnected event.<br/>
   * <ul>
   * <li>Publisher will not send more changes to a members.</li>
   * <li>Subscriber will start the merge without members changes, but with all members connected
   * changes received.</li>
   * </ul>
   * 
   * <br/>
   * 
   */
  void onDisconnectMembers(List<Member> member);
  
}
