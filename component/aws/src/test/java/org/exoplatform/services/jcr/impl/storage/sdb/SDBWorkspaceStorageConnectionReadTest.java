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
package org.exoplatform.services.jcr.impl.storage.sdb;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.ItemExistsException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.util.SIDGenerator;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 13.10.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class SDBWorkspaceStorageConnectionReadTest extends SDBWorkspaceTestBase {

  /**
   * Test get Item by Id.
   * 
   * @throws Exception
   *           test error
   */
  public void testGetItemId() throws Exception {

    // prepare
    sdbConn.add(jcrRoot);
    sdbConn.add(testRoot);
    sdbConn.add(testProperty);
    sdbConn.commit();

    try {
      ItemData item = sdbConn.getItemData(testRoot.getIdentifier());

      assertTrue("Item is a Node", item.isNode());

      NodeData node = (NodeData) item;

      assertEquals("Node id should be same", testRoot.getIdentifier(), node.getIdentifier());

      assertEquals("Node path should be same", testRoot.getQPath(), node.getQPath());
    } catch (ItemExistsException e) {
      LOG.error("get Node error", e);
      fail(e.getMessage());
    } catch (RepositoryException e) {
      LOG.error("get Node error", e);
      fail(e.getMessage());
    }
  }

  /**
   * Test get Item by parent Id and Name .
   * 
   * @throws Exception
   *           test error
   */
  public void testGetItemNameParentId() throws Exception {

    // prepare
    sdbConn.add(jcrRoot);
    sdbConn.add(testRoot);
    sdbConn.add(testProperty);
    sdbConn.commit();

    try {
      ItemData item = sdbConn.getItemData(jcrRoot,
                                          testRoot.getQPath().getEntries()[testRoot.getQPath()
                                                                                   .getEntries().length - 1]);

      assertTrue("Item is a Node", item.isNode());

      NodeData node = (NodeData) item;

      assertEquals("Node id should be same", testRoot.getIdentifier(), node.getIdentifier());

      assertEquals("Node path should be same", testRoot.getQPath(), node.getQPath());
    } catch (ItemExistsException e) {
      LOG.error("get Node error", e);
      fail(e.getMessage());
    } catch (RepositoryException e) {
      LOG.error("get Node error", e);
      fail(e.getMessage());
    }
  }

  /**
   * Test get Node child nodes.
   * 
   * @throws Exception
   *           test error
   */
  public void testGetChildNodes() throws Exception {

    // prepare
    sdbConn.add(jcrRoot);
    sdbConn.add(testRoot);

    final int childsCount = 20;
    final NodeData[] childs = new NodeData[childsCount];
    for (int i = 0; i < childsCount; i++) {
      childs[i] = new TransientNodeData(QPath.makeChildPath(testRoot.getQPath(),
                                                            QPathEntry.parse("[]subNode" + i + ":1")),
                                        SIDGenerator.generate(),
                                        1,
                                        Constants.NT_RESOURCE,
                                        new InternalQName[] { Constants.EXO_PRIVILEGEABLE,
                                            Constants.EXO_OWNEABLE },
                                        i,
                                        testRoot.getIdentifier(),
                                        null);
      sdbConn.add(childs[i]);
    }

    sdbConn.commit();

    try {
      List<NodeData> nodes = sdbConn.getChildNodesData(testRoot);

      assertEquals("Nodes count is wrong ", childs.length, nodes.size());

      for (int i = 0; i < childs.length; i++) {
        NodeData orig = childs[i];
        NodeData stored = nodes.get(i);

        assertEquals("Node id should be same", orig.getIdentifier(), stored.getIdentifier());
        assertEquals("Node path should be same", orig.getQPath(), stored.getQPath());
        assertEquals("Node order number should be same",
                     orig.getOrderNumber(),
                     stored.getOrderNumber());
        assertEquals("Node primaty type should be same",
                     orig.getPrimaryTypeName(),
                     stored.getPrimaryTypeName());
        assertEquals("Node mixin type should be same",
                     orig.getMixinTypeNames()[0],
                     stored.getMixinTypeNames()[0]);
        assertEquals("Node mixin type should be same",
                     orig.getMixinTypeNames()[1],
                     stored.getMixinTypeNames()[1]);
      }
    } catch (ItemExistsException e) {
      LOG.error("get Child Nodes error", e);
      fail(e.getMessage());
    } catch (RepositoryException e) {
      LOG.error("get Child Nodes error", e);
      fail(e.getMessage());
    }
  }

  /**
   * Test get Node child properties.
   * 
   * @throws Exception
   *           test error
   */
  public void testGetChildProperties() throws Exception {

    // prepare
    sdbConn.add(jcrRoot);
    sdbConn.add(testRoot);

    final int childsCount = 20;
    final PropertyData[] childs = new PropertyData[childsCount];
    for (int i = 0; i < childsCount; i++) {
      TransientPropertyData child = new TransientPropertyData(QPath.makeChildPath(testRoot.getQPath(),
                                                                                  QPathEntry.parse("[]property"
                                                                                      + i + ":1")),
                                                              SIDGenerator.generate(),
                                                              1,
                                                              PropertyType.STRING,
                                                              testRoot.getIdentifier(),
                                                              false);

      List<ValueData> values = new ArrayList<ValueData>(1);
      values.add(new TransientValueData("property data #" + i));
      child.setValues(values);

      childs[i] = child;

      sdbConn.add(child);
    }

    sdbConn.commit();

    try {
      List<PropertyData> props = sdbConn.getChildPropertiesData(testRoot);

      assertEquals("Properties count is wrong ", childs.length, props.size());

      for (int i = 0; i < childs.length; i++) {
        PropertyData orig = childs[i];
        PropertyData stored = props.get(i);

        assertEquals("Property id should be same", orig.getIdentifier(), stored.getIdentifier());
        assertEquals("Property path should be same", orig.getQPath(), stored.getQPath());
        assertEquals("Property type should be same", orig.getType(), stored.getType());
        assertEquals("Property value should be same",
                     new String(orig.getValues().get(0).getAsByteArray()),
                     new String(stored.getValues().get(0).getAsByteArray()));
      }
    } catch (ItemExistsException e) {
      LOG.error("get Child Properties error", e);
      fail(e.getMessage());
    } catch (RepositoryException e) {
      LOG.error("get Child Properties error", e);
      fail(e.getMessage());
    }
  }

}
