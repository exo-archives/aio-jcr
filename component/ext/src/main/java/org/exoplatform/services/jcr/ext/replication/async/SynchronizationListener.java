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
public interface SynchronizationListener {

  /**
   * On sycnhronization Start event action. <br/> operations.
   * <ul>
   * <li>Publisher will send changes.</li>
   * <li>Subscriber will skip this event.</li>
   * </ul>
   * 
   */
  void onStart();
  /**
   * On sycnhronization Done event action. <br/>
   * <ul>
   * <li>Publisher will not send more changes.</li>
   * <li>Subscriber will start the merge of received changes on last active member done. All income
   * changes will be deleted after the merge will be done.</li>
   * </ul>
   * 
   */
  void onDone();

  /**
   * On sycnhronization Cancel event action. <br/> operations.
   * <ul>
   * <li>Publisher will not send more changes.</li>
   * <li>Subscriber will stops changes receive process or stops the merge of received changes. Then
   * all income changes will be deleted.</li>
   * </ul>
   * 
   */
  void onCancel();

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
  void onMembersDisconnected(List<Member> member);
}
