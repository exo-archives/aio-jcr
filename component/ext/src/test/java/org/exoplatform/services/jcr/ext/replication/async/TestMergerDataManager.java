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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.InvalidItemStateException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.exoplatform.services.jcr.core.WorkspaceContainerFacade;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.replication.async.merge.CompositeChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;
import org.exoplatform.services.jcr.impl.core.SessionDataManagerTestWrapper;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.WorkspaceImpl;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CacheableWorkspaceDataManager;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: TestMergerDataManager.java 111 2008-11-11 11:11:11Z $
 */
public class TestMergerDataManager extends BaseStandaloneTest {

  private final int                         LOCAL_PRIORITY = 10;

  protected SessionImpl                     session3;

  protected WorkspaceImpl                   workspace3;

  protected Node                            root3;

  protected SessionImpl                     session4;

  protected WorkspaceImpl                   workspace4;

  protected Node                            root4;

  protected MergeDataManager                merger;

  protected List<ChangesStorage<ItemState>> membersChanges;

  /**
   * {@inheritDoc}
   */
  public void setUp() throws Exception {
    super.setUp();

    session3 = (SessionImpl) repository.login(credentials, "ws3");
    workspace3 = session3.getWorkspace();
    root3 = session3.getRootNode();

    session4 = (SessionImpl) repository.login(credentials, "ws4");
    workspace4 = session4.getWorkspace();
    root4 = session4.getRootNode();

    merger = new MergeDataManager(new RemoteExporterImpl(null, null),
                                  null,
                                  null,
                                  LOCAL_PRIORITY,
                                  "target/storage");
    membersChanges = new ArrayList<ChangesStorage<ItemState>>();
  }

  public void testAddMergeLocalPriority() throws Exception {
    root3.addNode("item1");

    SessionDataManagerTestWrapper dataManager = new SessionDataManagerTestWrapper(session3.getTransientNodesManager());
    addChangesToChangesStorage(dataManager.getChangesLog(), LOCAL_PRIORITY - 1);
    addChangesToChangesStorage(new PlainChangesLogImpl(), LOCAL_PRIORITY);

    saveResultedChanges(merger.merge(membersChanges.iterator()), "ws4");
    session3.save();

    assertTrue(isWorkspacesEquals());
  }

  /**
   * CompareWorkspaces.
   */
  protected boolean isWorkspacesEquals() throws Exception {
    return isNodesEquals(root3, root4);
  }

  /**
   * Compare two nodes.
   * 
   * @param src
   * @param dst
   * @return
   */
  private boolean isNodesEquals(Node src, Node dst) throws Exception {
    // compare node name
    // TODO compare UUID
    if (!src.getName().equals(dst.getName())) {
      return false;
    }

    // compare properties
    PropertyIterator srcProps = src.getProperties();
    PropertyIterator dstProps = dst.getProperties();
    while (srcProps.hasNext()) {
      if (!dstProps.hasNext()) {
        return false;
      }

      Property srcProp = srcProps.nextProperty();
      Property dstProp = dstProps.nextProperty();

      // TODO compare UUID
      if (!srcProp.getName().equals(dstProp.getName())) {
        return false;
      }

      Value srcValues[];
      try {
        srcValues = srcProp.getValues();
      } catch (ValueFormatException e) {
        srcValues = new Value[1];
        srcValues[0] = srcProp.getValue();
      }

      Value dstValues[];
      try {
        dstValues = dstProp.getValues();
      } catch (ValueFormatException e) {
        dstValues = new Value[1];
        dstValues[0] = dstProp.getValue();
      }

      // TODO compare value property
      // if (!srcValues.toString().equals(dstValues.toString())) {
      // return false;
      // }
    }

    if (dstProps.hasNext()) {
      return false;
    }

    // compare child nodes
    NodeIterator srcNodes = src.getNodes();
    NodeIterator dstNodes = dst.getNodes();
    while (srcNodes.hasNext()) {
      if (!dstNodes.hasNext()) {
        return false;
      }

      if (!isNodesEquals(srcNodes.nextNode(), dstNodes.nextNode())) {
        return false;
      }
    }

    if (dstNodes.hasNext()) {
      return false;
    }

    return true;
  }

  /**
   * Add changes to changes storage.
   * 
   * @param log
   * @param priority
   */
  protected void addChangesToChangesStorage(PlainChangesLog log, int priority) {
    Member member = new Member(null, priority);
    TransactionChangesLog cLog = new TransactionChangesLog(log);
    CompositeChangesStorage<ItemState> changes = new CompositeChangesStorage<ItemState>(cLog,
                                                                                        member);
    membersChanges.add(changes);
  }

  /**
   * Save resulted changes into workspace
   * 
   * @param res
   * @throws RepositoryException
   * @throws UnsupportedOperationException
   * @throws InvalidItemStateException
   */
  protected void saveResultedChanges(ChangesStorage<ItemState> changes, String workspaceName) throws Exception {
    WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(workspaceName);
    DataManager dm = (DataManager) wsc.getComponent(CacheableWorkspaceDataManager.class);

    PlainChangesLog resLog = new PlainChangesLogImpl();

    for (Iterator<ItemState> itemStates = changes.getChanges(); itemStates.hasNext();) {
      resLog.add(itemStates.next());
    }

    dm.save(resLog);
  }

  /**
   * {@inheritDoc}
   */
  public void tearDown() throws Exception {

    // clear ws3
    if (session3 != null) {
      try {
        session3.refresh(false);
        Node rootNode = session3.getRootNode();
        if (rootNode.hasNodes()) {
          for (NodeIterator children = rootNode.getNodes(); children.hasNext();) {
            children.nextNode().remove();
          }
          session3.save();
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        session4.logout();
      }
    }

    // clear ws4
    if (session4 != null) {
      try {
        session4.refresh(false);
        Node rootNode = session4.getRootNode();
        if (rootNode.hasNodes()) {
          for (NodeIterator children = rootNode.getNodes(); children.hasNext();) {
            children.nextNode().remove();
          }
          session4.save();
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        session4.logout();
      }
    }

    super.tearDown();
  }

}
