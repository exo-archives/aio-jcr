/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.response;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.WebDavCommandContext;
import org.exoplatform.services.webdav.common.resource.DavResource;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PutCommand.java 12004 2007-01-17 12:03:57Z geaz $
 */

public class HtmlBuilder {
  
  private static Log log = ExoLogger.getLogger("jcr.HtmlBuilder");

  public static final String HTML_PATH = "/exo/projects/v2.x/exo-jcr/services/webdav/src/java/resource/page.html";
  public static final String HTML_FOLDERS = "/exo/projects/v2.x/exo-jcr/services/webdav/src/java/resource/folders.html";
  public static final String HTML_FILES = "/exo/projects/v2.x/exo-jcr/services/webdav/src/java/resource/files.html";  
  
  public static final String MASK_NAME ="<!--$davName-->";
  public static final String MASK_INNER = "<!--$davInnerHtml-->";  
  public static final String MASK_IMAGE = "<!--$davImgage-->";
  public static final String MASK_HREF = "<!--$davHref-->";
  public static final String MASK_FILENAME = "<!--$davFileName-->";
  public static final String MASK_FILESIZE = "<!--$davFileSize-->"; 

  private WebDavCommandContext context;

  public HtmlBuilder(WebDavCommandContext context) {
    this.context = context;
  }  
  
  private String getHtmlResource(String resName) throws Exception {
    File f = new File(resName); 
    FileInputStream fins = new FileInputStream(f);
    ByteArrayOutputStream outS = new ByteArrayOutputStream();
    byte []buffer = new byte[4096];
    while (true) {
      int readed = fins.read(buffer);
      if (readed < 0) {
        break;
      }
      outS.write(buffer, 0, readed);
    }
    fins.close();
    
    String html = new String(outS.toByteArray());
    return html;
  }
  
  private String getListHtml(String path, ArrayList<DavResource> resources) throws Exception {    
    String htmlPreset = getHtmlResource(HTML_FILES);
    
    String html = "";
    String serverApp = context.getWebDavRequest().getServerApp();
    String serverPrefix = context.getWebDavRequest().getServerPrefix();
    
    for (int i = 0; i < resources.size(); i++) {      
      DavResource resource = resources.get(i);
      
      String rImage = "";
      if (resource.isCollection()) {
        rImage = serverApp + "/images/folder.gif";
      }
      
      String rHref = serverPrefix + path + resource.getName();
      log.info("HREF: [" + rHref + "]");
      
      String rName = resource.getName();

      html += htmlPreset.replace(MASK_IMAGE, rImage).
        replace(MASK_HREF, rHref).
        replace(MASK_FILENAME, rName);
      
    }
    
    return html; 
  }
  
  public InputStream getHtml(String path, ArrayList<DavResource> resources) {    
    log.info("LOCAL HREF: [" + path + "]");

    String serverApp = context.getWebDavRequest().getServerApp();
    String serverPrefix = context.getWebDavRequest().getServerPrefix();
    
    try {
      String html = getHtmlResource(HTML_PATH);
      
      String allDirs = MASK_INNER;
      
      String rootDir = getHtmlResource(HTML_FOLDERS).
          replace(MASK_IMAGE, serverApp + "/images/folder.gif").
          replace(MASK_NAME, "repository").
          replace(MASK_HREF, serverPrefix);
      allDirs = allDirs.replace(MASK_INNER, rootDir);
      
      String []pathes = path.split("/");
      for (int i = pathes.length - 1; i >= 0; i--) {
        log.info("filling for FOLDER >>>>>>>>>>>>>>>>>>");
        String curPath = pathes[i];
        
//        String folders = getHtmlResource(HTML_FOLDERS).
//          replace(MASK_IMAGE, serverApp + "/images/folder.gif").
//          replace(MASK_NAME, "root").
//          replace(MASK_HREF, serverPrefix).
//          replace(MASK_INNER, "&nbsp");
//        allDirs = allDirs.replace(MASK_INNER, folders);
      }
      
      html = html.replace(MASK_INNER, allDirs);
      
      //String files = getListHtml(path, resources);
//      
//      for (int i = 0; i < resources.size(); i++) {
//        log.info("RESOURCE NAME: [" + resources.get(i).getName() + "]");
//      }
//
//      String table = buildDirectoryTable(serverPrefix, "root", "&nbsp;");
//      html = html.replace(INSIDE_DIRESTORY_MASK, table);
      
//      String []pathes = path.split("/");
//      for (String p : pathes) {
//        String table = getFolderTable("/");
//      }
      
      return new ByteArrayInputStream(html.getBytes()); 
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
      return null;
    }
    
  }
  
}
