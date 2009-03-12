/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.util;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.datamodel.Identifier;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.PropertyImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.dataflow.ItemDataRemoveVisitor;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.xml.ItemDataKeeperAdapter;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class VersionHistoryImporter {
  /**
   * Class logger.
   */
  private static final Log            LOG = ExoLogger.getLogger(VersionHistoryImporter.class);

  private final NodeImpl              versionableNode;

  private final InputStream           versionHistoryStream;

  private final SessionImpl           userSession;

  private final NodeTypeDataManager   nodeTypeDataManager;

  private final ItemDataKeeperAdapter dataKeeper;

  private final String                baseVersionUuid;

  private final String[]              predecessors;

  private final String                versionHistory;

  /**
   * @param versionableNode
   * @param versionHistoryStream
   * @throws RepositoryException
   */
  public VersionHistoryImporter(NodeImpl versionableNode,
                                InputStream versionHistoryStream,
                                String baseVersionUuid,
                                String[] predecessors,
                                String versionHistory) throws RepositoryException {
    super();
    this.versionableNode = versionableNode;
    this.versionHistoryStream = versionHistoryStream;
    this.baseVersionUuid = baseVersionUuid;
    this.predecessors = predecessors;
    this.versionHistory = versionHistory;
    this.userSession = versionableNode.getSession();
    this.nodeTypeDataManager = userSession.getWorkspace().getNodeTypesHolder();
    this.dataKeeper = new ItemDataKeeperAdapter(userSession.getTransientNodesManager());
  }

  public void doImport() throws RepositoryException, IOException {
    String path = versionableNode.getVersionHistory().getParent().getPath();

    NodeData versionable = (NodeData) versionableNode.getData();
    // ----- VERSIONABLE properties -----
    // jcr:versionHistory
    TransientPropertyData vh = TransientPropertyData.createPropertyData(versionable,
                                                                        Constants.JCR_VERSIONHISTORY,
                                                                        PropertyType.REFERENCE,
                                                                        false);
    vh.setValue(new TransientValueData(new Identifier(versionHistory)));

    // jcr:baseVersion
    TransientPropertyData bv = TransientPropertyData.createPropertyData(versionable,
                                                                        Constants.JCR_BASEVERSION,
                                                                        PropertyType.REFERENCE,
                                                                        false);
    bv.setValue(new TransientValueData(new Identifier(baseVersionUuid)));

    // jcr:predecessors
    TransientPropertyData pd = TransientPropertyData.createPropertyData(versionable,
                                                                        Constants.JCR_PREDECESSORS,
                                                                        PropertyType.REFERENCE,
                                                                        true);
    for (int i = 0; i < predecessors.length; i++) {
      pd.setValue(new TransientValueData(new Identifier(predecessors[i])));
    }

    PlainChangesLog changesLog = new PlainChangesLogImpl();
    RemoveVisitor rv = new RemoveVisitor();
    rv.visit((NodeData) ((NodeImpl) versionableNode.getVersionHistory()).getData());
    changesLog.addAll(rv.getRemovedStates());
    changesLog.add(ItemState.createAddedState(vh));
    changesLog.add(ItemState.createAddedState(bv));
    changesLog.add(ItemState.createAddedState(pd));
    // remove version properties to avoid referential integrety check
    PlainChangesLog changesLogDeltete = new PlainChangesLogImpl();

    changesLogDeltete.add(ItemState.createDeletedState(((PropertyImpl) versionableNode.getProperty("jcr:versionHistory")).getData()));
    changesLogDeltete.add(ItemState.createDeletedState(((PropertyImpl) versionableNode.getProperty("jcr:baseVersion")).getData()));
    changesLogDeltete.add(ItemState.createDeletedState(((PropertyImpl) versionableNode.getProperty("jcr:predecessors")).getData()));
    dataKeeper.save(changesLogDeltete);
    userSession.save();
    // remove version history
    dataKeeper.save(changesLog);
    // import new version history
    userSession.importXML(path, versionHistoryStream, 0);
    userSession.save();
  }

  protected class RemoveVisitor extends ItemDataRemoveVisitor {

    RemoveVisitor() throws RepositoryException {
      super(userSession.getTransientNodesManager(), null,
      // userSession.getWorkspace().getNodeTypeManager(),
            nodeTypeDataManager,
            userSession.getAccessManager(),
            userSession.getUserState());
    }

    protected void validateReferential(NodeData node) throws RepositoryException {
      // no REFERENCE validation here
    }
  };
}
