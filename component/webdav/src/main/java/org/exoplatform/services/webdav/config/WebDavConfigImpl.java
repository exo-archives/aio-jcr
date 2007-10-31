/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.config;

import java.util.Vector;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.DavConst;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class WebDavConfigImpl implements WebDavConfig {

  private static Log log = ExoLogger.getLogger("jcr.DavConfig");

  public static final String INIT_PARAM_DEFAULT_IDENTITY = "default-identity";
  public static final String INIT_PARAM_AUTH_HEADER = "auth-header";
  public static final String INIT_PARAM_DEF_FOLDER_NODE_TYPE = "def-folder-node-type";
  public static final String INIT_PARAM_DEF_FILE_NODE_TYPE = "def-file-node-type";
  public static final String INIT_PARAM_DEF_FILE_MIME_TYPE = "def-file-mimetype";
  public static final String INIT_PARAM_UPDATE_POLICY = "update-policy";

  public static final String INIT_VALUE_CREATE_VERSION = "create-version";
  public static final String INIT_VALUE_REPLACE = "replace";
  public static final String INIT_VALUE_ADD = "add";

  protected String _defIdentity = null;
  protected String _authHeader = "";
  protected String _defFolderNodeType = DavConst.NodeTypes.NT_FOLDER;
  protected String _defFileNodeType = DavConst.NodeTypes.NT_FILE;
  protected String _defFileMimeType = DavConst.DAV_DEFAULT_MIME_TYPE;
  protected String _updatePolicyType = INIT_VALUE_ADD;
  
  public static final String XML_WEBDAVCONFIG = "webdav-config";
  
  public static final String XML_DOCUMENTSET = "document-set";
  public static final String XML_PROPERTYSET = "property-set";
  public static final String XML_DOCUMENT = "document";
  
  public static final String XML_REQUESTDOUMENT = "request-document";
  public static final String XML_DOCUMENTNAME = "document-name";
  public static final String XML_NAMESPACE = "namespace";
  public static final String XML_CLASSNAME = "class-name";
  
  public WebDavConfigImpl(InitParams params, String configFilePath) throws Exception {
    ValueParam pIdentity = params.getValueParam(INIT_PARAM_DEFAULT_IDENTITY);
    if (pIdentity != null) {
      _defIdentity = pIdentity.getValue();
      log.info(INIT_PARAM_DEFAULT_IDENTITY + " = " + _defIdentity);
    }

    ValueParam pAuthHeader = params.getValueParam(INIT_PARAM_AUTH_HEADER);
    if (pAuthHeader != null) {
      _authHeader = pAuthHeader.getValue();
      log.info(INIT_PARAM_AUTH_HEADER + " = " + _authHeader);
    }

    ValueParam pDefFolderNodeType = params.getValueParam(INIT_PARAM_DEF_FOLDER_NODE_TYPE);
    if (pDefFolderNodeType != null) {
      _defFolderNodeType = pDefFolderNodeType.getValue();
      log.info(INIT_PARAM_DEF_FOLDER_NODE_TYPE + " = " + _defFolderNodeType);
    }

    ValueParam pDefFileNodeType = params.getValueParam(INIT_PARAM_DEF_FILE_NODE_TYPE);
    if (pDefFileNodeType != null) {
      _defFileNodeType = pDefFileNodeType.getValue();
      log.info(INIT_PARAM_DEF_FILE_NODE_TYPE + " = " + _defFileNodeType);
    }

    ValueParam pDefFileMimeType = params.getValueParam(INIT_PARAM_DEF_FILE_MIME_TYPE);
    if (pDefFileMimeType != null) {
      _defFileMimeType = pDefFileMimeType.getValue();
      log.info(INIT_PARAM_DEF_FILE_MIME_TYPE + " = " + _defFileMimeType);
    }

    ValueParam pUpdatePolicy = params.getValueParam(INIT_PARAM_UPDATE_POLICY);
    if (pUpdatePolicy != null) {
      _updatePolicyType = pUpdatePolicy.getValue();
      log.info(INIT_PARAM_UPDATE_POLICY + " = " + _updatePolicyType);
    }
    
  }
  
  public String getDefIdentity() {
    return _defIdentity;
  }

  public String getAuthHeader() {
    return _authHeader;
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

  public String getUpdatePolicyType() {
    return _updatePolicyType;
  }

  public Vector<String> defSearchNodeTypes() {
         Vector<String> nodeTypes = new Vector<String>();

          nodeTypes.add("nt:folder");
          nodeTypes.add("nt:file");

          return nodeTypes;
  }
  
}
