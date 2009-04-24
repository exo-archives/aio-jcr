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

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.serialization.ObjectReader;
import org.exoplatform.services.jcr.dataflow.serialization.ObjectWriter;
import org.exoplatform.services.jcr.datamodel.IllegalNameException;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionDataManager;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.dataflow.serialization.ItemStateReader;
import org.exoplatform.services.jcr.impl.dataflow.serialization.ObjectReaderImpl;
import org.exoplatform.services.jcr.impl.dataflow.serialization.ObjectWriterImpl;

/**
 * Created by The eXo Platform SAS Author : Karpenko Sergiy karpenko.sergiy@gmail.com.
 */
public class ItemDataExportVisitorTest extends BaseStandaloneTest {

  static int suf = 0;

  public void testGetItemAddStates() throws Exception {
    NodeImpl n = (NodeImpl) root.addNode("test", "nt:unstructured");
    root.save();

    NodeData d = (NodeData) n.getData();

    File chLogFile = File.createTempFile("chLog", "" + (suf++));
    ObjectWriter out = new ObjectWriterImpl(new FileOutputStream(chLogFile));

    ItemDataExportVisitor vis = new ItemDataExportVisitor(out,
                                                          d,
                                                          ((SessionImpl) session).getWorkspace()
                                                                                 .getNodeTypesHolder(),
                                                          ((SessionImpl) session).getTransientNodesManager(),
                                                          ((SessionImpl) session).getTransientNodesManager());

    d.accept(vis);
    out.close();

    List<ItemState> list = getItemStatesFromChLog(chLogFile);

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
    NodeImpl sn = (NodeImpl) n.addNode("secondName", "nt:base");
    root.save();

    NodeData d = (NodeData) n.getData();

    File chLogFile = File.createTempFile("chLog", "" + (suf++));
    ObjectWriter out = new ObjectWriterImpl(new FileOutputStream(chLogFile));
    ItemDataExportVisitor vis = new ItemDataExportVisitor(out,
                                                          d,
                                                          ((SessionImpl) session).getWorkspace()
                                                                                 .getNodeTypesHolder(),
                                                          ((SessionImpl) session).getTransientNodesManager(),
                                                          ((SessionImpl) session).getTransientNodesManager());

    d.accept(vis);
    out.close();
    List<ItemState> list = getItemStatesFromChLog(chLogFile);

    SessionDataManager dataManager = ((SessionImpl) session).getTransientNodesManager();

    List<ItemState> expl = new ArrayList<ItemState>();

    ItemState is1 = new ItemState(d, ItemState.ADDED, false, d.getQPath());
    expl.add(is1);

    for (PropertyData data : dataManager.getChildPropertiesData(d)) {
      ItemState is = new ItemState(data, ItemState.ADDED, false, d.getQPath());
      expl.add(is);
    }

    NodeData sd = (NodeData) sn.getData();
    ItemState is2 = new ItemState(sd, ItemState.ADDED, false, d.getQPath());
    expl.add(is2);

    for (PropertyData data : dataManager.getChildPropertiesData(sd)) {
      ItemState is = new ItemState(data, ItemState.ADDED, false, sd.getQPath());
      expl.add(is);
    }

    checkList(list, expl);
  }

  public void testGetItemVersionSystemWS() throws Exception {
    NodeImpl nr = (NodeImpl) root.addNode("test", "nt:unstructured");
    NodeImpl n = (NodeImpl) nr.addNode("versionName", "nt:unstructured");
    n.addMixin("mix:versionable");
    root.save();

    n.setProperty("myprop", "propval");
    root.save();

    SessionDataManager dataManager = ((SessionImpl) session).getTransientNodesManager();

    NodeData p = (NodeData) nr.getData();
    NodeData d = (NodeData) n.getData();

    File chLogFile = File.createTempFile("chLog", "" + (suf++));
    ObjectWriter out = new ObjectWriterImpl(new FileOutputStream(chLogFile));

    ItemDataExportVisitor vis = new ItemDataExportVisitor(out,
                                                          p,
                                                          ((SessionImpl) session).getWorkspace()
                                                                                 .getNodeTypesHolder(),
                                                          ((SessionImpl) session).getTransientNodesManager(),
                                                          ((SessionImpl) session).getTransientNodesManager());
    d.accept(vis);
    out.close();

    List<ItemState> list = getItemStatesFromChLog(chLogFile);
    assertEquals(21, list.size());

    List<ItemState> expl = new ArrayList<ItemState>();

    ItemState is1 = new ItemState(d, ItemState.ADDED, false, p.getQPath());
    expl.add(is1);

    // get version history
    PropertyData property = (PropertyData) dataManager.getItemData(d,
                                                                   new QPathEntry(Constants.JCR_VERSIONHISTORY,
                                                                                  1));
    String ref;
    try {
      ref = ((TransientValueData) property.getValues().get(0)).getString();
    } catch (IOException e) {
      throw new RepositoryException(e);
    }

    NodeData verStorage = (NodeData) dataManager.getItemData(Constants.VERSIONSTORAGE_UUID);

    QPathEntry nam;
    try {
      nam = QPathEntry.parse("[]" + ref + ":1");
    } catch (IllegalNameException e) {
      throw new RepositoryException(e);
    }

    NodeData verHistory = (NodeData) dataManager.getItemData(verStorage, nam);

    ItemState vh = new ItemState(verHistory,
                                 ItemState.ADDED,
                                 false,
                                 Constants.JCR_VERSION_STORAGE_PATH);
    expl.add(vh);

    for (PropertyData data : dataManager.getChildPropertiesData(verHistory)) {
      ItemState is = new ItemState(data, ItemState.ADDED, false, verHistory.getQPath());
      expl.add(is);
    }

    for (NodeData data : dataManager.getChildNodesData(verHistory)) {
      ItemState cvh = new ItemState(data, ItemState.ADDED, false, verHistory.getQPath());
      expl.add(cvh);
      // add props
      for (PropertyData props : dataManager.getChildPropertiesData(data)) {
        ItemState is = new ItemState(props, ItemState.ADDED, false, data.getQPath());
        expl.add(is);
      }
    }

    // add original props
    for (PropertyData data : dataManager.getChildPropertiesData(d)) {
      ItemState is = new ItemState(data, ItemState.ADDED, false, d.getQPath());
      expl.add(is);
    }

    checkList(list, expl);
  }

  public void testGetItemVersionNotSystemWS() throws Exception {
    SessionImpl sessionWS1 = (SessionImpl) repository.login(credentials, "ws1");
    Node rootWS1 = sessionWS1.getRootNode();

    NodeImpl nr = (NodeImpl) rootWS1.addNode("test", "nt:unstructured");
    NodeImpl n = (NodeImpl) nr.addNode("versionName", "nt:unstructured");
    n.addMixin("mix:versionable");
    n.setProperty("myprop", "propval");
    sessionWS1.save();

    SessionDataManager systemDataManager = ((SessionImpl) session).getTransientNodesManager();
    SessionDataManager dataManager = ((SessionImpl) sessionWS1).getTransientNodesManager();

    NodeData p = (NodeData) nr.getData();
    NodeData d = (NodeData) n.getData();

    File chLogFile = File.createTempFile("chLog", "" + (suf++));
    ObjectWriter out = new ObjectWriterImpl(new FileOutputStream(chLogFile));

    ItemDataExportVisitor vis = new ItemDataExportVisitor(out,
                                                          p,
                                                          ((SessionImpl) session).getWorkspace()
                                                                                 .getNodeTypesHolder(),
                                                          dataManager,
                                                          systemDataManager);
    d.accept(vis);
    out.close();

    List<ItemState> list = getItemStatesFromChLog(chLogFile);
    assertEquals(21, list.size());

    List<ItemState> expl = new ArrayList<ItemState>();

    ItemState is1 = new ItemState(d, ItemState.ADDED, false, p.getQPath());
    expl.add(is1);

    // get version history
    PropertyData property = (PropertyData) dataManager.getItemData(d,
                                                                   new QPathEntry(Constants.JCR_VERSIONHISTORY,
                                                                                  1));
    String ref;
    try {
      ref = ((TransientValueData) property.getValues().get(0)).getString();
    } catch (IOException e) {
      throw new RepositoryException(e);
    }

    NodeData verStorage = (NodeData) systemDataManager.getItemData(Constants.VERSIONSTORAGE_UUID);

    QPathEntry nam;
    try {
      nam = QPathEntry.parse("[]" + ref + ":1");
    } catch (IllegalNameException e) {
      throw new RepositoryException(e);
    }

    NodeData verHistory = (NodeData) systemDataManager.getItemData(verStorage, nam);

    ItemState vh = new ItemState(verHistory,
                                 ItemState.ADDED,
                                 false,
                                 Constants.JCR_VERSION_STORAGE_PATH);
    expl.add(vh);

    for (PropertyData data : dataManager.getChildPropertiesData(verHistory)) {
      ItemState is = new ItemState(data, ItemState.ADDED, false, verHistory.getQPath());
      expl.add(is);
    }

    for (NodeData data : dataManager.getChildNodesData(verHistory)) {
      ItemState cvh = new ItemState(data, ItemState.ADDED, false, verHistory.getQPath());
      expl.add(cvh);
      // add props
      for (PropertyData props : dataManager.getChildPropertiesData(data)) {
        ItemState is = new ItemState(props, ItemState.ADDED, false, data.getQPath());
        expl.add(is);
      }
    }

    // add original props
    for (PropertyData data : dataManager.getChildPropertiesData(d)) {
      ItemState is = new ItemState(data, ItemState.ADDED, false, d.getQPath());
      expl.add(is);
    }

    checkList(list, expl);
  }

  public void testGetItemRoot() throws Exception {
    root.addNode("test", "nt:unstructured");
    root.save();

    NodeData p = (NodeData) ((NodeImpl) root).getData();
    File chLogFile = File.createTempFile("chLog", "" + (suf++));
    ObjectWriter out = new ObjectWriterImpl(new FileOutputStream(chLogFile));

    ItemDataExportVisitor vis = new ItemDataExportVisitor(out,
                                                          p,
                                                          ((SessionImpl) session).getWorkspace()
                                                                                 .getNodeTypesHolder(),
                                                          ((SessionImpl) session).getTransientNodesManager(),
                                                          ((SessionImpl) session).getTransientNodesManager());

    p.accept(vis);

    out.close();

    List<ItemState> list = getItemStatesFromChLog(chLogFile);
    ItemState elem = list.get(0);

    assertNotNull(elem.getAncestorToSave());
    assertEquals(p.getQPath(), elem.getData().getQPath());
  }

  public void testGetUnExistedItem() throws Exception {
    File chLogFile = File.createTempFile("chLog", "" + (suf++));
    ObjectWriter out = new ObjectWriterImpl(new FileOutputStream(chLogFile));
    out.close();

    List<ItemState> list = getItemStatesFromChLog(chLogFile);
    assertEquals(list.size(), 0);
  }

  private boolean hasState(List<ItemState> changes, ItemState expected, boolean respectId) {
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

  private void checkList(List<ItemState> changes, List<ItemState> expected) throws Exception {

    assertEquals(expected.size(), changes.size());

    for (int i = 0; i < expected.size(); i++) {
      ItemState expect = expected.get(i);
      ItemState elem = changes.get(i);

      assertEquals(expect.getState(), elem.getState());
      assertNotNull(elem.getAncestorToSave());
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

  protected List<ItemState> getItemStatesFromChLog(File f) throws Exception {

    ObjectReader in = new ObjectReaderImpl(new FileInputStream(f));
    ItemState elem;
    List<ItemState> list = new ArrayList<ItemState>();
    try {
      ItemStateReader rdr = new ItemStateReader(fileCleaner, maxBufferSize, holder);
      while (true) {
        elem = rdr.read(in);
        list.add(elem);
      }
    } catch (EOFException e) {
    }
    return list;
  }

}
