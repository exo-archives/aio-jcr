/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.services.jcr.impl.dataflow.session;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.services.jcr.impl.core.XASessionImpl;
import org.exoplatform.services.transaction.TransactionException;

/**
 * Created by The eXo Platform SAS
 * Author : Peter Nedonosko
 *          peter.nedonosko@exoplatform.com.ua
 * 07.08.2006
 * @version $Id$
 */
public class TransactionableResourceManager { //TransactionableResourceManager
  
  private Map<String, List<SoftReference<XASessionImpl>>> txManagers = new HashMap<String, List<SoftReference<XASessionImpl>>>(); //TransactionableDataManager
  
  public TransactionableResourceManager() {
  }
  
  public void join(XASessionImpl userSession) {
    
    final List<SoftReference<XASessionImpl>> joinedList = txManagers.get(userSession.getUserID());
    if (joinedList != null) {
      // remove unused session from user list and put this list back 
      synchronized (joinedList) {
        final List<SoftReference<XASessionImpl>> userSessions = new ArrayList<SoftReference<XASessionImpl>>(joinedList);
        final List<SoftReference<XASessionImpl>> removeList = new ArrayList<SoftReference<XASessionImpl>>();
        for (SoftReference<XASessionImpl> sr: userSessions) {        
          XASessionImpl xaSession = sr.get();
          if (xaSession == null || (xaSession != null && !xaSession.isLive()))
            removeList.add(sr);
        }
        for (SoftReference<XASessionImpl> sr: removeList) {
          userSessions.remove(sr);
        }
        
        userSessions.add(new SoftReference<XASessionImpl>(userSession));
        txManagers.put(userSession.getUserID(), userSessions);
      } 
    } else {
      final List<SoftReference<XASessionImpl>> newJoinedList = new ArrayList<SoftReference<XASessionImpl>>();
      newJoinedList.add(new SoftReference<XASessionImpl>(userSession));
      txManagers.put(userSession.getUserID(), newJoinedList);
    }
  }
  
  public void commit(XASessionImpl userSession) throws TransactionException {
    List<SoftReference<XASessionImpl>> joinedList = null;
    synchronized (txManagers) {
      joinedList = txManagers.remove(userSession.getUserID());
    }
    if (joinedList != null) {
      for (SoftReference<XASessionImpl> sr: joinedList) {
        XASessionImpl xaSession = sr.get();
        if (xaSession != null && xaSession.isLive()) {
          TransactionableDataManager txManager = xaSession.getTransientNodesManager().getTransactManager();
          txManager.commit();
        }
      }
    } 
  }
  
  public void start(XASessionImpl userSession) {
    List<SoftReference<XASessionImpl>> joinedList = null;
    synchronized (txManagers) {
      joinedList = txManagers.get(userSession.getUserID());
    }
    if (joinedList != null) {
      for (SoftReference<XASessionImpl> sr: joinedList) {
        XASessionImpl xaSession = sr.get();
        if (xaSession != null && xaSession.isLive()) {
          TransactionableDataManager txManager = xaSession.getTransientNodesManager().getTransactManager();
          txManager.start();
        }
      }
    } 
  }
  
  public void rollback(XASessionImpl userSession) {
    List<SoftReference<XASessionImpl>> joinedList = null;
    synchronized (txManagers) {
      joinedList = txManagers.get(userSession.getUserID());
    }
    if (joinedList != null) {
      for (SoftReference<XASessionImpl> sr: joinedList) {
        XASessionImpl xaSession = sr.get();
        if (xaSession != null && xaSession.isLive()) {
          TransactionableDataManager txManager = xaSession.getTransientNodesManager().getTransactManager();
          txManager.rollback();
        }
      }
    } 
  }
  
}
