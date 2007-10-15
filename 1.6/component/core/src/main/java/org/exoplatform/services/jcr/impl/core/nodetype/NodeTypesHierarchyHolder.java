/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core.nodetype;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.jcr.datamodel.InternalQName;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: NodeTypesHierarchyHolder.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class NodeTypesHierarchyHolder {

  private final Map <InternalQName, Set <InternalQName>> nodeTypes;
  
  public NodeTypesHierarchyHolder() {
    nodeTypes = new HashMap <InternalQName, Set <InternalQName>>();
  }
  
  public boolean isNodeType(InternalQName testTypeName, InternalQName superTypeName) {
    if(testTypeName.equals(superTypeName))
      return true;
    Set<InternalQName> testTypes = nodeTypes.get(testTypeName);
    if(testTypes == null)
      return false;
    return testTypes.contains(superTypeName);
  }
  
  public Set <InternalQName> getSuperypes(InternalQName nodeTypeName) {
    return nodeTypes.get(nodeTypeName);
  }
  
  void addNodeType(ExtendedNodeType nodeType) {
    Set <InternalQName> stSet = new HashSet<InternalQName>();
    fillSupertypes(stSet, nodeType);
    nodeTypes.put(nodeType.getQName(), stSet);
  }
  
  private void fillSupertypes(Set<InternalQName> list, ExtendedNodeType subtype) {
    if (subtype.getDeclaredSupertypes() != null) {
      for (int i = 0; i < subtype.getDeclaredSupertypes().length; i++) {
        ExtendedNodeType nt = (ExtendedNodeType)subtype.getDeclaredSupertypes()[i];
        list.add(nt.getQName());
        fillSupertypes(list, nt);
      }
    }
  }

}
