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
package org.exoplatform.services.jcr.ext.replication;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.ChangesLogIterator;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.WorkspaceContainer;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspacePersistentDataManager;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SAS
 * Author : Alex Reshetnyak
 *          alex.reshetnyak@exoplatform.com.ua
 * 25.04.2008  
 */
public class TestNodeNameDuplicated extends BaseStandaloneTest {
  public void testDuplicatNodeName() throws Exception {
    WorkspaceContainer wContainer = (WorkspaceContainer) (repository.getSystemSession()
        .getContainer());

    WorkspacePersistentDataManager workspacePersistentDataManager = (WorkspacePersistentDataManager) wContainer
        .getComponentInstanceOfType(WorkspacePersistentDataManager.class);
    
    
    session.save();
    session.getRootNode().addNode("exo:registry", "exo:registry");
    session.save();
    
    PlainChangesLogImpl plainChangesLog1 = new PlainChangesLogImpl(session.getId());
    addNode("exo:registry", 1, session.getRootNode(), plainChangesLog1);
    
    PlainChangesLogImpl plainChangesLog2 = new PlainChangesLogImpl(session.getId());
    addNode("exo:registry", 1, session.getRootNode(), plainChangesLog2);
    
    //save to JCR
    try {
    dump(plainChangesLog1);  
    workspacePersistentDataManager.save(plainChangesLog1);
    log.info("After # 1 :");
    
    dump(plainChangesLog2);
    workspacePersistentDataManager.save(plainChangesLog2);
    log.info("After # 2 :");
    
    } catch (Exception e) {
      printRootNode();
      
      log.error("Fail : ", e);
      fail();
    }
    
    printRootNode();
  }
  

  private InternalQName NT_EXO_REGISTRY = new InternalQName(Constants.NS_EXO_URI, "registry");
  
  private InternalQName NT_EXO_REGISTRY_NAME = new InternalQName(Constants.NS_EXO_PREFIX, "registry");
  
  public TransientNodeData addNode(String name, int orderNum, NodeImpl parentNode, PlainChangesLogImpl changesLog) throws Exception {
      
    TransientNodeData nodeData = createNodeData_exo_registry(name, orderNum, parentNode);
    changesLog.add(ItemState.createAddedState(nodeData));

    TransientPropertyData primaryTypeData = new TransientPropertyData(QPath.makeChildPath(nodeData
        .getQPath(), Constants.JCR_PRIMARYTYPE), IdGenerator.generate(), -1, PropertyType.NAME,
        nodeData.getIdentifier(), false);
    primaryTypeData.setValue(new TransientValueData(NT_EXO_REGISTRY));
    changesLog.add(ItemState.createAddedState(primaryTypeData));

    return nodeData;
  }
  
  public TransientNodeData createNodeData_exo_registry(String name, int orderNum, NodeImpl parentNode) {

    InternalQName[] mixinTypeNames = new InternalQName[0];

    InternalQName iQName = new InternalQName(Constants.NS_EMPTY_PREFIX, name);

    QPath path = QPath.makeChildPath(parentNode.getInternalPath(), iQName);

    AccessControlList acl = new AccessControlList();

    String uuid = IdGenerator.generate();

    TransientNodeData nodeData = new TransientNodeData(path, uuid, -1, NT_EXO_REGISTRY_NAME,
        mixinTypeNames, orderNum, parentNode.getInternalIdentifier(), acl);

    return nodeData;
  }
  
  private void printRootNode() throws RepositoryException {
    NodeIterator ni = session.getRootNode().getNodes();
    while (ni.hasNext()) {
      Node cNode = ni.nextNode();
      log.info(cNode.getName() + ":" + cNode.getIndex());
    }
  }
  
  private void dump(PlainChangesLog changesLog) {
      log.info(changesLog.dump());
  }
}
