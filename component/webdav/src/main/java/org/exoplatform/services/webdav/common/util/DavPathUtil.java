/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.util;

import javax.servlet.http.HttpServletRequest;

import org.exoplatform.services.webdav.DavConst;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PutCommand.java 12004 2007-01-17 12:03:57Z geaz $
 */

public class DavPathUtil {
  
  public static String getServerPrefix(HttpServletRequest request) {
    return request.getScheme() + "://" + request.getServerName() + ":" +
      String.format("%s", request.getServerPort()) + 
      request.getContextPath() + request.getServletPath();
  }
  
  public static String getServletApp(HttpServletRequest request) {
    return request.getScheme() + "://" + request.getServerName() + ":" +
    String.format("%s", request.getServerPort()) + 
    request.getContextPath();
  }

  public static String getWorkspace(String path) {
    String workPath = path;
    if (workPath == null) {
      workPath = "/";
    }
    String []pathes = workPath.split("/");
    if (pathes.length < 2) {
      return "";
    }
    return DavTextUtil.UnEscape(pathes[1], '%') ;
  }
  
  public static String getPath(String path) {
    String workPath = path;
    if (workPath == null) {
      workPath = "/";
    }
    String workspacePrefix = "/" + getWorkspace(workPath);
    if (workPath.startsWith(workspacePrefix)) {
      workPath = workPath.substring(workspacePrefix.length());
    }
    if (!workPath.startsWith("/")) {
      workPath = "/" + workPath;
    }
    if (workPath.endsWith("/") && !workPath.equals("/")) {
      workPath = workPath.substring(0, workPath.length() - 1);
    }
    
//    System.out.println("SRCPATH ----------------------------- " + workPath);
//    
//    byte []aa = workPath.getBytes();
//    for (int i = 0; i < aa.length; i++) {
//      log.info("-- " + aa[i]);
//    }
    
    return DavTextUtil.UnEscape(workPath, '%');
  }
  
  public static String getSrcWorkspace(HttpServletRequest request) {
    return getWorkspace(request.getPathInfo());
  }
  
  public static String getSrcPath(HttpServletRequest request) {
    return getPath(request.getPathInfo());
  }

  public static boolean isSameDestHost(HttpServletRequest request, String serverPrefix) {
    String destinationHeader = request.getHeader(DavConst.Headers.DESTINATION);
    if (destinationHeader.startsWith(serverPrefix)) {
      return true;
    }
    return false;
  }
  
  public static String getLocalDestPath(HttpServletRequest request) {
    String serverPrefix = getServerPrefix(request);
    String destinationHeader = request.getHeader(DavConst.Headers.DESTINATION);
    if (!destinationHeader.startsWith(serverPrefix)) {
      return "/";
    }
    return destinationHeader.substring(serverPrefix.length());
  }
  
  public static String getDestWorkspace(HttpServletRequest request) {
    return getWorkspace(getLocalDestPath(request));
  }
  
  public static String getDestPath(HttpServletRequest request) {
    return getPath(getLocalDestPath(request));
  }
  
}
