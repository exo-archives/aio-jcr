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
import java.util.List;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionDataManager;
import org.exoplatform.services.jcr.impl.core.SessionImpl;

/**
 * Created by The eXo Platform SAS Author : Karpenko Sergiy exo@exoplatform.com
 */
public class TestItemDataExportVisitor extends BaseStandaloneTest {

  public void testGetItemAddStates() throws Exception {
    NodeImpl n = (NodeImpl) root.addNode("test", "nt:unstructured");
    root.save();

    NodeData d = (NodeData) n.getData();
    ItemDataExportVisitor vis = new ItemDataExportVisitor(d,
                                                          ((SessionImpl) session).getWorkspace()
                                                                                 .getNodeTypesHolder(),
                                                          ((SessionImpl) session).getTransientNodesManager());

    d.accept(vis);
    List<ItemState> list = vis.getItemAddStates();

    SessionDataManager dataManager = ((SessionImpl) session).getTransientNodesManager();

    List<ItemState> expl = new ArrayList<ItemState>();

    ItemState is1 = new ItemState(d, ItemState.ADDED, false, d.getQPath());
    expl.add(is1);

    for (PropertyData data : dataManager.getChildPropertiesData(d)) {
      ItemState is = new ItemState(data, ItemState.ADDED, false, d.getQPath());
      expl.add(is);
    }

    checkList(list, expl);
  }

  public void testGetItemAddStatesSubNodes() throws Exception {
    NodeImpl n = (NodeImpl) root.addNode("test", "nt:unstructured");
    n.addNode("secondName", "nt:base");
    root.save();

    NodeData d = (NodeData) n.getData();
    ItemDataExportVisitor vis = new ItemDataExportVisitor(d,
                                                          ((SessionImpl) session).getWorkspace()
                                                                                 .getNodeTypesHolder(),
                                                          ((SessionImpl) session).getTransientNodesManager());

    d.accept(vis);
    List<ItemState> list = vis.getItemAddStates();
    assertEquals(8, list.size());
  }

  public void testGetItemVersion() throws Exception {
    NodeImpl nr = (NodeImpl) root.addNode("test", "nt:unstructured");
    NodeImpl n = (NodeImpl) nr.addNode("versionName", "nt:unstructured");
    n.addMixin("mix:versionable");
    root.save();

    n.setProperty("myprop", "propval");
    root.save();
    n.checkin();

    NodeData p = (NodeData) nr.getData();
    NodeData d = (NodeData) n.getData();
    ItemDataExportVisitor vis = new ItemDataExportVisitor(p,
                                                          ((SessionImpl) session).getWorkspace()
                                                                                 .getNodeTypesHolder(),
                                                          ((SessionImpl) session).getTransientNodesManager());

    d.accept(vis);
    List<ItemState> list = vis.getItemAddStates();
    assertEquals(21, list.size());
  }

  public void testGetItemRoot() throws Exception {
    root.addNode("test", "nt:unstructured");
    root.save();

    NodeData p = (NodeData) ((NodeImpl) root).getData();

    ItemDataExportVisitor vis = new ItemDataExportVisitor(p,
                                                          ((SessionImpl) session).getWorkspace()
                                                                                 .getNodeTypesHolder(),
                                                          ((SessionImpl) session).getTransientNodesManager());

    p.accept(vis);
    List<ItemState> list = vis.getItemAddStates();
    ItemState elem = list.get(0);

    assertEquals(p.getQPath(), elem.getAncestorToSave());
    assertEquals(p.getQPath(), elem.getData().getQPath());
  }

  private boolean hasState(List<ItemState> changes, ItemState expected, boolean respectId) {
    for (ItemState st : changes) {
      if (st.getData().getQPath().equals(expected.getData().getQPath())
          && st.getState() == expected.getState()
          && (respectId ? st.getData().getIdentifier().equals(expected.getData().getIdentifier())
                       : true))
        return true;
    }
    return false;
  }

  private void checkList(List<ItemState> changes, List<ItemState> expected) throws Exception {

    assertEquals(expected.size(), changes.size());

    for (int i = 0; i < expected.size(); i++) {
      ItemState expect = expected.get(i);
      ItemState elem = changes.get(i);

      assertEquals(expect.getState(), elem.getState());
      assertEquals(expect.getAncestorToSave(), elem.getAncestorToSave());
      ItemData expData = expect.getData();
      ItemData elemData = elem.getData();
      assertEquals(expData.getQPath(), elemData.getQPath());
      assertEquals(expData.isNode(), elemData.isNode());
      assertEquals(expData.getIdentifier(), elemData.getIdentifier());
      assertEquals(expData.getParentIdentifier(), elemData.getParentIdentifier());

      if (!expData.isNode()) {
        PropertyData expProp = (PropertyData) expData;
        PropertyData elemProp = (PropertyData) elemData;
        assertEquals(expProp.getType(), elemProp.getType());
        assertEquals(expProp.isMultiValued(), elemProp.isMultiValued());

        List<ValueData> expValDat = expProp.getValues();
        List<ValueData> elemValDat = elemProp.getValues();
        assertEquals(expValDat.size(), elemValDat.size());
        for (int j = 0; j < expValDat.size(); j++) {
          assertTrue(java.util.Arrays.equals(expValDat.get(j).getAsByteArray(),
                                             elemValDat.get(j).getAsByteArray()));
        }
      }
    }
  }

}
