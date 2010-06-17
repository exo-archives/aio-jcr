/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.test;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.backup.BackupConfig;
import org.exoplatform.services.jcr.ext.backup.BackupManager;
import org.exoplatform.services.jcr.ext.replication.ReplicationService;
import org.exoplatform.services.jcr.ext.replication.test.bandwidth.BandwidthAllocationTestCase;
import org.exoplatform.services.jcr.ext.replication.test.concurrent.ConcurrentModificationTestCase;
import org.exoplatform.services.jcr.ext.replication.test.priority.BasePriorityTestCase;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryTemplate;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.StringOutputTransformer;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id$
 */

@URITemplate("/replication-test/")
@OutputTransformer(StringOutputTransformer.class)
public class ReplicationTestService implements ResourceContainer {

  /**
   * Definition the constants to ReplicationTestService.
   * 
   */
  public final class Constants {
    /**
     * The base path to this service.
     */
    public static final String BASE_URL         = "/rest/replication-test";

    /**
     * The operation prefix.
     */
    public static final String OPERATION_PREFIX = "?operation=";

    /**
     * Definition the operation types.
     * 
     */
    public final class OperationType {
      /**
       * Add nt:file operation.
       */
      public static final String ADD_NT_FILE                   = "addNTFile";

      /**
       * Check nt:file operation.
       */
      public static final String CHECK_NT_FILE                 = "checkNTFile";

      /**
       * Start backup.
       */
      public static final String START_BACKUP                  = "startBackup";

      /**
       * Set the lock to node.
       */
      public static final String SET_LOCK                      = "lock";

      /**
       * Check the lock on node.
       */
      public static final String CECK_LOCK                     = "checkLock";

      /**
       * Add the versionable node.
       */
      public static final String ADD_VERSIONODE                = "addVersionNode";

      /**
       * Check the versionable node.
       */
      public static final String CHECK_VERSION_NODE            = "checkVersionNode";

      /**
       * Add new version to versionable node.
       */
      public static final String ADD_NEW_VERSION               = "addNewVersion";

      /**
       * Restore the previous version.
       */
      public static final String RESTORE_RPEVIOUS_VERSION      = "restorePreviousVersion";

      /**
       * Restore the base version.
       */
      public static final String RESTORE_BASE_VERSION          = "restoreBaseVersion";

      /**
       * Delete the node.
       */
      public static final String DELETE                        = "delete";

      /**
       * Check the deleted node.
       */
      public static final String CHECK_DELETE                  = "checkDelete";

      /**
       * The copy node by workspace.
       */
      public static final String WORKSPACE_COPY                = "workspaceCopy";

      /**
       * The move node by workspace.
       */
      public static final String WORKSPASE_MOVE                = "workspaceMove";

      /**
       * The move node by session.
       */
      public static final String SESSION_MOVE                  = "sessionMove";

      /**
       * Check the copy or move node.
       */
      public static final String CHECK_COPY_MOVE_NODE          = "checkCopyMoveNode";

      /**
       * Disconnect the cluster node.
       */
      public static final String DISCONNECT_CLUSTER_NODE       = "disconnectClusterNode";

      /**
       * Disconnect by ID the cluster node.
       */
      public static final String DISCONNECT_CLUSTER_NODE_BY_ID = "disconnectClusterNodeById";

      /**
       * Allow the connect the cluster node.
       */
      public static final String ALLOW_CONNECT                 = "allowConnect";

      /**
       * The forced allow the connect the cluster node.
       */
      public static final String ALLOW_CONNECT_FORCED          = "allowConnectForced";

      /**
       * Check 'read-only' the workspace.
       */
      public static final String WORKSPACE_IS_READ_ONLY        = "workspaceIsReadOnly";

      /**
       * Create content in workspace.
       */
      public static final String CREATE_CONTENT                = "createContent";

      /**
       * Compare data in workspace.
       */
      public static final String COMPARE_DATA                  = "compareData";

      /**
       * Start the thread updater.
       */
      public static final String START_THREAD_UPDATER          = "startThreadUpdater";

      /**
       * Create the base node.
       */
      public static final String CREATE_BASE_NODE              = "createBaseNode";

      /**
       * Add empty node.
       */
      public static final String ADD_EMPTY_NODE                = "addEmptyNode";

      /**
       * Add only string property to existing node.
       */
      public static final String ADD_STRING_PROPETY_ONLY       = "addStringPropertyOnly";

      /**
       * Add only binary property to existing node.
       */
      public static final String ADD_BINARY_PROPERTY_ONLY      = "addBinaryPropertyOnly";

      /**
       * OperationType constructor.
       * 
       */
      private OperationType() {

      }
    }

    /**
     * Constants constructor.
     * 
     */
    private Constants() {
    }
  }

  /**
   * The apache logger.
   */
  private static Log        log = ExoLogger.getLogger("ext.ReplicationTestService");

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
   * @param repoService
   *          the RepositoryService
   * @param replicationService
   *          the ReplicationService
   * @param backupManager
   *          the BackupManager
   * @param params
   *          the configuration parameters
   */
  public ReplicationTestService(RepositoryService repoService,
                                ReplicationService replicationService,
                                BackupManager backupManager,
                                InitParams params) {
    repositoryService = repoService;
    this.backupManager = backupManager;

    log.info("ReplicationTestService inited");
  }

  /**
   * ReplicationTestService constructor.
   * 
   * @param repoService
   *          the RepositoryService
   * @param backupManager
   *          the BackupManager
   * @param params
   *          the configuration parameters
   */
  public ReplicationTestService(RepositoryService repoService,
                                BackupManager backupManager,
                                InitParams params) {
    this(repoService, null, backupManager, params);
  }

  /**
   * addNTFile.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param repoPath
   *          the repository path
   * @param fileName
   *          the file name
   * @param fileSize
   *          the file size
   * @return Response return the response
   */
  @QueryTemplate("operation=addNTFile")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/{fileName}/{fileSize}/")
  public Response addNTFile(@URIParam("repositoryName") String repositoryName,
                            @URIParam("workspaceName") String workspaceName,
                            @URIParam("userName") String userName,
                            @URIParam("password") String password,
                            @URIParam("repoPath") String repoPath,
                            @URIParam("fileName") String fileName,
                            @URIParam("fileSize") Long fileSize) {
    NtFileTestCase ntFileTestCase = new NtFileTestCase(repositoryService,
                                                       repositoryName,
                                                       workspaceName,
                                                       userName,
                                                       password);
    StringBuffer sb = ntFileTestCase.addNtFile(repoPath, fileName, fileSize);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  /**
   * checkNTFile.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param repoPath
   *          the repository path
   * @param fileName
   *          the file name
   * @param fileSize
   *          the file size
   * @return Response return the response
   */
  @QueryTemplate("operation=checkNTFile")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/{fileName}/{fileSize}/")
  public Response checkNTFile(@URIParam("repositoryName") String repositoryName,
                              @URIParam("workspaceName") String workspaceName,
                              @URIParam("userName") String userName,
                              @URIParam("password") String password,
                              @URIParam("repoPath") String repoPath,
                              @URIParam("fileName") String fileName,
                              @URIParam("fileSize") Long fileSize) {
    NtFileTestCase ntFileTestCase = new NtFileTestCase(repositoryService,
                                                       repositoryName,
                                                       workspaceName,
                                                       userName,
                                                       password);
    StringBuffer sb = ntFileTestCase.checkNtFile(repoPath, fileName, fileSize);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  /**
   * startBackup.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param incementalPeriod
   *          the period for incremental backup (seconds)
   * @return Response return the response
   */
  @QueryTemplate("operation=startBackup")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{incementalPeriod}/")
  public Response startBackup(@URIParam("repositoryName") String repositoryName,
                              @URIParam("workspaceName") String workspaceName,
                              @URIParam("userName") String userName,
                              @URIParam("password") String password,
                              @URIParam("incementalPeriod") Long incementalPeriod) {
    BackupConfig config = new BackupConfig();
    config.setBuckupType(BackupManager.FULL_AND_INCREMENTAL);
    config.setRepository(repositoryName);
    config.setWorkspace(workspaceName);
    config.setBackupDir(backupManager.getBackupDirectory());
    config.setIncrementalJobPeriod(incementalPeriod);

    String result = "ok";

    try {
      backupManager.startBackup(config);
    } catch (Exception e) {
      result = "fail";
      log.error("Can't start backup", e);
    }

    return Response.Builder.ok(result, "text/plain").build();
  }

  /**
   * lock.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param repoPath
   *          the repository path
   * @return Response return the response
   */
  @QueryTemplate("operation=lock")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/")
  public Response lock(@URIParam("repositoryName") String repositoryName,
                       @URIParam("workspaceName") String workspaceName,
                       @URIParam("userName") String userName,
                       @URIParam("password") String password,
                       @URIParam("repoPath") String repoPath) {
    LockTestCase lockTestCase = new LockTestCase(repositoryService,
                                                 repositoryName,
                                                 workspaceName,
                                                 userName,
                                                 password);
    StringBuffer sb = lockTestCase.lock(repoPath);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  /**
   * checkLock.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param repoPath
   *          the repository path
   * @return Response return the response
   */
  @QueryTemplate("operation=checkLock")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/")
  public Response checkLock(@URIParam("repositoryName") String repositoryName,
                            @URIParam("workspaceName") String workspaceName,
                            @URIParam("userName") String userName,
                            @URIParam("password") String password,
                            @URIParam("repoPath") String repoPath) {
    LockTestCase lockTestCase = new LockTestCase(repositoryService,
                                                 repositoryName,
                                                 workspaceName,
                                                 userName,
                                                 password);
    StringBuffer sb = lockTestCase.isLocked(repoPath);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  /**
   * addVersionNode.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param repoPath
   *          the repository path
   * @param value
   *          value to versionable node
   * @return Response return the response
   */
  @QueryTemplate("operation=addVersionNode")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/{value}/")
  public Response addVersionNode(@URIParam("repositoryName") String repositoryName,
                                 @URIParam("workspaceName") String workspaceName,
                                 @URIParam("userName") String userName,
                                 @URIParam("password") String password,
                                 @URIParam("repoPath") String repoPath,
                                 @URIParam("value") String value) {
    VersionTestCase versionTestCase = new VersionTestCase(repositoryService,
                                                          repositoryName,
                                                          workspaceName,
                                                          userName,
                                                          password);
    StringBuffer sb = versionTestCase.addVersionNode(repoPath, value);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  /**
   * checkVersionNode.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param repoPath
   *          the repository path
   * @param checkedValue
   *          checking value to versionable node
   * @return Response return the response
   */
  @QueryTemplate("operation=checkVersionNode")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/{checkedValue}/")
  public Response checkVersionNode(@URIParam("repositoryName") String repositoryName,
                                   @URIParam("workspaceName") String workspaceName,
                                   @URIParam("userName") String userName,
                                   @URIParam("password") String password,
                                   @URIParam("repoPath") String repoPath,
                                   @URIParam("checkedValue") String checkedValue) {
    VersionTestCase versionTestCase = new VersionTestCase(repositoryService,
                                                          repositoryName,
                                                          workspaceName,
                                                          userName,
                                                          password);
    StringBuffer sb = versionTestCase.checkVersionNode(repoPath, checkedValue);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  /**
   * addNewVersion.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param repoPath
   *          the repository path
   * @param newValue
   *          new value to versionable node
   * @return Response return the response
   */
  @QueryTemplate("operation=addNewVersion")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/{newValue}/")
  public Response addNewVersion(@URIParam("repositoryName") String repositoryName,
                                @URIParam("workspaceName") String workspaceName,
                                @URIParam("userName") String userName,
                                @URIParam("password") String password,
                                @URIParam("repoPath") String repoPath,
                                @URIParam("newValue") String newValue) {
    VersionTestCase versionTestCase = new VersionTestCase(repositoryService,
                                                          repositoryName,
                                                          workspaceName,
                                                          userName,
                                                          password);
    StringBuffer sb = versionTestCase.addNewVersion(repoPath, newValue);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  /**
   * restorePreviousVersion.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param repoPath
   *          the repository path
   * @return Response return the response
   */
  @QueryTemplate("operation=restorePreviousVersion")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/")
  public Response restorePreviousVersion(@URIParam("repositoryName") String repositoryName,
                                         @URIParam("workspaceName") String workspaceName,
                                         @URIParam("userName") String userName,
                                         @URIParam("password") String password,
                                         @URIParam("repoPath") String repoPath) {
    VersionTestCase versionTestCase = new VersionTestCase(repositoryService,
                                                          repositoryName,
                                                          workspaceName,
                                                          userName,
                                                          password);
    StringBuffer sb = versionTestCase.restorePreviousVersion(repoPath);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  /**
   * restoreBaseVersion.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param repoPath
   *          the repository path
   * @return Response return the response
   */
  @QueryTemplate("operation=restoreBaseVersion")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/")
  public Response restoreBaseVersion(@URIParam("repositoryName") String repositoryName,
                                     @URIParam("workspaceName") String workspaceName,
                                     @URIParam("userName") String userName,
                                     @URIParam("password") String password,
                                     @URIParam("repoPath") String repoPath) {
    VersionTestCase versionTestCase = new VersionTestCase(repositoryService,
                                                          repositoryName,
                                                          workspaceName,
                                                          userName,
                                                          password);
    StringBuffer sb = versionTestCase.restoreBaseVersion(repoPath);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  /**
   * delete.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param repoPath
   *          the repository path
   * @param nodeName
   *          the name of deleting node
   * @return Response return the response
   */
  @QueryTemplate("operation=delete")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/{nodeName}/")
  public Response delete(@URIParam("repositoryName") String repositoryName,
                         @URIParam("workspaceName") String workspaceName,
                         @URIParam("userName") String userName,
                         @URIParam("password") String password,
                         @URIParam("repoPath") String repoPath,
                         @URIParam("nodeName") String nodeName) {
    DeleteTestCase deleteTestCase = new DeleteTestCase(repositoryService,
                                                       repositoryName,
                                                       workspaceName,
                                                       userName,
                                                       password);
    StringBuffer sb = deleteTestCase.delete(repoPath, nodeName);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  /**
   * checkDelete.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param repoPath
   *          the repository path
   * @param nodeName
   *          the name of deleted node
   * @return Response return the response
   */
  @QueryTemplate("operation=checkDelete")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/{nodeName}/")
  public Response checkDelete(@URIParam("repositoryName") String repositoryName,
                              @URIParam("workspaceName") String workspaceName,
                              @URIParam("userName") String userName,
                              @URIParam("password") String password,
                              @URIParam("repoPath") String repoPath,
                              @URIParam("nodeName") String nodeName) {
    DeleteTestCase deleteTestCase = new DeleteTestCase(repositoryService,
                                                       repositoryName,
                                                       workspaceName,
                                                       userName,
                                                       password);
    StringBuffer sb = deleteTestCase.checkDelete(repoPath, nodeName);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  /**
   * workspaceCopy.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param srcRepoPath
   *          the source repository path
   * @param nodeName
   *          the source node name
   * @param destNodeName
   *          the destination node name
   * @param contentSize
   *          the content size
   * @return Response return the response
   */
  @QueryTemplate("operation=workspaceCopy")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{srcRepoPath}/{nodeName}/{destNodeName}/{contentSize}/")
  public Response workspaceCopy(@URIParam("repositoryName") String repositoryName,
                                @URIParam("workspaceName") String workspaceName,
                                @URIParam("userName") String userName,
                                @URIParam("password") String password,
                                @URIParam("srcRepoPath") String srcRepoPath,
                                @URIParam("nodeName") String nodeName,
                                @URIParam("destNodeName") String destNodeName,
                                @URIParam("contentSize") Long contentSize) {
    CopyMoveTestCase copyMoveTestCase = new CopyMoveTestCase(repositoryService,
                                                             repositoryName,
                                                             workspaceName,
                                                             userName,
                                                             password);
    StringBuffer sb = copyMoveTestCase.workspaceCopy(srcRepoPath,
                                                     nodeName,
                                                     destNodeName,
                                                     contentSize);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  /**
   * workspaceMove.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param srcRepoPath
   *          the source repository path
   * @param nodeName
   *          the source node name
   * @param destNodeName
   *          the destination node name
   * @param contentSize
   *          the content size
   * @return Response return the response
   */
  @QueryTemplate("operation=workspaceMove")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{srcRepoPath}/{nodeName}/{destNodeName}/{contentSize}/")
  public Response workspaceMove(@URIParam("repositoryName") String repositoryName,
                                @URIParam("workspaceName") String workspaceName,
                                @URIParam("userName") String userName,
                                @URIParam("password") String password,
                                @URIParam("srcRepoPath") String srcRepoPath,
                                @URIParam("nodeName") String nodeName,
                                @URIParam("destNodeName") String destNodeName,
                                @URIParam("contentSize") Long contentSize) {
    CopyMoveTestCase copyMoveTestCase = new CopyMoveTestCase(repositoryService,
                                                             repositoryName,
                                                             workspaceName,
                                                             userName,
                                                             password);
    StringBuffer sb = copyMoveTestCase.workspaceMove(srcRepoPath,
                                                     nodeName,
                                                     destNodeName,
                                                     contentSize);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  /**
   * sessionMove.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param srcRepoPath
   *          the source repository path
   * @param nodeName
   *          the source node name
   * @param destNodeName
   *          the destination node name
   * @param contentSize
   *          the content size
   * @return Response return the response
   */
  @QueryTemplate("operation=sessionMove")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{srcRepoPath}/{nodeName}/{destNodeName}/{contentSize}/")
  public Response sessionMove(@URIParam("repositoryName") String repositoryName,
                              @URIParam("workspaceName") String workspaceName,
                              @URIParam("userName") String userName,
                              @URIParam("password") String password,
                              @URIParam("srcRepoPath") String srcRepoPath,
                              @URIParam("nodeName") String nodeName,
                              @URIParam("destNodeName") String destNodeName,
                              @URIParam("contentSize") Long contentSize) {
    CopyMoveTestCase copyMoveTestCase = new CopyMoveTestCase(repositoryService,
                                                             repositoryName,
                                                             workspaceName,
                                                             userName,
                                                             password);
    StringBuffer sb = copyMoveTestCase.sessionMove(srcRepoPath, nodeName, destNodeName, contentSize);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  /**
   * checkCopyMoveNode.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param srcRepoPath
   *          the source repository path
   * @param nodeName
   *          the source node name
   * @param destNodeName
   *          the destination node name
   * @param contentSize
   *          the content size
   * @return Response return the response
   */
  @QueryTemplate("operation=checkCopyMoveNode")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{srcRepoPath}/{nodeName}/{destNodeName}/{contentSize}/")
  public Response checkCopyMoveNode(@URIParam("repositoryName") String repositoryName,
                                    @URIParam("workspaceName") String workspaceName,
                                    @URIParam("userName") String userName,
                                    @URIParam("password") String password,
                                    @URIParam("srcRepoPath") String srcRepoPath,
                                    @URIParam("nodeName") String nodeName,
                                    @URIParam("destNodeName") String destNodeName,
                                    @URIParam("contentSize") Long contentSize) {
    CopyMoveTestCase copyMoveTestCase = new CopyMoveTestCase(repositoryService,
                                                             repositoryName,
                                                             workspaceName,
                                                             userName,
                                                             password);
    StringBuffer sb = copyMoveTestCase.checkCopyMoveNode(srcRepoPath,
                                                         nodeName,
                                                         destNodeName,
                                                         contentSize);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  /**
   * disconnectClusterNode.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @return Response return the response
   */
  @QueryTemplate("operation=disconnectClusterNode")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/")
  public Response disconnectClusterNode(@URIParam("repositoryName") String repositoryName,
                                        @URIParam("workspaceName") String workspaceName,
                                        @URIParam("userName") String userName,
                                        @URIParam("password") String password) {
    BasePriorityTestCase priorityTestCase = new BasePriorityTestCase(repositoryService,
                                                                     repositoryName,
                                                                     workspaceName,
                                                                     userName,
                                                                     password);
    StringBuffer sb = priorityTestCase.disconnectClusterNode();

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  /**
   * disconnectClusterNodeById.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param id
   *          the id
   * @return Response return the response
   */
  @QueryTemplate("operation=disconnectClusterNodeById")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{id}/")
  public Response disconnectClusterNodeById(@URIParam("repositoryName") String repositoryName,
                                            @URIParam("workspaceName") String workspaceName,
                                            @URIParam("userName") String userName,
                                            @URIParam("password") String password,
                                            @URIParam("id") Integer id) {
    BasePriorityTestCase priorityTestCase = new BasePriorityTestCase(repositoryService,
                                                                     repositoryName,
                                                                     workspaceName,
                                                                     userName,
                                                                     password);
    StringBuffer sb = priorityTestCase.disconnectClusterNode(id);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  /**
   * allowConnect.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @return Response return the response
   */
  @QueryTemplate("operation=allowConnect")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/")
  public Response allowConnect(@URIParam("repositoryName") String repositoryName,
                               @URIParam("workspaceName") String workspaceName,
                               @URIParam("userName") String userName,
                               @URIParam("password") String password) {
    BasePriorityTestCase priorityTestCase = new BasePriorityTestCase(repositoryService,
                                                                     repositoryName,
                                                                     workspaceName,
                                                                     userName,
                                                                     password);
    StringBuffer sb = priorityTestCase.allowConnect();

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  /**
   * allowConnectForced.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @return Response return the response
   */
  @QueryTemplate("operation=allowConnectForced")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/")
  public Response allowConnectForced(@URIParam("repositoryName") String repositoryName,
                                     @URIParam("workspaceName") String workspaceName,
                                     @URIParam("userName") String userName,
                                     @URIParam("password") String password) {
    BasePriorityTestCase priorityTestCase = new BasePriorityTestCase(repositoryService,
                                                                     repositoryName,
                                                                     workspaceName,
                                                                     userName,
                                                                     password);
    StringBuffer sb = priorityTestCase.allowConnectForced();

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  /**
   * workspaceIsReadOnly.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @return Response return the response
   */
  @QueryTemplate("operation=workspaceIsReadOnly")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/")
  public Response workspaceIsReadOnly(@URIParam("repositoryName") String repositoryName,
                                      @URIParam("workspaceName") String workspaceName,
                                      @URIParam("userName") String userName,
                                      @URIParam("password") String password) {
    BasePriorityTestCase priorityTestCase = new BasePriorityTestCase(repositoryService,
                                                                     repositoryName,
                                                                     workspaceName,
                                                                     userName,
                                                                     password);
    StringBuffer sb = priorityTestCase.isReadOnly(workspaceName);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  /**
   * createContent.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param repoPath
   *          the repository path
   * @param fileName
   *          the file name
   * @param iterations
   *          how many iterations for simple content
   * @param simpleContent
   *          the simple content
   * @return Response return the response
   */
  @QueryTemplate("operation=createContent")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/{fileName}/{iterations}/{simpleContent}/")
  public Response createContent(@URIParam("repositoryName") String repositoryName,
                                @URIParam("workspaceName") String workspaceName,
                                @URIParam("userName") String userName,
                                @URIParam("password") String password,
                                @URIParam("repoPath") String repoPath,
                                @URIParam("fileName") String fileName,
                                @URIParam("iterations") Long iterations,
                                @URIParam("simpleContent") String simpleContent) {
    ConcurrentModificationTestCase concurrentModificationTestCase = new ConcurrentModificationTestCase(repositoryService,
                                                                                                       repositoryName,
                                                                                                       workspaceName,
                                                                                                       userName,
                                                                                                       password);
    StringBuffer sb = concurrentModificationTestCase.createContent(repoPath,
                                                                   fileName,
                                                                   iterations,
                                                                   simpleContent);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  /**
   * compareData.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param srcRepoPath
   *          the source repository path
   * @param srcFileName
   *          the source file name
   * @param destRepoPath
   *          the destination repository path
   * @param destFileName
   *          the destination file name
   * @return Response return the response
   */
  @QueryTemplate("operation=compareData")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{srcRepoPath}/{srcFileName}/{destRepoPath}/{destFileName}/")
  public Response compareData(@URIParam("repositoryName") String repositoryName,
                              @URIParam("workspaceName") String workspaceName,
                              @URIParam("userName") String userName,
                              @URIParam("password") String password,
                              @URIParam("srcRepoPath") String srcRepoPath,
                              @URIParam("srcFileName") String srcFileName,
                              @URIParam("destRepoPath") String destRepoPath,
                              @URIParam("destFileName") String destFileName) {
    ConcurrentModificationTestCase concurrentModificationTestCase = new ConcurrentModificationTestCase(repositoryService,
                                                                                                       repositoryName,
                                                                                                       workspaceName,
                                                                                                       userName,
                                                                                                       password);
    StringBuffer sb = concurrentModificationTestCase.compareData(srcRepoPath,
                                                                 srcFileName,
                                                                 destRepoPath,
                                                                 destFileName);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  /**
   * startThreadUpdater.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param srcRepoPath
   *          the source repository path
   * @param srcFileName
   *          the source file name
   * @param destRepoPath
   *          the destination repository path
   * @param destFileName
   *          the destination file name
   * @param iterations
   *          how many iterations the thread
   * @return Response return the response
   */
  @QueryTemplate("operation=startThreadUpdater")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{srcRepoPath}/{srcFileName}/{destRepoPath}/{destFileName}/{iterations}/")
  public Response startThreadUpdater(@URIParam("repositoryName") String repositoryName,
                                     @URIParam("workspaceName") String workspaceName,
                                     @URIParam("userName") String userName,
                                     @URIParam("password") String password,
                                     @URIParam("srcRepoPath") String srcRepoPath,
                                     @URIParam("srcFileName") String srcFileName,
                                     @URIParam("destRepoPath") String destRepoPath,
                                     @URIParam("destFileName") String destFileName,
                                     @URIParam("iterations") Long iterations) {
    ConcurrentModificationTestCase concurrentModificationTestCase = new ConcurrentModificationTestCase(repositoryService,
                                                                                                       repositoryName,
                                                                                                       workspaceName,
                                                                                                       userName,
                                                                                                       password);
    StringBuffer sb = concurrentModificationTestCase.startThreadUpdater(srcRepoPath,
                                                                        srcFileName,
                                                                        destRepoPath,
                                                                        destFileName,
                                                                        iterations);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  /**
   * createBaseNode.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param repoPath
   *          the repository path
   * @param nodeName
   *          the node name
   * @return Response return the response
   */
  @QueryTemplate("operation=createBaseNode")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/{nodeName}/")
  public Response createBaseNode(@URIParam("repositoryName") String repositoryName,
                                 @URIParam("workspaceName") String workspaceName,
                                 @URIParam("userName") String userName,
                                 @URIParam("password") String password,
                                 @URIParam("repoPath") String repoPath,
                                 @URIParam("nodeName") String nodeName) {
    BandwidthAllocationTestCase bandwidthAllocationTestCase = new BandwidthAllocationTestCase(repositoryService,
                                                                                              repositoryName,
                                                                                              workspaceName,
                                                                                              userName,
                                                                                              password);
    StringBuffer sb = bandwidthAllocationTestCase.createBaseNode(repoPath, nodeName);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  /**
   * addEmptyNode.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param repoPath
   *          the repository path
   * @param nodeName
   *          the node name
   * @param iterations
   *          how many adding the empty node
   * @return Response return the response
   */
  @QueryTemplate("operation=addEmptyNode")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/{nodeName}/{iterations}/")
  public Response addEmptyNode(@URIParam("repositoryName") String repositoryName,
                               @URIParam("workspaceName") String workspaceName,
                               @URIParam("userName") String userName,
                               @URIParam("password") String password,
                               @URIParam("repoPath") String repoPath,
                               @URIParam("nodeName") String nodeName,
                               @URIParam("iterations") Long iterations) {
    BandwidthAllocationTestCase bandwidthAllocationTestCase = new BandwidthAllocationTestCase(repositoryService,
                                                                                              repositoryName,
                                                                                              workspaceName,
                                                                                              userName,
                                                                                              password);
    StringBuffer sb = bandwidthAllocationTestCase.addEmptyNode(repoPath, nodeName, iterations);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  /**
   * addStringPropertyOnly.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param repoPath
   *          the repository path
   * @param nodeName
   *          the node name
   * @param size
   *          the size of string property
   * @param iterations
   *          how many adding the string property
   * @return Response return the response
   */
  @QueryTemplate("operation=addStringPropertyOnly")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/{nodeName}/{size}/{iterations}/")
  public Response addStringPropertyOnly(@URIParam("repositoryName") String repositoryName,
                                        @URIParam("workspaceName") String workspaceName,
                                        @URIParam("userName") String userName,
                                        @URIParam("password") String password,
                                        @URIParam("repoPath") String repoPath,
                                        @URIParam("nodeName") String nodeName,
                                        @URIParam("size") Long size,
                                        @URIParam("iterations") Long iterations) {
    BandwidthAllocationTestCase bandwidthAllocationTestCase = new BandwidthAllocationTestCase(repositoryService,
                                                                                              repositoryName,
                                                                                              workspaceName,
                                                                                              userName,
                                                                                              password);
    StringBuffer sb = bandwidthAllocationTestCase.addStringPropertyOnly(repoPath,
                                                                        nodeName,
                                                                        size,
                                                                        iterations);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  /**
   * addBinaryPropertyOnly.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param repoPath
   *          the repository path
   * @param nodeName
   *          the node name
   * @param size
   *          the size of binary property
   * @param iterations
   *          how many adding the binary property
   * @return Response return the response
   */
  @QueryTemplate("operation=addBinaryPropertyOnly")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/{nodeName}/{size}/{iterations}/")
  public Response addBinaryPropertyOnly(@URIParam("repositoryName") String repositoryName,
                                        @URIParam("workspaceName") String workspaceName,
                                        @URIParam("userName") String userName,
                                        @URIParam("password") String password,
                                        @URIParam("repoPath") String repoPath,
                                        @URIParam("nodeName") String nodeName,
                                        @URIParam("size") Long size,
                                        @URIParam("iterations") Long iterations) {
    BandwidthAllocationTestCase bandwidthAllocationTestCase = new BandwidthAllocationTestCase(repositoryService,
                                                                                              repositoryName,
                                                                                              workspaceName,
                                                                                              userName,
                                                                                              password);
    StringBuffer sb = bandwidthAllocationTestCase.addBinaryPropertyOnly(repoPath,
                                                                        nodeName,
                                                                        size,
                                                                        iterations);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }
  
  
  /**
   * addBinaryPropertyOnly.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param repoPath
   *          the repository path
   * @param nodeName
   *          the node name
   * @param size
   *          the size of binary property
   * @param iterations
   *          how many adding the binary property
   * @return Response return the response
   */
  @QueryTemplate("operation=docviewImport")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/{docviewPath}/")
  public Response docviewImport(@URIParam("repositoryName") String repositoryName,
                                        @URIParam("workspaceName") String workspaceName,
                                        @URIParam("userName") String userName,
                                        @URIParam("password") String password,
                                        @URIParam("repoPath") String repoPath,
                                        @URIParam("docviewPath") String docviewPath) {
    DocviewImportTestCase docviewTestCase = new DocviewImportTestCase(repositoryService,
                                                                                              repositoryName,
                                                                                              workspaceName,
                                                                                              userName,
                                                                                              password);
    StringBuffer sb = docviewTestCase.docviewImport(repoPath, docviewPath);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }
  
  /**
   * addBinaryPropertyOnly.
   * 
   * @param repositoryName
   *          the repository name
   * @param workspaceName
   *          the workspace name
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param repoPath
   *          the repository path
   * @param nodeName
   *          the node name
   * @param size
   *          the size of binary property
   * @param iterations
   *          how many adding the binary property
   * @return Response return the response
   */
  @QueryTemplate("operation=export")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/{pathDir}/")
  public Response export(@URIParam("repositoryName") String repositoryName,
                                        @URIParam("workspaceName") String workspaceName,
                                        @URIParam("userName") String userName,
                                        @URIParam("password") String password,
                                        @URIParam("repoPath") String repoPath,
                                        @URIParam("pathDir") String pathDir) {
    ExportTestCase exportTestCase = new ExportTestCase(repositoryService,
                                                                                              repositoryName,
                                                                                              workspaceName,
                                                                                              userName,
                                                                                              password);
    StringBuffer sb = exportTestCase.export(repoPath, pathDir);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }
}
