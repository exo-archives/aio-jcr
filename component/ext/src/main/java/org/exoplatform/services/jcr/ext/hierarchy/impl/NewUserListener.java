/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.ext.hierarchy.impl;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.ext.hierarchy.impl.HierarchyConfig.JcrPath;
import org.exoplatform.services.log.LogService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;

/**
 * Created by The eXo Platform SARL
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
  
  private static final String USERS_PATH = "usersPath";
  
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
      nodeHierarchyCreatorService_.createNode(userNode, jcrPath.getPath(), jcrPath.getNodeType(), 
          jcrPath.getMixinTypes(), nodeHierarchyCreatorService_.getPermissions(jcrPath.getPermissions())) ;
    }    
    usersHome.save();
    session.save();
    session.logout();
  }
  
  public void preDelete(User user) {
    Session session;
    try {
      //use a anonymous connection for the configuration as the user is not
      // authentified at that time
      List<RepositoryEntry> repositories = jcrService_.getConfig().getRepositoryConfigurations() ;
      String defaultRepository = jcrService_.getDefaultRepository().getConfiguration().getName() ;
      for(RepositoryEntry repo : repositories) {
        try{
          session = jcrService_.getRepository(repo.getName()).login();
          Node usersHome = (Node) session.getItem(
              nodeHierarchyCreatorService_.getJcrPath(USERS_PATH));
          usersHome.getNode(user.getUserName()).remove();
          usersHome.save();
          session.save();
          session.logout();
          if(repo.getName().equals(defaultRepository)) {
            // Manage backup workspace
            session = jcrService_.getRepository(defaultRepository).login(nodeHierarchyCreatorService_.getBackupWorkspace());
            usersHome = (Node) session.getItem(userPath_);
            usersHome.getNode(user.getUserName()).remove();
            usersHome.save();
            session.save();
            session.logout();
          }  
        }catch(Exception e) {          
        }       
      }
    } catch (PathNotFoundException ex) {
      log_.info("Can not delete home dir of user " + user.getUserName());
    } catch (RepositoryException e) {
      log_.error("RepositoryException while trying to delete a user home dir", e);
    } catch (RepositoryConfigurationException e) {
      log_.error("RepositoryException while trying to delete a user home dir", e);
    }
  }
}
