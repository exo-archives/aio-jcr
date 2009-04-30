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
import java.io.UnsupportedEncodingException;

import javax.ws.rs.core.Response;

import org.exoplatform.services.jcr.ext.backup.BackupJob;
import org.exoplatform.services.jcr.ext.backup.BackupManager;
import org.exoplatform.services.jcr.ext.backup.server.HTTPBackupAgent;
import org.exoplatform.services.jcr.ext.backup.impl.JobWorkspaceRestore;
import org.exoplatform.services.jcr.ext.backup.server.bean.BackupConfigBean;
import org.exoplatform.services.jcr.ext.backup.server.bean.RestoreBean;
import org.exoplatform.services.jcr.ext.backup.server.bean.response.BackupServiceInfoBean;
import org.exoplatform.services.jcr.ext.backup.server.bean.response.DetailedInfo;
import org.exoplatform.services.jcr.ext.backup.server.bean.response.ShortInfo;
import org.exoplatform.services.jcr.ext.backup.server.bean.response.ShortInfoList;
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
    String sURL = HTTPBackupAgent.Constants.BASE_URL + 
                  HTTPBackupAgent.Constants.OperationType.START_BACKUP +
                  "/" + repositoryName +
                  "/" + workspaceName;

    BackupConfigBean bean = new BackupConfigBean(BackupManager.FULL_BACKUP_ONLY,
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
      return "\nSuccessful : \n" +
      		   "\tstatus code = " + response.getStatus() + "\n";
    } else {
      return failureProcessing(response);
    } 
  }

  /**
   * {@inheritDoc}
   */
  public String startIncrementalBackUp(String repositoryName, String workspaceName, String backupDir, long incr) throws IOException,
                                                                                 BackupExecuteException {
    String sURL = HTTPBackupAgent.Constants.BASE_URL + 
                  HTTPBackupAgent.Constants.OperationType.START_BACKUP +
                  "/" + repositoryName +
                  "/" + workspaceName;

    BackupConfigBean bean = new BackupConfigBean(BackupManager.FULL_AND_INCREMENTAL,
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
      return "\nSuccessful : \n" +
             "\tstatus code = " + response.getStatus() + "\n";
    } else {
      return failureProcessing(response);
    } 
  }

  /**
   * {@inheritDoc}
   */
  public String status(String backupId) throws IOException, BackupExecuteException {
    String sURL = HTTPBackupAgent.Constants.BASE_URL + 
                  HTTPBackupAgent.Constants.OperationType.CURRENT_OR_COMPLETED_BACKUP_INFO + 
                  "/" + backupId;

    BackupAgentResponse response  = transport.executeGET(sURL);
    
    if (response.getStatus() == Response.Status.OK.getStatusCode()) {
      DetailedInfo info;
      try {
        info = (DetailedInfo) getObject(DetailedInfo.class, response.getResponseData());
      } catch (Exception e) {
        throw new  RuntimeException("Can not get DetailedInfo from responce.", e);
      }
      
      if (info.getType() == DetailedInfo.COMPLETED) {
        String result = "\nThe completed (ready to restore) backup information : \n";
      
        BackupConfigBean configBean = info.getBackupConfig();
        
        result  += ("\t\tbackup id               : " + info.getBackupId() + "\n" 
                  + "\t\tbackup folder           : " + configBean.getBackupDir() + "\n" 
                  + "\t\trepository name         : " + info.getRepositoryName() + "\n"
                  + "\t\tworkspace name          : " + info.getWorkspaceName() + "\n"
                  + "\t\tbackup type             : " + (configBean.getBackupType() == BackupManager.FULL_AND_INCREMENTAL ? "full + incremetal" : "full only") + "\n"
                  + "\t\tstarted time            : " + info.getStartedTime() + "\n"
                  + (info.getFinishedTime().equals("") ? "\n" : 
                    "\t\tfinished time           : " + info.getFinishedTime() + "\n\n"));
        
        return result;
      } else {
        String result = "\nThe current backup information : \n";
        
        BackupConfigBean configBean = info.getBackupConfig();
        
        result  += ("\t\tbackup id                : " + info.getBackupId() + "\n" 
                  + "\t\tbackup folder            : " + configBean.getBackupDir() + "\n" 
                  + "\t\trepository name          : " + info.getRepositoryName() + "\n"
                  + "\t\tworkspace name           : " + info.getWorkspaceName() + "\n"
                  + "\t\tbackup type              : " + (configBean.getBackupType() == BackupManager.FULL_AND_INCREMENTAL ? "full + incremetal" : "full only") + "\n"
                  + "\t\tfull backup state        : " + getState(info.getState())) + "\n"
                  + (info.getBackupType() == BackupManager.FULL_BACKUP_ONLY ? "" : 
                    "\t\tincremental backup state : " +  "working" + "\n")
                  + "\t\tstarted time             : " + info.getStartedTime() + "\n"
                  + (info.getFinishedTime().equals("") ? "\n" : 
                    "\t\tfinished time            : " + info.getFinishedTime() + "\n\n");
        
        return result;
      }
    } else {
      return failureProcessing(response);
    }
  }

  /**
   * {@inheritDoc}
   */
  public String stop(String backupId) throws IOException, BackupExecuteException {
    String sURL = HTTPBackupAgent.Constants.BASE_URL + 
                  HTTPBackupAgent.Constants.OperationType.STOP_BACKUP + 
                  "/" + backupId;

    BackupAgentResponse response  = transport.executeGET(sURL);
    
    if (response.getStatus() == Response.Status.OK.getStatusCode()) {
      return "\nSuccessful : \n" +
             "\tstatus code = " + response.getStatus() + "\n";
    } else {
      return failureProcessing(response);
    } 
  }

  /**
   * {@inheritDoc}
   */
  public String restore(String repositoryName, String workspaceName, String backupId, InputStream config) throws IOException,
                                                                                 BackupExecuteException {
    String sURL = HTTPBackupAgent.Constants.BASE_URL + 
                  HTTPBackupAgent.Constants.OperationType.RESTORE +
                  "/" + repositoryName +
                  "/" + workspaceName;

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
      return "\nSuccessful : \n" +
             "\tstatus code = " + response.getStatus() + "\n";
    } else {
      return failureProcessing(response);
    }
  }

  /**
   * {@inheritDoc}
   */
  public String drop(boolean forceClose, String repositoryName, String workspaceName) throws IOException,
                                                         BackupExecuteException {
    String sURL = HTTPBackupAgent.Constants.BASE_URL + 
                  HTTPBackupAgent.Constants.OperationType.DROP_WORKSPACE +
                  "/" + repositoryName +
                  "/" + workspaceName + 
                  "/" + forceClose;

    BackupAgentResponse response  = transport.executeGET(sURL);
    
    if (response.getStatus() == Response.Status.OK.getStatusCode()) {
      return "\nSuccessful : \n" +
             "\tstatus code = " + response.getStatus() + "\n";
    } else {
      return failureProcessing(response);
    } 
  }

  /**
   * {@inheritDoc}
   */
  public String info() throws IOException, BackupExecuteException {
    String sURL = HTTPBackupAgent.Constants.BASE_URL + HTTPBackupAgent.Constants.OperationType.BACKUP_SERVICE_INFO;
    BackupAgentResponse response = transport.executeGET(sURL);
    
    if (response.getStatus() == Response.Status.OK.getStatusCode()) {
      BackupServiceInfoBean info;
      try {
        info = (BackupServiceInfoBean) getObject(BackupServiceInfoBean.class, response.getResponseData());
      } catch (Exception e) {
        throw new  RuntimeException("Can not get BackupServiceInfoBean from responce.", e);
      }
      
      String result = "\nThe backup service information : \n"
                      + "\tfull backup type               : " + info.getFullBackupType() + "\n"
                      + "\tincremetal backup type         : " + info.getIncrementalBackupType() + "\n"
                      + "\tbackup log folder              : " + info.getBackupLogDir() + "\n"
                      + "\tdefault incremental job period : " + info.getDefaultIncrementalJobPeriod() + "\n\n";
      
      return result;
    } else {
      return failureProcessing(response);
    } 
  }

  /**
   * {@inheritDoc}
   */
  public String list() throws IOException, BackupExecuteException {
    String sURL = HTTPBackupAgent.Constants.BASE_URL + HTTPBackupAgent.Constants.OperationType.CURRENT_BACKUPS_INFO;
    BackupAgentResponse response = transport.executeGET(sURL);
    
    if (response.getStatus() == Response.Status.OK.getStatusCode()) {
      ShortInfoList infoList;
      try {
        infoList = (ShortInfoList) getObject(ShortInfoList.class, response.getResponseData());
      } catch (Exception e) {
        throw new  RuntimeException("Can not get ShortInfoList from responce.", e);
      }
      
      String result = "\nThe current backups information : \n";
      
      if (infoList.getBackups().size() == 0)
        result += "\tNo active backups.\n\n";
      
      int count = 1;
      for (ShortInfo shortInfo : infoList.getBackups()) {
        result += "\t" + count + ") Backup with id " + shortInfo.getBackupId()  + " :\n";
        
        result  += ("\t\trepository name           : " + shortInfo.getRepositoryName() + "\n"
                  + "\t\tworkspace name            : " + shortInfo.getWorkspaceName() + "\n"
                  + "\t\tbackup type               : " + (shortInfo.getBackupType() == BackupManager.FULL_AND_INCREMENTAL ? "full + incremetal" : "full only") + "\n"  
                  + "\t\tfull backup state         : " + getState(shortInfo.getState())) + "\n"
                  + (shortInfo.getBackupType() == BackupManager.FULL_BACKUP_ONLY ? "" : 
                    "\t\tincremental backup state  : " +  "working" + "\n")
                  + "\t\tstarted time              : " + shortInfo.getStartedTime() + "\n"
                  + (shortInfo.getFinishedTime().equals("") ? "" : 
                    "\t\tfinished time             : " + shortInfo.getFinishedTime() + "\n");
        count++;
      }   
      
      return result;
    } else {
      return failureProcessing(response);
    }
  }

  /**
   * {@inheritDoc}
   */
  public String listCompleted() throws IOException, BackupExecuteException {
    String sURL = HTTPBackupAgent.Constants.BASE_URL + HTTPBackupAgent.Constants.OperationType.COMPLETED_BACKUPS_INFO;
    BackupAgentResponse response = transport.executeGET(sURL);
    
    if (response.getStatus() == Response.Status.OK.getStatusCode()) {
      ShortInfoList infoList;
      try {
        infoList = (ShortInfoList) getObject(ShortInfoList.class, response.getResponseData());
        new String(response.getResponseData());
      } catch (Exception e) {e.printStackTrace();
        throw new  RuntimeException("Can not get ShortInfoList from responce.", e);
      }
      
      String result = "\nThe completed (ready to restore) backups information : \n";
      
      if (infoList.getBackups().size() == 0)
        result += "\tNo completed backups.\n\n";
      
      int count = 1;
      for (ShortInfo shortInfo : infoList.getBackups()) {
        result += "\t" + count + ") Backup with id " + shortInfo.getBackupId()  + " :\n";
        
        result  += ("\t\trepository name           : " + shortInfo.getRepositoryName() + "\n"
                  + "\t\tworkspace name            : " + shortInfo.getWorkspaceName() + "\n"
                  + "\t\tbackup type               : " + (shortInfo.getBackupType() == BackupManager.FULL_AND_INCREMENTAL ? "full + incremetal" : "full only") + "\n"  
                  + "\t\tstarted time              : " + shortInfo.getStartedTime() + "\n"
                  + (shortInfo.getFinishedTime().equals("") ? "\n" : 
                    "\t\tfinished time             : " + shortInfo.getFinishedTime() + "\n"));
        count++;
      }   
      
      return result;
    } else {
      return failureProcessing(response);
    }
  }

  /**
   * {@inheritDoc}
   */
  public String restores(String repositoryName, String workspaceName) throws IOException, BackupExecuteException {
    String sURL = HTTPBackupAgent.Constants.BASE_URL + 
                  HTTPBackupAgent.Constants.OperationType.CURRENT_RESTORE_INFO_ON_WS +
                  "/" + repositoryName +
                  "/" + workspaceName;
    BackupAgentResponse response = transport.executeGET(sURL);
    
    if (response.getStatus() == Response.Status.OK.getStatusCode()) {
      DetailedInfo info;
      try {
        info = (DetailedInfo) getObject(DetailedInfo.class, response.getResponseData());
      } catch (Exception e) {
        throw new  RuntimeException("Can not get DetailedInfo from responce.", e);
      }
      
      
      String result = "\nThe current restores information : \n";
      
        result += "\tRestore with id " + info.getBackupId()  + ":\n";
        
        BackupConfigBean configBean = info.getBackupConfig();
        
        result  += ("\t\tbackup folder           : " + configBean.getBackupDir() + "\n" 
                  + "\t\trepository name         : " + info.getRepositoryName() + "\n"
                  + "\t\tworkspace name          : " + info.getWorkspaceName() + "\n"
                  + "\t\tbackup type             : " + (configBean.getBackupType() == BackupManager.FULL_AND_INCREMENTAL ? "full + incremetal" : "full only") + "\n"
                  + "\t\trestore state           : " +  getRestoreState(info.getState()) + "\n"
                  + "\t\tstarted time            : " + info.getStartedTime() + "\n"
                  + (info.getFinishedTime().equals("") ? "\n" : 
                    "\t\tfinished time           : " + info.getFinishedTime() + "\n\n"));
        
        return result;
    } else {
      return failureProcessing(response);
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
  private String failureProcessing(BackupAgentResponse response) throws BackupExecuteException {
    try {
      String result = "\nFailure :\n"
                      + "\tsatatus code : " + response.getStatus() + "\n"
                      + "\tmessage      : " + new String(response.getResponseData(), "UTF-8") + "\n\n";
      
      return result;
    } catch (UnsupportedEncodingException e) {
      throw new BackupExecuteException("Can not encoded the responce : " + e.getMessage(), e);
    }
  }
}
