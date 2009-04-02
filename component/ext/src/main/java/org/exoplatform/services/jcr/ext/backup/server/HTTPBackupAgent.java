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
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.WorkspaceContainerFacade;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.backup.BackupChain;
import org.exoplatform.services.jcr.ext.backup.BackupChainLog;
import org.exoplatform.services.jcr.ext.backup.BackupConfig;
import org.exoplatform.services.jcr.ext.backup.BackupManager;
import org.exoplatform.services.jcr.ext.backup.server.bean.BackupBean;
import org.exoplatform.services.jcr.ext.backup.server.bean.BackupConfigBean;
import org.exoplatform.services.jcr.ext.backup.server.bean.DropWorkspaceBean;
import org.exoplatform.services.jcr.ext.backup.server.bean.RestoreBean;
import org.exoplatform.services.jcr.ext.backup.server.bean.response.BackupChainBean;
import org.exoplatform.services.jcr.ext.backup.server.bean.response.BackupChainInfoBean;
import org.exoplatform.services.jcr.ext.backup.server.bean.response.BackupChainListBean;
import org.exoplatform.services.jcr.ext.backup.server.bean.response.BackupServiceInfoBean;
import org.exoplatform.services.jcr.ext.backup.server.bean.response.ChainLogBean;
import org.exoplatform.services.jcr.ext.backup.server.bean.response.ChainLogListBean;
import org.exoplatform.services.jcr.ext.backup.server.bean.response.FailureBean;
import org.exoplatform.services.jcr.ext.backup.server.bean.response.MessageBean;
import org.exoplatform.services.jcr.ext.backup.server.bean.response.RestoreChainLogBean;
import org.exoplatform.services.jcr.ext.backup.server.bean.response.RestoreChainLogListBean;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.SessionRegistry;
import org.exoplatform.services.jcr.impl.util.JCRDateFormat;
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

@Path("/jcr-backup/")
public class HTTPBackupAgent implements ResourceContainer {

  /**
   * Definition the constants to ReplicationTestService.
   */
  public static final class Constants {
    /**
     * The base path to this service.
     */
    public static final String BASE_URL = "/rest/jcr-backup";

    /**
     * Definition the operation types.
     */
    public static final class OperationType {
      /**
       * Start backup operation.
       */
      public static final String START_BACKUP   = "startBackup";

      /**
       * Restore operations.
       */
      public static final String RESTORE        = "restore";

      /**
       * Stop backup operations.
       */
      public static final String STOP_BACKUP    = "stopBackup";

      /**
       * The current backups info operations.
       */
      public static final String CURRENT_BACKUPS_INFO = "currentBackupsInfo";
      
      /**
       * The current backup info operations.
       */
      public static final String CURRENT_BACKUP_INFO = "currentBackupInfo";
      
      /**
       * The current restores info operations.
       */
      public static final String CURRENT_RESTORES_INFO = "currentRestoresInfo";
      
      /**
       * The completed backups info operations.
       */
      public static final String COMPLETED_BACKUPS_INFO = "completedBackupsInfo";
      
      /**
       * The backup service info operations.
       */
      public static final String BACKUP_SERVICE_INFO = "backupServiceInfo";

      /**
       * The drop workspace operations.
       */
      public static final String DROP_WORKSPACE = "dropWorkspace";

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
   * The 24h timeout for JobRestoresCleaner.
   */
  private static final int JOB_RESTORES_CLEANER = 24 * 60 * 60 * 1000;

  /**
   * The apache logger.
   */
  private static Log                        log = ExoLogger.getLogger("ext.BackupServer");

  /**
   * The repository service.
   */
  private RepositoryService                 repositoryService;

  /**
   * The backup manager.
   */
  private BackupManager                     backupManager;

  /**
   * Will be get session over base authenticate.
   */
  private ThreadLocalSessionProviderService sessionProviderService;
  
  /**
   * The list of restore job.
   */
  private  List<JobWorkspaceRestore>        restoreJobs;
  
  /**
   * The cleaner for restoreJobs.
   */
  private final JobRestoresCleaner          jobRestoresCleaner;

  /**
   * ReplicationTestService constructor.
   * 
   * @param repoService
   *          the RepositoryService
   * @param backupManager
   *          the BackupManager
   * @param sessionProviderService
   *          the ThreadLocalSessionProviderService
   */
  public HTTPBackupAgent(RepositoryService repoService,
                         BackupManager backupManager,
                         ThreadLocalSessionProviderService sessionProviderService) {
    this.repositoryService = repoService;
    this.backupManager = backupManager;
    this.sessionProviderService = sessionProviderService;
    this.restoreJobs = new ArrayList<JobWorkspaceRestore>();
    this.jobRestoresCleaner = new JobRestoresCleaner();
    this.jobRestoresCleaner.start();

    log.info("HTTPBackupAgent inited");
  }

  /**
   * startBackup.
   *
   * @param bConfigBeen
   *          BackupConfigBeen, the been with backup configuration. 
   * @return Response return the response
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/startBackup")
  public Response startBackup(BackupConfigBean bConfigBeen) {
    try {
      File backupDir = new File(bConfigBeen.getBackupDir());
      if (!backupDir.exists()) 
        throw new RuntimeException("The backup folder not exists :  " + backupDir.getAbsolutePath());
  
      BackupConfig config = new BackupConfig();
      config.setBackupType(bConfigBeen.getBackupType());
      config.setRepository(bConfigBeen.getRepositoryName());
      config.setWorkspace(bConfigBeen.getWorkspaceName());
      config.setBackupDir(backupDir);
      config.setIncrementalJobPeriod(bConfigBeen.getIncrementalJobPeriod());

      validateRepositoryName(bConfigBeen.getRepositoryName());
      validateWorkspaceName(bConfigBeen.getRepositoryName(), bConfigBeen.getWorkspaceName());
      validateOneBackupInstants(bConfigBeen.getRepositoryName(), bConfigBeen.getWorkspaceName());

      BackupChain backupChain = backupManager.startBackup(config);
      
      BackupChainBean backupChainBean = new BackupChainBean(backupChain);

      return Response.ok(backupChainBean).build();
    } catch (Exception e) {
      log.error("Can not start backup", e);
      
      FailureBean failureBean = new FailureBean("Can not start backup", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                     .entity(failureBean)
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
  }

  /**
   * dropWorkspace.
   *
   * @param dropWorkspaceBean
   *          the been for drop workspace
   * @return Response return the response
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/dropWorkspace")
  public Response dropWorkspace(DropWorkspaceBean dropWorkspaceBean) {

    try {
      String res = "";
      
      if (dropWorkspaceBean.getForceSessionClose()) {
        int closedSessions = forceCloseSession(dropWorkspaceBean.getRepositoryName(),
                                               dropWorkspaceBean.getWorkspaceName());
        res += ("The " + closedSessions + " sessions was closed on workspace '" + "/"
            + dropWorkspaceBean.getRepositoryName() + "/" + dropWorkspaceBean.getWorkspaceName() + "'\n");
      }

      validateRepositoryName(dropWorkspaceBean.getRepositoryName());
      validateWorkspaceName(dropWorkspaceBean.getRepositoryName(),
                            dropWorkspaceBean.getWorkspaceName());

      RepositoryImpl repository = (RepositoryImpl) repositoryService.getRepository(dropWorkspaceBean.getRepositoryName());
      repository.removeWorkspace(dropWorkspaceBean.getWorkspaceName());

      res += "The workspace '/" + dropWorkspaceBean.getRepositoryName() + "/"
             + dropWorkspaceBean.getWorkspaceName() + "' was dropped.";

      MessageBean messageBean = new MessageBean(res);
      
      return Response.ok(messageBean).build();      
    } catch (Exception e) {
      log.error("Can not drop the workspace '" + "/" + dropWorkspaceBean.getRepositoryName() + "/"
          + dropWorkspaceBean.getWorkspaceName() + "'", e);
      
      FailureBean failureBean = new FailureBean("Can not drop the workspace '" + "/" + dropWorkspaceBean.getRepositoryName() + "/"
                                                + dropWorkspaceBean.getWorkspaceName() + "'",
                                                e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                     .entity(failureBean)
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
  }

  /**
   * restore.
   *
   * @param restoreBean
   *          RestoreBeen, the restore been configuration
   * @return Response return the response
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/restore")
  public Response restore(RestoreBean restoreBean) {
    try {
      validateOneRestoreInstants(restoreBean.getRepositoryName(), restoreBean.getWorkspaceName());
      
      File backupLog = getBackupLogbyId(restoreBean.getBackupId());
      
      // validate backup log file
      if (backupLog == null)
        throw new RuntimeException("The backup log file with id " + restoreBean.getBackupId() + " not exists.");

      ByteArrayInputStream wEntryStream = new ByteArrayInputStream(restoreBean.getWorkspaceConfig()
                                                                              .getBytes("UTF-8"));

      JobWorkspaceRestore jobRestore = new JobWorkspaceRestore(repositoryService,
                                                      backupManager,
                                                      restoreBean.getRepositoryName(),
                                                      restoreBean.getWorkspaceName(),
                                                      backupLog.getAbsolutePath(),
                                                      wEntryStream);

      validateRepositoryName(restoreBean.getRepositoryName());

      restoreJobs.add(jobRestore);
      jobRestore.start();

      String res = ("The workspace '" + "/" + restoreBean.getRepositoryName() + "/"
          + restoreBean.getWorkspaceName() + "' is start restoring from " + backupLog.getAbsolutePath() + ".");
      
      MessageBean messageBean = new MessageBean(res);
      
      return Response.ok(messageBean).build();
    } catch (Exception e) {
      log.error("Can not start restore the workspace '" + "/" + restoreBean.getRepositoryName() + "/"
          + restoreBean.getWorkspaceName() + "' from backup log with id '" 
          + restoreBean.getBackupId() + "'", e);
      
      FailureBean failureBean = new FailureBean("Can not start restore the workspace '" + "/" + restoreBean.getRepositoryName() + "/"
                                                + restoreBean.getWorkspaceName() + "' from backup log with id '" 
                                                + restoreBean.getBackupId() + "'", e);
      
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                     .entity(failureBean)
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
  }

  /**
   * stopBackup.
   *
   * @param backupBean
   *          BackupBeen, the configuration for stop backup 
   * @return Response return the response
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/stopBackup")
  public Response stopBackup(BackupBean backupBean) {

    try {
      String result = "";
      
      BackupChain bch = backupManager.findBackup(backupBean.getBackupId());

      if (bch != null) {
        backupManager.stopBackup(bch);
        result += "The backup with id '" + backupBean.getBackupId()
            + "' was stoped for workspace '" + "/" + bch.getBackupConfig().getRepository() + "/"
            + bch.getBackupConfig().getWorkspace() + "'";
      } else
        throw new RuntimeException("No active backup with id '" + backupBean.getBackupId() + "'");

      MessageBean messageBean = new MessageBean(result);
      
      return Response.ok(messageBean).build();
    } catch (Exception e) {
      log.error("Can not stop backup", e);
      
      FailureBean failureBean = new FailureBean("Can not stop backup", e);
      
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                     .entity(failureBean)
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }

    
  }

  /**
   * Will be returned the current backups info.
   *
   * @return Response 
   *           return the response
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/currentBackupsInfo")
  public Response currentBackupsInfo() {
    try { 
      List<BackupChainBean> list = new ArrayList<BackupChainBean>();
      
      for (BackupChain chain : backupManager.getCurrentBackups())
        list.add(new BackupChainBean(chain));
     
      BackupChainListBean listBeen = new BackupChainListBean(list);
  
      return Response.ok(listBeen).build();
    } catch (Exception e) {
      log.error("Can not get information about current backups (in progress)", e);
      
      FailureBean failureBean = new FailureBean("Can not get information about current backups (in progress)", e);
      
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                     .entity(failureBean)
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
  }
  
  /**
   * Will be returned the current backup info by backupId.
   *
   * @param backupBean
   *          BackupBeen, the parameters for command currentBackupInfo 
   * @return return the response
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/currentBackupInfo")
  public Response currentBackupInfo(BackupBean backupBean) {
    try {
      BackupChain backupChain = backupManager.findBackup(backupBean.getBackupId());
      
      if (backupChain == null) 
        throw new RuntimeException("No active backup with id '" + backupBean.getBackupId() + "'");
      
      BackupChainInfoBean chainInfoBeen = new BackupChainInfoBean(backupChain,
                                                                  new BackupConfigBean(backupChain.getBackupConfig()));
    
      return Response.ok(chainInfoBeen).build();
    } catch (Exception e) {
      log.error("Can not get detailed info for backup with id '" + backupBean.getBackupId() + "'", e);
      
      FailureBean failureBean = new FailureBean("Can not get detailed info for backup with id '" + backupBean.getBackupId() + "'", e);
      
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                     .entity(failureBean)
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
    
    
  }
  
  /**
   * Will be returned the current restores info.
   *
   * @return Response 
   *           return the response
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/currentRestoresInfo")
  public Response currentRestoresInfo() {
    try {
      List<RestoreChainLogBean> list = new ArrayList<RestoreChainLogBean>();
      
      for (JobWorkspaceRestore job : restoreJobs) {
        BackupConfigBean configBeen = new BackupConfigBean(job.getBackupChainLog().getBackupConfig());
        
        RestoreChainLogBean been = new RestoreChainLogBean(job.getBackupChainLog(),
                                                           configBeen,
                                                           job.getStateRestore(),
                                                           JCRDateFormat.format(job.getStartTime()),
                                                           (job.getEndTime() == null ? "" : JCRDateFormat.format(job.getEndTime())),
                                                           job.getRepositoryName(),
                                                           job.getWorkspaceName(),
                                                           (job.getRestoreException() == null ? "" : job.getRestoreException().getMessage()));
        list.add(been);
      }
  
      RestoreChainLogListBean listBeen = new RestoreChainLogListBean(list);
  
      return Response.ok(listBeen).build();
    } catch (Exception e) {
      log.error("Can not get information about current restores", e);
      
      FailureBean failureBean = new FailureBean("Can not get information about current restores", e);
      
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                     .entity(failureBean)
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
  }
  
  /**
   * Will be returned the completed backups info.
   *
   * @return Response 
   *           return the response
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/completedBackupsInfo")
  public Response completedBackupsInfo() {
    try {
      List<ChainLogBean> list = new ArrayList<ChainLogBean>();
      
      for (BackupChainLog chainLog : backupManager.getBackupsLogs()) 
        if (backupManager.findBackup(chainLog.getBackupId()) == null) 
           list.add(new ChainLogBean(chainLog, new BackupConfigBean(chainLog.getBackupConfig())));
      
      ChainLogListBean logsListBeen = new ChainLogListBean(list);
  
      return Response.ok(logsListBeen).build();
    } catch (Exception e) {
      log.error("Can not get information about completed (ready to restore) backups", e);
      
      FailureBean failureBean = new FailureBean("Can not get information about completed (ready to restore) backups", e);
      
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                     .entity(failureBean)
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
  }
  
  /**
   * Will be returned the backup service info.
   *
   * @return Response 
   *           return the response
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/backupServiceInfo")
  public Response backupServiceInfo() {
    try {
      BackupServiceInfoBean infoBeen = new BackupServiceInfoBean(backupManager.getFullBackupType(),
                                                                 backupManager.getIncrementalBackupType(),
                                                                 backupManager.getBackupDirectory().getAbsolutePath());
  
      return Response.ok(infoBeen).build();
    } catch (Exception e) {
      log.error("Can not get information about backup service", e);
      
      FailureBean failureBean = new FailureBean("Can not get information about backup service", e);
      
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                     .entity(failureBean)
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
  }
  
  /**
   * validateRepositoryName.
   * 
   * @param repositoryName
   *          the repository name
   */
  private void validateRepositoryName(String repositoryName) {
    try {
      repositoryService.getRepository(repositoryName);
    } catch (RepositoryException e) {
      throw new RuntimeException("Can not get repository '" + repositoryName + "'", e);
    } catch (RepositoryConfigurationException e) {
      throw new RuntimeException("Can not get repository '" + repositoryName + "'", e);
    }
  }

  /**
   * validateWorkspaceName.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   */
  private void validateWorkspaceName(String repositoryName, String workspaceName) {
    try {
      Session ses = sessionProviderService.getSessionProvider(null)
                                          .getSession(workspaceName,
                                                      repositoryService.getRepository(repositoryName));
      ses.logout();
    } catch (LoginException e) {
      throw new RuntimeException("Can not loogin to workspace '" + workspaceName + "'", e);
    } catch (NoSuchWorkspaceException e) {
      throw new RuntimeException("Can not get workspace '" + workspaceName + "'", e);
    } catch (RepositoryException e) {
      throw new RuntimeException("Can not get workspace '" + workspaceName + "'", e);
    } catch (RepositoryConfigurationException e) {
      throw new RuntimeException("Can not get workspace '" + workspaceName + "'", e);
    }
  }

  /**
   * validateOneBackupInstants.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @throws WorkspaceRestoreExeption
   *           will be generated WorkspaceRestoreExeption
   */
  private void validateOneBackupInstants(String repositoryName, String workspaceName) throws WorkspaceRestoreExeption {

    BackupChain bch = backupManager.findBackup(repositoryName, workspaceName);

    if (bch != null)
      throw new WorkspaceRestoreExeption("The backup is already working on workspace '" + "/"
          + repositoryName + "/" + workspaceName + "'");
  }
  
  /**
   * validateOneRestoreInstants.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @throws WorkspaceRestoreExeption
   *           will be generated WorkspaceRestoreExeption
   */
  private void validateOneRestoreInstants(String repositoryName, String workspaceName) throws WorkspaceRestoreExeption {

    for (JobWorkspaceRestore job : restoreJobs) 
      if (repositoryName.equals(job.getRepositoryName()) 
          && workspaceName.endsWith(job.getWorkspaceName())
          && (job.getStateRestore() == JobWorkspaceRestore.RESTORE_INITIALIZED 
              || job.getStateRestore() == JobWorkspaceRestore.RESTORE_STARTED)) {
        throw new WorkspaceRestoreExeption("The workspace '" + "/"
                                           + repositoryName + "/" + workspaceName 
                                           + "' is already restoring.");
      }
  }

  /**
   * forceCloseSession. Close sessions on specific workspace.
   * 
   * @param repositoryName
   *          repository name
   * @param workspaceName
   *          workspace name
   * @return int how many sessions was closed
   * @throws RepositoryConfigurationException
   *           will be generate RepositoryConfigurationException
   * @throws RepositoryException
   *           will be generate RepositoryException
   */
  private int forceCloseSession(String repositoryName, String workspaceName) throws RepositoryException,
                                                                            RepositoryConfigurationException {
    ManageableRepository mr = repositoryService.getRepository(repositoryName);
    WorkspaceContainerFacade wc = mr.getWorkspaceContainer(workspaceName);

    SessionRegistry sessionRegistry = (SessionRegistry) wc.getComponent(SessionRegistry.class);

    return sessionRegistry.closeSessions(workspaceName);
  }

  /**
   * getBackupLogbyId.
   *
   * @param backupId
   *          String, the backup identifier
   * @return File
   *           return backup log file
   */
  private File getBackupLogbyId(String backupId) {
    FilenameFilter backupLogsFilter = new FilenameFilter() {

      public boolean accept(File dir, String name) {
        return (name.endsWith(".xml") && name.startsWith("backup-"));
      }
    };

    File[] files = backupManager.getBackupDirectory().listFiles(backupLogsFilter);

    if (files.length != 0)
      for (File f : files) 
        if (f.getName().replaceAll(".xml", "").replaceAll("backup-", "").equals(backupId))
          return f;

    return null;
  }
  
  /**
   * JobRestoresCleaner.
   *   Will be clean the list of JobWorkspaceRestore.
   *
   */
  private class JobRestoresCleaner extends Thread {
    
    /**
     * {@inheritDoc}
     */
    public void run() {
      while (true) {
        try {
          Thread.sleep(JOB_RESTORES_CLEANER);
          
          synchronized (restoreJobs) {
            for (JobWorkspaceRestore job : restoreJobs) 
              if (job.getStateRestore() == JobWorkspaceRestore.RESTORE_FAIL
                  || job.getStateRestore() == JobWorkspaceRestore.RESTORE_SUCCESSFUL)
                restoreJobs.remove(job);
              
          }
          
        } catch (InterruptedException e) {
          log.error("The JobRestoresCleaner was interapted", e);
        }
      }
    }
  }
}
