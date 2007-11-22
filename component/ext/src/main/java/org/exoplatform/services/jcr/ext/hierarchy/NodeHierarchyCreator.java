/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.ext.hierarchy;

import java.util.List;
import java.util.Map;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.hierarchy.impl.HierarchyConfig.Permission;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 15, 2007 10:10:10 AM
 */
public interface NodeHierarchyCreator {

  public String getBackupWorkspace();
  
  public String getContentLocation();
  
  public String getJcrPath(String alias);
  
  public void init(String repository) throws Exception ;
  
  public void createNode(Node rootNode, String path, String nodetype, List<String> mixinTypes, 
      Map permissions) throws Exception ;
  
  public Map getPermissions(List<Permission> permissions) ;
}