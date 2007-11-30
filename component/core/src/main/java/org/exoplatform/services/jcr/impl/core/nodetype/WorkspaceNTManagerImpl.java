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
 * Created by The eXo Platform SAS.
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
