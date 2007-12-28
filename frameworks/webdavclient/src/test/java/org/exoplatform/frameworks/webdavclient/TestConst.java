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

import org.exoplatform.frameworks.webdavclient.http.HttpClient;
import org.exoplatform.frameworks.webdavclient.http.HttpHeader;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class TestConst {

  public static final String HOST = "localhost";
  public static final int PORT = 8080;
  public static final String SERVLET_PATH = "/jcr-webdav/repository";  

  public static HttpClient getTestClient(String davCommand, String serverPath) throws Exception {
    
    HttpClient client = new HttpClient(HOST, PORT);
    client.setHttpCommand(davCommand);
    client.setRequestPath(serverPath);
    
    client.setRequestHeader(HttpHeader.CONNECTION, HttpHeader.TE);
    client.setRequestHeader(HttpHeader.TE, "trailers, deflate, gzip, compress");
    client.setRequestHeader(HttpHeader.DEPTH, "1");
    client.setRequestHeader(HttpHeader.TRANSLATE, "f");
    client.setRequestHeader(HttpHeader.ACCEPTENCODING, "deflate, gzip, x-gzip, compress, x-compress");
    client.setRequestHeader(HttpHeader.CONTENTTYPE, "text/xml");            
    
    return client;
  }
  
}
