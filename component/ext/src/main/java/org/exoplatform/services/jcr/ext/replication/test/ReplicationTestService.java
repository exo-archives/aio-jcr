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
 * Created by The eXo Platform SAS Author : Alex Reshetnyak
 * alex.reshetnyak@exoplatform.com.ua 16.05.2008
 */

@URITemplate("/replication-test/")
@OutputTransformer(StringOutputTransformer.class)
public class ReplicationTestService implements ResourceContainer {
  public class Constants {
    public final static String BASE_URL         = "/rest/replication-test";

    public final static String OPERATION_PREFIX = "?operation=";

    public class OperationType {
      public final static String ADD_NT_FILE                   = "addNTFile";

      public final static String CHECK_NT_FILE                 = "checkNTFile";

      public final static String START_BACKUP                  = "startBackup";

      public final static String SET_LOCK                      = "lock";

      public final static String CECK_LOCK                     = "checkLock";

      public final static String ADD_VERSIONODE                = "addVersionNode";

      public final static String CHECK_VERSION_NODE            = "checkVersionNode";

      public final static String ADD_NEW_VERSION               = "addNewVersion";

      public final static String RESTORE_RPEVIOUS_VERSION      = "restorePreviousVersion";

      public final static String RESTORE_BASE_VERSION          = "restoreBaseVersion";

      public final static String DELETE                        = "delete";

      public final static String CHECK_DELETE                  = "checkDelete";

      public final static String WORKSPACE_COPY                = "workspaceCopy";

      public final static String WORKSPASE_MOVE                = "workspaceMove";

      public final static String SESSION_MOVE                  = "sessionMove";

      public final static String CHECK_COPY_MOVE_NODE          = "checkCopyMoveNode";

      public final static String DISCONNECT_CLUSTER_NODE       = "disconnectClusterNode";

      public final static String DISCONNECT_CLUSTER_NODE_BY_ID = "disconnectClusterNodeById";

      public final static String ALLOW_CONNECT                 = "allowConnect";
      
      public final static String ALLOW_CONNECT_FORCED          = "allowConnectForced";
      
      public final static String WORKSPACE_IS_READ_ONLY        = "workspaceIsReadOnly";
      
      public final static String CREATE_CONTENT                = "createContent";
      
      public final static String COMPARE_DATA                  = "compareData";
      
      public final static String START_THREAD_UPDATER          = "startThreadUpdater";
      
      public final static String CREATE_BASE_NODE              = "createBaseNode";
      
      public final static String ADD_EMPTY_NODE                = "addEmptyNode";
      
      public final static String ADD_STRING_PROPETY_ONLY       = "addStringPropertyOnly";
      
      public final static String ADD_BINARY_PROPERTY_ONLY      = "addBinaryPropertyOnly";
    }
  }

  private static Log        log = ExoLogger.getLogger("ext.ReplicationTestService");

  private RepositoryService repositoryService;

  private BackupManager     backupManager;

  public ReplicationTestService(RepositoryService repoService,
      ReplicationService replicationService, BackupManager backupManager, InitParams params) {
    repositoryService = repoService;
    this.backupManager = backupManager;

    log.info("ReplicationTestService inited");
  }

  public ReplicationTestService(RepositoryService repoService, BackupManager backupManager,
      InitParams params) {
    this(repoService, null, backupManager, params);
  }

  @QueryTemplate("operation=addNTFile")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/{fileName}/{fileSize}/")
  public Response addNTFile(@URIParam("repositoryName")
  String repositoryName, @URIParam("workspaceName")
  String workspaceName, @URIParam("userName")
  String userName, @URIParam("password")
  String password, @URIParam("repoPath")
  String repoPath, @URIParam("fileName")
  String fileName, @URIParam("fileSize")
  Long fileSize) {
    NtFileTestCase ntFileTestCase = new NtFileTestCase(repositoryService, repositoryName,
        workspaceName, userName, password);
    StringBuffer sb = ntFileTestCase.addNtFile(repoPath, fileName, fileSize);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  @QueryTemplate("operation=checkNTFile")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/{fileName}/{fileSize}/")
  public Response checkNTFile(@URIParam("repositoryName")
  String repositoryName, @URIParam("workspaceName")
  String workspaceName, @URIParam("userName")
  String userName, @URIParam("password")
  String password, @URIParam("repoPath")
  String repoPath, @URIParam("fileName")
  String fileName, @URIParam("fileSize")
  Long fileSize) {
    NtFileTestCase ntFileTestCase = new NtFileTestCase(repositoryService, repositoryName,
        workspaceName, userName, password);
    StringBuffer sb = ntFileTestCase.checkNtFile(repoPath, fileName, fileSize);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  @QueryTemplate("operation=startBackup")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{incementalPeriod}/")
  public Response startBackup(@URIParam("repositoryName")
  String repositoryName, @URIParam("workspaceName")
  String workspaceName, @URIParam("userName")
  String userName, @URIParam("password")
  String password, @URIParam("incementalPeriod")
  Long incementalPeriod) {
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

  @QueryTemplate("operation=lock")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/")
  public Response lock(@URIParam("repositoryName")
  String repositoryName, @URIParam("workspaceName")
  String workspaceName, @URIParam("userName")
  String userName, @URIParam("password")
  String password, @URIParam("repoPath")
  String repoPath) {
    LockTestCase lockTestCase = new LockTestCase(repositoryService, repositoryName, workspaceName,
        userName, password);
    StringBuffer sb = lockTestCase.lock(repoPath);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  @QueryTemplate("operation=checkLock")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/")
  public Response checkLock(@URIParam("repositoryName")
  String repositoryName, @URIParam("workspaceName")
  String workspaceName, @URIParam("userName")
  String userName, @URIParam("password")
  String password, @URIParam("repoPath")
  String repoPath) {
    LockTestCase lockTestCase = new LockTestCase(repositoryService, repositoryName, workspaceName,
        userName, password);
    StringBuffer sb = lockTestCase.isLocked(repoPath);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  @QueryTemplate("operation=addVersionNode")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/{value}/")
  public Response addVersionNode(@URIParam("repositoryName")
  String repositoryName, @URIParam("workspaceName")
  String workspaceName, @URIParam("userName")
  String userName, @URIParam("password")
  String password, @URIParam("repoPath")
  String repoPath, @URIParam("value")
  String value) {
    VersionTestCase versionTestCase = new VersionTestCase(repositoryService, repositoryName,
        workspaceName, userName, password);
    StringBuffer sb = versionTestCase.addVersionNode(repoPath, value);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  @QueryTemplate("operation=checkVersionNode")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/{checkedValue}/")
  public Response checkVersionNode(@URIParam("repositoryName")
  String repositoryName, @URIParam("workspaceName")
  String workspaceName, @URIParam("userName")
  String userName, @URIParam("password")
  String password, @URIParam("repoPath")
  String repoPath, @URIParam("checkedValue")
  String checkedValue) {
    VersionTestCase versionTestCase = new VersionTestCase(repositoryService, repositoryName,
        workspaceName, userName, password);
    StringBuffer sb = versionTestCase.checkVersionNode(repoPath, checkedValue);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  @QueryTemplate("operation=addNewVersion")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/{newValue}/")
  public Response addNewVersion(@URIParam("repositoryName")
  String repositoryName, @URIParam("workspaceName")
  String workspaceName, @URIParam("userName")
  String userName, @URIParam("password")
  String password, @URIParam("repoPath")
  String repoPath, @URIParam("newValue")
  String newValue) {
    VersionTestCase versionTestCase = new VersionTestCase(repositoryService, repositoryName,
        workspaceName, userName, password);
    StringBuffer sb = versionTestCase.addNewVersion(repoPath, newValue);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  @QueryTemplate("operation=restorePreviousVersion")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/")
  public Response restorePreviousVersion(@URIParam("repositoryName")
  String repositoryName, @URIParam("workspaceName")
  String workspaceName, @URIParam("userName")
  String userName, @URIParam("password")
  String password, @URIParam("repoPath")
  String repoPath) {
    VersionTestCase versionTestCase = new VersionTestCase(repositoryService, repositoryName,
        workspaceName, userName, password);
    StringBuffer sb = versionTestCase.restorePreviousVersion(repoPath);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  @QueryTemplate("operation=restoreBaseVersion")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/")
  public Response restoreBaseVersion(@URIParam("repositoryName")
  String repositoryName, @URIParam("workspaceName")
  String workspaceName, @URIParam("userName")
  String userName, @URIParam("password")
  String password, @URIParam("repoPath")
  String repoPath) {
    VersionTestCase versionTestCase = new VersionTestCase(repositoryService, repositoryName,
        workspaceName, userName, password);
    StringBuffer sb = versionTestCase.restoreBaseVersion(repoPath);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  @QueryTemplate("operation=delete")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/{nodeName}/")
  public Response delete(@URIParam("repositoryName")
  String repositoryName, @URIParam("workspaceName")
  String workspaceName, @URIParam("userName")
  String userName, @URIParam("password")
  String password, @URIParam("repoPath")
  String repoPath, @URIParam("nodeName")
  String nodeName) {
    DeleteTestCase deleteTestCase = new DeleteTestCase(repositoryService, repositoryName,
        workspaceName, userName, password);
    StringBuffer sb = deleteTestCase.delete(repoPath, nodeName);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  @QueryTemplate("operation=checkDelete")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/{nodeName}/")
  public Response checkDelete(@URIParam("repositoryName")
  String repositoryName, @URIParam("workspaceName")
  String workspaceName, @URIParam("userName")
  String userName, @URIParam("password")
  String password, @URIParam("repoPath")
  String repoPath, @URIParam("nodeName")
  String nodeName) {
    DeleteTestCase deleteTestCase = new DeleteTestCase(repositoryService, repositoryName,
        workspaceName, userName, password);
    StringBuffer sb = deleteTestCase.checkDelete(repoPath, nodeName);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  @QueryTemplate("operation=workspaceCopy")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{srcRepoPath}/{nodeName}/{destNodeName}/{contentSize}/")
  public Response workspaceCopy(@URIParam("repositoryName")
  String repositoryName, @URIParam("workspaceName")
  String workspaceName, @URIParam("userName")
  String userName, @URIParam("password")
  String password, @URIParam("srcRepoPath")
  String srcRepoPath, @URIParam("nodeName")
  String nodeName, @URIParam("destNodeName")
  String destNodeName, @URIParam("contentSize")
  Long contentSize) {
    CopyMoveTestCase copyMoveTestCase = new CopyMoveTestCase(repositoryService, repositoryName,
        workspaceName, userName, password);
    StringBuffer sb = copyMoveTestCase.workspaceCopy(srcRepoPath, nodeName, destNodeName,
        contentSize);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  @QueryTemplate("operation=workspaceMove")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{srcRepoPath}/{nodeName}/{destNodeName}/{contentSize}/")
  public Response workspaceMove(@URIParam("repositoryName")
  String repositoryName, @URIParam("workspaceName")
  String workspaceName, @URIParam("userName")
  String userName, @URIParam("password")
  String password, @URIParam("srcRepoPath")
  String srcRepoPath, @URIParam("nodeName")
  String nodeName, @URIParam("destNodeName")
  String destNodeName, @URIParam("contentSize")
  Long contentSize) {
    CopyMoveTestCase copyMoveTestCase = new CopyMoveTestCase(repositoryService, repositoryName,
        workspaceName, userName, password);
    StringBuffer sb = copyMoveTestCase.workspaceMove(srcRepoPath, nodeName, destNodeName,
        contentSize);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  @QueryTemplate("operation=sessionMove")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{srcRepoPath}/{nodeName}/{destNodeName}/{contentSize}/")
  public Response sessionMove(@URIParam("repositoryName")
  String repositoryName, @URIParam("workspaceName")
  String workspaceName, @URIParam("userName")
  String userName, @URIParam("password")
  String password, @URIParam("srcRepoPath")
  String srcRepoPath, @URIParam("nodeName")
  String nodeName, @URIParam("destNodeName")
  String destNodeName, @URIParam("contentSize")
  Long contentSize) {
    CopyMoveTestCase copyMoveTestCase = new CopyMoveTestCase(repositoryService, repositoryName,
        workspaceName, userName, password);
    StringBuffer sb = copyMoveTestCase
        .sessionMove(srcRepoPath, nodeName, destNodeName, contentSize);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  @QueryTemplate("operation=checkCopyMoveNode")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{srcRepoPath}/{nodeName}/{destNodeName}/{contentSize}/")
  public Response checkCopyMoveNode(@URIParam("repositoryName")
  String repositoryName, @URIParam("workspaceName")
  String workspaceName, @URIParam("userName")
  String userName, @URIParam("password")
  String password, @URIParam("srcRepoPath")
  String srcRepoPath, @URIParam("nodeName")
  String nodeName, @URIParam("destNodeName")
  String destNodeName, @URIParam("contentSize")
  Long contentSize) {
    CopyMoveTestCase copyMoveTestCase = new CopyMoveTestCase(repositoryService, repositoryName,
        workspaceName, userName, password);
    StringBuffer sb = copyMoveTestCase.checkCopyMoveNode(srcRepoPath, nodeName, destNodeName,
        contentSize);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  @QueryTemplate("operation=disconnectClusterNode")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/")
  public Response disconnectClusterNode(@URIParam("repositoryName")
  String repositoryName, @URIParam("workspaceName")
  String workspaceName, @URIParam("userName")
  String userName, @URIParam("password")
  String password) {
    BasePriorityTestCase priorityTestCase = new BasePriorityTestCase(repositoryService,
        repositoryName, workspaceName, userName, password);
    StringBuffer sb = priorityTestCase.disconnectClusterNode();

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  @QueryTemplate("operation=disconnectClusterNodeById")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{id}/")
  public Response disconnectClusterNodeById(@URIParam("repositoryName")
  String repositoryName, @URIParam("workspaceName")
  String workspaceName, @URIParam("userName")
  String userName, @URIParam("password")
  String password, @URIParam("id")  Integer id) {
    BasePriorityTestCase priorityTestCase = new BasePriorityTestCase(repositoryService,
        repositoryName, workspaceName, userName, password);
    StringBuffer sb = priorityTestCase.disconnectClusterNode(id);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  @QueryTemplate("operation=allowConnect")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/")
  public Response allowConnect(@URIParam("repositoryName")
  String repositoryName, @URIParam("workspaceName")
  String workspaceName, @URIParam("userName")
  String userName, @URIParam("password")
  String password) {
    BasePriorityTestCase priorityTestCase = new BasePriorityTestCase(repositoryService,
        repositoryName, workspaceName, userName, password);
    StringBuffer sb = priorityTestCase.allowConnect();

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }
  
  @QueryTemplate("operation=allowConnectForced")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/")
  public Response allowConnectForced(@URIParam("repositoryName")
  String repositoryName, @URIParam("workspaceName")
  String workspaceName, @URIParam("userName")
  String userName, @URIParam("password")
  String password) {
    BasePriorityTestCase priorityTestCase = new BasePriorityTestCase(repositoryService,
        repositoryName, workspaceName, userName, password);
    StringBuffer sb = priorityTestCase.allowConnectForced();

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }
  
  @QueryTemplate("operation=workspaceIsReadOnly")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/")
  public Response workspaceIsReadOnly(@URIParam("repositoryName")
  String repositoryName, @URIParam("workspaceName")
  String workspaceName, @URIParam("userName")
  String userName, @URIParam("password")
  String password) {
    BasePriorityTestCase priorityTestCase = new BasePriorityTestCase(repositoryService,
        repositoryName, workspaceName, userName, password);
    StringBuffer sb = priorityTestCase.isReadOnly(workspaceName);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }
  
  @QueryTemplate("operation=createContent")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/{fileName}/{iterations}/{simpleContent}/")
  public Response createContent(@URIParam("repositoryName")
  String repositoryName, @URIParam("workspaceName")
  String workspaceName, @URIParam("userName")
  String userName, @URIParam("password")
  String password, @URIParam("repoPath")
  String repoPath, @URIParam("fileName")
  String fileName, @URIParam("iterations")
  Long iterations, @URIParam("simpleContent")
  String simpleContent) {
    ConcurrentModificationTestCase concurrentModificationTestCase = new ConcurrentModificationTestCase(repositoryService,
        repositoryName, workspaceName, userName, password);
    StringBuffer sb = concurrentModificationTestCase.createContent(repoPath, fileName, iterations, simpleContent);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }
  
  @QueryTemplate("operation=compareData")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{srcRepoPath}/{srcFileName}/{destRepoPath}/{destFileName}/")
  public Response compareData(@URIParam("repositoryName")
  String repositoryName, @URIParam("workspaceName")
  String workspaceName, @URIParam("userName")
  String userName, @URIParam("password")
  String password, @URIParam("srcRepoPath")
  String srcRepoPath, @URIParam("srcFileName")
  String srcFileName, @URIParam("destRepoPath")
  String destRepoPath, @URIParam("destFileName")
  String destFileName) {
    ConcurrentModificationTestCase concurrentModificationTestCase = new ConcurrentModificationTestCase(repositoryService,
        repositoryName, workspaceName, userName, password);
    StringBuffer sb = concurrentModificationTestCase.compareData(srcRepoPath, srcFileName, destRepoPath, destFileName);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }
  
  @QueryTemplate("operation=startThreadUpdater")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{srcRepoPath}/{srcFileName}/{destRepoPath}/{destFileName}/{iterations}/")
  public Response startThreadUpdater(@URIParam("repositoryName")
  String repositoryName, @URIParam("workspaceName")
  String workspaceName, @URIParam("userName")
  String userName, @URIParam("password")
  String password, @URIParam("srcRepoPath")
  String srcRepoPath, @URIParam("srcFileName")
  String srcFileName, @URIParam("destRepoPath")
  String destRepoPath, @URIParam("destFileName")
  String destFileName, @URIParam("iterations")
  Long iterations) {
    ConcurrentModificationTestCase concurrentModificationTestCase = new ConcurrentModificationTestCase(repositoryService,
        repositoryName, workspaceName, userName, password);
    StringBuffer sb = concurrentModificationTestCase.startThreadUpdater(srcRepoPath, srcFileName, destRepoPath, destFileName, iterations);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }
  
  @QueryTemplate("operation=createBaseNode")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/{nodeName}/")
  public Response createBaseNode(@URIParam("repositoryName")
  String repositoryName, @URIParam("workspaceName")
  String workspaceName, @URIParam("userName")
  String userName, @URIParam("password")
  String password, @URIParam("repoPath")
  String repoPath, @URIParam("nodeName")
  String nodeName) {
    BandwidthAllocationTestCase bandwidthAllocationTestCase = new BandwidthAllocationTestCase(repositoryService,
        repositoryName, workspaceName, userName, password);
    StringBuffer sb = bandwidthAllocationTestCase.createBaseNode(repoPath, nodeName);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }
  
  @QueryTemplate("operation=addEmptyNode")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/{nodeName}/{iterations}/")
  public Response addEmptyNode(@URIParam("repositoryName")
  String repositoryName, @URIParam("workspaceName")
  String workspaceName, @URIParam("userName")
  String userName, @URIParam("password")
  String password, @URIParam("repoPath")
  String repoPath, @URIParam("nodeName")
  String nodeName, @URIParam("iterations")
  Long iterations) {
    BandwidthAllocationTestCase bandwidthAllocationTestCase = new BandwidthAllocationTestCase(repositoryService,
        repositoryName, workspaceName, userName, password);
    StringBuffer sb = bandwidthAllocationTestCase.addEmptyNode(repoPath, nodeName, iterations);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }
  
  @QueryTemplate("operation=addStringPropertyOnly")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/{nodeName}/{size}/{iterations}/")
  public Response addStringPropertyOnly(@URIParam("repositoryName")
  String repositoryName, @URIParam("workspaceName")
  String workspaceName, @URIParam("userName")
  String userName, @URIParam("password")
  String password, @URIParam("repoPath")
  String repoPath, @URIParam("nodeName")
  String nodeName, @URIParam("size")
  Long size, @URIParam("iterations")
  Long iterations) {
    BandwidthAllocationTestCase bandwidthAllocationTestCase = new BandwidthAllocationTestCase(repositoryService,
        repositoryName, workspaceName, userName, password);
    StringBuffer sb = bandwidthAllocationTestCase.addStringPropertyOnly(repoPath, nodeName, size, iterations);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }
  
  @QueryTemplate("operation=addBinaryPropertyOnly")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{userName}/{password}/{repoPath}/{nodeName}/{size}/{iterations}/")
  public Response addBinaryPropertyOnly(@URIParam("repositoryName")
  String repositoryName, @URIParam("workspaceName")
  String workspaceName, @URIParam("userName")
  String userName, @URIParam("password")
  String password, @URIParam("repoPath")
  String repoPath, @URIParam("nodeName")
  String nodeName, @URIParam("size")
  Long size, @URIParam("iterations")
  Long iterations) {
    BandwidthAllocationTestCase bandwidthAllocationTestCase = new BandwidthAllocationTestCase(repositoryService,
        repositoryName, workspaceName, userName, password);
    StringBuffer sb = bandwidthAllocationTestCase.addBinaryPropertyOnly(repoPath, nodeName, size, iterations);

    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }
}
