/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

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
