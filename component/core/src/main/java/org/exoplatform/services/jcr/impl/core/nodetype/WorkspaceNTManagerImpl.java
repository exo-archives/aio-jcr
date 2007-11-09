/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core.nodetype;

import java.util.List;
import java.util.Map;

import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: WorkspaceNTManagerImpl.java 12841 2007-02-16 08:58:38Z peterit $
 */

class WorkspaceNTManagerImpl extends NodeTypeManagerImpl {

  protected static Log log = ExoLogger.getLogger("jcr.WorkspaceNTManagerImpl");
  
  private SessionImpl session;
  
  WorkspaceNTManagerImpl(
      NamespaceRegistry namespaceRegistry, 
      String accessControlPolicy, 
      SessionImpl session,
      NodeTypeDataPersister persister,
      Map<InternalQName,ExtendedNodeType> nodeTypes) throws RepositoryException {
    super(
        session.getLocationFactory(), session.getValueFactory(), 
        namespaceRegistry, 
        accessControlPolicy,
        persister,
        nodeTypes);
    this.session = session;
  }

  public SessionImpl getSession() {
    return session;
  }

  public ExtendedNodeType getNodeType(InternalQName qName) throws NoSuchNodeTypeException, RepositoryException {
    
    NodeTypeImpl nt = (NodeTypeImpl) super.getNodeType(qName);
    /*if (nt.value != null) {
      return new WorkspaceNTImpl(nt, nt.value, session);
    }*/
    return new WorkspaceNTImpl(nt, session);
  }
  
  public synchronized void initStorage(Node sysNode, boolean forcePersistence)
    throws RepositoryException {
    throw new UnsupportedRepositoryOperationException("Method is not accessible from this class");
  }
  
}
