/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.command.web.fckeditor;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.exoplatform.frameworks.jcr.command.web.DisplayResourceCommand;
import org.exoplatform.frameworks.jcr.command.web.GenericWebAppContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: GetFoldersOrFilesCommand.java 5800 2006-05-28 18:03:31Z geaz $
 */

public class GetFoldersOrFilesCommand extends FCKConnectorXMLOutput implements Command {

  public boolean execute(Context context) throws Exception {
    
    GenericWebAppContext webCtx = (GenericWebAppContext)context;
    HttpServletResponse response = webCtx.getResponse();
    HttpServletRequest request = webCtx.getRequest();
    
    String filter = (String)context.get("Command");
        
    String type = (String)context.get("Type");
    if(type == null)
      type = "";
    
    String currentFolderStr = (String)context.get("CurrentFolder");
    if(currentFolderStr == null)
      currentFolderStr = "";
    
    String jcrMapping = (String)context.get(GenericWebAppContext.JCR_CONTENT_MAPPING);
    if(jcrMapping == null)
      jcrMapping = DisplayResourceCommand.DEFAULT_MAPPING;

    String digitalWS = (String)webCtx.get(AppConstants.DIGITAL_ASSETS_PROP);
    if(digitalWS == null)
      digitalWS = AppConstants.DEFAULT_DIGITAL_ASSETS_WS;
    
    webCtx.setCurrentWorkspace(digitalWS);
    
    Node currentFolder = (Node) webCtx.getSession().getItem(currentFolderStr);

    //initRootElement(filter, type, currentPath, request.getContextPath()+currentPath);
    String url = request.getContextPath()+jcrMapping+"?"+
    "workspace="+digitalWS+
    "&path="+currentFolderStr;

    initRootElement(filter, type, currentFolderStr, url);
    
    Document doc = rootElement.getOwnerDocument();
    if(!filter.equals("GetFiles")) {
      Element nodesElement = rootElement.getOwnerDocument().createElement("Folders");
      rootElement.appendChild(nodesElement);
      NodeIterator nodeList = currentFolder.getNodes();
      while (nodeList.hasNext()) {
        Node n = nodeList.nextNode();
        // System.out.println(" >>> "+n.getPath());
        if (n.isNodeType("nt:folder") || n.isNodeType("nt:unstructured")) {
          Element folderElement = doc.createElement("Folder");
          folderElement.setAttribute("name", n.getName());
          nodesElement.appendChild(folderElement);
        }
      }
    }
    if(!filter.equals("GetFolders")) {
      Element nodesElement = rootElement.getOwnerDocument().createElement("Files");
      rootElement.appendChild(nodesElement);
      NodeIterator nodeList = currentFolder.getNodes();
      while (nodeList.hasNext()) {
        Node n = nodeList.nextNode();
        //System.out.println(" >>> " + n.getPath() + " "
        //    + n.isNodeType("nt:file") + " " + n.getPrimaryNodeType().getName());
        if (n.isNodeType("nt:file")) {
          Element fileElement = doc.createElement("File");
          long size = n.getNode("jcr:content").getProperty("jcr:data").getLength();
          fileElement.setAttribute("name", n.getName());
          fileElement.setAttribute("size", "" + size);
          nodesElement.appendChild(fileElement);
        }
      }
    }
    
    outRootElement(response);
    
    return false;
  }
  

}
