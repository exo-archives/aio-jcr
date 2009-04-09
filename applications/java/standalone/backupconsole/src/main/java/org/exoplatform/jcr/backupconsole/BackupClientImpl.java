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
package org.exoplatform.jcr.backupconsole;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.Response;

import org.exoplatform.services.jcr.ext.backup.BackupJob;
import org.exoplatform.services.jcr.ext.backup.BackupManager;
import org.exoplatform.services.jcr.ext.backup.server.HTTPBackupAgent;
import org.exoplatform.services.jcr.ext.backup.server.JobWorkspaceRestore;
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
import org.exoplatform.ws.frameworks.json.JsonHandler;
import org.exoplatform.ws.frameworks.json.JsonParser;
import org.exoplatform.ws.frameworks.json.impl.BeanBuilder;
import org.exoplatform.ws.frameworks.json.impl.JsonDefaultHandler;
import org.exoplatform.ws.frameworks.json.impl.JsonException;
import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;
import org.exoplatform.ws.frameworks.json.impl.JsonParserImpl;
import org.exoplatform.ws.frameworks.json.value.JsonValue;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: BackupClientImpl.java 111 2008-11-11 11:11:11Z serg $
 */
public class BackupClientImpl implements BackupClient {

  /**
   * Block size.
   */
  private static final int   BLOCK_SIZE = 1024;

  /**
   * Client transport.
   */
  private ClientTransport transport;

  /**
   * User login.
   */
  private final String    userName;

  /**
   * User password.
   */
  private final String    pass;

  /**
   * Constructor.
   * 
   * @param transport ClientTransport implementation.
   * @param login user login.
   * @param pass user password.
   */
  public BackupClientImpl(ClientTransport transport, String login, String pass) {
    this.transport = transport;
    this.userName = login;
    this.pass = pass;
  }

  /**
   * {@inheritDoc}
   */
  public String startBackUp(String repositoryName, String workspaceName, String backupDir) throws IOException, BackupExecuteException {
    String sURL = HTTPBackupAgent.Constants.BASE_URL + "/" + HTTPBackupAgent.Constants.OperationType.START_BACKUP;

    BackupConfigBean bean = new BackupConfigBean(BackupManager.FULL_BACKUP_ONLY,
                                                 repositoryName,
                                                 workspaceName,
                                                 backupDir);
    
    JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
    JsonValue json;
    try {
      json = generatorImpl.createJsonObject(bean);
    } catch (JsonException e) {
      throw new BackupExecuteException("Can not get json from  : " + bean.getClass().toString(), e);
    }
        
    BackupAgentResponse response  = transport.executePOST(sURL, json.toString());
    
    if (response.getStatus() == Response.Status.OK.getStatusCode()) {
      BackupChainBean chainBean;
      try {
        chainBean = (BackupChainBean) getObject(BackupChainBean.class, response.getResponseData());
      } catch (Exception e) {
        throw new  BackupExecuteException("Can not get BackupChainBean from responce.", e);
      }
      
      String result = "\nThe backup has been started : \n";
      
      result  += ("\tbackup id                : " + chainBean.getBackupId() + "\n"
                + "\tbackup type              : " + (chainBean.getBackupType() == BackupManager.FULL_AND_INCREMENTAL ? "full + incremetal" : "full only") + "\n" 
                + "\trepository name          : " + chainBean.getRepositoryName() + "\n"
                + "\tworkspace name           : " + chainBean.getWorkspaceName() + "\n\n");
      
      return result;
    } else {
      return failureProcessing(response.getResponseData());
    } 
  }

  /**
   * {@inheritDoc}
   */
  public String startIncrementalBackUp(String repositoryName, String workspaceName, String backupDir, long incr) throws IOException,
                                                                                 BackupExecuteException {
    String sURL = HTTPBackupAgent.Constants.BASE_URL + "/" + HTTPBackupAgent.Constants.OperationType.START_BACKUP;

    BackupConfigBean bean = new BackupConfigBean(BackupManager.FULL_AND_INCREMENTAL,
                                                 repositoryName,
                                                 workspaceName,
                                                 backupDir,
                                                 incr);
    
    JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
    JsonValue json;
    try {
      json = generatorImpl.createJsonObject(bean);
    } catch (JsonException e) {
      throw new BackupExecuteException("Can not get json from  : " + bean.getClass().toString(), e);
    }
        
    BackupAgentResponse response  = transport.executePOST(sURL, json.toString());
    
    if (response.getStatus() == Response.Status.OK.getStatusCode()) {
      BackupChainBean chainBean;
      try {
        chainBean = (BackupChainBean) getObject(BackupChainBean.class, response.getResponseData());
      } catch (Exception e) {
        throw new  BackupExecuteException("Can not get BackupChainBean from responce.", e);
      }
      
      String result = "\nThe backup has been started : \n";
      
      result  += ("\tbackup id                : " + chainBean.getBackupId() + "\n"
                + "\tbackup type              : " + (chainBean.getBackupType() == BackupManager.FULL_AND_INCREMENTAL ? "full + incremetal" : "full only") + "\n" 
                + "\trepository name          : " + chainBean.getRepositoryName() + "\n"
                + "\tworkspace name           : " + chainBean.getWorkspaceName() + "\n\n");
      
      return result;
    } else {
      return failureProcessing(response.getResponseData());
    } 
  }

  /**
   * {@inheritDoc}
   */
  public String status(String backupId) throws IOException, BackupExecuteException {
    String sURL = HTTPBackupAgent.Constants.BASE_URL + "/" + HTTPBackupAgent.Constants.OperationType.CURRENT_BACKUP_INFO + "/" + backupId;

    BackupAgentResponse response  = transport.executeGET(sURL);
    
    if (response.getStatus() == Response.Status.OK.getStatusCode()) {
      BackupChainInfoBean infoBeen;
      try {
        infoBeen = (BackupChainInfoBean) getObject(BackupChainInfoBean.class, response.getResponseData());
      } catch (Exception e) {
        throw new  BackupExecuteException("Can not get BackupChainInfoBean from responce.", e);
      }
      
      String result = "\nThe active backup information : \n";
      
      result  += ("\tbackup id                : " + infoBeen.getBackupId() + "\n"
                + "\tbackup log file          : " + infoBeen.getBackupLog() + "\n" 
                + "\trepository name          : " + infoBeen.getRepositoryName() + "\n"
                + "\tworkspace name           : " + infoBeen.getWorkspaceName() + "\n"
                + "\tbackup type              : " + (infoBeen.getBackupType() == BackupManager.FULL_AND_INCREMENTAL ? "full + incremetal" : "full only") + "\n"
                + (infoBeen.getBackupType() == BackupManager.FULL_BACKUP_ONLY ? "" : 
                  "\tincremental job period   : " + infoBeen.getBackupConfigBeen().getIncrementalJobPeriod() + "\n")
                + "\tpath to backup folder    : " + infoBeen.getBackupConfigBeen().getBackupDir() + "\n"
                + "\tfull backup state        : " +  getState(infoBeen.getFullBackupState()) + "\n"
                + (infoBeen.getBackupType() == BackupManager.FULL_BACKUP_ONLY ? "\n" : "\tincremental backup state : " +  "working" + "\n\n"));
      
      return result;
    } else {
      return failureProcessing(response.getResponseData());
    }
  }

  /**
   * {@inheritDoc}
   */
  public String stop(String backupId) throws IOException, BackupExecuteException {
    String sURL = HTTPBackupAgent.Constants.BASE_URL + "/" + HTTPBackupAgent.Constants.OperationType.STOP_BACKUP + "/" + backupId;

    BackupAgentResponse response  = transport.executeGET(sURL);
    
    if (response.getStatus() == Response.Status.OK.getStatusCode()) {
      MessageBean message;
      try {
        message = (MessageBean) getObject(MessageBean.class, response.getResponseData());
      } catch (Exception e) {
        throw new  BackupExecuteException("Can not get MessageBean from responce.", e);
      }
      
      return "\n" + message.getMessage() + "\n\n";
    } else {
      return failureProcessing(response.getResponseData());
    } 
  }

  /**
   * {@inheritDoc}
   */
  public String restore(String repositoryName, String workspaceName, String backupId, InputStream config) throws IOException,
                                                                                 BackupExecuteException {
    String sURL = HTTPBackupAgent.Constants.BASE_URL + "/" + HTTPBackupAgent.Constants.OperationType.RESTORE;

    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    byte[] b = new byte[BLOCK_SIZE];
    int len = 0;
    while ((len = config.read(b)) != -1) {
      bout.write(b, 0, len);
    }
    config.close();
    byte[] cb = bout.toByteArray();
    bout.close();

    RestoreBean bean = new RestoreBean(backupId,
                                       repositoryName,
                                       workspaceName,
                                       new String(cb, "UTF-8"));
    JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
    JsonValue json;
    
    try {
      json = generatorImpl.createJsonObject(bean);
    } catch (JsonException e) {
      throw new BackupExecuteException("Can not get json from  : " + bean.getClass().toString(), e);
    }
        
    BackupAgentResponse response  = transport.executePOST(sURL, json.toString());
    
    if (response.getStatus() == Response.Status.OK.getStatusCode()) {
      MessageBean message;
      try {
        message = (MessageBean) getObject(MessageBean.class, response.getResponseData());
      } catch (Exception e) {
        throw new  BackupExecuteException("Can not get MessageBean from responce.", e);
      }
      
      return "\n" + message.getMessage() + "\n\n";
    } else {
      return failureProcessing(response.getResponseData());
    }
  }

  /**
   * {@inheritDoc}
   */
  public String drop(boolean forceClose, String repositoryName, String workspaceName) throws IOException,
                                                         BackupExecuteException {
    String sURL = HTTPBackupAgent.Constants.BASE_URL + "/" + HTTPBackupAgent.Constants.OperationType.DROP_WORKSPACE;

    DropWorkspaceBean bean = new DropWorkspaceBean(repositoryName,
                                                   workspaceName, 
                                                   forceClose);
    
    JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
    JsonValue json;
    try {
      json = generatorImpl.createJsonObject(bean);
    } catch (JsonException e) {
      throw new BackupExecuteException("Can not get json from  : " + bean.getClass().toString(), e);
    }
    
    BackupAgentResponse response  = transport.executePOST(sURL, json.toString());
    
    if (response.getStatus() == Response.Status.OK.getStatusCode()) {
      MessageBean message;
      try {
        message = (MessageBean) getObject(MessageBean.class, response.getResponseData());
      } catch (Exception e) {
        throw new  BackupExecuteException("Can not get MessageBean from responce.", e);
      }
      
      return "\n" + message.getMessage() + "\n\n";
    } else {
      return failureProcessing(response.getResponseData());
    } 
  }

  /**
   * {@inheritDoc}
   */
  public String info() throws IOException, BackupExecuteException {
    String sURL = HTTPBackupAgent.Constants.BASE_URL + "/" + HTTPBackupAgent.Constants.OperationType.BACKUP_SERVICE_INFO;
    BackupAgentResponse response = transport.executePOST(sURL, null);
    
    if (response.getStatus() == Response.Status.OK.getStatusCode()) {
      BackupServiceInfoBean info;
      try {
        info = (BackupServiceInfoBean) getObject(BackupServiceInfoBean.class, response.getResponseData());
      } catch (Exception e) {
        throw new  RuntimeException("Can not get BackupServiceInfoBean from responce.", e);
      }
      
      String result = "\nThe backup service information : \n"
                      + "\tfull backup type       : " + info.getFullBackupType() + "\n"
                      + "\tincremetal backup type : " + info.getIncrementalBackupType() + "\n"
                      + "\tbackup log folder      : " + info.getBackupLogDir() + "\n\n";
      
      return result;
    } else {
      return failureProcessing(response.getResponseData());
    } 
  }

  /**
   * {@inheritDoc}
   */
  public String list() throws IOException, BackupExecuteException {
    String sURL = HTTPBackupAgent.Constants.BASE_URL + "/" + HTTPBackupAgent.Constants.OperationType.CURRENT_BACKUPS_INFO;
    BackupAgentResponse response = transport.executePOST(sURL, null);
    
    if (response.getStatus() == Response.Status.OK.getStatusCode()) {
      BackupChainListBean listBeen;
      try {
        listBeen = (BackupChainListBean) getObject(BackupChainListBean.class, response.getResponseData());
      } catch (Exception e) {
        throw new  RuntimeException("Can not get BackupChainListBean from responce.", e);
      }
      
      
      String result = "\nThe current backups information : \n";
      
      if (listBeen.getBackupChains().size() == 0)
        result += "\tNo active backups.\n\n";
      
      int count = 1;
      for (BackupChainBean chainBean : listBeen.getBackupChains()) {
        result += "\t" + count + ") Backup with id " + chainBean.getBackupId()  + " :\n";
        
        result  += ("\t\trepository name           : " + chainBean.getRepositoryName() + "\n"
                  + "\t\tworkspace name            : " + chainBean.getWorkspaceName() + "\n"
                  + "\t\tbackup type               : " + (chainBean.getBackupType() == BackupManager.FULL_AND_INCREMENTAL ? "full + incremetal" : "full only") + "\n"  
                  + "\t\tfull backup state         : " +  getState(chainBean.getFullBackupState()) + "\n"
                  + (chainBean.getBackupType() == BackupManager.FULL_BACKUP_ONLY ? "\n" : 
                    "\t\tincremental backup state  : " +  "working" + "\n\n"));
        count++;
      }   
      
      return result;
    } else {
      return failureProcessing(response.getResponseData());
    }
  }

  /**
   * {@inheritDoc}
   */
  public String listCompleted() throws IOException, BackupExecuteException {
    String sURL = HTTPBackupAgent.Constants.BASE_URL + "/" + HTTPBackupAgent.Constants.OperationType.COMPLETED_BACKUPS_INFO;
    BackupAgentResponse response = transport.executePOST(sURL, null);
    
    if (response.getStatus() == Response.Status.OK.getStatusCode()) {
      ChainLogListBean listBeen;
      try {
        listBeen = (ChainLogListBean) getObject(ChainLogListBean.class, response.getResponseData());
      } catch (Exception e) {
        throw new  RuntimeException("Can not get ChainLogListBean from responce.", e);
      }
      
      
      String result = "\nThe completed (ready to restore) backups information : \n";
      
      if (listBeen.getChainLogs().size() == 0)
        result += "\tNo completed backups.\n\n";
      
      int count = 1;
      for (ChainLogBean chainLogBean : listBeen.getChainLogs()) {
        result += "\t" + count + ") Backup with id " + chainLogBean.getBackupId()  + " :\n";
        
        BackupConfigBean configBean = chainLogBean.getBackupConfigBeen();
        
        result  += ("\t\tfull backup date         : " + chainLogBean.getFullBackupDate() + "\n"
                  + "\t\tbackup log file         : " + chainLogBean.getBackupLog() + "\n" 
                  + "\t\trepository name         : " + configBean.getRepositoryName() + "\n"
                  + "\t\tworkspace name          : " + configBean.getWorkspaceName() + "\n"
                  + "\t\tbackup type             : " + (configBean.getBackupType() == BackupManager.FULL_AND_INCREMENTAL ? "full + incremetal" : "full only") + "\n"
                  + (configBean.getBackupType() == BackupManager.FULL_BACKUP_ONLY ? "" : "\t\tincremental job period  : " + configBean.getIncrementalJobPeriod() + "\n")
                  + "\t\tpath to backup folder   : " + configBean.getBackupDir() + "\n\n");
        count++;
      }   
      
      return result;
    } else {
      return failureProcessing(response.getResponseData());
    }
  }

  /**
   * {@inheritDoc}
   */
  public String restores() throws IOException, BackupExecuteException {
    String sURL = HTTPBackupAgent.Constants.BASE_URL + "/" + HTTPBackupAgent.Constants.OperationType.CURRENT_RESTORES_INFO;
    BackupAgentResponse response = transport.executePOST(sURL, null);
    
    if (response.getStatus() == Response.Status.OK.getStatusCode()) {
      RestoreChainLogListBean listBeen;
      try {
        listBeen = (RestoreChainLogListBean) getObject(RestoreChainLogListBean.class, response.getResponseData());
      } catch (Exception e) {
        throw new  RuntimeException("Can not get RestoreChainLogListBean from responce.", e);
      }
      
      
      String result = "\nThe current restores information : \n";
      
      if (listBeen.getRestoresChainLogs().size() == 0)
        result += "\tNo active restores.\n";
      
      int count = 1;
      for (RestoreChainLogBean chainLogBean : listBeen.getRestoresChainLogs()) {
        result += "\t" + count + ") Restore with id " + chainLogBean.getBackupId()  + ":\n";
        
        BackupConfigBean configBean = chainLogBean.getBackupConfigBeen();
        
        result  += ("\t\tfull backup date        : " + chainLogBean.getFullBackupDate() + "\n"
                  + "\t\tbackup log file         : " + chainLogBean.getBackupLog() + "\n" 
                  + "\t\trepository name         : " + chainLogBean.getRepositoryName() + "\n"
                  + "\t\tworkspace name          : " + chainLogBean.getWorkspaceName() + "\n"
                  + "\t\tbackup type             : " + (configBean.getBackupType() == BackupManager.FULL_AND_INCREMENTAL ? "full + incremetal" : "full only") + "\n"
                  + "\t\tpath to backup folder   : " + configBean.getBackupDir() + "\n"
                  + "\t\trestore state           : " +  getRestoreState(chainLogBean.getRestoreState()) + "\n"
                  + (chainLogBean.getRestoreState() == JobWorkspaceRestore.RESTORE_FAIL ? 
                    "\t\tfailure message         : "  + chainLogBean.getFailMessage()  + "\n" : "\n"));
        count++;
      }   
      
      return result;
    } else {
      return failureProcessing(response.getResponseData());
    }
  }
  
  
  /**
   * Will be created the Object from JSON binary data.
   *
   * @param cl 
   *          Class
   * @param data
   *          binary data (JSON)
   * @return Object
   * @throws Exception
   *           will be generated Exception
   */
  private Object getObject(Class cl, byte[] data) throws Exception { 
    JsonHandler jsonHandler = new JsonDefaultHandler();
    JsonParser jsonParser = new JsonParserImpl();
    InputStream inputStream = new ByteArrayInputStream(data);
    jsonParser.parse(inputStream, jsonHandler);
    JsonValue jsonValue = jsonHandler.getJsonObject();

    return new BeanBuilder().createObject(cl, jsonValue);
  }
  
  
  private String getRestoreState(int restoreState) {
    String state = "unknown sate of restore";
    
    switch (restoreState) {
    case JobWorkspaceRestore.RESTORE_INITIALIZED:
       state = "initialized";
      break;
      
    case JobWorkspaceRestore.RESTORE_STARTED:
      state = "started";
     break;
     
    case JobWorkspaceRestore.RESTORE_SUCCESSFUL:
      state = "successful";
     break;
     
    case JobWorkspaceRestore.RESTORE_FAIL:
      state = "fail";
     break;

    default:
      break;
    }
    
    return state;
  }
  
  /**
   * getState.
   * 
   * @param state
   *          value of state
   * @return String sate
   */
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
    default:
      break;
    }

    return st;
  }
  
  /**
   * failureProcessing.
   *
   * @param data
   *          response data
   * @return String
   *           result
   * @throws BackupExecuteException
   *           will be generated BackupExecuteException  
   */
  private String failureProcessing(byte[] data) throws BackupExecuteException {
    FailureBean failure;
    try {
      failure = (FailureBean) getObject(FailureBean.class, data);
    } catch (Exception e) {
      throw new  BackupExecuteException("Can not get FailureBean from responce.", e);
    }
    
    String result = "\n" + failure.getMessage() + "\n"
                    + "\texception message    : " + failure.getExceptionMessage() + "\n\n";
    
    return result;
  }
}
