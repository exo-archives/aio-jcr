/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.jcr.webdav;

import org.exoplatform.common.http.client.HTTPConnection;

/**
 * Created by The eXo Platform SAS
 * Author : Dmytro Katayev
 *          work.visor.ck@gmail.com
 * Aug 14, 2008  
 */
public class TestUtils {
  
  public static final String HOST = "localhost";
  public static final int PORT = 8080;
  
  public static final String SERVLET_PATH = "/rest/jcr/repository";
  public static final String WORKSPACE = "/production";
  public static final String INAVLID_WORKSPACE = "/invalid";  
  public static final String REALM = "eXo REST services";
  
  public static final String ROOTID = "root";
  public static final String ROOTPASS = "exo";
  
  public static HTTPConnection GetAuthConnection() {
    HTTPConnection connection = new HTTPConnection(HOST, PORT);
    connection.addBasicAuthorization(REALM, ROOTID, ROOTPASS);
    
    return connection;
  }
  
  public static String getFullWorkSpacePath() {
    return SERVLET_PATH + WORKSPACE;
  }
  
  public static String getFullUri(){
    return "http://" + HOST + ":" + PORT + getFullWorkSpacePath();
  }
 
  public static String getFolderName() {
    return "/test folder " + System.currentTimeMillis();
  }
  
  public static String getFileName() {
    return "test file " + System.currentTimeMillis() + ".txt";
  }
  

}
