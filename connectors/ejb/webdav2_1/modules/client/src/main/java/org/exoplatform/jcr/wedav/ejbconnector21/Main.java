/**
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

package org.exoplatform.jcr.wedav.ejbconnector21;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.exoplatform.common.transport.SerialInputData;
import org.exoplatform.common.transport.SerialRequest;
import org.exoplatform.common.transport.SerialResponse;
import org.exoplatform.jcr.webdav.ejbconnector21.WebDAVEJBConnector;
import org.exoplatform.jcr.webdav.ejbconnector21.WebDAVEJBConnectorHome;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class Main {

  private static final String BEAN_NAME = "WebDAVEJBConnector";

  private static final String BASE_URL = "/jcr/repository/production/";

  private static final String data = "Hello world";

  private WebDAVEJBConnector getBean() throws Exception {
    InitialContext ctx = new InitialContext();
    System.out.println("Looking for " + BEAN_NAME + "...");
    Object obj = ctx.lookup(BEAN_NAME);
    WebDAVEJBConnectorHome webdavEJBConnectorHome = (WebDAVEJBConnectorHome) PortableRemoteObject
        .narrow(obj, WebDAVEJBConnectorHome.class);

    return webdavEJBConnectorHome.create();
  }

  public static void main(String[] args) throws Exception {
    Main main = new Main();

    WebDAVEJBConnector bean = main.getBean();
    SerialResponse response = null;
    
    // create directory 1
    String testDir1 = BASE_URL + "test " + System.currentTimeMillis();
    System.out.println("MKCOL : create directory : " + testDir1);
    response = bean.service(createRequest("MKCOL", testDir1, null, null, null));
    System.out.println(response.getStatus());
    if (response.getData() != null)
      printStream(response.getData().getStream());

    // upload file test.txt
    System.out.println("PUT : upload file in created directory");
    response = bean.service(createRequest("PUT", testDir1 + "/test.txt", null, null,
        new SerialInputData(data.getBytes())));
    System.out.println(response.getStatus());
    if (response.getData() != null)
      printStream(response.getData().getStream());

    // create directory 2
    String testDir2 = BASE_URL + "test " + System.currentTimeMillis();
    System.out.println("MKCOL : create directory : " + testDir2);
    response = bean.service(createRequest("MKCOL", testDir2, null, null, null));
    System.out.println(response.getStatus());
    if (response.getData() != null)
      printStream(response.getData().getStream());

    // copy file from directory 1 to directory 2
    System.out.println("COPY : copy file from " + testDir1 + " to " + testDir2);
    HashMap<String, String> headers = new HashMap<String, String>();
    headers.put("Destination", testDir2 + "/test.txt");
    response = bean.service(createRequest("COPY", testDir1 + "/test.txt", headers, null, null));
    System.out.println(response.getStatus());
    if (response.getData() != null)
      printStream(response.getData().getStream());

    // remove first file
    System.out.println("DELETE : delete first file");
    response = bean.service(createRequest("DELETE", testDir1 + "/test.txt", null, null, null));
    System.out.println(response.getStatus());
    if (response.getData() != null)
      printStream(response.getData().getStream());
    
    // move file from directory 2 to directory 1
    System.out.println("MOVE : move file from " + testDir2 + " to " + testDir1);
    headers = new HashMap<String, String>();
    headers.put("Destination", testDir1 + "/test.txt");
    response = bean.service(createRequest("MOVE", testDir2 + "/test.txt", headers, null, null));
    System.out.println(response.getStatus());
    if (response.getData() != null)
      printStream(response.getData().getStream());

    // remove directory 2
    System.out.println("DELETE : delete directory " + testDir2);
    response = bean.service(createRequest("DELETE", testDir2, null, null, null));
    System.out.println(response.getStatus());
    if (response.getData() != null)
      printStream(response.getData().getStream());

    // get file test.txt
    System.out.println("GET : get file " + testDir1 + "/test.txt");
    response = bean.service(createRequest("GET", testDir1 + "/test.txt", null, null, null));
    System.out.println(response.getStatus());
    if (response.getData() != null)
      printStream(response.getData().getStream());

    // remove directory 1
    System.out.println("DELETE : delete directory " + testDir1);
    response = bean.service(createRequest("DELETE", testDir1, null, null, null));
    System.out.println(response.getStatus());
    if (response.getData() != null)
      printStream(response.getData().getStream());

  }

  private static SerialRequest createRequest(String method, String url,
      HashMap<String, String> headers, HashMap<String, String> queries, SerialInputData data) {
    SerialRequest request = new SerialRequest();
    request.setMethod(method);
    request.setUrl(url);
    request.setHeaders(headers);
    request.setQueries(queries);
    request.setData(data);
    return request;
  }

  private static void printStream(InputStream in) throws IOException {
    int rd = -1;
    while ((rd = in.read()) != -1)
      System.out.print((char) rd);

    System.out.print('\n');
  }

}
