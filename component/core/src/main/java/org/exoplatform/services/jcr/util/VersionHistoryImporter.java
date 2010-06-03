/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.datamodel.Identifier;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.PropertyImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.dataflow.ItemDataRemoveVisitor;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.xml.ItemDataKeeperAdapter;
import org.exoplatform.services.log.ExoLogger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: VersionHistoryImporter.java 44109 2010-03-02 14:37:19Z
 *          NZamosenchuk $
 */
public class VersionHistoryImporter {
  /**
   * Class logger.
   */
  private static final Log            LOG = ExoLogger.getLogger(VersionHistoryImporter.class);

  /**
   * Versioned node.
   */
  private final NodeImpl              versionableNode;

  /**
   * Version history data.
   */
  private final InputStream           versionHistoryStream;

  /**
   * User session.
   */
  private final SessionImpl           userSession;

  /**
   * Node-type data manager.
   */
  private final NodeTypeManagerImpl   nodeTypeDataManager;

  /**
   * Data keeper.
   */
  private final ItemDataKeeperAdapter dataKeeper;

  /**
   * jcr:baseVersion - uuid.
   */
  private final String                baseVersionUuid;

  /**
   * predecessors uuids.
   */
  private final String[]              predecessors;

  /**
   * Version history - uuid.
   */
  private final String                versionHistory;

  /**
   * Versionable node uuid.
   */
  private String                      uuid;

  /**
   * Versionable node path.
   */
  private String                      path;

  /**
   * VersionHistoryImporter constructor.
   * 
   * @param versionableNode - versionable node.
   * @param versionHistoryStream - Version history data.
   * @param baseVersionUuid - jcr:baseVersion - uuid.
   * @param predecessors - predecessors uuids.
   * @param versionHistory - Version history - uuid
   * @throws RepositoryException -if an error occurs while getting
   *           NodeTypesHolder.
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
    this.nodeTypeDataManager = userSession.getWorkspace().getNodeTypeManager();
    this.dataKeeper = new ItemDataKeeperAdapter(userSession.getTransientNodesManager());
  }

  /**
   * Do import.
   * 
   * @throws RepositoryException -if an error occurs while importing.
   * @throws IOException -i f an error occurs while importing.
   */
  public void doImport() throws RepositoryException, IOException {
    try {
      uuid = versionableNode.getUUID();
      path = versionableNode.getVersionHistory().getParent().getPath();
      LOG.info("Started: Import version history for node wiht path=" + path + " and UUID=" + uuid);
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
      List<ValueData> values = new ArrayList<ValueData>();
      for (int i = 0; i < predecessors.length; i++) {
        values.add(new TransientValueData(new Identifier(predecessors[i])));
      }
      pd.setValues(values);

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
      // remove version history
      dataKeeper.save(changesLog);
      userSession.save();
      // import new version history
      userSession.getWorkspace().importXML(path, versionHistoryStream, 0);
      userSession.save();
      LOG.info("Completed: Import version history for node wiht path=" + path + " and UUID=" + uuid);
    } catch (RepositoryException exception) {
      LOG.error("Failed: Import version history for node wiht path=" + path + " and UUID=" + uuid,
                exception);
      throw new RepositoryException(exception);
    } catch (IOException exception) {
      LOG.error("Failed: Import version history for node wiht path=" + path + " and UUID=" + uuid,
                exception);
      IOException newException = new IOException();
      newException.initCause(exception);
      throw newException;
    }
  }

  /**
   * Remover helper.
   * 
   * @author sj
   */
  protected class RemoveVisitor extends ItemDataRemoveVisitor {
    /**
     * Default constructor.
     * 
     * @throws RepositoryException - exception.
     */
    RemoveVisitor() throws RepositoryException {
      super(userSession.getTransientNodesManager(), null,
      // userSession.getWorkspace().getNodeTypeManager(),
            nodeTypeDataManager,
            userSession.getAccessManager(),
            userSession.getUserState());
    }

    /**
     * {@inheritDoc}
     */
    protected void validateReferential(NodeData node) throws RepositoryException {
      // no REFERENCE validation here
    }
  };
}
