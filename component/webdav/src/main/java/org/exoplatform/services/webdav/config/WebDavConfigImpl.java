/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.config;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.property.factory.PropertyFactory;
import org.exoplatform.services.webdav.common.util.DavUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: WebDavConfigImpl.java 12134 2007-01-20 15:50:13Z gavrikvetal $
 */

public class WebDavConfigImpl implements WebDavConfig {

  private static Log log = ExoLogger.getLogger("jcr.DavConfig");

  public static final String INIT_PARAM_DEFAULT_IDENTITY = "default-identity";
  public static final String INIT_PARAM_AUTH_HEADER = "auth-header";
  public static final String INIT_PARAM_AUTO_MIX_LOCKABLE = "auto-mix-lockable";
  public static final String INIT_PARAM_DEF_FOLDER_NODE_TYPE = "def-folder-node-type";
  public static final String INIT_PARAM_DEF_FILE_NODE_TYPE = "def-file-node-type";
  public static final String INIT_PARAM_DEF_FILE_MIME_TYPE = "def-file-mimetype";
  public static final String INIT_PARAM_UPDATE_POLICY = "update-policy";

  public static final String INIT_VALUE_CREATE_VERSION = "create-version";
  public static final String INIT_VALUE_REPLACE = "replace";
  public static final String INIT_VALUE_ADD = "add";

  protected String _defIdentity = null;
  protected String _authHeader = "";
  protected boolean _autoMixLockable = false;
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
  
  public static final String MAPPING_PATH = "/conf/webdav-mapping-table.xml";
  public static final String PROPERTYCONFIG_PATH = "/conf/webdav-propertyconfig.xml";  
  
  private PropertyFactory propertyFactory;
  
  private ArrayList<HashMap<String, String>> documents = new ArrayList<HashMap<String, String>>();
  
  public WebDavConfigImpl(InitParams params, String configFilePath) throws Exception {
    InputStream mappingStream = getClass().getResourceAsStream(MAPPING_PATH);
    MappingLoader mappingLoader = new MappingLoader(mappingStream);
    
    InputStream configStream = getClass().getResourceAsStream(PROPERTYCONFIG_PATH);
    PropertyConfigLoader configLoader = new PropertyConfigLoader(configStream);
    
    propertyFactory = new PropertyFactory(mappingLoader.getMappingTable(), 
        configLoader.getConfigTable());
    

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

    ValueParam pAutoMixLocable = params.getValueParam(INIT_PARAM_AUTO_MIX_LOCKABLE);
    if (pAutoMixLocable != null) {
      _autoMixLockable = true;
      log.info(INIT_PARAM_AUTO_MIX_LOCKABLE + " = " + _autoMixLockable);
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
    
    try {
      InputStream configFileStream = getClass().getResourceAsStream(configFilePath);    
      Document configDocument = DavUtil.GetDocumentFromInputStream(configFileStream);
      
      Node configNode = configDocument.getChildNodes().item(0);
      parseWebDavConfig(configNode);            
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
    }
    
  }
  
  protected void parseWebDavConfig(Node configNode) {
    NodeList childs = configNode.getChildNodes();
    
    for (int i = 0; i < childs.getLength(); i++) {
      Node childNode = childs.item(i);
      
      if (childNode.getLocalName() == null) {
        continue;
      }
      
      if (XML_DOCUMENTSET.equals(childNode.getLocalName())) {
        parseDocumentSet(childNode);
        continue;
      }
      
      if (XML_PROPERTYSET.equals(childNode.getLocalName())) {
        parsePropertySet(childNode);
        continue;
      }      
    }
    
  }
  
  private void parseDocumentSet(Node documentSetNode) {
    NodeList documentNodes = documentSetNode.getChildNodes();
    for (int i = 0; i < documentNodes.getLength(); i++) {
      Node documentNode = documentNodes.item(i);
      
      if (XML_DOCUMENT.equals(documentNode.getLocalName())) {
        parseDocument(documentNode);
      }

    }
    
  }
  
  private void parsePropertySet(Node propertySetNode) {
  }
  
  protected void parseDocument(Node documentNode) {    
    String documentName = DavUtil.getChildNode(documentNode, XML_DOCUMENTNAME).getTextContent();
    String nameSpace = DavUtil.getChildNode(documentNode, XML_NAMESPACE).getTextContent();
    String className = DavUtil.getChildNode(documentNode, XML_CLASSNAME).getTextContent();
    
    HashMap<String, String> documentMap = new HashMap<String, String>();
    documentMap.put(XML_DOCUMENTNAME, documentName);
    documentMap.put(XML_NAMESPACE, nameSpace);
    documentMap.put(XML_CLASSNAME, className);
    
    documents.add(documentMap);
  }

  public String getDefIdentity() {
    return _defIdentity;
  }

  public String getAuthHeader() {
    return _authHeader;
  }

  public boolean isAutoMixLockable() {
    return _autoMixLockable;
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
  
  public ArrayList<HashMap<String, String>> getRequestDocuments() {
    return documents;
  }
  
  public PropertyFactory getPropertyFactory() {
    return propertyFactory;
  }  

}
