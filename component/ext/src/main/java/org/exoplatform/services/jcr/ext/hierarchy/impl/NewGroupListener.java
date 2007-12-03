/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.ext.hierarchy.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.ext.hierarchy.impl.HierarchyConfig.JcrPath;
import org.exoplatform.services.jcr.ext.hierarchy.impl.HierarchyConfig.Permission;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupEventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 15, 2007 11:13:25 AM
 */
public class NewGroupListener extends GroupEventListener {
  
  private HierarchyConfig config_;
  private RepositoryService jcrService_;
  
  private String groupsPath_ ;
  final static String NT_UNSTRUCTURED = "nt:unstructured".intern() ;
  
  final static private String GROUPS_PATH = "groupsPath";
  
  public NewGroupListener(RepositoryService jcrService,
      NodeHierarchyCreator nodeHierarchyCreatorService, 
      InitParams params) throws Exception {
    jcrService_ = jcrService;
    config_ = (HierarchyConfig) params.getObjectParamValues(HierarchyConfig.class).get(0);
    groupsPath_ = nodeHierarchyCreatorService.getJcrPath(GROUPS_PATH) ; 
  }
  
  @SuppressWarnings("unused")
  public void preSave(Group group, boolean isNew) throws Exception { 
    String  groupId = null ;
    String parentId = group.getParentId() ;
    if(parentId == null || parentId.length() == 0) groupId = "/" + group.getGroupName() ;
    else groupId = parentId + "/" + group.getGroupName() ;
    List<RepositoryEntry> repositories = jcrService_.getConfig().getRepositoryConfigurations() ;
    for(RepositoryEntry repo : repositories) {
      buildGroupStructure(repo.getName(), groupId);
    }
  }
  
  public void preDelete(Group group) throws Exception {
    String  groupId = null ;
    String parentId = group.getParentId() ;
    if(parentId == null || parentId.length() == 0) groupId = "/" + group.getGroupName() ;
    else groupId = parentId + "/" + group.getGroupName() ;
    List<RepositoryEntry> repositories = jcrService_.getConfig().getRepositoryConfigurations() ;
    for(RepositoryEntry repo : repositories) {
      removeGroup(repo.getName(), groupId);
    }
  }
  
  private void removeGroup(String repoName, String groupId) throws Exception {
    ManageableRepository manageableRepository = jcrService_.getRepository(repoName) ;
    String systemWorkspace = manageableRepository.getConfiguration().getDefaultWorkspaceName() ;
    Session session = manageableRepository.getSystemSession(systemWorkspace);           
    Node groupsHome = (Node) session.getItem(groupsPath_);
    Node groupNode = (Node) session.getItem(groupsPath_ + groupId) ;
    groupNode.remove() ;
    groupsHome.save() ;
    session.save() ;
    session.logout() ;
  }
  
  @SuppressWarnings("unchecked")
  private void buildGroupStructure(String repository, String groupId) throws Exception {           
    ManageableRepository manageableRepository = jcrService_.getRepository(repository) ;
    String systemWorkspace = manageableRepository.getConfiguration().getDefaultWorkspaceName() ;
    Session session = manageableRepository.getSystemSession(systemWorkspace);           
    Node groupsHome = (Node) session.getItem(groupsPath_);
    Node groupNode = null ;
    try {
      groupNode = (Node) session.getItem(groupsPath_ + groupId) ;
    } catch(PathNotFoundException e) {
      groupNode = groupsHome.addNode(groupId.substring(1, groupId.length())) ;
    }
    List jcrPaths = config_.getJcrPaths() ;
    for(JcrPath jcrPath : (List<JcrPath>)jcrPaths) {
      createNode(groupNode, jcrPath.getPath(), jcrPath.getNodeType(), jcrPath.getMixinTypes(), 
          getPermissions(jcrPath.getPermissions())) ;
    }
    groupsHome.save();
    session.save();
    session.logout();
  }
  
  @SuppressWarnings("unchecked")
  private void createNode(Node rootNode, String path, String nodeType, List<String> mixinTypes, 
      Map permissions) throws Exception {    
    Node node = rootNode ;
    for (String token : path.split("/")) {
      try {
        node = node.getNode(token) ;
      } catch(PathNotFoundException e) {
        if(nodeType == null || nodeType.length() == 0) nodeType = NT_UNSTRUCTURED ;
        node = node.addNode(token, nodeType);
        if (node.canAddMixin("exo:privilegeable")) node.addMixin("exo:privilegeable");
        if(permissions != null) ((ExtendedNode)node).setPermissions(permissions);
        if(mixinTypes.size() > 0) {
          for(String mixin : mixinTypes) {
            if(node.canAddMixin(mixin)) node.addMixin(mixin) ;
          }
        }
      }      
    }
  }
  
  private Map getPermissions(List<Permission> permissions) {
    Map<String, String[]> permissionsMap = new HashMap<String, String[]>();
    for(Permission permission : permissions) {
      StringBuilder strPer = new StringBuilder() ;
      if("true".equals(permission.getRead())) strPer.append(PermissionType.READ) ;
      if("true".equals(permission.getAddNode())) strPer.append(",").append(PermissionType.ADD_NODE) ;
      if("true".equals(permission.getSetProperty())) strPer.append(",").append(PermissionType.SET_PROPERTY) ;
      if("true".equals(permission.getRemove())) strPer.append(",").append(PermissionType.REMOVE) ;
      permissionsMap.put(permission.getIdentity(), strPer.toString().split(",")) ;
    }
    return permissionsMap;
  }
}
