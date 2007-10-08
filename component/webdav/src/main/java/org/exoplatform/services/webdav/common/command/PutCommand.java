/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.command;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.lock.LockException;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.HeaderParam;
import org.exoplatform.services.rest.InputTransformer;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.transformer.PassthroughInputTransformer;
import org.exoplatform.services.rest.transformer.PassthroughOutputTransformer;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavMethod;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.WebDavHeaders;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.FakeResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.resource.WebDavResourceLocator;
import org.exoplatform.services.webdav.common.resource.WebDavResourceLocatorImpl;
import org.exoplatform.services.webdav.config.WebDavConfigImpl;
import org.exoplatform.services.webdav.lock.FakeLockTable;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

@URITemplate("/jcr/")
public class PutCommand extends NodeTypedCommand {
  
  private String getFilePath(String repoPath) {
    String curPath = repoPath;
    curPath = curPath.substring(curPath.indexOf("/"));
    curPath = curPath.substring(0, curPath.lastIndexOf("/"));
    if ("".equals(curPath)) {
      curPath = "/";
    }
    return curPath;
  }
  
  private String getFileName(String repoPath) {
    String []curNames = repoPath.split("/"); 
    return curNames[curNames.length - 1];
  }  
  
  public PutCommand(WebDavService webDavService, 
      ResourceDispatcher resourceDispatcher,
      ThreadLocalSessionProviderService sessionProviderService) {
    super(webDavService, resourceDispatcher, sessionProviderService);
  }  
  
  @HTTPMethod(WebDavMethod.PUT)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(PassthroughInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response put(
      @URIParam("repoName") String repoName,
      @URIParam("repoPath") String repoPath,
      InputStream inputStream,
      
      @HeaderParam(WebDavHeaders.AUTHORIZATION) String authorization,

      @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
      @HeaderParam(WebDavHeaders.IF) String ifHeader,
      
      @HeaderParam(WebDavHeaders.NODETYPE) String nodeTypeHeader,
      @HeaderParam(WebDavHeaders.MIXTYPE) String mixinTypesHeader,
      @HeaderParam(WebDavHeaders.CONTENTTYPE) String mimeType      
      ) {
    
    try {      
      String serverPrefix = getServerPrefix(repoName);
      
      ArrayList<String> lockTokens = getLockTokens(lockTokenHeader, ifHeader);
      
      SessionProvider sessionProvider = getSessionProvider(authorization);
      
      String fileName = getFileName(repoPath);
      
      String fileDirectory = getFilePath(repoPath);      

      String nodeType = getNodeType(nodeTypeHeader);
      if (nodeType == null) {
        nodeType = webDavService.getConfig().getDefFileNodeType();
      }      
      
      WebDavResourceLocator resourceLocator = new WebDavResourceLocatorImpl(webDavService, repoName, sessionProvider, lockTokens, serverPrefix, repoPath);      
      
      ArrayList<String> mixinTypes = getMixinTypes(mixinTypesHeader);
      
      if (mimeType == null) {      
        MimeTypeResolver mimeTypeResolver = new MimeTypeResolver();
        mimeTypeResolver.setDefaultMimeType(webDavService.getConfig().getDefFileMimeType());
        mimeType = mimeTypeResolver.getMimeType(fileName);
      }
      
      WebDavResource resource = resourceLocator.getSrcResource(true);

      if (resource instanceof FakeResource) {
        checkLocked(repoName + "/" + repoPath, lockTokens);
        Session session = ((FakeResource)resource).getSession(); 
        createNtFile(session, fileDirectory, fileName, inputStream, mimeType, nodeType);
        return Response.Builder.withStatus(WebDavStatus.CREATED).build();
      }
    
      if (!(resource instanceof AbstractNodeResource)) {
        throw new AccessDeniedException("Can't create resource!!!!!!!");      
      }
      
      String updatePolicyType = webDavService.getConfig().getUpdatePolicyType();
      
      if (updatePolicyType.equals(WebDavConfigImpl.INIT_VALUE_ADD)) {
        Session session = ((AbstractNodeResource)resource).getSession(); 
        createNtFile(session, fileDirectory, fileName, inputStream, mimeType, nodeType);
      } else if (updatePolicyType.equals(WebDavConfigImpl.INIT_VALUE_CREATE_VERSION)) {
        createNtFileVersion(((AbstractNodeResource)resource).getNode(), mimeType, inputStream);
      } else {      
        updateNtFile(((AbstractNodeResource)resource).getNode(), mimeType, inputStream);
      }

      return Response.Builder.withStatus(WebDavStatus.CREATED).build();      
    } catch (Exception exc) {
      return responseByException(exc);
    }
  }
  

  private void checkLocked(String sourceHref, ArrayList<String> sessionLockTokens) throws LockException {
    FakeLockTable lockTable = webDavService.getLockTable();      
    String presentLockToken = lockTable.getLockToken(sourceHref);
    
    if (presentLockToken != null) {
      boolean tokenFinded = false; 
      for (int i = 0; i < sessionLockTokens.size(); i++) {
        if (presentLockToken.equals(sessionLockTokens.get(i))) {
          tokenFinded = true;
          break;
        }
      }
      
      if (!tokenFinded) {
        throw new LockException("Resource locked!!!");
      }        
    }    
  }

  protected void createNtFile(Session session, String folderPath, String fileName, InputStream inputStream, String mimeType, String nodeType) throws Exception {
    String folderNodeType = webDavService.getConfig().getDefFolderNodeType();
    String []pathes = folderPath.split("/");
    
    Node node = session.getRootNode();
    for (int i = 0; i < pathes.length; i++) {
      if ("".equals(pathes[i])) {
        continue;
      }
      
      if (node.hasNode(pathes[i])) {
        node = node.getNode(pathes[i]);
      } else {
        node = node.addNode(pathes[i], folderNodeType);
      }
    }    
    
    Node ntFileNode = node.addNode(fileName, nodeType);
    Node contentNode = ntFileNode.addNode(DavConst.NodeTypes.JCR_CONTENT, DavConst.NodeTypes.NT_RESOURCE);
    
    fillFileContent(contentNode, mimeType, inputStream);
    
    session.save();
  }
  
  private void fillFileContent(Node node, String mimeType, InputStream inputStream) throws Exception {
    node.setProperty(DavConst.NodeTypes.JCR_MIMETYPE, mimeType);
    node.setProperty(DavConst.NodeTypes.JCR_LASTMODIFIED, Calendar.getInstance());
    node.setProperty(DavConst.NodeTypes.JCR_DATA, inputStream);    
  }
  
  protected void createNtFileVersion(Node fileNode, String mimeType, InputStream inputStream) throws Exception {
    if (!fileNode.isNodeType(DavConst.NodeTypes.MIX_VERSIONABLE)) {
      
      if (webDavService.getConfig().isAutoMixLockable()) {
        if (!fileNode.isNodeType(DavConst.NodeTypes.MIX_LOCKABLE)) {
          fileNode.addMixin(DavConst.NodeTypes.MIX_LOCKABLE);
        }        
      }
      
      fileNode.addMixin(DavConst.NodeTypes.MIX_VERSIONABLE);
      
      fileNode.getSession().save();
      
      fileNode.checkout();
      fileNode.checkin();
    }
    
    fileNode.checkout();    
    updateNtFile(fileNode, mimeType, inputStream);    
    fileNode.checkin();    
  }
  
  protected void updateNtFile(Node fileNode, String mimeType, InputStream inputStream) throws Exception {    
    Node contentNode = fileNode.getNode(DavConst.NodeTypes.JCR_CONTENT);    
    fillFileContent(contentNode, mimeType, inputStream);    
    fileNode.getSession().save();
  }  

}
