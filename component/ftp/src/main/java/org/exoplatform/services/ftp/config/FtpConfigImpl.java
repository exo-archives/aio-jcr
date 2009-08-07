/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.services.ftp.config;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.ftp.FtpConst;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>.
 * 
 * @version $Id$
 */
public class FtpConfigImpl implements FtpConfig {

  private static Log         log                             = ExoLogger.getLogger(FtpConst.FTP_PREFIX
                                                                 + "FtpConfigImpl");

  /**
   * "command-port".
   */
  public static final String INIT_PARAM_COMMAND_PORT         = "command-port";

  /**
   * "data-min-port".
   */
  public static final String INIT_PARAM_DATA_MIN_PORT        = "data-min-port";

  /**
   * "data-max-port".
   */
  public static final String INIT_PARAM_DATA_MAX_PORT        = "data-max-port";

  /**
   * "system".
   */
  public static final String INIT_PARAM_SYSTEM               = "system";

  /**
   * "client-side-encoding".
   */
  public static final String INIT_PARAM_CLIENT_SIDE_ENCODING = "client-side-encoding";

  /**
   * "def-folder-node-type".
   */
  public static final String INIT_PARAM_DEF_FOLDER_NODE_TYPE = "def-folder-node-type";

  /**
   * "def-file-node-type".
   */
  public static final String INIT_PARAM_DEF_FILE_NODE_TYPE   = "def-file-node-type";

  /**
   * "def-file-mime-type".
   */
  public static final String INIT_PARAM_DEF_FILE_MIME_TYPE   = "def-file-mime-type";

  /**
   * "cache-folder-name".
   */
  public static final String INIT_PARAM_CACHE_FOLDER_NAME    = "cache-folder-name";

  /**
   * "upload-speed-limit".
   */
  public static final String INIT_PARAM_UPLOAD_SPEED_LIMIT   = "upload-speed-limit";

  /**
   * "download-speed-limit".
   */
  public static final String INIT_PARAM_DOWNLOAD_SPEED_LIMIT = "download-speed-limit";

  /**
   * "timeout".
   */
  public static final String INIT_PARAM_TIME_OUT             = "timeout";

  /**
   * Portal container name.
   */
  public static final String PORTAL_CONTAINER_NAME           = "portalContainerName";

  /**
   * Command port.
   */
  private int                _commandPort                    = 21;

  /**
   * Data min port.
   */
  private int                _dataMinPort                    = 7000;

  /**
   * Data max port.
   */
  private int                _dataMaxPort                    = 7100;

  /**
   * System type.
   */
  private String             _system                         = "Windows_NT";

  /**
   * Client side encoding.
   */
  private String             _clientSideEncoding             = "";

  /**
   * Default folder node type.
   */
  private String             _defFolderNodeType              = FtpConst.NodeTypes.NT_FOLDER;

  /**
   * Default file node type.
   */
  private String             _defFileNodeType                = FtpConst.NodeTypes.NT_FILE;

  /**
   * Default file mimetype.
   */
  private String             _defFileMimeType                = "application/zip";

  /**
   * Cache folder name.
   */
  private String             _cacheFolderName                = "";

  /**
   * Is need slow upload.
   */
  private boolean            _needSlowUpLoad                 = false;

  /**
   * Upload speed.
   */
  private int                _upLoadSpeed                    = 0;

  /**
   * Is need slow download.
   */
  private boolean            _needSlowDownLoad               = false;

  /**
   * Download speed.
   */
  private int                _downLoadSpeed                  = 0;

  /**
   * Is need timeout.
   */
  private boolean            _needTimeOut                    = false;

  /**
   * Timeout value.
   */
  private int                _timeOutValue                   = 0;

  /**
   * Is enable trace.
   */
  protected boolean          ENABLE_TRACE                    = true;

  /**
   * Container name.
   */
  protected String           portalContainerName             = null;

  /**
   * Constructor.
   * 
   * @param InitParams
   */
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

    ValueParam pPortalContainerName = params.getValueParam(PORTAL_CONTAINER_NAME);
    portalContainerName = pPortalContainerName != null ? pPortalContainerName.getValue() : "portal";

    if (log.isDebugEnabled()) {
      log.debug(INIT_PARAM_COMMAND_PORT + " = " + _commandPort);
      log.debug(INIT_PARAM_DATA_MIN_PORT + " = " + _dataMinPort);
      log.debug(INIT_PARAM_DATA_MAX_PORT + " = " + _dataMaxPort);
      log.debug(INIT_PARAM_SYSTEM + " = " + _system);
      log.debug(INIT_PARAM_CLIENT_SIDE_ENCODING + " = " + _clientSideEncoding);
      log.debug(INIT_PARAM_DEF_FOLDER_NODE_TYPE + " = " + _defFolderNodeType);
      log.debug(INIT_PARAM_DEF_FILE_NODE_TYPE + " = " + _defFileNodeType);
      log.debug(INIT_PARAM_DEF_FILE_MIME_TYPE + " = " + _defFileMimeType);
      log.debug(INIT_PARAM_CACHE_FOLDER_NAME + " = " + _cacheFolderName);

      log.debug(INIT_PARAM_UPLOAD_SPEED_LIMIT + " = " + _needSlowUpLoad);
      if (_needSlowUpLoad) {
        log.debug(INIT_PARAM_UPLOAD_SPEED_LIMIT + ".value = " + _upLoadSpeed);
      }

      log.debug(INIT_PARAM_DOWNLOAD_SPEED_LIMIT + " = " + _needSlowDownLoad);
      if (_needSlowDownLoad) {
        log.debug(INIT_PARAM_DOWNLOAD_SPEED_LIMIT + ".value = " + _downLoadSpeed);
      }

      log.info(INIT_PARAM_TIME_OUT + " = " + _needTimeOut);
      if (_needTimeOut) {
        log.debug(INIT_PARAM_TIME_OUT + ".value = " + _timeOutValue);
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

  public String getPortalContainerName() {
    return portalContainerName;
  }

}
