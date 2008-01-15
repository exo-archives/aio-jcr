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
 * Created by The eXo Platform SAS        .
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

    // To limit browsing set Servlet init param "digitalAssetsPath"
    // with desired JCR path
    String rootFolderStr = (String)context.get("digitalAssetsPath");
    if(rootFolderStr == null)
      rootFolderStr = "/";

    // set current folder
    String currentFolderStr = (String)context.get("CurrentFolder");
    if(currentFolderStr == null)
      currentFolderStr = "";
    else if(currentFolderStr.length() < rootFolderStr.length())
      currentFolderStr = rootFolderStr;

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
