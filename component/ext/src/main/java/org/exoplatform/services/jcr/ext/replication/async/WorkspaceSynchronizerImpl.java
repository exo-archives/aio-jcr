/**
 * 
 */
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.LocalStorage;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;
import org.exoplatform.services.jcr.impl.Constants;

/**
 * Created by The eXo Platform SAS. <br/>Date: 12.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class WorkspaceSynchronizerImpl implements WorkspaceSynchronizer, ChangesPublisher {


  protected final AsyncTransmitter    transmitter;

  protected final LocalStorage        storage;

  protected final DataManager         dataManager;

  protected final NodeTypeDataManager ntManager;

  public WorkspaceSynchronizerImpl(
                               AsyncTransmitter transmitter,
                               LocalStorage storage,
                               DataManager dataManager,
                               NodeTypeDataManager ntManager) {
    this.transmitter = transmitter;
    this.storage = storage;
    this.dataManager = dataManager;
    this.ntManager = ntManager;
  }

  /**
   * Return local changes.<br/> 1. to a merger<br/> 2. to a receiver
   * 
   * @return ChangesStorage
   */
  public ChangesStorage getLocalChanges() {
    return storage.getLocalChanges();
  }
  
  /**
   * {@inheritDoc}
   */
  public void save(ChangesStorage synchronizedChanges) {
    // TODO Auto-generated method stub
    
  }

  /**
   * {@inheritDoc}
   */
  public void sendChanges(List<Member> subscribers) {
    
    List<ChangesFile> changes = new ArrayList<ChangesFile>();
    // TODO fill list
    
    transmitter.sendChanges(changes, subscribers);
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

  public void onStart(List<Member> member) {
    // TODO Auto-generated method stub
    
  }
  
}
