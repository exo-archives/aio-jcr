/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.load.blob.thread;

import javax.jcr.InvalidItemStateException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.impl.core.PropertyImpl;
import org.exoplatform.services.jcr.load.blob.TestSwap;

/**
 * Created by The eXo Platform SARL Author : Peter Nedonosko
 * peter.nedonosko@exoplatform.com.ua 24.10.2006
 * 
 * @version $Id: DeleteThread.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class DeleteThread extends UserThread {
  
  public DeleteThread(Session threadSession) {
    super(threadSession);
  }
  
  public void testAction() {
    while (process || TestSwap.consumedNodes.size()>0) {
      deleteAction();
      try {
        sleep(2500);
      } catch(InterruptedException e) {
        threadLog.error("Sleep error: " + e.getMessage(), e);
      }
    }
  }
  
  public void deleteAction() {
    
    final String[] nodes = TestSwap.consumedNodes.toArray(new String[TestSwap.consumedNodes.size()]);
    try {
      threadSession.refresh(false);
    } catch(RepositoryException th) {
      threadLog.error("Refresh before delete error: " + th.getMessage(), th);
    }
    for (String nodePath: nodes) {
      String nodeInfo = "";
      try {
        Node node = (Node) threadSession.getItem(nodePath);
        PropertyImpl data = (PropertyImpl) node.getProperty("jcr:content/jcr:data");
        nodeInfo = "node: " + node.getPath() + ", data: " + data.getInternalIdentifier();
        node.remove();
        if (threadLog.isDebugEnabled())
          threadLog.debug("Delete " + nodeInfo);
      } catch(PathNotFoundException e) {
        threadLog.warn(e.getMessage());
      //} catch(InvalidItemStateException e) {
      } catch(Throwable th) {
        threadLog.error("Delete error: " + th.getMessage() + ". " + nodeInfo, th);
      } finally {
        try {
          threadSession.save();
        } catch(RepositoryException e) {
          threadLog.error("Repository error: " + e.getMessage(), e);
        }
        TestSwap.consumedNodes.remove(nodePath);
      }
    }
  }
}  
