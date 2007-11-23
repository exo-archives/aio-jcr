/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.ext.hierarchy.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.ext.hierarchy.impl.HierarchyConfig.JcrPath;
import org.exoplatform.services.jcr.ext.hierarchy.impl.HierarchyConfig.Permission;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 15, 2007 2:21:57 PM
 */
public class NodeHierarchyCreatorImpl implements NodeHierarchyCreator, Startable{

  final static String NT_UNSTRUCTURED = "nt:unstructured".intern() ;

  private RepositoryService jcrService_;

  List<AddPathPlugin> pathPlugins_ = new ArrayList<AddPathPlugin>();

  private PropertiesParam propertiesParam_;

  public NodeHierarchyCreatorImpl(InitParams params,
      RepositoryService jcrService) throws Exception {
    jcrService_ = jcrService;
    propertiesParam_ = params.getPropertiesParam("cms.configuration");
  }

  public void start() {    
    try {
      processAddPathPlugin() ;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void stop() {
  }

  public void init(String repository) throws Exception {   
    initBasePath(repository) ;
  }  
  
  @SuppressWarnings("unchecked")
  public void createNode(Node rootNode, String path, String nodeType, List<String> mixinTypes, 
      Map permissions) throws Exception {    
    String[] tokens = path.split("/") ;
    Node node = rootNode ;
    for (int i = 0; i < tokens.length; i++) {
      String token = tokens[i];
      if(node.hasNode(token)) {
        node = node.getNode(token) ;
      }else {
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
  
  public Map getPermissions(List<Permission> permissions) {
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
  
  @SuppressWarnings("unchecked")
  private void processAddPathPlugin()  throws Exception {           
    Session session = null ;
    for(AddPathPlugin pathPlugin:pathPlugins_) {
      HierarchyConfig hierarchyConfig = pathPlugin.getPaths() ;
      String repository = hierarchyConfig.getRepository() ;
      List<JcrPath> jcrPaths = hierarchyConfig.getJcrPaths() ;
      for(String workspaceName:hierarchyConfig.getWorkspaces()) {
        session = jcrService_.getRepository(repository).getSystemSession(workspaceName) ;
        Node rootNode = session.getRootNode() ;
        for(JcrPath jcrPath:jcrPaths) {                    
          String nodeType = jcrPath.getNodeType() ;
          if(nodeType == null || nodeType.length() == 0) nodeType = NT_UNSTRUCTURED ;
          List<String> mixinTypes = jcrPath.getMixinTypes() ;
          if(mixinTypes == null) mixinTypes = new ArrayList<String>() ;
          createNode(rootNode, jcrPath.getPath(),nodeType, mixinTypes, 
              getPermissions(jcrPath.getPermissions()));
        }
        session.save() ;
        session.logout() ;
      }
    }    
  }
  
  @SuppressWarnings("unchecked")
  private void initBasePath(String repository) throws Exception {    
    Session session = null ;
    ManageableRepository manageableRepository = jcrService_.getRepository(repository);
    String defaultWorkspace = manageableRepository.getConfiguration().getDefaultWorkspaceName();
    String systemWorkspace = manageableRepository.getConfiguration().getSystemWorkspaceName();
    boolean isSameWorksapce = defaultWorkspace.equalsIgnoreCase(systemWorkspace);        
    String[] workspaceNames = manageableRepository.getWorkspaceNames();
    for(AddPathPlugin pathPlugin:pathPlugins_) {
      HierarchyConfig hierarchyConfig = pathPlugin.getPaths() ;
      List<JcrPath> jcrPaths = hierarchyConfig.getJcrPaths() ;
      for(String workspaceName:workspaceNames) {
        if(!isSameWorksapce && workspaceName.equalsIgnoreCase(systemWorkspace)) continue;
        session = manageableRepository.getSystemSession(workspaceName);
        Node rootNode = session.getRootNode() ;
        for(JcrPath jcrPath:jcrPaths) {                    
          String nodeType = jcrPath.getNodeType() ;
          if(nodeType == null || nodeType.length() == 0) nodeType = NT_UNSTRUCTURED ;
          List<String> mixinTypes = jcrPath.getMixinTypes() ;
          if(mixinTypes == null) mixinTypes = new ArrayList<String>() ;
          createNode(rootNode, jcrPath.getPath(),nodeType, mixinTypes, 
              getPermissions(jcrPath.getPermissions()));
        }
        session.save() ;
        session.logout() ;
      }
    }       
  }

  public String getBackupWorkspace() {
    return propertiesParam_.getProperty("backup");
  }

  public String getContentLocation() {
    return propertiesParam_.getProperty("contentLocation");
  }

  public String getJcrPath(String alias) { 
    for (int j = 0; j < pathPlugins_.size(); j++) {
      HierarchyConfig config = pathPlugins_.get(j).getPaths();
      List jcrPaths = config.getJcrPaths();
      for (Iterator iter = jcrPaths.iterator(); iter.hasNext();) {
        HierarchyConfig.JcrPath jcrPath = (HierarchyConfig.JcrPath) iter.next();
        if (jcrPath.getAlias().equals(alias)) {
          return jcrPath.getPath();
        }
      }
    }
    return null;
  }
  
  public void addPlugin(ComponentPlugin plugin) {
    if (plugin instanceof AddPathPlugin) pathPlugins_.add((AddPathPlugin) plugin);    
  }

  @SuppressWarnings("unused")
  public ComponentPlugin removePlugin(String name) {
    return null;
  }

  public Collection getPlugins() {
    return null;
  }    
}