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

import java.util.HashMap;
import java.util.List;

import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.transport.ChangesPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: ChangesSubscriberImpl.java 111 2008-11-11 11:11:11Z serg $
 */
public class ChangesSubscriberImpl implements ChangesSubscriber {

  /**
   * Map with CRC key and RandomAccess File
   */
  protected final HashMap<String, ChangesFile> incomChanges;
  
  protected final MergeDataManager mergeManager;

  public ChangesSubscriberImpl(MergeDataManager mergeManager) {
    this.incomChanges = new HashMap<String, ChangesFile>();
    this.mergeManager = mergeManager;
  }

  public void onChanges(ChangesPacket packet) {
    // TODO
  }

  /**
   * {@inheritDoc}
   */
  public void onCancel(Member member) {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public void onDone(Member member) {
    // TODO Auto-generated method stub

  }
  
  /**
   * {@inheritDoc}
   */
  public void onDisconnectMembers(List<Member> member) {
    // TODO Auto-generated method stub
    
  }

  public void onStart(List<Member> members) {
    // nothing to do
  }
  
  protected void merge() {
    
  }

}
