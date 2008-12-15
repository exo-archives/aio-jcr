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

import java.util.List;

import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: AddMergerTest.java 25005 2008-12-12 16:33:15Z tolusha $
 */
public class BaseMergerTest extends BaseStandaloneTest {

  protected TransactionChangesLog local;

  protected TransactionChangesLog income;

  protected ItemData              remoteItem1;

  protected ItemData              remoteItem11;

  protected ItemData              remoteItem112;
  
  protected ItemData              remoteItem12;

  protected ItemData              remoteItem121;

  protected ItemData              remoteItem2;

  protected ItemData              remoteItem3;

  protected ItemData              localItem1;

  protected ItemData              localItem2;

  protected ItemData              localItem3;

  protected ItemData              localItem11;
  
  protected ItemData              localItem111;

  protected ItemData              localItem12;

  protected ItemData              localItem122;

  /**
   * {@inheritDoc}
   */
  public void setUp() throws Exception {
    super.setUp();

    final String testItem1 = "testItem1";
    // create /testItem1
    localItem1 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                           new InternalQName(null, testItem1)),
                                       IdGenerator.generate(),
                                       0,
                                       new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                       new InternalQName[0],
                                       0,
                                       Constants.ROOT_UUID,
                                       new AccessControlList());
    // create /testItem1/item11
    localItem11 = new TransientNodeData(QPath.makeChildPath(localItem1.getQPath(),
                                                            new InternalQName(null, "item11")),
                                        IdGenerator.generate(),
                                        0,
                                        new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                        new InternalQName[0],
                                        0,
                                        localItem1.getIdentifier(),
                                        new AccessControlList());
    // create /testItem1/item11/item111
    localItem111 = new TransientNodeData(QPath.makeChildPath(localItem11.getQPath(),
                                                             new InternalQName(null, "item111")),
                                         IdGenerator.generate(),
                                         0,
                                         new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                         new InternalQName[0],
                                         0,
                                         localItem11.getIdentifier(),
                                         new AccessControlList());    
    // create /testItem1/item12
    localItem12 = new TransientNodeData(QPath.makeChildPath(localItem1.getQPath(),
                                                            new InternalQName(null, "item12")),
                                        IdGenerator.generate(),
                                        0,
                                        new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                        new InternalQName[0],
                                        1,
                                        localItem1.getIdentifier(),
                                        new AccessControlList());
    // create /testItem1/item12/item122
    localItem122 = new TransientNodeData(QPath.makeChildPath(localItem12.getQPath(),
                                                             new InternalQName(null, "item122")),
                                         IdGenerator.generate(),
                                         0,
                                         new InternalQName(Constants.NS_NT_URI, "unstructured"),
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
                                       new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                       new InternalQName[0],
                                       1,
                                       Constants.ROOT_UUID,
                                       new AccessControlList());

    // create /testItem3
    final String testItem3 = "testItem3";
    localItem3 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                           new InternalQName(null, testItem3)),
                                       IdGenerator.generate(),
                                       0,
                                       new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                       new InternalQName[0],
                                       2,
                                       Constants.ROOT_UUID,
                                       new AccessControlList());

    // create /testItem1
    remoteItem1 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                            new InternalQName(null, testItem1)),
                                        IdGenerator.generate(),
                                        0,
                                        new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                        new InternalQName[0],
                                        0,
                                        Constants.ROOT_UUID,
                                        new AccessControlList());
    // create /testItem1/item11
    remoteItem11 = new TransientNodeData(QPath.makeChildPath(remoteItem1.getQPath(),
                                                             new InternalQName(null, "item11")),
                                         IdGenerator.generate(),
                                         0,
                                         new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                         new InternalQName[0],
                                         0,
                                         remoteItem1.getIdentifier(),
                                         new AccessControlList());
    // create /testItem1/item11/item112
    remoteItem112 = new TransientNodeData(QPath.makeChildPath(remoteItem11.getQPath(),
                                                              new InternalQName(null, "item112")),
                                          IdGenerator.generate(),
                                          0,
                                          new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                          new InternalQName[0],
                                          0,
                                          remoteItem11.getIdentifier(),
                                          new AccessControlList());    
    // create /testItem1/item12
    remoteItem12 = new TransientNodeData(QPath.makeChildPath(remoteItem1.getQPath(),
                                                             new InternalQName(null, "item12")),
                                         IdGenerator.generate(),
                                         0,
                                         new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                         new InternalQName[0],
                                         1,
                                         remoteItem1.getIdentifier(),
                                         new AccessControlList());
    // create /testItem1/item12/item121
    remoteItem121 = new TransientNodeData(QPath.makeChildPath(remoteItem12.getQPath(),
                                                              new InternalQName(null, "item121")),
                                          IdGenerator.generate(),
                                          0,
                                          new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                          new InternalQName[0],
                                          0,
                                          remoteItem12.getIdentifier(),
                                          new AccessControlList());

    // create /testItem2
    remoteItem2 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                            new InternalQName(null, testItem2)),
                                        IdGenerator.generate(),
                                        0,
                                        new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                        new InternalQName[0],
                                        0,
                                        Constants.ROOT_UUID,
                                        new AccessControlList());

    // create /testItem3
    remoteItem3 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                            new InternalQName(null, testItem3)),
                                        IdGenerator.generate(),
                                        0,
                                        new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                        new InternalQName[0],
                                        2,
                                        Constants.ROOT_UUID,
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

  protected ItemState findState(List<ItemState> changes, QPath path) {
    for (ItemState st : changes) {
      if (st.getData().getQPath().equals(path))
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
}
