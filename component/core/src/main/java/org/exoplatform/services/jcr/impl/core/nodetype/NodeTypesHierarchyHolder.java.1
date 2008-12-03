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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.jcr.datamodel.InternalQName;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author Gennady Azarenkov
 * @version $Id: NodeTypesHierarchyHolder.java 11907 2008-03-13 15:36:21Z ksm $
 */

public class NodeTypesHierarchyHolder {

  private final Map<InternalQName, Set<InternalQName>> nodeTypes;

  public NodeTypesHierarchyHolder() {
    nodeTypes = new HashMap<InternalQName, Set<InternalQName>>();
  }

  public boolean isNodeType(InternalQName testTypeName, InternalQName superTypeName) {
    if (testTypeName.equals(superTypeName))
      return true;
    Set<InternalQName> testTypes = nodeTypes.get(testTypeName);
    if (testTypes == null)
      return false;
    return testTypes.contains(superTypeName);
  }

  public Set<InternalQName> getSuperypes(InternalQName nodeTypeName) {
    return nodeTypes.get(nodeTypeName);
  }

  void addNodeType(ExtendedNodeType nodeType) {
    Set<InternalQName> stSet = new HashSet<InternalQName>();
    fillSupertypes(stSet, nodeType);
    nodeTypes.put(nodeType.getQName(), stSet);
  }

  private void fillSupertypes(Set<InternalQName> list, ExtendedNodeType subtype) {
    if (subtype.getDeclaredSupertypes() != null) {
      for (int i = 0; i < subtype.getDeclaredSupertypes().length; i++) {
        ExtendedNodeType nt = (ExtendedNodeType) subtype.getDeclaredSupertypes()[i];
        list.add(nt.getQName());
        fillSupertypes(list, nt);
      }
    }
  }

}
