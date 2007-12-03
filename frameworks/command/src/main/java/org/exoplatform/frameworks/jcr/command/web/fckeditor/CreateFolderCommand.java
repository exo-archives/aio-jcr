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
 * @version $Id: CreateFolderCommand.java 5800 2006-05-28 18:03:31Z geaz $
 */

public class CreateFolderCommand extends FCKConnectorXMLOutput implements Command {

  public boolean execute(Context context) throws Exception {
    GenericWebAppContext webCtx = (GenericWebAppContext)context;
    HttpServletResponse response = webCtx.getResponse();
    HttpServletRequest request = webCtx.getRequest();
    
    String type = (String)context.get("Type");
    if(type == null)
      type = "";
    
    String currentFolderStr = (String)context.get("CurrentFolder");
    if(currentFolderStr == null)
      currentFolderStr = "";

    String folderName = (String)context.get("NewFolderName");
    if(folderName == null)
      throw new Exception("NewFolderName not defined");
    
    String jcrMapping = (String)context.get(GenericWebAppContext.JCR_CONTENT_MAPPING);
    if(jcrMapping == null)
      jcrMapping = DisplayResourceCommand.DEFAULT_MAPPING;
    
    String digitalWS = (String)webCtx.get(AppConstants.DIGITAL_ASSETS_PROP);
    if(digitalWS == null)
      digitalWS = AppConstants.DEFAULT_DIGITAL_ASSETS_WS;
    
    webCtx.setCurrentWorkspace(digitalWS);
    
    Node currentFolder = (Node) webCtx.getSession().getItem(currentFolderStr);
    Node newFolder = currentFolder.addNode(folderName, "nt:folder");
    currentFolder.save();
    
    String url = request.getContextPath()+jcrMapping+"?"+
    "workspace="+digitalWS+
    "&path="+currentFolderStr;

    initRootElement("CreateFolder", type, newFolder.getPath()+"/"
        , url);

    Document doc = rootElement.getOwnerDocument();
    
    // TODO
    String retVal = "0";

    Element errElement = doc.createElement("Error");
    errElement.setAttribute("number", retVal);
    rootElement.appendChild(errElement);

    outRootElement(response);

    return false;
  }

}
