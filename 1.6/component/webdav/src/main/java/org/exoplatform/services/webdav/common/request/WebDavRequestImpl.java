/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.request;

import java.io.InputStream;
import java.util.ArrayList;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavSessionProvider;
import org.exoplatform.services.webdav.common.property.factory.PropertyFactory;
import org.exoplatform.services.webdav.common.request.documents.PropFindDocument;
import org.exoplatform.services.webdav.common.request.documents.RequestDocument;
import org.exoplatform.services.webdav.common.util.DavPathUtil;
import org.exoplatform.services.webdav.common.util.DavUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: WebDavRequestImpl.java 12787 2007-02-13 12:13:17Z gavrikvetal $
 */

public class WebDavRequestImpl extends HttpServletRequestWrapper implements WebDavRequest {

  private static Log log = ExoLogger.getLogger("jcr.WebDavRequestImpl");

  protected String serverPrefix;
  protected String serverApp;
  
  protected String sourcePath;
  protected String sourceWorkspace;
  protected String sourceVersion;
  
  protected String destinationPath;
  protected String destinationWorkspace;
  protected String destinationVersion;
  
  protected long rangeStart = -1;
  protected long rangeEnd = -1;
  
  protected PropertyFactory propertyFactory;
  
  public WebDavRequestImpl(HttpServletRequest request, PropertyFactory propertyFactory) {
    super(request);
    
    this.propertyFactory = propertyFactory;
    
    serverPrefix = DavPathUtil.getServerPrefix(this);
    serverApp = DavPathUtil.getServletApp(this);
    
    sourceWorkspace = DavPathUtil.getSrcWorkspace(this);
    sourcePath = DavPathUtil.getSrcPath(this);
    
    if (request.getParameter(DavConst.DAV_VERSIONIDENTIFIER) != null) {
      sourceVersion = request.getParameter(DavConst.DAV_VERSIONIDENTIFIER);
    } else {
      if (sourcePath.indexOf("?" + DavConst.DAV_VERSIONIDENTIFIER) >= 0) {
        sourceVersion = sourcePath.substring(sourcePath.indexOf("?") + 1);
        sourceVersion = sourceVersion.substring(DavConst.DAV_VERSIONIDENTIFIER.length() + 1);
        sourcePath = sourcePath.substring(0, sourcePath.indexOf("?"));
      }      
    }
    
    if (getHeader(DavConst.Headers.DESTINATION) != null) {
      destinationWorkspace = DavPathUtil.getDestWorkspace(this);
      destinationPath = DavPathUtil.getDestPath(this);
    }
    
    String rangeHeader = getHeader(DavConst.Headers.RANGE);
//    log.info("RANGE HEADER: [" + rangeHeader + "]");
    
    if (rangeHeader != null) {
      if (rangeHeader.startsWith("bytes=")) {
        
        String rangeString = rangeHeader.substring(rangeHeader.indexOf("=") + 1);
//        log.info("RANGE STRING: [" + rangeString + "]");
        
//        if (rangeString.indexOf(",") > 0) {
//          rangeString = rangeString.substring(0, rangeString.indexOf(","));
//          log.info("RANGE STRING MODIFIED: [" + rangeString + "]");
//        }
        
        String []ranges = rangeString.split("-");

//        for (int i = 0; i < ranges.length; i++) {
//          log.info("RANGES: [" + ranges[i] + "]");
//        }
        
        if (ranges.length > 0) {
          rangeStart = new Long(ranges[0]);
          if (ranges.length > 1) {
            rangeEnd = new Long(ranges[1]);
          }
        }
        
      }
    }
    
  }
  
  public PropertyFactory getPropertyFactory() {
    return propertyFactory;
  }
  
  public String getNodeType() {
    String nodetypeHeader = getHeader(DavConst.Headers.NODETYPE);
    
    if (nodetypeHeader == null) {
      return null;
    }
    
    String nodeType = new String(Base64.decodeBase64(nodetypeHeader.getBytes()));    
    return nodeType;
  }
  
  public ArrayList<String> getMixTypes() {    
    String mixTypesHeader = getHeader(DavConst.Headers.MIXTYPE);
    
    if (mixTypesHeader == null) {
      mixTypesHeader = "";
    }
    
    String mixTypes =  new String(Base64.decodeBase64(mixTypesHeader.getBytes()));
    
    String []mixType = mixTypes.split(";");
    ArrayList<String> mixinTypes = new ArrayList<String>(); 
    for (int i = 0; i < mixType.length; i++) {
      String curMixType = mixType[i];
      if ("".equals(curMixType)) {
        continue;
      }
      mixinTypes.add(curMixType);
    }
    
    return mixinTypes;
  }
  
  public ArrayList<String> getLockTokens() {
    ArrayList<String> lockTokens = new ArrayList<String>();
    
    String lockToken = getHeader(DavConst.Headers.LOCKTOKEN);
    if (lockToken != null) {      
      lockToken = lockToken.substring(1, lockToken.length() - 1);
      lockTokens.add(lockToken);      
    }

    String ifHeaderValue = getHeader(DavConst.Headers.IF);
    if (ifHeaderValue != null) {
      String headerLockToken = ifHeaderValue.substring(ifHeaderValue.indexOf("("));
      headerLockToken = headerLockToken.substring(2, headerLockToken.length() - 2);
      lockTokens.add(headerLockToken);
    }
    
    return lockTokens;
  }

  protected void fillLockTokens(Session session) {
    if (session == null) {
      return;
    }
    ArrayList<String> lockTokens = getLockTokens(); 
    
    for (int i = 0; i < lockTokens.size(); i++) {
      String curLockToken = lockTokens.get(i);
      session.addLockToken(curLockToken);      
    }
  }  
  
  public Session getSourceSession(WebDavSessionProvider sessionProvider) throws RepositoryException {    
    String authHeader = getHeader(DavConst.Headers.AUTHORIZATION);
    Session sourceSession = sessionProvider.getSession(authHeader, getSrcWorkspace());
    fillLockTokens(sourceSession);    
    return sourceSession;
  }
  
  public Session getDestinationSession(WebDavSessionProvider sessionProvider) throws RepositoryException {
    String authHeader = getHeader(DavConst.Headers.AUTHORIZATION);
    Session destinationSession = sessionProvider.getSession(authHeader, getDestWorkspace());
    fillLockTokens(destinationSession);
    return destinationSession;
  }
  
  public Session getSession(WebDavSessionProvider sessionProvider, String workspaceName) throws RepositoryException {
    String authHeader = getHeader(DavConst.Headers.AUTHORIZATION);    
    Session session = sessionProvider.getSession(authHeader, workspaceName);
    fillLockTokens(session);
    return session;
  }
  
  public boolean isSameHosts() {
    return DavPathUtil.isSameDestHost(this, getServerPrefix());
  }
  
  public String getServerPrefix() {
    return serverPrefix;
  }
  
  public String getServerApp() {
    return serverApp;
  }
  
  public String getSrcWorkspace() {
    return sourceWorkspace;
  }
  
  public String getSrcPath() {
    return sourcePath;
  }
  
  public String getSrcVersion() {
    return sourceVersion;
  }
  
  public String getDestWorkspace() {
    return destinationWorkspace;
  }
  
  public String getDestPath() {
    return destinationPath;
  }
  
  public RequestDocument getDocumentFromRequest() {
    try {      
      Document doc = DavUtil.GetDocumentFromRequest(this);
      
      if (doc == null) {        

        if ("PROPFIND".equals(this.getMethod())) {
          
          PropFindDocument propsDoc = new PropFindDocument();
          propsDoc.initFactory(propertyFactory);
          
          return propsDoc;
        }
        return null;
      }
      
      Node documentNode = doc.getChildNodes().item(0);
      if (!DavConst.DAV_NAMESPACE.equals(documentNode.getNamespaceURI())) {
        log.info("Input document namespace not equals DAV:");
        return null;
      }
      
      String documentName = documentNode.getLocalName();
      RequestDocument requestDocument = null;

      for (int i = 0; i < DocumentFactory.AVAILABLE_DOCUMENTS.length; i++) {
        if (documentName.equals(DocumentFactory.AVAILABLE_DOCUMENTS[i][0])) {
          requestDocument = (RequestDocument)Class.forName(DocumentFactory.AVAILABLE_DOCUMENTS[i][1]).newInstance();
        }
      }    
      
      if (requestDocument == null) {
        return null;
      }
      
      if (!requestDocument.init(doc, propertyFactory)) {
        return null;
      }
      
      return requestDocument;
    } catch (Exception exc) {
      exc.printStackTrace();
    }
    
    return null;
  }
  
  public InputStream getRequestStream() throws Exception {
    return getInputStream();
  }
  
  public String getContentType() {
    return getHeader(DavConst.Headers.CONTENTTYPE);
  }
  
  public int getDepth() {    
    String depthValue = getHeader(DavConst.Headers.DEPTH);
    
    int depth;
    
    if (depthValue == null) {
      depth = 1;
    } else {
      if (depthValue.equalsIgnoreCase("Infinity")) {
        depth = Integer.MAX_VALUE;
      } else {
        depth = new Integer(depthValue);
      }
    }
    
    if (depth < 1) {
      depth = 1;
    }
    
    return depth;
  }
  
  public long getRangeStart() {
    return rangeStart;
  }
  
  public long getRangeEnd() {
    return rangeEnd;
  }
  
}
