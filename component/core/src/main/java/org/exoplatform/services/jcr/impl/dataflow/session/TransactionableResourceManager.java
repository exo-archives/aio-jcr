/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.dataflow.session;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.services.jcr.impl.core.XASessionImpl;
import org.exoplatform.services.transaction.TransactionException;

/**
 * Created by The eXo Platform SARL
 * Author : Peter Nedonosko
 *          peter.nedonosko@exoplatform.com.ua
 * 07.08.2006
 * @version $Id: TransactionableResourceManager.java 12841 2007-02-16 08:58:38Z peterit $
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
        
        // TODO [PN] Check with real XA test and with agressive session logins
        userSessions.add(new SoftReference<XASessionImpl>(userSession));
        txManagers.put(userSession.getUserID(), userSessions);
      } 
      //joinedList.add(new SoftReference<XASessionImpl>(userSession));
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
