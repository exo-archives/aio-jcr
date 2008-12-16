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

package org.exoplatform.jcr.webdav.ejbconnector30;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.ws.rs.core.MultivaluedMap;

import org.exoplatform.services.rest.ext.transport.SerialInputData;
import org.exoplatform.services.rest.ext.transport.SerialRequest;
import org.exoplatform.services.rest.ext.transport.SerialResponse;
import org.exoplatform.services.rest.impl.InputHeadersMap;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class Main {

  // default rules for name easybeans container on Jonas
  private static final String JNDI_NAME = "org.exoplatform.jcr.webdav.ejbconnector30.WebDAVEJBConnector" +
      "_" + WebDAVEJBConnectorRemote.class.getName() + "@Remote";

//  /**
//   * mappedName="WebDAVEJBConnector"
//   */
//  private static final String JNDI_NAME = "WebDAVEJBConnector";

  private static final String BASE_URL = "/jcr/repository/production/";

  private static final String data = "Hello world";

  private WebDAVEJBConnectorRemote getBean() throws Exception {
    Properties env = new Properties();
 // env.put(Context.INITIAL_CONTEXT_FACTORY, "org.objectweb.carol.jndi.spi.MultiOrbInitialContextFactory");
     env.put(Context.INITIAL_CONTEXT_FACTORY, "org.ow2.easybeans.component.smartclient.spi.SmartContextFactory");
 // env.put("java.naming.provider.url", "rmi://localhost:1099");
     env.put("java.naming.provider.url", "rmi://localhost:2503");
 // env.put("java.naming.factory.url.pkgs", "org.objectweb.carol.jndi.spi");

    InitialContext ctx = new InitialContext(env);
     
    System.out.println("Looking for " + JNDI_NAME + "...");
    return (WebDAVEJBConnectorRemote) ctx.lookup(JNDI_NAME);
  }

  public static void main(String[] args) throws Exception {
    test();
  }
  
  private static void test() throws Exception {
    Main main = new Main();

    WebDAVEJBConnectorRemote bean = main.getBean();
    // create directory 1
    String testDir1 = BASE_URL + "test" + System.currentTimeMillis();
    System.out.println("MKCOL : create directory : " + testDir1);
    callService("MKCOL", new URI(testDir1), null, null, bean);

    // upload file test.txt
    System.out.println("PUT : upload file in created directory");
    callService("PUT", new URI(testDir1 + "/test.txt"), null, new SerialInputData(data.getBytes()), bean);

    // create directory 2
    String testDir2 = BASE_URL + "test" + System.currentTimeMillis();
    System.out.println("MKCOL : create directory : " + testDir2);
    callService("MKCOL", new URI(testDir2), null, null, bean);

    // copy file from directory 1 to directory 2
    System.out.println("COPY : copy file from " + testDir1 + " to " + testDir2);
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.add("Destination", testDir2 + "/test.txt");
    callService("COPY", new URI(testDir1 + "/test.txt"), new InputHeadersMap(headers), null, bean);

    // remove first file
    System.out.println("DELETE : delete first file");
    callService("DELETE", new URI(testDir1 + "/test.txt"), null, null, bean);
    
    // move file from directory 2 to directory 1
    System.out.println("MOVE : move file from " + testDir2 + " to " + testDir1);
    headers.clear();
    headers.add("Destination", testDir1 + "/test.txt");
    callService("MOVE", new URI(testDir2 + "/test.txt"), new InputHeadersMap(headers), null, bean);

    // remove directory 2
    System.out.println("DELETE : delete directory " + testDir2);
    callService("DELETE", new URI(testDir2), null, null, bean);

    // get file test.txt
    System.out.println("GET : get file " + testDir1 + "/test.txt");
    callService("GET", new URI(testDir1 + "/test.txt"), null, null, bean);

    // remove directory 1
    System.out.println("DELETE : delete directory " + testDir1);
    callService("DELETE", new URI(testDir1), null, null, bean);

  }

  private static void callService(String method,
                                  URI serviceURI,
                                  MultivaluedMap<String, String> headers,
                                  SerialInputData data,
                                  WebDAVEJBConnectorRemote bean) throws Exception {
    System.out.println("\t>>> method " + method);
    SerialRequest request = new SerialRequest(method, serviceURI, headers, data);

    SerialResponse response = bean.service(request);
    System.out.println("response status: " + response.getStatus());
    if (response.getData() != null)
      printStream(response.getData().getStream());
  }

  private static void printStream(InputStream in) throws IOException {
    int rd = -1;
    while ((rd = in.read()) != -1)
      System.out.print((char) rd);

    System.out.print('\n');
  }

}
