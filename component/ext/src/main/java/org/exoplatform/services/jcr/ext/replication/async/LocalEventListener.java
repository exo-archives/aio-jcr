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
/**
 * LocalEventListener.
 *
 */
public interface LocalEventListener {
  
  /**
   * On start send local changes to other members.
   * 
   * <ul>
   * <li>Publisher will start send changes.</li>
   * </ul>
   * 
   * @param members List of Members
   * 
   */
  void onStart(List<Member> members);

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
   * Fire synchronization done (stop local system).
   * <ul>
   * <li>Publisher will stop work, run local storage rotation and set Repository RW state.</li>
   * <li>Subscriber will stop work, run finalyzation (storage clean).</li>
   * </ul>
   * 
   */
  void onStop();
}
