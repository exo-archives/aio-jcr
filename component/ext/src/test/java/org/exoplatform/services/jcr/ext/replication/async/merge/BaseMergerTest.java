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
package org.exoplatform.services.jcr.ext.replication.async.merge;

import java.io.ByteArrayInputStream;
import java.util.List;

import javax.jcr.PropertyType;

import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: AddMergerTest.java 25005 2008-12-12 16:33:15Z tolusha $
 */
public class BaseMergerTest extends BaseStandaloneTest {

  protected NodeTypeDataManager     nodeTypeDataManager;

  /**
   * Test nodetype. UNSTRUCTURED but child nodes SNS disallowed.
   */
  public static final InternalQName EXO_TEST_UNSTRUCTURED_NOSNS = new InternalQName(Constants.NS_EXO_URI,
                                                                                    "testUnstructuredNoSNS");

  protected TransactionChangesLog   local;

  protected TransactionChangesLog   income;

  // remote

  protected NodeData                remoteItem1;

  protected NodeData                remoteItem11;

  protected NodeData                remoteItem112;

  protected NodeData                remoteItem1121;

  protected NodeData                remoteItem1111;

  protected NodeData                remoteItem12;

  protected NodeData                remoteItem121;

  protected NodeData                remoteItem111;

  protected NodeData                remoteItem2;

  protected NodeData                remoteItem21;

  protected NodeData                remoteItem211;

  protected NodeData                remoteItem3;

  protected NodeData                remoteItem21x21;

  protected NodeData                remoteItem21x22;

  protected NodeData                remoteItem212;

  protected NodeData                remoteItem2121;

  protected PropertyData            remoteProperty1;

  protected PropertyData            remoteProperty2;

  protected PropertyData            remoteProperty11;

  protected PropertyData            remoteProperty111;

  // local

  protected NodeData                localItem1;

  protected NodeData                localItem2;

  protected NodeData                localItem21;

  protected NodeData                localItem21x2B;

  protected NodeData                localItem21x2A;

  protected NodeData                localItem21x1B1;

  protected NodeData                localItem21x1B;

  protected NodeData                localItem3;

  protected NodeData                localItem11;

  protected NodeData                localItem111;

  protected NodeData                localItem112;

  protected NodeData                localItem12;

  protected NodeData                localItem122;

  protected PropertyData            localProperty1;

  protected PropertyData            localProperty2;

  /**
   * {@inheritDoc}
   */
  public void setUp() throws Exception {
    super.setUp();

    nodeTypeDataManager = session.getWorkspace().getNodeTypesHolder();

    assertNotNull(nodeTypeDataManager);

    final String testItem1 = "testItem1";
    // create /testItem1
    localItem1 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                           new InternalQName(null, testItem1)),
                                       IdGenerator.generate(),
                                       0,
                                       EXO_TEST_UNSTRUCTURED_NOSNS,
                                       new InternalQName[0],
                                       0,
                                       Constants.ROOT_UUID,
                                       new AccessControlList());
    // create /testItem1/item11
    localItem11 = new TransientNodeData(QPath.makeChildPath(localItem1.getQPath(),
                                                            new InternalQName(null, "item11")),
                                        IdGenerator.generate(),
                                        0,
                                        EXO_TEST_UNSTRUCTURED_NOSNS,
                                        new InternalQName[0],
                                        0,
                                        localItem1.getIdentifier(),
                                        new AccessControlList());
    // create /testItem1/item11/item111
    localItem111 = new TransientNodeData(QPath.makeChildPath(localItem11.getQPath(),
                                                             new InternalQName(null, "item111")),
                                         IdGenerator.generate(),
                                         0,
                                         EXO_TEST_UNSTRUCTURED_NOSNS,
                                         new InternalQName[0],
                                         0,
                                         localItem11.getIdentifier(),
                                         new AccessControlList());

    // create /testItem1/item11/item111
    localItem112 = new TransientNodeData(QPath.makeChildPath(localItem11.getQPath(),
                                                             new InternalQName(null, "item112")),
                                         IdGenerator.generate(),
                                         0,
                                         EXO_TEST_UNSTRUCTURED_NOSNS,
                                         new InternalQName[0],
                                         0,
                                         localItem11.getIdentifier(),
                                         new AccessControlList());

    // create /testItem1/item12
    localItem12 = new TransientNodeData(QPath.makeChildPath(localItem1.getQPath(),
                                                            new InternalQName(null, "item12")),
                                        IdGenerator.generate(),
                                        0,
                                        EXO_TEST_UNSTRUCTURED_NOSNS,
                                        new InternalQName[0],
                                        1,
                                        localItem1.getIdentifier(),
                                        new AccessControlList());
    // create /testItem1/item12/item122
    localItem122 = new TransientNodeData(QPath.makeChildPath(localItem12.getQPath(),
                                                             new InternalQName(null, "item122")),
                                         IdGenerator.generate(),
                                         0,
                                         EXO_TEST_UNSTRUCTURED_NOSNS,
                                         new InternalQName[0],
                                         0,
                                         localItem12.getIdentifier(),
                                         new AccessControlList());

    // create /testItem2
    final String testItem2 = "testItem2";
    localItem2 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                           new InternalQName(null, testItem2)),
                                       IdGenerator.generate(),
                                       0,
                                       Constants.NT_UNSTRUCTURED,
                                       new InternalQName[0],
                                       1,
                                       Constants.ROOT_UUID,
                                       new AccessControlList());

    // create /testItem2/item21
    localItem21 = new TransientNodeData(QPath.makeChildPath(localItem2.getQPath(),
                                                            new InternalQName(null, "item21")),
                                        IdGenerator.generate(),
                                        0,
                                        EXO_TEST_UNSTRUCTURED_NOSNS,
                                        new InternalQName[0],
                                        1,
                                        localItem2.getIdentifier(),
                                        new AccessControlList());

    // create /testItem3
    final String testItem3 = "testItem3";
    localItem3 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                           new InternalQName(null, testItem3)),
                                       IdGenerator.generate(),
                                       0,
                                       Constants.NT_UNSTRUCTURED,
                                       new InternalQName[0],
                                       2,
                                       Constants.ROOT_UUID,
                                       new AccessControlList());

    // local Properties
    localProperty1 = new TransientPropertyData(QPath.makeChildPath(localItem1.getQPath(),
                                                                   new InternalQName(null,
                                                                                     "testProperty1")),
                                               IdGenerator.generate(),
                                               0,
                                               PropertyType.STRING,
                                               localItem1.getIdentifier(),
                                               false);
    ((TransientPropertyData) localProperty1).setValue(new TransientValueData("test string"));

    localProperty2 = new TransientPropertyData(QPath.makeChildPath(localItem1.getQPath(),
                                                                   new InternalQName(null,
                                                                                     "testProperty2")),
                                               IdGenerator.generate(),
                                               0,
                                               PropertyType.BINARY,
                                               localItem1.getIdentifier(),
                                               false);
    ((TransientPropertyData) localProperty2).setValue(new TransientValueData(new ByteArrayInputStream("test binary".getBytes())));

    // create /testItem1
    remoteItem1 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                            new InternalQName(null, testItem1)),
                                        IdGenerator.generate(),
                                        0,
                                        EXO_TEST_UNSTRUCTURED_NOSNS,
                                        new InternalQName[0],
                                        0,
                                        Constants.ROOT_UUID,
                                        new AccessControlList());
    // create /testItem1/item11
    remoteItem11 = new TransientNodeData(QPath.makeChildPath(remoteItem1.getQPath(),
                                                             new InternalQName(null, "item11")),
                                         IdGenerator.generate(),
                                         0,
                                         EXO_TEST_UNSTRUCTURED_NOSNS,
                                         new InternalQName[0],
                                         0,
                                         remoteItem1.getIdentifier(),
                                         new AccessControlList());
    // create /testItem1/item11/item112
    remoteItem112 = new TransientNodeData(QPath.makeChildPath(remoteItem11.getQPath(),
                                                              new InternalQName(null, "item112")),
                                          IdGenerator.generate(),
                                          0,
                                          EXO_TEST_UNSTRUCTURED_NOSNS,
                                          new InternalQName[0],
                                          0,
                                          remoteItem11.getIdentifier(),
                                          new AccessControlList());

    // create /testItem1/item11/item111
    remoteItem111 = new TransientNodeData(QPath.makeChildPath(remoteItem11.getQPath(),
                                                              new InternalQName(null, "item111")),
                                          IdGenerator.generate(),
                                          0,
                                          EXO_TEST_UNSTRUCTURED_NOSNS,
                                          new InternalQName[0],
                                          0,
                                          remoteItem11.getIdentifier(),
                                          new AccessControlList());

    // create /testItem1/item12/item112/1121
    remoteItem1121 = new TransientNodeData(QPath.makeChildPath(remoteItem112.getQPath(),
                                                               new InternalQName(null, "item1121")),
                                           IdGenerator.generate(),
                                           0,
                                           EXO_TEST_UNSTRUCTURED_NOSNS,
                                           new InternalQName[0],
                                           0,
                                           remoteItem112.getIdentifier(),
                                           new AccessControlList());

    // create /testItem1/item12/item112/1111
    remoteItem1111 = new TransientNodeData(QPath.makeChildPath(remoteItem111.getQPath(),
                                                               new InternalQName(null, "item1111")),
                                           IdGenerator.generate(),
                                           0,
                                           EXO_TEST_UNSTRUCTURED_NOSNS,
                                           new InternalQName[0],
                                           0,
                                           remoteItem111.getIdentifier(),
                                           new AccessControlList());

    // create /testItem1/item12
    remoteItem12 = new TransientNodeData(QPath.makeChildPath(remoteItem1.getQPath(),
                                                             new InternalQName(null, "item12")),
                                         IdGenerator.generate(),
                                         0,
                                         EXO_TEST_UNSTRUCTURED_NOSNS,
                                         new InternalQName[0],
                                         1,
                                         remoteItem1.getIdentifier(),
                                         new AccessControlList());
    // create /testItem1/item12/item121
    remoteItem121 = new TransientNodeData(QPath.makeChildPath(remoteItem12.getQPath(),
                                                              new InternalQName(null, "item121")),
                                          IdGenerator.generate(),
                                          0,
                                          EXO_TEST_UNSTRUCTURED_NOSNS,
                                          new InternalQName[0],
                                          0,
                                          remoteItem12.getIdentifier(),
                                          new AccessControlList());

    // create /testItem2
    remoteItem2 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                            new InternalQName(null, testItem2)),
                                        IdGenerator.generate(),
                                        0,
                                        EXO_TEST_UNSTRUCTURED_NOSNS,
                                        new InternalQName[0],
                                        0,
                                        Constants.ROOT_UUID,
                                        new AccessControlList());

    // create /testItem2/item21
    remoteItem21 = new TransientNodeData(QPath.makeChildPath(remoteItem2.getQPath(),
                                                             new InternalQName(null, "item21")),
                                         IdGenerator.generate(),
                                         0,
                                         EXO_TEST_UNSTRUCTURED_NOSNS,
                                         new InternalQName[0],
                                         1,
                                         remoteItem2.getIdentifier(),
                                         new AccessControlList());

    // create /testItem2/item21/item211
    remoteItem211 = new TransientNodeData(QPath.makeChildPath(remoteItem21.getQPath(),
                                                              new InternalQName(null, "item211")),
                                          IdGenerator.generate(),
                                          0,
                                          EXO_TEST_UNSTRUCTURED_NOSNS,
                                          new InternalQName[0],
                                          0,
                                          remoteItem21.getIdentifier(),
                                          new AccessControlList());

    // create /testItem3
    remoteItem3 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                            new InternalQName(null, testItem3)),
                                        IdGenerator.generate(),
                                        0,
                                        EXO_TEST_UNSTRUCTURED_NOSNS,
                                        new InternalQName[0],
                                        2,
                                        Constants.ROOT_UUID,
                                        new AccessControlList());

    // remote property (as prop of local item 1)
    remoteProperty1 = new TransientPropertyData(QPath.makeChildPath(remoteItem1.getQPath(),
                                                                    new InternalQName(null,
                                                                                      "testProperty1")),
                                                IdGenerator.generate(),
                                                0,
                                                PropertyType.LONG,
                                                remoteItem1.getIdentifier(),
                                                false);
    ((TransientPropertyData) remoteProperty1).setValue(new TransientValueData(123l));

    // remote property (as prop of local item 11)
    remoteProperty11 = new TransientPropertyData(QPath.makeChildPath(remoteItem11.getQPath(),
                                                                     new InternalQName(null,
                                                                                       "testProperty11")),
                                                 IdGenerator.generate(),
                                                 0,
                                                 PropertyType.LONG,
                                                 remoteItem11.getIdentifier(),
                                                 false);
    ((TransientPropertyData) remoteProperty11).setValue(new TransientValueData(2222));

    // remote property (as prop of local item 11)
    remoteProperty111 = new TransientPropertyData(QPath.makeChildPath(remoteItem111.getQPath(),
                                                                      new InternalQName(null,
                                                                                        "testProperty111")),
                                                  IdGenerator.generate(),
                                                  0,
                                                  PropertyType.LONG,
                                                  remoteItem111.getIdentifier(),
                                                  false);
    ((TransientPropertyData) remoteProperty111).setValue(new TransientValueData(111));

    // remote property (as prop of local item 1)
    remoteProperty2 = new TransientPropertyData(QPath.makeChildPath(localItem1.getQPath(),
                                                                    new InternalQName(null,
                                                                                      "testProperty2")),
                                                IdGenerator.generate(),
                                                0,
                                                PropertyType.LONG,
                                                localItem1.getIdentifier(),
                                                false);
    ((TransientPropertyData) remoteProperty2).setValue(new TransientValueData(1111));

    // SNS items
    localItem21x2B = new TransientNodeData(QPath.makeChildPath(localItem2.getQPath(),
                                                               new InternalQName(null, "item21"),
                                                               2),
                                           IdGenerator.generate(),
                                           0,
                                           Constants.NT_UNSTRUCTURED,
                                           new InternalQName[0],
                                           1,
                                           localItem1.getIdentifier(),
                                           new AccessControlList());

    localItem21x1B = new TransientNodeData(QPath.makeChildPath(localItem2.getQPath(),
                                                               new InternalQName(null, "item21"),
                                                               1),
                                           localItem21x2B.getIdentifier(),
                                           0,
                                           Constants.NT_UNSTRUCTURED,
                                           new InternalQName[0],
                                           0,
                                           localItem1.getIdentifier(),
                                           new AccessControlList());
    localItem21x1B1 = new TransientNodeData(QPath.makeChildPath(localItem21x1B.getQPath(),
                                                                new InternalQName(null,
                                                                                  "item21x1-1"),
                                                                1),
                                            IdGenerator.generate(),
                                            0,
                                            Constants.NT_UNSTRUCTURED,
                                            new InternalQName[0],
                                            0,
                                            localItem21x1B.getIdentifier(),
                                            new AccessControlList());

    localItem21x2A = new TransientNodeData(QPath.makeChildPath(localItem2.getQPath(),
                                                               new InternalQName(null, "item21"),
                                                               2),
                                           localItem11.getIdentifier(),
                                           0,
                                           Constants.NT_UNSTRUCTURED,
                                           new InternalQName[0],
                                           0,
                                           localItem1.getIdentifier(),
                                           new AccessControlList());

    // remote, will conflict with localItem21x1B1 (path of parent reordered [2] -> [1], different
    // Node Id)
    remoteItem21x21 = new TransientNodeData(QPath.makeChildPath(localItem21x2B.getQPath(),
                                                                localItem21x1B1.getQPath()
                                                                               .getEntries()[localItem21x1B1.getQPath()
                                                                                                            .getEntries().length - 1]),
                                            IdGenerator.generate(), // new id
                                            0,
                                            Constants.NT_UNSTRUCTURED,
                                            new InternalQName[0],
                                            0,
                                            localItem21x2B.getIdentifier(),
                                            new AccessControlList());
    // new node, not conflicted
    remoteItem21x22 = new TransientNodeData(QPath.makeChildPath(localItem21x2B.getQPath(),
                                                                new InternalQName(null,
                                                                                  "item11x1-2"),
                                                                1),
                                            IdGenerator.generate(),
                                            0,
                                            Constants.NT_UNSTRUCTURED,
                                            new InternalQName[0],
                                            0,
                                            localItem21x2B.getIdentifier(),
                                            new AccessControlList());

    remoteItem212 = new TransientNodeData(QPath.makeChildPath(remoteItem21.getQPath(),
                                                              new InternalQName(null, "item212")),
                                          IdGenerator.generate(),
                                          0,
                                          Constants.NT_UNSTRUCTURED,
                                          new InternalQName[0],
                                          0,
                                          localItem21x2A.getIdentifier(),
                                          new AccessControlList());

    remoteItem2121 = new TransientNodeData(QPath.makeChildPath(remoteItem212.getQPath(),
                                                               new InternalQName(null, "item2121")),
                                           IdGenerator.generate(),
                                           0,
                                           Constants.NT_UNSTRUCTURED,
                                           new InternalQName[0],
                                           0,
                                           remoteItem212.getIdentifier(),
                                           new AccessControlList());

    // logs
    local = new TransactionChangesLog();
    income = new TransactionChangesLog();
  }

  /**
   * {@inheritDoc}
   */
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  protected ItemState findStateByPath(List<ItemState> changes, QPath path) {
    for (ItemState st : changes) {
      if (st.getData().getQPath().equals(path))
        return st;
    }

    return null;
  }

  protected ItemState findStateById(List<ItemState> changes, String id) {
    for (ItemState st : changes) {
      if (st.getData().getIdentifier().equals(id))
        return st;
    }

    return null;
  }

  protected boolean hasState(List<ItemState> changes, ItemState expected, boolean respectId) {
    for (ItemState st : changes) {
      if (st.getData().getQPath().equals(expected.getData().getQPath())
          && st.getState() == expected.getState()
          && (respectId
              ? st.getData().getIdentifier().equals(expected.getData().getIdentifier())
              : true))
        return true;
    }

    return false;
  }

  public void test() {
  }
}
