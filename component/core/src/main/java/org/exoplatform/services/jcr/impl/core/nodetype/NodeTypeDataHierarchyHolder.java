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

import java.util.Collection;
import java.util.HashSet;
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

  private final Map<InternalQName, Set<InternalQName>> nodeTypes;

  public NodeTypeDataHierarchyHolder() {
    nodeTypes = new ConcurrentHashMap<InternalQName, Set<InternalQName>>();
  }

  public boolean isNodeType(final InternalQName testTypeName, final InternalQName superTypeName) {
    if (testTypeName.equals(superTypeName))
      return true;
    
    final Set<InternalQName> testTypes = nodeTypes.get(testTypeName);
    if (testTypes == null)
      return false;
    
    return testTypes.contains(superTypeName);
  }

  public Set<InternalQName> getSupertypes(final InternalQName nodeTypeName) {
    return nodeTypes.get(nodeTypeName);
  }

  void addNodeType(final NodeTypeData nodeType) {
    final Set<InternalQName> supers = new HashSet<InternalQName>();
    fillSupertypes(supers, nodeType.getDeclaredSupertypeNames());
    nodeTypes.put(nodeType.getName(), supers);
  }

  private void fillSupertypes(final Collection<InternalQName> list, final InternalQName[] supers) {
    if (supers != null) {
      for (InternalQName su : supers) {
        list.add(su);
        addSupertypes(list, nodeTypes.get(su));
      }
    }
  }
  
  private void addSupertypes(final Collection<InternalQName> list, final Collection<InternalQName> supers) {
    if (supers != null) {
      for (InternalQName su : supers) {
        list.add(su);
        addSupertypes(list, nodeTypes.get(su));
      }
    }
  }

}
