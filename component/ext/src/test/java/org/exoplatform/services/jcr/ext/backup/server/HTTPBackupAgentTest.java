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
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Session;
import javax.ws.rs.core.MultivaluedMap;

import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.backup.BackupJob;
import org.exoplatform.services.jcr.ext.backup.BackupManager;
import org.exoplatform.services.jcr.ext.backup.server.bean.BackupConfigBean;
import org.exoplatform.services.jcr.ext.backup.server.bean.response.DetailedInfo;
import org.exoplatform.services.jcr.ext.backup.server.bean.response.ShortInfo;
import org.exoplatform.services.jcr.ext.backup.server.bean.response.ShortInfoList;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.registry.RESTRegistryTest.DummyContainerResponseWriter;
import org.exoplatform.services.rest.RequestHandler;
import org.exoplatform.services.rest.impl.ContainerRequest;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.InputHeadersMap;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.rest.impl.ResourceBinder;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;
import org.exoplatform.ws.frameworks.json.value.JsonValue;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 21.04.2009
 *
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a> 
 * @version $Id: HTTPBackupAgentTest.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class HTTPBackupAgentTest extends BaseStandaloneTest {
  
  private String HTTP_BACKUP_AGENT_PATH = HTTPBackupAgent.Constants.BASE_URL.replaceAll("/rest", "");

  private ResourceBinder binder;

  private RequestHandler handler;
  
  /**
   * {@inheritDoc}
   */
  public void setUp() throws Exception {
    super.setUp();

    binder = (ResourceBinder) container.getComponentInstanceOfType(ResourceBinder.class);
    handler = (RequestHandler) container.getComponentInstanceOfType(RequestHandler.class);
    
    SessionProviderService sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(ThreadLocalSessionProviderService.class);
    assertNotNull(sessionProviderService);
    sessionProviderService.setSessionProvider(null, new SessionProvider(new ConversationState(new Identity("root"))));
  }
  
  /*public void testInfo() throws Exception {
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    ContainerRequest creq = new ContainerRequest("GET",
                                                 new URI(HTTP_BACKUP_AGENT_PATH + HTTPBackupAgent.Constants.OperationType.BACKUP_SERVICE_INFO),
                                                 new URI(""),
                                                 null,
                                                 new InputHeadersMap(headers));
    ContainerResponse cres = new ContainerResponse(new DummyContainerResponseWriter());
    handler.handleRequest(creq, cres);

    assertEquals(200, cres.getStatus());
    
    BackupServiceInfoBean info = (BackupServiceInfoBean) cres.getEntity();
    BackupManager backupManager = (BackupManager) container.getComponentInstanceOfType(BackupManager.class);
    
    assertNotNull(info);
    assertEquals(backupManager.getBackupDirectory().getAbsolutePath() ,info.getBackupLogDir());
    assertEquals(backupManager.getFullBackupType(), info.getFullBackupType());
    assertEquals(backupManager.getIncrementalBackupType(), info.getIncrementalBackupType());
    assertEquals(backupManager.getDefaultIncrementalJobPeriod(), info.getDefaultIncrementalJobPeriod().longValue());
  }
  
  public void testDropWorkspace() throws Exception {
    //login to workspace '/db6/ws1'
    Session session_db6_ws1 = repositoryService.getRepository("db6").login(credentials, "ws1");
    
    assertNotNull(session_db6_ws1);    
    
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    ContainerRequest creq = new ContainerRequest("GET",
                                                 new URI(HTTP_BACKUP_AGENT_PATH + HTTPBackupAgent.Constants.OperationType.DROP_WORKSPACE +
                                                         "/db6/ws1/true"),
                                                 new URI(""),
                                                 null,
                                                 new InputHeadersMap(headers));
    ContainerResponse cres = new ContainerResponse(new DummyContainerResponseWriter());
    handler.handleRequest(creq, cres);

    assertEquals(200, cres.getStatus());
    
    try {
      Session ses_db6_ws1 = repositoryService.getRepository("db6").login(credentials, "ws1");
      fail();
    } catch (NoSuchWorkspaceException e) {
      //ok
    }
  }*/
  
  public void testStart() throws Exception {
    //login to workspace '/db6/ws2'
    Session session_db6_ws2 = repositoryService.getRepository("db6").login(credentials, "ws2");
    assertNotNull(session_db6_ws2);
    
    session_db6_ws2.getRootNode().addNode("NODE_NAME_TO_TEST");
    session_db6_ws2.save();
    
    
    File f = new File("target/temp/backup/" + System.currentTimeMillis() );
    f.mkdirs();
    
    BackupConfigBean configBean = new BackupConfigBean(BackupManager.FULL_AND_INCREMENTAL,
                                                       f.getPath(),
                                                       10000l);
    
    JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
    JsonValue json = generatorImpl.createJsonObject(configBean);
    
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("Content-Type", "application/json; charset=UTF-8");
    ContainerRequest creq = new ContainerRequest("POST",
                                                 new URI(HTTP_BACKUP_AGENT_PATH + HTTPBackupAgent.Constants.OperationType.START_BACKUP +
                                                         "/db6/ws2"),
                                                 new URI(""),
                                                 new ByteArrayInputStream(json.toString().getBytes("UTF-8")),
                                                 new InputHeadersMap(headers));
    ContainerResponse cres = new ContainerResponse(new DummyContainerResponseWriter());
    handler.handleRequest(creq, cres);

    assertEquals(200, cres.getStatus());
    
    Thread.sleep(5000);
  }
  
  public void testInfoBackup() throws Exception {
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    ContainerRequest creq = new ContainerRequest("GET",
                                                 new URI(HTTP_BACKUP_AGENT_PATH + HTTPBackupAgent.Constants.OperationType.CURRENT_AND_COMPLETED_BACKUPS_INFO),
                                                 new URI(""),
                                                 null,
                                                 new InputHeadersMap(headers));
    ContainerResponse cres = new ContainerResponse(new DummyContainerResponseWriter());
    handler.handleRequest(creq, cres);

    assertEquals(200, cres.getStatus());
    
    ShortInfoList infoList = (ShortInfoList) cres.getEntity();
    List<ShortInfo> list = new ArrayList<ShortInfo>(infoList.getBackups());
    
    assertEquals(1, list.size());
    
    ShortInfo info = list.get(0);
    
    assertNotNull(info);
    assertEquals(BackupManager.FULL_AND_INCREMENTAL ,info.getBackupType().intValue());
    assertNotNull(info.getStartedTime());
    assertNotNull(info.getFinishedTime());
    assertEquals(ShortInfo.CURRENT, info.getType().intValue());
    assertEquals(BackupJob.FINISHED, info.getState().intValue());
    assertEquals("db6", info.getRepositoryName());
    assertEquals("ws2", info.getWorkspaceName());
  }
  
  public void testInfoBackupOnWorkspace() throws Exception {
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    ContainerRequest creq = new ContainerRequest("GET",
                                                 new URI(HTTP_BACKUP_AGENT_PATH + HTTPBackupAgent.Constants.OperationType.CURRENT_AND_COMPLETED_BACKUPS_INFO_ON_WS +
                                                         "/db6/ws2"),
                                                 new URI(""),
                                                 null,
                                                 new InputHeadersMap(headers));
    ContainerResponse cres = new ContainerResponse(new DummyContainerResponseWriter());
    handler.handleRequest(creq, cres);

    assertEquals(200, cres.getStatus());
    
    ShortInfoList infoList = (ShortInfoList) cres.getEntity();
    List<ShortInfo> list = new ArrayList<ShortInfo>(infoList.getBackups());
    
    assertEquals(1, list.size());
    
    ShortInfo info = list.get(0);
    
    assertNotNull(info);
    assertEquals(BackupManager.FULL_AND_INCREMENTAL ,info.getBackupType().intValue());
    assertNotNull(info.getStartedTime());
    assertNotNull(info.getFinishedTime());
    assertEquals(ShortInfo.CURRENT, info.getType().intValue());
    assertEquals(BackupJob.FINISHED, info.getState().intValue());
    assertEquals("db6", info.getRepositoryName());
    assertEquals("ws2", info.getWorkspaceName());
  }
  
  public void testInfoBackupCurrent() throws Exception {
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    ContainerRequest creq = new ContainerRequest("GET",
                                                 new URI(HTTP_BACKUP_AGENT_PATH + HTTPBackupAgent.Constants.OperationType.CURRENT_BACKUPS_INFO),
                                                 new URI(""),
                                                 null,
                                                 new InputHeadersMap(headers));
    ContainerResponse cres = new ContainerResponse(new DummyContainerResponseWriter());
    handler.handleRequest(creq, cres);

    assertEquals(200, cres.getStatus());
    
    ShortInfoList infoList = (ShortInfoList) cres.getEntity();
    List<ShortInfo> list = new ArrayList<ShortInfo>(infoList.getBackups());
    
    assertEquals(1, list.size());
    
    ShortInfo info = list.get(0);
    
    assertNotNull(info);
    assertEquals(BackupManager.FULL_AND_INCREMENTAL ,info.getBackupType().intValue());
    assertNotNull(info.getStartedTime());
    assertNotNull(info.getFinishedTime());
    assertEquals(ShortInfo.CURRENT, info.getType().intValue());
    assertEquals(BackupJob.FINISHED, info.getState().intValue());
    assertEquals("db6", info.getRepositoryName());
    assertEquals("ws2", info.getWorkspaceName());
  }
  
  public void testInfoBackupCurrentById() throws Exception {
    // Get backup id for backup on workspace /db6/ws2
    String id = null;
    
    {
      MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
      ContainerRequest creq = new ContainerRequest("GET",
                                                   new URI(HTTP_BACKUP_AGENT_PATH + HTTPBackupAgent.Constants.OperationType.CURRENT_BACKUPS_INFO),
                                                   new URI(""),
                                                   null,
                                                   new InputHeadersMap(headers));
      ContainerResponse cres = new ContainerResponse(new DummyContainerResponseWriter());
      handler.handleRequest(creq, cres);
  
      assertEquals(200, cres.getStatus());
      
      ShortInfoList infoList = (ShortInfoList) cres.getEntity();
      List<ShortInfo> list = new ArrayList<ShortInfo>(infoList.getBackups());
      
      assertEquals(1, list.size());
      
      ShortInfo info = list.get(0);
      
      assertEquals(info.getRepositoryName(), "db6");
      assertEquals(info.getWorkspaceName(), "ws2");
      
      id = info.getBackupId();
    }
    
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    ContainerRequest creq = new ContainerRequest("GET",
                                                 new URI(HTTP_BACKUP_AGENT_PATH + HTTPBackupAgent.Constants.OperationType.CURRENT_OR_COMPLETED_BACKUP_INFO +
                                                         "/" + id),
                                                 new URI(""),
                                                 null,
                                                 new InputHeadersMap(headers));
    ContainerResponse cres = new ContainerResponse(new DummyContainerResponseWriter());
    handler.handleRequest(creq, cres);

    assertEquals(200, cres.getStatus());
    
    DetailedInfo info = (DetailedInfo) cres.getEntity();
    
    assertNotNull(info);
    assertEquals(BackupManager.FULL_AND_INCREMENTAL ,info.getBackupType().intValue());
    assertNotNull(info.getStartedTime());
    assertNotNull(info.getFinishedTime());
    assertEquals(ShortInfo.CURRENT, info.getType().intValue());
    assertEquals(BackupJob.FINISHED, info.getState().intValue());
    assertEquals("db6", info.getRepositoryName());
    assertEquals("ws2", info.getWorkspaceName());
    assertNotNull(info.getBackupConfig());
  }
  
  public void testStop() throws Exception {
    // Get backup id for backup on workspace /db6/ws2
    String id = null;
    
    {
      MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
      ContainerRequest creq = new ContainerRequest("GET",
                                                   new URI(HTTP_BACKUP_AGENT_PATH + HTTPBackupAgent.Constants.OperationType.CURRENT_BACKUPS_INFO),
                                                   new URI(""),
                                                   null,
                                                   new InputHeadersMap(headers));
      ContainerResponse cres = new ContainerResponse(new DummyContainerResponseWriter());
      handler.handleRequest(creq, cres);
  
      assertEquals(200, cres.getStatus());
      
      ShortInfoList infoList = (ShortInfoList) cres.getEntity();
      List<ShortInfo> list = new ArrayList<ShortInfo>(infoList.getBackups());
      
      assertEquals(1, list.size());
      
      ShortInfo info = list.get(0);
      
      assertEquals(info.getRepositoryName(), "db6");
      assertEquals(info.getWorkspaceName(), "ws2");
      
      id = info.getBackupId();
    }
    
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    ContainerRequest creq = new ContainerRequest("GET",
                                                 new URI(HTTP_BACKUP_AGENT_PATH + HTTPBackupAgent.Constants.OperationType.STOP_BACKUP +
                                                         "/" + id),
                                                 new URI(""),
                                                 null,
                                                 new InputHeadersMap(headers));
    ContainerResponse cres = new ContainerResponse(new DummyContainerResponseWriter());
    handler.handleRequest(creq, cres);

    assertEquals(200, cres.getStatus());
  }
  
  public void testInfoBackupCompleted() throws Exception {
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    ContainerRequest creq = new ContainerRequest("GET",
                                                 new URI(HTTP_BACKUP_AGENT_PATH + HTTPBackupAgent.Constants.OperationType.COMPLETED_BACKUPS_INFO),
                                                 new URI(""),
                                                 null,
                                                 new InputHeadersMap(headers));
    ContainerResponse cres = new ContainerResponse(new DummyContainerResponseWriter());
    handler.handleRequest(creq, cres);

    assertEquals(200, cres.getStatus());
    
    ShortInfoList infoList = (ShortInfoList) cres.getEntity();
    List<ShortInfo> list = new ArrayList<ShortInfo>(infoList.getBackups());
    
    assertEquals(1, list.size());
    
    ShortInfo info = list.get(0);
    
    assertNotNull(info);
    assertEquals(BackupManager.FULL_AND_INCREMENTAL ,info.getBackupType().intValue());
    assertNotNull(info.getStartedTime());
    assertNotNull(info.getFinishedTime());
    assertEquals(ShortInfo.COMPLETED, info.getType().intValue());
    assertEquals(0, info.getState().intValue());
    assertEquals("db6", info.getRepositoryName());
    assertEquals("ws2", info.getWorkspaceName());
  }
  
  public void testInfoBackupCompletedById() throws Exception {
    // Get backup id for backup on workspace /db6/ws2
    String id = null;
    
    {
      MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
      ContainerRequest creq = new ContainerRequest("GET",
                                                   new URI(HTTP_BACKUP_AGENT_PATH + HTTPBackupAgent.Constants.OperationType.COMPLETED_BACKUPS_INFO),
                                                   new URI(""),
                                                   null,
                                                   new InputHeadersMap(headers));
      ContainerResponse cres = new ContainerResponse(new DummyContainerResponseWriter());
      handler.handleRequest(creq, cres);
  
      assertEquals(200, cres.getStatus());
      
      ShortInfoList infoList = (ShortInfoList) cres.getEntity();
      List<ShortInfo> list = new ArrayList<ShortInfo>(infoList.getBackups());
      
      assertEquals(1, list.size());
      
      ShortInfo info = list.get(0);
      
      assertEquals(info.getRepositoryName(), "db6");
      assertEquals(info.getWorkspaceName(), "ws2");
      
      id = info.getBackupId();
    }
    
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    ContainerRequest creq = new ContainerRequest("GET",
                                                 new URI(HTTP_BACKUP_AGENT_PATH + HTTPBackupAgent.Constants.OperationType.CURRENT_OR_COMPLETED_BACKUP_INFO +
                                                         "/" + id),
                                                 new URI(""),
                                                 null,
                                                 new InputHeadersMap(headers));
    ContainerResponse cres = new ContainerResponse(new DummyContainerResponseWriter());
    handler.handleRequest(creq, cres);

    assertEquals(200, cres.getStatus());
    
    DetailedInfo info = (DetailedInfo) cres.getEntity();
    
    assertNotNull(info);
    assertEquals(BackupManager.FULL_AND_INCREMENTAL ,info.getBackupType().intValue());
    assertNotNull(info.getStartedTime());
    assertNotNull(info.getFinishedTime());
    assertEquals(ShortInfo.COMPLETED, info.getType().intValue());
    assertEquals(0, info.getState().intValue());
    assertEquals("db6", info.getRepositoryName());
    assertEquals("ws2", info.getWorkspaceName());
    
    assertNotNull(info.getBackupConfig());
  }
  
}
