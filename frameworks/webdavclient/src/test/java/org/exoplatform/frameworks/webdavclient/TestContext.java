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

package org.exoplatform.frameworks.webdavclient;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class TestContext {

  public static final String HOST = "localhost";
  
  public static final int PORT = 8080;
  
  public static final String SERVLET_PATH = "/rest/jcr/repository";
  //public static final String SERVLET_PATH = "/portal/rest/jcr/repository";
  
  public static final String USER_ID = "admin";
  public static final String USER_PASS = "admin";
//  public static final String USER_ID = "exoadmin";
//  public static final String USER_PASS = "exo";
  
  public static final WebDavContext getContext() {
    return new WebDavContext(HOST, PORT, SERVLET_PATH);
  }
  
  public static final WebDavContext getInvalidContext() {
    return new WebDavContext(HOST, PORT, SERVLET_PATH, "invalid_user", "invalid_pass");
  }
  
  public static final WebDavContext getContextAuthorized() {
    return new WebDavContext(HOST, PORT, SERVLET_PATH, USER_ID, USER_PASS);
  }
  
}
