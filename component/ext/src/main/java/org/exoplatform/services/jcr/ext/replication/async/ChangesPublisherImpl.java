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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.storage.LocalStorage;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 29.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: ChangesPublisherImpl.java 111 2008-11-11 11:11:11Z pnedonosko $
 */
public class ChangesPublisherImpl implements ChangesPublisher, SynchronizationEventListener,
    SynchronizationEventProducer {

  protected final AsyncTransmitter transmitter;

  protected final LocalStorage     storage;
  
  /**
   * Listeners in order of addition.
   */
  protected final Set<SynchronizationEventListener> listeners = new LinkedHashSet<SynchronizationEventListener>();

  public ChangesPublisherImpl(AsyncTransmitter transmitter, LocalStorage storage) {
    this.transmitter = transmitter;
    this.storage = storage;
  }

  public void sendChanges(List<Member> subscribers) {
    List<ChangesFile> changes = new ArrayList<ChangesFile>();
    // TODO fill list
    try {
      transmitter.sendChanges(changes, subscribers);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void onCancel(Member member) {
    // TODO Auto-generated method stub

  }

  public void onDisconnectMembers(List<Member> member) {
    // TODO Auto-generated method stub

  }

  public void onDone(Member member) {
    // TODO Auto-generated method stub

  }

  public void onStart(List<Member> members) {
    // TODO Auto-generated method stub
    sendChanges(null); // TODO
  }

  /**
   * {@inheritDoc}
   */
  public void addSynchronizationListener(SynchronizationEventListener listener) {
    // TODO Auto-generated method stub
    
  }

  /**
   * {@inheritDoc}
   */
  public void removeSynchronizationListener(SynchronizationEventListener listener) {
    // TODO Auto-generated method stub
    
  }
  
}
