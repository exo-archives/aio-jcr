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
package org.exoplatform.services.jcr.ext.hierarchy.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.ext.hierarchy.impl.HierarchyConfig.JcrPath;
import org.exoplatform.services.jcr.ext.hierarchy.impl.HierarchyConfig.Permission;
import org.exoplatform.services.log.LogService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 15, 2007 11:13:12 AM
 */
public class NewUserListener extends UserEventListener {
  
  private HierarchyConfig config_;
  private RepositoryService jcrService_;
  
  private Log log_;
  private NodeHierarchyCreator nodeHierarchyCreatorService_;  
  private String userPath_ ;
  
  final static private String USERS_PATH = "usersPath";
  final static String NT_UNSTRUCTURED = "nt:unstructured".intern() ;
  
  public NewUserListener(RepositoryService jcrService,
                         NodeHierarchyCreator nodeHierarchyCreatorService, 
                         LogService logService,
                         InitParams params)    throws Exception {
    jcrService_ = jcrService;
    nodeHierarchyCreatorService_ = nodeHierarchyCreatorService;
    log_ = logService.getLog(getClass().getName());
    config_ = (HierarchyConfig) params.getObjectParamValues(HierarchyConfig.class).get(0);
    userPath_ = nodeHierarchyCreatorService.getJcrPath(USERS_PATH) ;
  }
  
  @SuppressWarnings("unused")
  public void preSave(User user, boolean isNew) throws Exception {        
    String userName = user.getUserName();
    List<RepositoryEntry> repositories = jcrService_.getConfig().getRepositoryConfigurations() ;
    for(RepositoryEntry repo : repositories) {
      processUserStructure(repo.getName(), userName);
    }
  }
  
  @SuppressWarnings("unchecked")
  private void processUserStructure(String repository, String userName) throws Exception {           
    ManageableRepository manageableRepository = jcrService_.getRepository(repository) ;
    String systemWorkspace = manageableRepository.getConfiguration().getDefaultWorkspaceName() ;
    Session session = manageableRepository.getSystemSession(systemWorkspace);           
    Node usersHome = (Node) session.getItem(userPath_);     
    Node userNode = null ;
    try {
      userNode = (Node) session.getItem(userPath_ + "/" + userName) ;
    } catch(PathNotFoundException e) {
      userNode = usersHome.addNode(userName) ;
    }
    List jcrPaths = config_.getJcrPaths() ;
    for(JcrPath jcrPath : (List<JcrPath>)jcrPaths) {
      createNode(userNode, jcrPath.getPath(), jcrPath.getNodeType(), jcrPath.getMixinTypes(), 
          getPermissions(jcrPath.getPermissions(),userName)) ;
    }    
    usersHome.save();
    session.save();
    session.logout();
  }
  
  public void preDelete(User user) {
    Session session;
      //use a anonymous connection for the configuration as the user is not
      // authentified at that time
    List<RepositoryEntry> repositories = jcrService_.getConfig().getRepositoryConfigurations() ;
    for(RepositoryEntry repo : repositories) {
      try{
        session = jcrService_.getRepository(repo.getName()).login();
        Node usersHome = (Node) session.getItem(nodeHierarchyCreatorService_.getJcrPath(USERS_PATH));
        usersHome.getNode(user.getUserName()).remove();
        usersHome.save();
        session.save();
        session.logout();
      }catch(Exception e) {
      }       
    }
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
  
  private Map getPermissions(List<Permission> permissions,String userId) {
    Map<String, String[]> permissionsMap = new HashMap<String, String[]>();
    permissionsMap.put(userId,PermissionType.ALL);
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
