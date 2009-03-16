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

package org.exoplatform.connectors.jcr.ejb30;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Hashtable;

import javax.naming.InitialContext;
import javax.ws.rs.core.MultivaluedMap;

import org.exoplatform.services.rest.ext.transport.SerialInputData;
import org.exoplatform.services.rest.ext.transport.SerialRequest;
import org.exoplatform.services.rest.ext.transport.SerialResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class Client {

  // default rules for name easybeans container on Jonas
  /**
   * Bean name in JNDI.
   */
  private static final String BEAN_NAME = "org.exoplatform.connectors.jcr.ejb30.JcrRestEJBConnector"
      + "_" + JcrRestEJBConnectorRemote.class.getName() + "@Remote";

  /**
   * Default JCR path.
   */
  private static final String DEFAULT_JCR_PATH = "/jcr/repository/production/";

  /**
   * Default application server address.
   */
  private static final String DEFAULT_AS_URL   = "smart://127.0.0.1:2503";

  /**
   * Test string.
   */
  private static final String DATA             = "Hello world";

  /**
   * Actual application server address.
   */
  private String              serverUrl;

  /**
   * Actual JCR path.
   */
  private String              jcrUrl;

  /**
   * Get application server address.
   * 
   * @return server address, it is not preset yet then default address
   */
  public String getServerUrl() {
    if (serverUrl == null || serverUrl.length() == 0)
      serverUrl = DEFAULT_AS_URL;
    return serverUrl;
  }

  /**
   * Set application server address.
   * 
   * @param url application server address
   */
  public void setServerUrl(String url) {
    this.serverUrl = url;
  }

  /**
   * Get JCR path.
   * 
   * @return JCR path, it is not preset yet then default path
   */
  public String getJcrUrl() {
    if (jcrUrl == null || jcrUrl.length() == 0)
      jcrUrl = DEFAULT_JCR_PATH;
    return jcrUrl;
  }

  /**
   * Set JCR path.
   * 
   * @param url JCR path
   */
  public void setJcrUrl(String url) {
    this.jcrUrl = url.endsWith("/") ? url : url + "/";
  }

  /**
   * Lookup bean.
   * 
   * @return enterprise bean instance
   * @throws Exception if any error occurs
   */
  private JcrRestEJBConnectorRemote getBean() throws Exception {
    Hashtable<String, String> props = new Hashtable<String, String>();
    props.put(javax.naming.Context.PROVIDER_URL, getServerUrl());
    props.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY,
              "org.ow2.easybeans.component.smartclient.spi.SmartContextFactory");
    InitialContext ctx = new InitialContext(props);
    return (JcrRestEJBConnectorRemote) ctx.lookup(BEAN_NAME);
  }

  /**
   * Run test.
   * 
   * @return result of test as string
   * @throws Exception if any error occurs
   */
  public String run() throws Exception {

    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    PrintWriter out = new PrintWriter(buf);

    out.println("Looking for " + BEAN_NAME + "...");
    JcrRestEJBConnectorRemote bean = getBean();
    SerialResponse response = null;

    // create directory 1
    String testDir1 = getJcrUrl() + "test" + System.currentTimeMillis();
    out.println("MKCOL : create directory : " + testDir1);
    response = bean.service(new SerialRequest("MKCOL", new URI(testDir1), null, null));
    out.println(response.getStatus());
    if (response.getData() != null)
      printStream(response.getData().getStream(), out);

    // upload file test.txt
    out.println("PUT : upload file in created directory");
    response = bean.service(new SerialRequest("PUT",
                                              new URI(testDir1 + "/test.txt"),
                                              null,
                                              new SerialInputData(DATA.getBytes())));
    out.println(response.getStatus());
    if (response.getData() != null)
      printStream(response.getData().getStream(), out);

    // create directory 2
    String testDir2 = getJcrUrl() + "test" + System.currentTimeMillis();
    out.println("MKCOL : create directory : " + testDir2);
    response = bean.service(new SerialRequest("MKCOL", new URI(testDir2), null, null));
    out.println(response.getStatus());
    if (response.getData() != null)
      printStream(response.getData().getStream(), out);

    // copy file from directory 1 to directory 2
    out.println("COPY : copy file from " + testDir1 + " to " + testDir2);
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.add("Destination", testDir2 + "/test.txt");
    response = bean.service(new SerialRequest("COPY",
                                              new URI(testDir1 + "/test.txt"),
                                              headers,
                                              null));
    out.println(response.getStatus());
    if (response.getData() != null)
      printStream(response.getData().getStream(), out);

    // remove first file
    out.println("DELETE : delete first file");
    response = bean.service(new SerialRequest("DELETE", new URI(testDir1 + "/test.txt"), null, null));
    out.println(response.getStatus());
    if (response.getData() != null)
      printStream(response.getData().getStream(), out);

    // move file from directory 2 to directory 1
    out.println("MOVE : move file from " + testDir2 + " to " + testDir1);
    headers.clear();
    headers.add("Destination", testDir1 + "/test.txt");
    headers.add("Overwrite", "F");
    response = bean.service(new SerialRequest("MOVE",
                                              new URI(testDir2 + "/test.txt"),
                                              headers,
                                              null));
    out.println(response.getStatus());
    if (response.getData() != null)
      printStream(response.getData().getStream(), out);

    // remove directory 2
    out.println("DELETE : delete directory " + testDir2);
    response = bean.service(new SerialRequest("DELETE", new URI(testDir2), null, null));
    out.println(response.getStatus());
    if (response.getData() != null)
      printStream(response.getData().getStream(), out);

    // get file test.txt
    out.println("GET : get file " + testDir1 + "/test.txt");
    response = bean.service(new SerialRequest("GET", new URI(testDir1 + "/test.txt"), null, null));
    out.println(response.getStatus());
    if (response.getData() != null)
      printStream(response.getData().getStream(), out);

    // remove directory 1
    out.println("DELETE : delete directory " + testDir1);
    response = bean.service(new SerialRequest("DELETE", new URI(testDir1), headers, null));
    out.println(response.getStatus());
    if (response.getData() != null)
      printStream(response.getData().getStream(), out);
    out.flush();
    out.close();
    return buf.toString("UTF-8");
  }

  /**
   * @param in input stream
   * @param out output writer
   * @throws IOException it any i/o errors occurs
   */
  private static void printStream(InputStream in, PrintWriter out) throws IOException {
    int rd = -1;
    while ((rd = in.read()) != -1)
      out.print((char) rd);

    out.print('\n');
  }

}
