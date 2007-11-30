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

package org.exoplatform.services.jcr.impl.core.version;

import java.io.IOException;
import java.util.Stack;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemDataTraversingVisitor;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.SessionDataManager;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.dataflow.session.SessionChangesLog;
import org.exoplatform.services.log.ExoLogger;


/**
 * Created by The eXo Platform SAS
 *
 * 22.06.2007
 * 
 * Traverse through all versions in the version history and check if visited child histories isn't used in repository.
 * If the child version isn't used it will be removed immediately.
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class ChildVersionRemoveVisitor extends ItemDataTraversingVisitor {

  private final Log log = ExoLogger.getLogger("jcr.ChildVersionRemoveVisitor");
  
  protected final Stack<NodeData> parents = new Stack<NodeData>();
  protected final NodeTypeManagerImpl ntManager;
  protected final QPath ancestorToSave;
  protected final QPath containingHistory;
  protected final SessionImpl userSession;
  
  public ChildVersionRemoveVisitor(SessionImpl userSession, QPath containingHistory, QPath ancestorToSave) throws RepositoryException {
    super(userSession.getTransientNodesManager());

    this.ancestorToSave = ancestorToSave;
    this.containingHistory = containingHistory;
    this.userSession = userSession; 
    this.ntManager = userSession.getWorkspace().getNodeTypeManager();
  }
  
  protected SessionDataManager dataManager() {
    return (SessionDataManager) dataManager;
  }
  
  @Override
  protected void entering(PropertyData property, int level) throws RepositoryException {
    if (property.getQPath().getName().equals(Constants.JCR_CHILDVERSIONHISTORY) 
        && ntManager.isNodeType(Constants.NT_VERSIONEDCHILD, parents.peek().getPrimaryTypeName(), parents.peek().getMixinTypeNames())) {

      // check and remove child VH
      try {
        String vhID = new String(property.getValues().get(0).getAsByteArray());
  
        dataManager().removeVersionHistory(vhID, containingHistory, ancestorToSave);
      } catch(IOException e) {
        throw new RepositoryException("Child version history UUID read error " + e, e);
      }
    }
  }

  @Override
  protected void entering(NodeData node, int level) throws RepositoryException {
    parents.push(node);
  }

  @Override
  protected void leaving(PropertyData property, int level) throws RepositoryException {
  }

  @Override
  protected void leaving(NodeData node, int level) throws RepositoryException {
    parents.pop();
  }

}
