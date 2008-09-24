package org.exoplatform.services.jcr.impl.storage;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.PropertyType;

import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.CompositeChangesLog;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.SessionDataManager;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.dataflow.session.TransactionableDataManager;
import org.exoplatform.services.jcr.impl.dataflow.session.WorkspaceStorageDataManagerProxy;
import org.exoplatform.services.jcr.impl.storage.jdbc.JDBCWorkspaceDataContainer;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;
import org.exoplatform.services.jcr.util.IdGenerator;

public class JDBCHWDCTest extends JcrImplBaseTest {

  protected JDBCWorkspaceDataContainer wdContainer = null;

  protected List<ItemData>             cleanUpList = new ArrayList<ItemData>();

  @Override
  public void setUp() throws Exception {
    super.setUp();

    wdContainer = (JDBCWorkspaceDataContainer) session.getContainer()
                                                      .getComponentInstanceOfType(JDBCWorkspaceDataContainer.class);
  }

  @Override
  protected void tearDown() throws Exception {
    SessionDataManager sdm = session.getTransientNodesManager();
    TransactionableDataManager trm = sdm.getTransactManager();
    WorkspaceStorageDataManagerProxy wdm = trm.getStorageDataManager();

    CompositeChangesLog clog = new TransactionChangesLog();
    PlainChangesLogImpl changes = new PlainChangesLogImpl();
    for (int i = cleanUpList.size(); i > 0;) {
      changes.add(ItemState.createDeletedState(cleanUpList.get(--i)));
    }

    clog.addLog(changes);
    wdm.save(clog);

    super.tearDown();
  }

  public void testItemAdd_Connection() throws Exception {
    SessionDataManager sdm = session.getTransientNodesManager();
    TransactionableDataManager trm = sdm.getTransactManager();
    WorkspaceStorageDataManagerProxy wdm = trm.getStorageDataManager();

    NodeData rootData = (NodeData) wdm.getItemData(Constants.ROOT_UUID);

    InternalQName nodeName = InternalQName.parse("[]testNode");
    QPath path = QPath.makeChildPath(rootData.getQPath(), nodeName, 1);

    AccessControlList acl = rootData.getACL();

    TransientNodeData nodeData = new TransientNodeData(path,
                                                       IdGenerator.generate(),
                                                       -1,
                                                       Constants.NT_UNSTRUCTURED,
                                                       new InternalQName[0],
                                                       0,
                                                       rootData.getIdentifier(),
                                                       acl);

    // jcr:primaryType
    TransientPropertyData ptData = TransientPropertyData.createPropertyData(nodeData,
                                                                            Constants.JCR_PRIMARYTYPE,
                                                                            PropertyType.NAME,
                                                                            false,
                                                                            new TransientValueData(Constants.NT_UNSTRUCTURED));

    // jcr:mixinTypes
    TransientPropertyData mtData = TransientPropertyData.createPropertyData(nodeData,
                                                                            Constants.JCR_MIXINTYPES,
                                                                            PropertyType.NAME,
                                                                            true,
                                                                            new TransientValueData(Constants.MIX_REFERENCEABLE));

    // jcr:uuid
    TransientPropertyData uuidData = TransientPropertyData.createPropertyData(nodeData,
                                                                              Constants.JCR_UUID,
                                                                              PropertyType.STRING,
                                                                              false,
                                                                              new TransientValueData(nodeData.getIdentifier()));

    // add
    WorkspaceStorageConnection con = wdContainer.openConnection();
    try {
      con.add(nodeData);
      con.add(ptData);
      con.add(mtData);
      con.add(uuidData);
      con.commit();
    } catch (Exception e) {
      con.rollback();
      throw e;
    } finally {
      cleanUpList.add(nodeData);
      cleanUpList.add(ptData);
      cleanUpList.add(mtData);
      cleanUpList.add(uuidData);
    }

    // get
    con = wdContainer.openConnection();
    try {
      NodeData storedNode = (NodeData) con.getItemData(rootData, new QPathEntry(nodeName, 1));
      assertEquals(path, storedNode.getQPath());
    } catch (Exception e) {
      throw e;
    } finally {
      con.rollback();
    }

  }

  public void testItemAdd_DataManager() throws Exception {
    SessionDataManager sdm = session.getTransientNodesManager();
    TransactionableDataManager trm = sdm.getTransactManager();
    WorkspaceStorageDataManagerProxy wdm = trm.getStorageDataManager();

    NodeData rootData = (NodeData) wdm.getItemData(Constants.ROOT_UUID);

    InternalQName nodeName = InternalQName.parse("[]testNode");
    QPath path = QPath.makeChildPath(rootData.getQPath(), nodeName, 1);

    AccessControlList acl = rootData.getACL();

    TransientNodeData nodeData = new TransientNodeData(path,
                                                       IdGenerator.generate(),
                                                       -1,
                                                       Constants.NT_UNSTRUCTURED,
                                                       new InternalQName[0],
                                                       0,
                                                       rootData.getIdentifier(),
                                                       acl);

    // jcr:primaryType
    TransientPropertyData ptData = TransientPropertyData.createPropertyData(nodeData,
                                                                            Constants.JCR_PRIMARYTYPE,
                                                                            PropertyType.NAME,
                                                                            false,
                                                                            new TransientValueData(Constants.NT_UNSTRUCTURED));

    // jcr:mixinTypes
    TransientPropertyData mtData = TransientPropertyData.createPropertyData(nodeData,
                                                                            Constants.JCR_MIXINTYPES,
                                                                            PropertyType.NAME,
                                                                            true,
                                                                            new TransientValueData(Constants.MIX_REFERENCEABLE));

    // jcr:uuid
    TransientPropertyData uuidData = TransientPropertyData.createPropertyData(nodeData,
                                                                              Constants.JCR_UUID,
                                                                              PropertyType.STRING,
                                                                              false,
                                                                              new TransientValueData(nodeData.getIdentifier()));

    // add
    CompositeChangesLog clog = new TransactionChangesLog();
    PlainChangesLogImpl changes = new PlainChangesLogImpl();
    try {
      changes.add(ItemState.createAddedState(nodeData));
      changes.add(ItemState.createAddedState(ptData));
      changes.add(ItemState.createAddedState(mtData));
      changes.add(ItemState.createAddedState(uuidData));
      clog.addLog(changes);
      wdm.save(clog);
    } finally {
      cleanUpList.add(nodeData);
      cleanUpList.add(ptData);
      cleanUpList.add(mtData);
      cleanUpList.add(uuidData);
    }

    // get
    try {
      NodeData storedNode = (NodeData) wdm.getItemData(rootData, new QPathEntry(nodeName, 1));
      assertEquals(path, storedNode.getQPath());
    } catch (Exception e) {
      throw e;
    }
  }

}
