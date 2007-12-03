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

package org.exoplatform.services.webdav.common.util;

import javax.servlet.http.HttpServletRequest;

import org.exoplatform.services.webdav.DavConst;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
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
