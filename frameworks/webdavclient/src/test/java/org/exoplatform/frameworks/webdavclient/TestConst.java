/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.webdavclient;

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
    
    client.setRequestHeader(Const.HttpHeaders.CONNECTION, Const.HttpHeaders.TE);
    client.setRequestHeader(Const.HttpHeaders.TE, "trailers, deflate, gzip, compress");
    client.setRequestHeader(Const.HttpHeaders.DEPTH, "1");
    client.setRequestHeader(Const.HttpHeaders.TRANSLATE, "f");
    client.setRequestHeader(Const.HttpHeaders.ACCEPTENCODING, "deflate, gzip, x-gzip, compress, x-compress");
    client.setRequestHeader(Const.HttpHeaders.CONTENTTYPE, "text/xml");            
    
    return client;
  }
  
}
