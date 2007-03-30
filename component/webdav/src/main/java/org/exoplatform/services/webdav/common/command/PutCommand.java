/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.command;

import java.util.ArrayList;
import java.util.Calendar;

import javax.jcr.Node;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.config.WebDavConfigImpl;
import org.exoplatform.services.webdav.lock.FakeLockTable;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PutCommand.java 12304 2007-01-25 10:23:57Z gavrikvetal $
 */

public class PutCommand extends WebDavCommand {
  
  protected boolean process() throws Exception {
    String mimeType = davRequest().getContentType();

    if (mimeType == null) {
      String fileName = getFileName();

      MimeTypeResolver mimeTypeResolver = new MimeTypeResolver();
      mimeTypeResolver.setDefaultMimeType(davContext().getConfig().getDefFileMimeType());
      mimeType = mimeTypeResolver.getMimeType(fileName);        
    }

    if (davRequest().getRequestStream() == null) {
      davResponse().answerPreconditionFailed();
      return false;
    }
    
    String resourcePath = davRequest().getSrcWorkspace() + davRequest().getSrcPath();      
    FakeLockTable lockTable = davContext().getLockTable();      
    String presentLockToken = lockTable.getLockToken(resourcePath);

    if (presentLockToken != null) {        
      ArrayList<String> lockTokens = davRequest().getLockTokens();
      boolean tokenFinded = false; 
      for (int i = 0; i < lockTokens.size(); i++) {
        if (presentLockToken.equals(lockTokens.get(i))) {
          tokenFinded = true;
          break;
        }
      }
      
      if (!tokenFinded) {
        davResponse().answerForbidden();
        return false;          
      }        
    }

    if (jcrSrcSession().itemExists(davRequest().getSrcPath())) {          
      String updatePolicyType = davContext().getConfig().getUpdatePolicyType();
      
      if (updatePolicyType.equals(WebDavConfigImpl.INIT_VALUE_ADD)) {
        createNtFile(mimeType);
      } else if (updatePolicyType.equals(WebDavConfigImpl.INIT_VALUE_CREATE_VERSION)) {
        createNtFileVersion(mimeType);
      } else {
        updateNtFile(mimeType);
      }
      
    } else {
      createNtFile(mimeType);
    }

    davResponse().answerCreated();
    
    return true;
  }
  
  protected String getFilePath() {
    String fullPath = davRequest().getSrcPath();
    String filePath = fullPath.substring(0, fullPath.lastIndexOf("/"));

    if ("".equals(filePath)) {
      filePath = "/";
    }
    return filePath;
  }
  
  protected String getFileName() {
    String fullPath = davRequest().getSrcPath();

    String fileName = fullPath.substring(fullPath.lastIndexOf("/"));
    if (fileName.startsWith("/")) {
      fileName = fileName.substring(1);
    }
    return fileName;
  }

  protected void createNtFile(String mimeType) throws Exception {
    Node srcNode = (Node)jcrSrcSession().getItem(getFilePath());

    String nodeType = davRequest().getNodeType();
    
    if (nodeType == null) {
      nodeType = davContext().getConfig().getDefFileNodeType();
    }
    
    Node ntFileNode = srcNode.addNode(getFileName(), nodeType);
    Node contentNode = ntFileNode.addNode(DavConst.NodeTypes.JCR_CONTENT, DavConst.NodeTypes.NT_RESOURCE);
    
    fillFileContent(contentNode, mimeType);
    
    jcrSrcSession().save();
  }
  
  protected void updateNtFile(String mimeType) throws Exception {    
    Node fileNode = (Node)jcrSrcSession().getItem(davRequest().getSrcPath());
    Node contentNode = fileNode.getNode(DavConst.NodeTypes.JCR_CONTENT);
    
    fillFileContent(contentNode, mimeType);
    
    jcrSrcSession().save();
  }
  
  private void fillFileContent(Node node, String mimeType) throws Exception {
    node.setProperty(DavConst.NodeTypes.JCR_MIMETYPE, mimeType);
    node.setProperty(DavConst.NodeTypes.JCR_LASTMODIFIED, Calendar.getInstance());
    node.setProperty(DavConst.NodeTypes.JCR_DATA, davRequest().getRequestStream());    
  }
  
  protected void createNtFileVersion(String mimeType) throws Exception {
    Node fileNode = (Node)jcrSrcSession().getItem(davRequest().getSrcPath());
    
    if (!fileNode.isNodeType(DavConst.NodeTypes.MIX_VERSIONABLE)) {
      
      if (davContext().getConfig().isAutoMixLockable()) {
        if (!fileNode.isNodeType(DavConst.NodeTypes.MIX_LOCKABLE)) {
          fileNode.addMixin(DavConst.NodeTypes.MIX_LOCKABLE);
        }        
      }
      
      fileNode.addMixin(DavConst.NodeTypes.MIX_VERSIONABLE);
      
      jcrSrcSession().save();
      
      fileNode.checkout();
      fileNode.checkin();
    }
    
    fileNode.checkout();
    
    updateNtFile(mimeType);
    
    fileNode.checkin();
    
  }
  
}
