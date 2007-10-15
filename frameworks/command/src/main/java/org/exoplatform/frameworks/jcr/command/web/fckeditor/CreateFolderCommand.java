/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
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
 * Created by The eXo Platform SARL        .
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
