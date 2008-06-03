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

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.exoplatform.frameworks.jcr.command.JCRCommandHelper;
import org.exoplatform.frameworks.jcr.command.web.DisplayResourceCommand;
import org.exoplatform.frameworks.jcr.command.web.GenericWebAppContext;

/**
 * Created by The eXo Platform SAS        .<br/>
 * connector?Command=FileUpload&Type=ResourceType&CurrentFolder=FolderPath
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: UploadFileCommand.java 12537 2007-02-02 22:38:23Z brice $
 */

public class UploadFileCommand implements Command {
  
  public boolean execute(Context context) throws Exception {
    
    GenericWebAppContext webCtx = (GenericWebAppContext)context;
    //Session session = webCtx.getSession();
    HttpServletResponse response = webCtx.getResponse();
    HttpServletRequest request = webCtx.getRequest();
    PrintWriter out = response.getWriter();
    response.setContentType("text/html; charset=UTF-8");
    response.setHeader("Cache-Control","no-cache");
    String type = (String)context.get("Type");
    if(type == null)
      type = "";
    
//  To limit browsing set Servlet init param "digitalAssetsPath"
    // with desired JCR path
    String rootFolderStr = (String)context.get("org.exoplatform.frameworks.jcr.command.web.fckeditor.digitalAssetsPath");
    
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
    
    Node parentFolder = (Node) webCtx.getSession().getItem(currentFolderStr);
    
    DiskFileUpload upload = new DiskFileUpload();
    List items = upload.parseRequest(request);

    Map fields = new HashMap();

    Iterator iter = items.iterator();
    while (iter.hasNext()) {
      FileItem item = (FileItem) iter.next();
      if (item.isFormField())
        fields.put(item.getFieldName(), item.getString());
      else
        fields.put(item.getFieldName(), item);
    }
    FileItem uplFile = (FileItem) fields.get("NewFile");

    // On IE, the file name is specified as an absolute path.
    String fileName = new File(uplFile.getName()).getName();

    Node file = JCRCommandHelper.createResourceFile(parentFolder, fileName, uplFile.getInputStream(), uplFile.getContentType());

    parentFolder.save();
    
    // TODO
    int retVal = 0;

    out.println("<script type=\"text/javascript\">");
    out.println("window.parent.frames['frmUpload'].OnUploadCompleted("+retVal+",'"+file.getName()+"');");
    out.println("</script>");
    out.flush();
    out.close();

    return false;
  }

}
