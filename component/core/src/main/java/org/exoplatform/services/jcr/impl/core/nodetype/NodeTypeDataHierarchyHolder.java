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
package org.exoplatform.services.jcr.impl.core.nodetype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.exoplatform.services.jcr.core.nodetype.NodeTypeData;
import org.exoplatform.services.jcr.datamodel.InternalQName;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author Gennady Azarenkov
 * @version $Id: NodeTypesHierarchyHolder.java 11907 2008-03-13 15:36:21Z ksm $
 */
public class NodeTypeDataHierarchyHolder {

  private final Map<InternalQName, NodeTypeHolder> nodeTypes;

  class NodeTypeHolder {
    
    final NodeTypeData nodeType;
    
    final Set<InternalQName> superTypes;
    
    NodeTypeHolder(NodeTypeData nodeType, Set<InternalQName> superTypes) {
      this.nodeType = nodeType;
      this.superTypes = superTypes;
    }
  }
  
  public NodeTypeDataHierarchyHolder() {
    nodeTypes = new ConcurrentHashMap<InternalQName, NodeTypeHolder>();
  }

  public NodeTypeData getNodeType(final InternalQName nodeTypeName) {
    final NodeTypeHolder nt = nodeTypes.get(nodeTypeName);
    return nt != null ? nt.nodeType : null; 
  }
  
  public Set<InternalQName> getSupertypes(final InternalQName nodeTypeName) {
    final NodeTypeHolder nt = nodeTypes.get(nodeTypeName);
    return nt != null ? nt.superTypes : null;
  }  
  
  public Collection<NodeTypeData> getAllNodeTypes() {
    Collection<NodeTypeHolder> hs = nodeTypes.values();
    List<NodeTypeData> nts = new ArrayList<NodeTypeData>(hs.size());
    for (NodeTypeHolder nt: hs) {
      nts.add(nt.nodeType);
    }
    return nts;
  }  
  
  public boolean isNodeType(final InternalQName testTypeName, final InternalQName... typesNames) {

    for (InternalQName typeName : typesNames) {
      if (testTypeName.equals(typeName))
        return true;

      NodeTypeHolder nt = nodeTypes.get(typeName);
      if (nt != null && (nt.superTypes.contains(testTypeName)))
        return true;
    }
    
    return false;
  }

  void addNodeType(final NodeTypeData nodeType) {
    final Set<InternalQName> supers = new HashSet<InternalQName>();
    fillSupertypes(supers, nodeType.getDeclaredSupertypeNames());
    nodeTypes.put(nodeType.getName(), new NodeTypeHolder(nodeType, supers));
  }

  private void fillSupertypes(final Collection<InternalQName> list, final InternalQName[] supers) {
    if (supers != null) {
      for (InternalQName su : supers) {
        list.add(su);
        addSupertypes(list, nodeTypes.get(su).superTypes);
      }
    }
  }

  private void addSupertypes(final Collection<InternalQName> list,
                             final Collection<InternalQName> supers) {
    if (supers != null) {
      for (InternalQName su : supers) {
        list.add(su);
        addSupertypes(list, nodeTypes.get(su).superTypes);
      }
    }
  }

}
