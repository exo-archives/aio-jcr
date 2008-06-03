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
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.backup.BackupConfig;
import org.exoplatform.services.jcr.ext.backup.BackupManager;
import org.exoplatform.services.jcr.ext.replication.ReplicationService;
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
  private static Log        log                 = ExoLogger.getLogger("ext.ReplicationTestService");

  private final String      TEST_PROPERTYS      = "test-propertys";

  private final String      REPOSITORY_PROPERTY = "repository";

  private final String      WORKSPACE_PROPERTY  = "workspace";

  private final String      USER_PROPERTY       = "user";

  private final String      PASSWORD_PROPERTY   = "password";

  private final String      reposytoryName;

  private final String      workspaceName;

  private final String      userName;

  private final String      password;

  private RepositoryService repositoryService;

  private BackupManager     backupManager;


  public ReplicationTestService(RepositoryService repoService,
      ReplicationService replicationService, BackupManager backupManager, InitParams params) {
    repositoryService = repoService;
    this.backupManager = backupManager;

    PropertiesParam pps = params.getPropertiesParam(TEST_PROPERTYS);

    reposytoryName = pps.getProperty(REPOSITORY_PROPERTY);
    workspaceName = pps.getProperty(WORKSPACE_PROPERTY);
    userName = pps.getProperty(USER_PROPERTY);
    password = pps.getProperty(PASSWORD_PROPERTY);

    log.info("ReplicationTestService inited");
  }

  @QueryTemplate("operation=addNTFile")
  @HTTPMethod("GET")
  @URITemplate("/{repoPath}/{fileName}/{fileSize}/")
  public Response addNTFile(@URIParam("repoPath") String repoPath,
                            @URIParam("fileName") String fileName, 
                            @URIParam("fileSize") Long fileSize) {
    NtFileTestCase ntFileTestCase = new NtFileTestCase(repositoryService, reposytoryName, workspaceName, userName, password);
    StringBuffer sb = ntFileTestCase.addNtFile(repoPath, fileName, fileSize);
    
    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }

  @QueryTemplate("operation=checkNTFile")
  @HTTPMethod("GET")
  @URITemplate("/{repoPath}/{fileName}/{fileSize}/")
  public Response checkNTFile(@URIParam("repoPath") String repoPath,
                              @URIParam("fileName") String fileName, 
                              @URIParam("fileSize") Long fileSize) {
    NtFileTestCase ntFileTestCase = new NtFileTestCase(repositoryService, reposytoryName, workspaceName, userName, password);
    StringBuffer sb = ntFileTestCase.checkNtFile(repoPath, fileName, fileSize); 
    
    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }
  
  @QueryTemplate("operation=startBackup")
  @HTTPMethod("GET")
  @URITemplate("/{repositoryName}/{workspaceName}/{incementalPeriod}/")
  public Response startBackup(@URIParam("repositoryName") String repositoryName,
                              @URIParam("workspaceName") String workspaceName, 
                              @URIParam("incementalPeriod") Long incementalPeriod) {
    BackupConfig config = new BackupConfig();
    config.setRepository(repositoryName);
    config.setWorkspace(workspaceName);
    config.setFullBackupType("org.exoplatform.services.jcr.ext.backup.impl.fs.FullBackupJob");
    config.setIncrementalBackupType("org.exoplatform.services.jcr.ext.backup.impl.fs.IncrementalBackupJob");

    config.setBackupDir(backupManager.getBackupDirectory());

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
  @URITemplate("/{repoPath}/")
  public Response lock(@URIParam("repoPath") String repoPath) {
    LockTestCase lockTestCase = new LockTestCase(repositoryService, reposytoryName, workspaceName, userName, password);
    StringBuffer sb = lockTestCase.lock(repoPath);
    
    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }
  
  @QueryTemplate("operation=checkLock")
  @HTTPMethod("GET")
  @URITemplate("/{repoPath}/")
  public Response checkLock(@URIParam("repoPath") String repoPath) {
    LockTestCase lockTestCase = new LockTestCase(repositoryService, reposytoryName, workspaceName, userName, password);
    StringBuffer sb = lockTestCase.isLocked(repoPath);
    
    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }
  
  @QueryTemplate("operation=addVersionNode")
  @HTTPMethod("GET")
  @URITemplate("/{repoPath}/{value}/")
  public Response addVersionNode(@URIParam("repoPath") String repoPath,
                                 @URIParam("value") String value) {
    VersionTestCase versionTestCase = new VersionTestCase(repositoryService, reposytoryName, workspaceName, userName, password);
    StringBuffer sb = versionTestCase.addVersionNode(repoPath, value);
    
    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }
  
  @QueryTemplate("operation=checkVersionNode")
  @HTTPMethod("GET")
  @URITemplate("/{repoPath}/{checkedValue}/")
  public Response checkVersionNode(@URIParam("repoPath") String repoPath,
                                   @URIParam("checkedValue") String checkedValue) {
    VersionTestCase versionTestCase = new VersionTestCase(repositoryService, reposytoryName, workspaceName, userName, password);
    StringBuffer sb =  versionTestCase.checkVersionNode(repoPath, checkedValue);
    
    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }
  
  @QueryTemplate("operation=addNewVersion")
  @HTTPMethod("GET")
  @URITemplate("/{repoPath}/{newValue}/")
  public Response addNewVersion(@URIParam("repoPath") String repoPath,
                                @URIParam("newValue") String newValue) {
    VersionTestCase versionTestCase = new VersionTestCase(repositoryService, reposytoryName, workspaceName, userName, password);
    StringBuffer sb =  versionTestCase.addNewVersion(repoPath, newValue);
    
    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }
  
  @QueryTemplate("operation=restorePreviousVersion")
  @HTTPMethod("GET")
  @URITemplate("/{repoPath}/")
  public Response restorePreviousVersion(@URIParam("repoPath") String repoPath) {
    VersionTestCase versionTestCase = new VersionTestCase(repositoryService, reposytoryName, workspaceName, userName, password);
    StringBuffer sb =  versionTestCase.restorePreviousVersion(repoPath);
    
    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }
  
  @QueryTemplate("operation=restoreBaseVersion")
  @HTTPMethod("GET")
  @URITemplate("/{repoPath}/")
  public Response restoreBaseVersion(@URIParam("repoPath") String repoPath) {
    VersionTestCase versionTestCase = new VersionTestCase(repositoryService, reposytoryName, workspaceName, userName, password);
    StringBuffer sb =  versionTestCase.restoreBaseVersion(repoPath);
    
    return Response.Builder.ok(sb.toString(), "text/plain").build();
  }
}
