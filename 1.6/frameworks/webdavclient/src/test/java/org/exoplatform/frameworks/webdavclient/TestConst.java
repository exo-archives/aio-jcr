/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.webdavclient;

import org.exoplatform.frameworks.httpclient.HttpClient;
import org.exoplatform.frameworks.httpclient.HttpHeader;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
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
