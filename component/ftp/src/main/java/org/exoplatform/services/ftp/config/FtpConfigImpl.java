/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.ftp.config;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.ftp.FtpConst;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class FtpConfigImpl implements FtpConfig {

  private static Log log = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "FtpConfigImpl");
  
  public static final String INIT_PARAM_COMMAND_PORT = "command-port";
  public static final String INIT_PARAM_DATA_MIN_PORT = "data-min-port";
  public static final String INIT_PARAM_DATA_MAX_PORT = "data-max-port";
  
  public static final String INIT_PARAM_SYSTEM = "system";
  public static final String INIT_PARAM_CLIENT_SIDE_ENCODING = "client-side-encoding";
  
  public static final String INIT_PARAM_DEF_FOLDER_NODE_TYPE = "def-folder-node-type";
  public static final String INIT_PARAM_DEF_FILE_NODE_TYPE = "def-file-node-type";
  public static final String INIT_PARAM_DEF_FILE_MIME_TYPE = "def-file-mime-type";
  
  public static final String INIT_PARAM_CACHE_FOLDER_NAME = "cache-folder-name";
  
  public static final String INIT_PARAM_UPLOAD_SPEED_LIMIT = "upload-speed-limit";
  public static final String INIT_PARAM_DOWNLOAD_SPEED_LIMIT = "download-speed-limit";  
  
  public static final String INIT_PARAM_TIME_OUT = "timeout";
  
  
  private int _commandPort = 21;
  private int _dataMinPort = 7000;
  private int _dataMaxPort = 7100;
  
  private String _system = "Windows_NT";
  private String _clientSideEncoding = "";
  
  private String _defFolderNodeType = FtpConst.NodeTypes.NT_FOLDER;
  private String _defFileNodeType = FtpConst.NodeTypes.NT_FILE;
  private String _defFileMimeType = "application/zip";
  
  private String _cacheFolderName = "";
  
  private boolean _needSlowUpLoad = false;
  private int _upLoadSpeed = 0;
  private boolean _needSlowDownLoad = false;
  private int _downLoadSpeed = 0;  

  private boolean _needTimeOut = false;
  private int _timeOutValue = 0;
  
  protected boolean ENABLE_TRACE = true;
  
  public FtpConfigImpl(InitParams params) {
    
    ValueParam pCommandPort = params.getValueParam(INIT_PARAM_COMMAND_PORT);
    if (pCommandPort != null) {
      _commandPort = new Integer(pCommandPort.getValue());
    }
    
    ValueParam pDataMinPort = params.getValueParam(INIT_PARAM_DATA_MIN_PORT);
    if (pDataMinPort != null) {
      _dataMinPort = new Integer(pDataMinPort.getValue());
    }
    
    ValueParam pDataMaxPort = params.getValueParam(INIT_PARAM_DATA_MAX_PORT);
    if (pDataMaxPort != null) {
      _dataMaxPort = new Integer(pDataMaxPort.getValue());
    }
    
    ValueParam pSystem = params.getValueParam(INIT_PARAM_SYSTEM);
    if (pSystem != null) {
      _system = pSystem.getValue();
    }
    
    ValueParam pClientSideEncoding = params.getValueParam(INIT_PARAM_CLIENT_SIDE_ENCODING);
    if (pClientSideEncoding != null) {
      _clientSideEncoding = pClientSideEncoding.getValue();
    }
    
    ValueParam pFolderNodeType = params.getValueParam(INIT_PARAM_DEF_FOLDER_NODE_TYPE);
    if (pFolderNodeType != null) {
      _defFolderNodeType = pFolderNodeType.getValue();
    }
    
    ValueParam pFileNodeType = params.getValueParam(INIT_PARAM_DEF_FILE_NODE_TYPE);
    if (pFileNodeType != null) {
      _defFileNodeType = pFileNodeType.getValue();
    }
    
    ValueParam pFileMimeType = params.getValueParam(INIT_PARAM_DEF_FILE_MIME_TYPE);
    if (pFileMimeType != null) {
      _defFileMimeType = pFileMimeType.getValue();
    }
    
    ValueParam pCacheFolderName = params.getValueParam(INIT_PARAM_CACHE_FOLDER_NAME);
    if (pCacheFolderName != null) {
      _cacheFolderName = pCacheFolderName.getValue();
    }
    
    ValueParam pSlowUpLoad = params.getValueParam(INIT_PARAM_UPLOAD_SPEED_LIMIT);
    if (pSlowUpLoad != null) {
      _needSlowUpLoad = true;
      _upLoadSpeed = new Integer(pSlowUpLoad.getValue());
    }
    
    ValueParam pSlowDownLoad = params.getValueParam(INIT_PARAM_DOWNLOAD_SPEED_LIMIT);
    if (pSlowDownLoad != null) {
      _needSlowDownLoad = true;
      _downLoadSpeed = new Integer(pSlowDownLoad.getValue());
    }
    
    ValueParam pTimeOut = params.getValueParam(INIT_PARAM_TIME_OUT);
    if (pTimeOut != null) {
      _needTimeOut = true;
      _timeOutValue = new Integer(pTimeOut.getValue());
    }
    
    if (ENABLE_TRACE) {
      log.info(INIT_PARAM_COMMAND_PORT + " = " + _commandPort);
      log.info(INIT_PARAM_DATA_MIN_PORT + " = " + _dataMinPort);
      log.info(INIT_PARAM_DATA_MAX_PORT + " = " + _dataMaxPort);
      log.info(INIT_PARAM_SYSTEM + " = " + _system);
      log.info(INIT_PARAM_CLIENT_SIDE_ENCODING + " = " + _clientSideEncoding);
      log.info(INIT_PARAM_DEF_FOLDER_NODE_TYPE + " = " + _defFolderNodeType);
      log.info(INIT_PARAM_DEF_FILE_NODE_TYPE + " = " + _defFileNodeType);
      log.info(INIT_PARAM_DEF_FILE_MIME_TYPE + " = " + _defFileMimeType);
      log.info(INIT_PARAM_CACHE_FOLDER_NAME + " = " + _cacheFolderName);
      
      log.info(INIT_PARAM_UPLOAD_SPEED_LIMIT + " = " + _needSlowUpLoad);
      if (_needSlowUpLoad) {
        log.info(INIT_PARAM_UPLOAD_SPEED_LIMIT + ".value = " + _upLoadSpeed);
      }
      
      log.info(INIT_PARAM_DOWNLOAD_SPEED_LIMIT + " = " + _needSlowDownLoad);
      if (_needSlowDownLoad) {
        log.info(INIT_PARAM_DOWNLOAD_SPEED_LIMIT + ".value = " + _downLoadSpeed);
      }
      
      log.info(INIT_PARAM_TIME_OUT + " = " + _needTimeOut);
      if (_needTimeOut) {
        log.info(INIT_PARAM_TIME_OUT + ".value = " + _timeOutValue);
      }
    }
    
  }
  
  public int getCommandPort() {
    return _commandPort;
  }
  
  public int getDataMinPort() {
    return _dataMinPort;
  }
  
  public int getDataMaxPort() {
    return _dataMaxPort;
  }
  
  public String getSystemType() {
    return _system;
  }
  
  public String getClientSideEncoding() {
    return _clientSideEncoding;
  }
  
  public String getDefFolderNodeType() {
    return _defFolderNodeType;
  }
  
  public String getDefFileNodeType() {
    return _defFileNodeType;
  }
  
  public String getDefFileMimeType() {
    return _defFileMimeType;
  }
  
  public String getCacheFolderName() {
    return _cacheFolderName;
  }
  
  public boolean isNeedSlowUpLoad() {
    return _needSlowUpLoad;
  }
  
  public int getUpLoadSpeed() {
    return _upLoadSpeed;
  }
  
  public boolean isNeedSlowDownLoad() {
    return _needSlowDownLoad;
  }
  
  public int getDownLoadSpeed() {
    return _downLoadSpeed;
  }
  
  public boolean isNeedTimeOut() {
    return _needTimeOut;
  }
  
  public int getTimeOut() {
    return _timeOutValue;
  }

  
}
