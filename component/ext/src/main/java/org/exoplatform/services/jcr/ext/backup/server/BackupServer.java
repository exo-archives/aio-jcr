/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.backup.server;

import java.io.ByteArrayInputStream;

import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.ws.commons.util.Base64;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.CredentialsImpl;
import org.exoplatform.services.jcr.ext.backup.BackupChain;
import org.exoplatform.services.jcr.ext.backup.BackupConfig;
import org.exoplatform.services.jcr.ext.backup.BackupJob;
import org.exoplatform.services.jcr.ext.backup.BackupManager;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.resource.ResourceContainer;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 24.02.2009
 *
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a> 
 * @version $Id: BackupServer.java 111 2008-11-11 11:11:11Z rainf0x $
 */

@Path("/backup-server/")
@Produces("text/plain")
public class BackupServer implements ResourceContainer {

  /**
   * Definition the constants to ReplicationTestService.
   */
  public static final class Constants {
    /**
     * The base path to this service.
     */
    public static final String BASE_URL               = "/rest/backup-server";

    /**
     * Definition the operation types.
     */
    public static final class OperationType {
      /**
       * Full backup only operation.
       */
      public static final String FULL_BACKUP_ONLY     = "fullOnly";

      /**
       * Full and incremental backup operations.
       */
      public static final String FULL_AND_INCREMENTAL = "fullAndIncremental";
      
      /**
       * Restore operations.
       */
      public static final String RESTORE              = "restore";
      
      /**
       * Stop backup operations.
       */
      public static final String STOP_BACKUP           = "stopBackup";
      
      /**
       * The backup status operations.
       */
      public static final String GET_STATUS            = "getStatus";
      
      /**
       * The drop workspace operations.
       */
      public static final String DROP_WORKSPACE        = "dropWorkspace";
      
      /**
       * OperationType constructor.
       */
      private OperationType() {
      }
    }

    /**
     * Constants constructor.
     */
    private Constants() {
    }
  }

  /**
   * The apache logger.
   */
  private static Log        log = ExoLogger.getLogger("ext.BackupServer");
  
  /**
   * The repository service.
   */
  private RepositoryService repositoryService;

  /**
   * The backup manager.
   */
  private BackupManager     backupManager;
  
  /**
   * ReplicationTestService constructor.
   * 
   * @param repoService the RepositoryService
   * @param backupManager the BackupManager
   */
  public BackupServer(RepositoryService repoService,
                                BackupManager backupManager) {
    this.repositoryService = repoService;
    this.backupManager = backupManager;

    log.info("ReplicationTestService inited");
  }
  
  /**
   * startFullBackup.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param userName the user name
   * @param password the password
   * @return Response return the response
   */
  @GET
  @Path("/{repositoryName}/{workspaceName}/{userName}/{password}/fullOnly")
  public Response startFullBackup(@PathParam("repositoryName") String repositoryName,
                              @PathParam("workspaceName") String workspaceName,
                              @PathParam("userName") String userName,
                              @PathParam("password") String password) {
    BackupConfig config = new BackupConfig();
    config.setBuckupType(BackupManager.FULL_BACKUP_ONLY);
    config.setRepository(repositoryName);
    config.setWorkspace(workspaceName);
    config.setBackupDir(backupManager.getBackupDirectory());

    String result = "OK\n";

    try {
      validateRepositoryName(repositoryName);
      validateWorkspaceName(repositoryName, workspaceName, userName, password);
      validateOneInstants(repositoryName, workspaceName);
      
      BackupChain backupChain = backupManager.startBackup(config);
      
      result += ("backup log : " + backupChain.getLogFilePath());
    } catch (Exception e) {
      result = "FAIL\n" + e.getMessage();
      log.error("Can't start backup", e);
    }

    return Response.ok(result).build();
  }
  
  /**
   * startBackup.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param userName the user name
   * @param password the password
   * @param incementalJobPeriod the period for incremental backup (seconds)
   * @param incementalJobNumber the number for incremental job
   * @return Response return the response
   */
  @GET
  @Path("/{repositoryName}/{workspaceName}/{userName}/{password}/{incementalJobPeriod}/{incementalJobNumber}/fullAndIncremental")
  public Response startBackup(@PathParam("repositoryName") String repositoryName,
                              @PathParam("workspaceName") String workspaceName,
                              @PathParam("userName") String userName,
                              @PathParam("password") String password,
                              @PathParam("incementalJobPeriod") Long incementalJobPeriod,
                              @PathParam("incementalJobNumber") int incementalJobNumber) {
    BackupConfig config = new BackupConfig();
    config.setBuckupType(BackupManager.FULL_AND_INCREMENTAL);
    config.setRepository(repositoryName);
    config.setWorkspace(workspaceName);
    config.setBackupDir(backupManager.getBackupDirectory());
    config.setIncrementalJobPeriod(incementalJobPeriod);
    config.setIncrementalJobNumber(incementalJobNumber);

    String result = "OK\n";

    try {
      validateRepositoryName(repositoryName);
      validateWorkspaceName(repositoryName, workspaceName, userName, password);
      validateOneInstants(repositoryName, workspaceName);
      
      BackupChain backupChain = backupManager.startBackup(config);
      
      result += ("backup log : " + backupChain.getLogFilePath());
    } catch (Exception e) {
      result = "FAIL\n" + e.getMessage();
      log.error("Can not start backup", e);
    }

    return Response.ok(result).build();
  }
  
  /**
   * restore.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param userName the user name
   * @param password the password
   * @return Response return the response
   */
  @GET
  @Path("/{repositoryName}/{workspaceName}/{userName}/{password}/dropWorkspace")
  public Response dropWorkspace(@PathParam("repositoryName") String repositoryName,
                              @PathParam("workspaceName") String workspaceName,
                              @PathParam("userName") String userName,
                              @PathParam("password") String password) {

    String res = "OK\n";
    
    try {
      validateRepositoryName(repositoryName);
      validateWorkspaceName(repositoryName, workspaceName, userName, password);
      
      RepositoryImpl repository = (RepositoryImpl) repositoryService.getRepository(repositoryName);
      repository.removeWorkspace(workspaceName);
      
      res += "The workspace '" + "/" + repositoryName + "/"+workspaceName+"' was dropped.";
    } catch (Exception e) {
      res = "FAIL\n" + e.getMessage();
      log.error("Can not drop the workspace '"
                + "/"+repositoryName
                + "/"+workspaceName+"'");
    }

    return Response.ok(res).build();
  }
  
  /**
   * restore.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param userName the user name
   * @param password the password
   * @return Response return the response
   */
  @GET
  @Path("/{repositoryName}/{workspaceName}/{userName}/{password}/{path}/{workspaceEntry}/restore")
  public Response restore(@PathParam("repositoryName") String repositoryName,
                              @PathParam("workspaceName") String workspaceName,
                              @PathParam("userName") String userName,
                              @PathParam("password") String password,
                              @PathParam("path") String path,
                              @PathParam("workspaceEntry") String workspaceEntry) {

    String res = "OK\n";
    String ePath = "";
    
    try {
      byte buf[] = Base64.decode(path);
      ePath = new String(buf, "UTF-8");
      
      byte bufDest[] = Base64.decode(workspaceEntry.replace("char_pluse", "+"));
      ByteArrayInputStream wEntryStream = new ByteArrayInputStream(bufDest);
      
      WorkspaceRestore restore = new WorkspaceRestore(repositoryService,
                                                      backupManager,
                                                      repositoryName,
                                                      workspaceName,
                                                      userName,
                                                      password,
                                                      ePath,
                                                      wEntryStream);
  
      validateRepositoryName(repositoryName);
      
      restore.restore();
      
      res += ("The workspace '" + "/" + repositoryName  + "/" + workspaceName+"' was restored.");
    } catch (Exception e) {
      res = "FAIL\n" + e.getMessage();
      log.error("Can not restore the workspace '"
                +"/"+repositoryName
                + "/"+workspaceName+"' from "
                + ePath, e);
    }

    return Response.ok(res).build();
  }
  
  /**
   * stopBackup.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param userName the user name
   * @param password the password
   * @return Response return the response
   */
  @GET
  @Path("/{repositoryName}/{workspaceName}/{userName}/{password}/stopBackup")
  public Response stopBackup(@PathParam("repositoryName") String repositoryName,
                              @PathParam("workspaceName") String workspaceName,
                              @PathParam("userName") String userName,
                              @PathParam("password") String password) {
    String result = "OK\n";

    try {
      validateRepositoryName(repositoryName);
      validateWorkspaceName(repositoryName, workspaceName, userName, password);
      
      BackupChain bch = backupManager.findBackup(repositoryName, workspaceName);
      
      if (bch != null) {
        backupManager.stopBackup(bch);
        result += "The backup was stoped for '" + "/"+repositoryName + "/"+workspaceName+"'";
      } else
        throw new RuntimeException("No active backup for '"
                                   +"/"+repositoryName 
                                   + "/"+workspaceName+"'");
      
    } catch (Exception e) {
      result = "FAIL\n" + e.getMessage();
      log.error("Can not stop backup", e);
    }

    return Response.ok(result).build();
  }
  
  /**
   * getSatus.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param userName the user name
   * @param password the password
   * @return Response return the response
   */
  @GET
  @Path("/{repositoryName}/{workspaceName}/{userName}/{password}/getStatus")
  public Response getStatus(@PathParam("repositoryName") String repositoryName,
                              @PathParam("workspaceName") String workspaceName,
                              @PathParam("userName") String userName,
                              @PathParam("password") String password) {
    String result = "OK\n";

    try {
      validateRepositoryName(repositoryName);
      validateWorkspaceName(repositoryName, workspaceName, userName, password);
      
      BackupChain bch = backupManager.findBackup(repositoryName, workspaceName);
      
      if (bch != null) {
        String incrementalBackupStatus = "";
        
        for (BackupJob job : bch.getBackupJobs()) 
          if (job.getType() == BackupJob.INCREMENTAL)
             incrementalBackupStatus = "The incremental backup is working";
        
        String fullBackupStatus = "The full backup " +(bch.getFullBackupState() == BackupJob.FINISHED ? "is " : "was " ) + getState(bch.getFullBackupState());
        
        result += fullBackupStatus + "\n" + incrementalBackupStatus;
      } else
        result += "No active backup for '" + "/"+repositoryName + "/"+workspaceName+"'";
      
    } catch (Exception e) {
      result = "FAIL\n" + e.getMessage();
      log.error("Can not get status of backup", e);
    }

    return Response.ok(result).build();
  }
  
  
  private void validateRepositoryName(String repositoryName) throws RuntimeException {
    try {
      repositoryService.getRepository(repositoryName);
    } catch (RepositoryException e) {
      throw new RuntimeException("Can not get repository '" + repositoryName +"'", e);
    } catch (RepositoryConfigurationException e) {
      throw new RuntimeException("Can not get repository '" + repositoryName +"'", e);
    }
  }
  
  private void validateWorkspaceName(String repositoryName, 
                                      String workspaceName, 
                                      String userName, 
                                      String password) throws RuntimeException {
    try {
      RepositoryImpl repository = (RepositoryImpl) repositoryService.getRepository(repositoryName);
      Session ses = repository.login(new CredentialsImpl(userName, password.toCharArray()), workspaceName);
      ses.logout();
    } catch (LoginException e) {
      throw new RuntimeException("Can not loogin to workspace '" + workspaceName +"' for " + userName + ":" + password, e);
    } catch (NoSuchWorkspaceException e) {
      throw new RuntimeException("Can not get workspace '" + workspaceName +"'", e);
    } catch (RepositoryException e) {
      throw new RuntimeException("Can not get workspace '" + workspaceName +"'", e);
    } catch (RepositoryConfigurationException e) {
      throw new RuntimeException("Can not get workspace '" + workspaceName +"'", e);
    }
  }
  
  private void validateOneInstants(String repositoryName, String workspaceName) throws WorkspaceRestoreExeption {
   
    BackupChain bch = backupManager.findBackup(repositoryName, workspaceName);
    
    if (bch != null)
      throw new WorkspaceRestoreExeption("The backup is already working on workspace '"
                                         +"/"+repositoryName 
                                         + "/"+workspaceName+"'");
  }
  
  
  private String getState(int state) {
    String st = "";
    switch (state) {
    
    case BackupJob.FINISHED:
      st = "finished";
      break;
      
    case BackupJob.WORKING:
      st = "working";
      break;
      
    case BackupJob.WAITING:
      st = "waiting";
      break;
      
    case BackupJob.STARTING:
      st = "starting";
      break;
    }
    
    return st;
  } 
}
