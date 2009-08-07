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
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.exoplatform.services.jcr.impl.core.XASessionImpl;
import org.exoplatform.services.transaction.TransactionException;

/**
 * Created by The eXo Platform SAS.
 * <p/>
 * 
 * Manager provides consistency of transaction operations performed by same user but in different
 * Repository Sessions.
 * <p/>
 * 
 * Manager stores list of XASessions involved in transaction by a user and then can be used to
 * broadcast transaction start/commit/rollback to all live Sessions of the user.
 * <p/>
 * 
 * Broadcast of operations it's an atomic operation regarding to the Sessions list. Until operation
 * broadcast request is active other requests or list modifications will wait for.
 * <p/>
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class TransactionableResourceManager {

  /**
   * XASessions involved in transaction. Sessions stored by userId.
   */
  private ConcurrentMap<String, List<SoftReference<XASessionImpl>>> txManagers = new ConcurrentHashMap<String, List<SoftReference<XASessionImpl>>>(); // TransactionableDataManager

  /**
   * TransactionableResourceManager constructor.
   * 
   */
  public TransactionableResourceManager() {
  }

  /**
   * Add session to the transaction group.
   * 
   * @param userSession
   *          user XASession
   */
  public void add(XASessionImpl userSession) {
    final List<SoftReference<XASessionImpl>> joinedList = txManagers.get(userSession.getUserID());
    if (joinedList != null) {
      // remove unused session from user list and put this list at the end
      synchronized (joinedList) { // sync for comodifications from concurrent threads of same user
        for (Iterator<SoftReference<XASessionImpl>> siter = joinedList.iterator(); siter.hasNext();) {
          try {
            XASessionImpl xaSession = siter.next().get();
            if (xaSession == null || !xaSession.isLive())
              siter.remove();
          } catch (ConcurrentModificationException e) {
            e.printStackTrace();
            System.err.println("same user >>> " + e); // TODO
          }
        }

        joinedList.add(new SoftReference<XASessionImpl>(userSession));
      }

      // make sure the list is not removed by another Session of same user, see remove()
      txManagers.putIfAbsent(userSession.getUserID(), joinedList);
    } else {
      // sync for same userId operations
      final List<SoftReference<XASessionImpl>> newJoinedList = new ArrayList<SoftReference<XASessionImpl>>();
      final List<SoftReference<XASessionImpl>> previous = txManagers.putIfAbsent(userSession.getUserID(),
                                                                                 newJoinedList);
      if (previous != null)
        previous.add(new SoftReference<XASessionImpl>(userSession));
      else
        newJoinedList.add(new SoftReference<XASessionImpl>(userSession));
    }
  }

  /**
   * Remove session from user Sessions list.
   * 
   * @param userSession
   *          user XASession
   */
  public void remove(XASessionImpl userSession) {
    final List<SoftReference<XASessionImpl>> joinedList = txManagers.get(userSession.getUserID());
    if (joinedList != null) {
      // traverse and remove unused sessions and given one
      synchronized (joinedList) { // sync for comodifications from concurrent threads of same user
        for (Iterator<SoftReference<XASessionImpl>> siter = joinedList.iterator(); siter.hasNext();) {
          XASessionImpl xaSession = siter.next().get();
          if (xaSession == null || !xaSession.isLive() || xaSession == userSession)
            siter.remove();
        }

        // if list is empty - remove mapping to the list
        if (joinedList.size() <= 0)
          txManagers.remove(userSession.getUserID(), joinedList);
      }
    }
  }

  /**
   * Commit all sessions.
   * 
   * @param userSession
   *          commit initializing session
   * @throws TransactionException
   *           Transaction error
   */
  public void commit(XASessionImpl userSession) throws TransactionException {
    List<SoftReference<XASessionImpl>> joinedList = txManagers.remove(userSession.getUserID());
    if (joinedList != null)
      synchronized (joinedList) {
        for (SoftReference<XASessionImpl> sr : joinedList) {
          XASessionImpl xaSession = sr.get();
          if (xaSession != null && xaSession.isLive()) {
            TransactionableDataManager txManager = xaSession.getTransientNodesManager()
                                                            .getTransactManager();
            txManager.commit();
          }
        }
      }
  }

  /**
   * Start transaction on all sessions.
   * 
   * @param userSession
   *          start initializing session
   */
  public void start(XASessionImpl userSession) {
    List<SoftReference<XASessionImpl>> joinedList = txManagers.get(userSession.getUserID());
    if (joinedList != null)
      synchronized (joinedList) {
        for (SoftReference<XASessionImpl> sr : joinedList) {
          XASessionImpl xaSession = sr.get();
          if (xaSession != null && xaSession.isLive()) {
            TransactionableDataManager txManager = xaSession.getTransientNodesManager()
                                                            .getTransactManager();
            txManager.start();
          }
        }
      }
  }

  /**
   * Rollback transaction on all sessions.
   * 
   * @param userSession
   *          rollback initializing session
   */
  public void rollback(XASessionImpl userSession) {
    List<SoftReference<XASessionImpl>> joinedList = txManagers.remove(userSession.getUserID());
    if (joinedList != null)
      synchronized (joinedList) {
        for (SoftReference<XASessionImpl> sr : joinedList) {
          XASessionImpl xaSession = sr.get();
          if (xaSession != null && xaSession.isLive()) {
            TransactionableDataManager txManager = xaSession.getTransientNodesManager()
                                                            .getTransactManager();
            txManager.rollback();
          }
        }
      }
  }

}
