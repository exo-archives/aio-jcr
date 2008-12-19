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
package org.exoplatform.services.jcr.impl.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.ValueData;

/**
 * Created by The eXo Platform SAS 15.05.2006 NodeData bulk reader.
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter
 *         Nedonosko</a>
 * @version $Id: NodeDataReader.java 11907 2008-03-13 15:36:21Z ksm $
 */
public class NodeDataReader2 extends ItemDataReader2 {

  private final HashMap<InternalQName, NodeInfo> nodes              = new HashMap<InternalQName, NodeInfo>();

  private final HashMap<InternalQName, NodeInfo> nodesByType        = new HashMap<InternalQName, NodeInfo>();

  private PropertyDataReader2                    nodePropertyReader = null;

  private final List<NodeData>                   skiped             = new ArrayList<NodeData>();

  private boolean                                rememberSkiped     = false;

  private class NodeInfo {
    private final InternalQName         nodeName;

    private final List<NodeDataReader2> childNodesReaders;

    NodeInfo(InternalQName nodeName, List<NodeDataReader2> childNodesReaders) {
      this.nodeName = nodeName;
      this.childNodesReaders = childNodesReaders;
    }

    public InternalQName getNodeName() {
      return nodeName;
    }

    public List<NodeDataReader2> getChildNodesReaders() {
      return childNodesReaders;
    }
  }

  public NodeDataReader2(NodeData node, DataManager dataManager) {
    super(node, dataManager);
  }

  public NodeDataReader2 forNodesByType(InternalQName name) {
    nodesByType.put(name, new NodeInfo(name, new ArrayList<NodeDataReader2>()));
    return this;
  }

  public NodeDataReader2 forNode(InternalQName name) {
    nodes.put(name, new NodeInfo(name, new ArrayList<NodeDataReader2>()));
    return this;
  }

  public List<NodeDataReader2> getNodes(InternalQName name) throws PathNotFoundException {
    List<NodeDataReader2> nr = nodes.get(name).getChildNodesReaders();
    if (nr.size() > 0)
      return nr;
    throw new PathNotFoundException("Node with name " + parent.getQPath().getAsString()
        + name.getAsString() + " not found");
  }

  public List<NodeDataReader2> getNodesByType(InternalQName typeName) throws PathNotFoundException {
    List<NodeDataReader2> nr = nodesByType.get(typeName).getChildNodesReaders();
    if (nr.size() > 0)
      return nr;
    throw new PathNotFoundException("Nodes with type " + typeName.getAsString()
        + " not found. Parent " + parent.getQPath().getAsString());
  }

  public ValueData getPropertyValue(InternalQName name) throws ValueFormatException,
                                                       PathNotFoundException,
                                                       RepositoryException {
    return nodePropertyReader.getPropertyValue(name);
  }

  public List<ValueData> getPropertyValues(InternalQName name) throws ValueFormatException,
                                                              PathNotFoundException,
                                                              RepositoryException {
    return nodePropertyReader.getPropertyValues(name);
  }

  /**
   * Read node properties
   * 
   * @param name
   * @param type
   * @param multiValued
   * @return
   */
  public PropertyDataReader2 forProperty(InternalQName name, int type) {
    if (nodePropertyReader == null) {
      nodePropertyReader = new PropertyDataReader2(parent, dataManager);
    }
    return nodePropertyReader.forProperty(name, type);
  }

  public void read() throws RepositoryException {

    cleanReaders();

    if (nodePropertyReader != null) {
      nodePropertyReader.read();
    }

    if (nodes.size() > 0 || nodesByType.size() > 0 || rememberSkiped) {

      List<NodeData> ndNodes = dataManager.getChildNodesData(parent);
      for (NodeData node : ndNodes) {

        boolean isSkiped = true;

        NodeDataReader2 cnReader = new NodeDataReader2(node, dataManager);

        NodeInfo nodeInfo = nodes.get(node.getQPath().getName());
        if (nodeInfo != null) {
          nodeInfo.getChildNodesReaders().add(cnReader);
          isSkiped = false;
        }

        NodeInfo nodesByTypeInfo = nodesByType.get(node.getPrimaryTypeName());
        if (nodesByTypeInfo != null) {
          nodesByTypeInfo.getChildNodesReaders().add(cnReader);
          isSkiped = false;
        }

        if (isSkiped && rememberSkiped) {
          skiped.add(node);
        }
      }
    }
  }

  private void cleanReaders() {
    for (Map.Entry<InternalQName, NodeInfo> nodesEntry : nodes.entrySet()) {
      nodesEntry.getValue().getChildNodesReaders().clear();
    }
    for (Map.Entry<InternalQName, NodeInfo> nodesEntry : nodesByType.entrySet()) {
      nodesEntry.getValue().getChildNodesReaders().clear();
    }
    skiped.clear();
  }

  public void clean() {
    nodes.clear();
    nodesByType.clear();
    skiped.clear();
    nodePropertyReader = null;
  }

  public boolean isRememberSkiped() {
    return rememberSkiped;
  }

  public void setRememberSkiped(boolean rememberSkiped) {
    this.rememberSkiped = rememberSkiped;
  }

  public List<NodeData> getSkiped() {
    return skiped;
  }
}
