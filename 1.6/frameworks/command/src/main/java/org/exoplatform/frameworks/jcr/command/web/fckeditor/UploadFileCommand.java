/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
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
 * Created by The eXo Platform SARL        .<br/>
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
