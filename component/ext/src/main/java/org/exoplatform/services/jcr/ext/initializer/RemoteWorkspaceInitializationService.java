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
package org.exoplatform.services.jcr.ext.initializer;

import java.io.File;

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
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.backup.BackupChain;
import org.exoplatform.services.jcr.ext.backup.BackupConfig;
import org.exoplatform.services.jcr.ext.backup.BackupManager;
import org.exoplatform.services.jcr.ext.initializer.impl.RemoteTransportImpl;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.resource.ResourceContainer;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 16.03.2009
 *
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a> 
 * @version $Id: RemoteWorkspaceInitializerService.java 111 2008-11-11 11:11:11Z rainf0x $
 */

@Path("/jcr-remote-workspace-initializer/")
@Produces("text/plain")
public class RemoteWorkspaceInitializationService implements ResourceContainer {

  /**
   * The template for ip-address in configuration.
   */
  private static final String IP_ADRESS_TEMPLATE  = "[$]bind-ip-address";
  /**
   * Definition the constants to ReplicationTestService.
   */
  public static final class Constants {
    /**
     * The base path to this service.
     */
    public static final String BASE_URL               = "/rest/jcr-remote-workspace-initializer";

    /**
     * Definition the operation types.
     */
    public static final class OperationType {
      /**
       * Full backup only operation.
       */
      public static final String GET_WORKSPACE  = "getWorkspaceData";

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
  private static Log              log = ExoLogger.getLogger("ext.RemoteWorkspaceInitializerService");
  
  /**
   * The repository service.
   */
  private final RepositoryService repositoryService;

  /**
   * The backup manager.
   */
  private final BackupManager     backupManager;
  
  /**
   * Will be get session over base authenticate.
   */
  private final ThreadLocalSessionProviderService sessionProviderService;

  /**
   * Url to remote data source. 
   */
  private final String dataSourceUrl;

  /**
   * Bind to IP address.
   */
  private final String bindIpAddress;

  /**
   * The channel configuration.
   */
  private final String channelConfig;

  /**
   * The channel name prefix.
   */
  private final String channelName;
  
  /**
   * The folder for temporary files.
   */
  private final File tempDir;

  /**
   * ReplicationTestService constructor.
   * 
   * @param repoService the RepositoryService
   * @param backupManager the BackupManager
   * @param sessionProviderService the ThreadLocalSessionProviderService
   */
  public RemoteWorkspaceInitializationService(RepositoryService repoService,
                      BackupManager backupManager,
                      ThreadLocalSessionProviderService sessionProviderService,
                      InitParams params) {
    this.repositoryService = repoService;
    this.backupManager = backupManager;
    this.sessionProviderService = sessionProviderService;
    
    PropertiesParam pps = params.getPropertiesParam("remote-initializer-properties");

    if (pps == null)
      throw new RuntimeException("remote-initializer-properties not specified");
    
    if (pps.getProperty("data-source-url") == null)
      throw new RuntimeException("data-source-url not specified");
    dataSourceUrl = pps.getProperty("data-source-url");
    
    if (pps.getProperty("bind-ip-address") == null)
      throw new RuntimeException("bind-ip-address not specified");
    bindIpAddress = pps.getProperty("bind-ip-address");
    
    if (pps.getProperty("channel-config") == null)
      throw new RuntimeException("channel-config not specified");
    channelConfig = pps.getProperty("channel-config");
    
    if (pps.getProperty("channel-name") == null)
      throw new RuntimeException("channel-name not specified");
    channelName = pps.getProperty("channel-name");
    
    if (pps.getProperty("temp-dir") == null)
      throw new RuntimeException("temp-dir not specified");
    String tempD = pps.getProperty("temp-dir");
    
    tempDir = new File(tempD);
    if (!tempDir.exists())
      tempDir.mkdirs();

    log.info("RemoteWorkspaceInitializerService");
  }
  
  
  /**
   * getWorkspaceData.
   *
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @return File 
   *          with workspace data 
   * @throws RemoteWorkspaceInitializationException
   *           will be generated the RemoteWorkspaceInitializerException
   */
  public File getWorkspaceData(String repository, String workspace) throws RemoteWorkspaceInitializationException {
    
    AsyncChannelManager channelManager = new AsyncChannelManager(channelConfig.replaceAll(IP_ADRESS_TEMPLATE, bindIpAddress),
                                                                 channelName + "_" + repository + "_" + workspace, 2);
    
    RemoteTransport remoteTransport = new RemoteTransportImpl(channelManager, tempDir, dataSourceUrl);
    
    try {
      remoteTransport.init();
      
      return remoteTransport.getWorkspaceData(repository, workspace);      
    } catch (Throwable t) {
      throw new RemoteWorkspaceInitializationException("Can't get workspace data", t);
    } finally {
      remoteTransport.close();
    }
  }
  
  /**
   * startFullBackup.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @return Response return the response
   */
  @GET
  @Path("/{repositoryName}/{workspaceName}/getWorkspaceData")
  public Response startFullBackup(@PathParam("repositoryName") String repositoryName,
                              @PathParam("workspaceName") String workspaceName) {
    BackupConfig config = new BackupConfig();
    config.setBuckupType(BackupManager.FULL_BACKUP_ONLY);
    config.setRepository(repositoryName);
    config.setWorkspace(workspaceName);
    config.setBackupDir(backupManager.getBackupDirectory());

    String result = "OK";

    try {
      validateRepositoryName(repositoryName);
      validateWorkspaceName(repositoryName, workspaceName);
      BackupChain backupChain = backupManager.startBackup(config);
      
      
    } catch (Exception e) {
      result = "FAIL\n" + e.getMessage();
      log.error("Can't start backup", e);
    }
    
    //init JChannel 
    

    return Response.ok(result).build();
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
  private void validateWorkspaceName(String repositoryName, 
                                      String workspaceName) {
    try {
      Session ses = sessionProviderService.getSessionProvider(null).getSession(workspaceName, repositoryService.getRepository(repositoryName));
      ses.logout();
    } catch (LoginException e) {
      throw new RuntimeException("Can not loogin to workspace '" + workspaceName + "'" , e);
    } catch (NoSuchWorkspaceException e) {
      throw new RuntimeException("Can not get workspace '" + workspaceName + "'", e);
    } catch (RepositoryException e) {
      throw new RuntimeException("Can not get workspace '" + workspaceName + "'", e);
    } catch (RepositoryConfigurationException e) {
      throw new RuntimeException("Can not get workspace '" + workspaceName + "'", e);
    }
  }
  
}
