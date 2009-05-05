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
package org.exoplatform.services.jcr.webdav.ext;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.common.http.client.HTTPResponse;
import org.exoplatform.common.http.client.NVPair;
import org.exoplatform.services.jcr.webdav.BaseStandaloneTest;
import org.exoplatform.services.jcr.webdav.utils.TestUtils;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;

/**
 * Created by The eXo Platform SAS. <br/>
 * Date: 17 Feb 2009
 * 
 * @author <a href="mailto:dmitry.kataev@exoplatform.com.ua">Dmytro Katayev</a>
 * @version $Id: TestEncoding.java
 */
public class TestEncoding extends BaseStandaloneTest {

  public static final String CONTENT          = "\u043f\u0440\u0438\u043a\u043b\u0430\u0434";

  // UTF-8 content
  public static final String UTF_FILE         = "/utfFile.txt";

  public static final String UTF_CONTENT_TYPE = "text/plain;charset=UTF-8";

  public static final String UTF_CHARSET      = "UTF8";

  public static byte[]       UTF_CONTENT;

  // windows-1251 content
  public static final String WIN_FILE         = "/winFile.txt";

  public static final String WIN_CONTENT_TYPE = "text/plain;charset=windows-1251";

  public static final String WIN_CHARSET      = "Cp1251";

  public static byte[]       WIN_CONTENT;

  // ISO 8859-5 content
  public static final String ISO_FILE         = "/isoFile.txt";

  public static final String ISO_CONTENT_TYPE = "text/plain;charset=iso-8859-5";

  public static final String ISO_CHARSET      = "ISO-8859-5";

  public static byte[]       ISO_CONTENT;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    UTF_CONTENT = CONTENT.getBytes(UTF_CHARSET);

    WIN_CONTENT = CONTENT.getBytes(WIN_CHARSET);

    ISO_CONTENT = CONTENT.getBytes(ISO_CHARSET);

  }

  public void testNoContentTypeHeader() throws Exception {

//    System.out.println("\ttestNoContentTypeHeader:");

    ContainerResponse response = service("PUT", getPathWS() + UTF_FILE, "", null, UTF_CONTENT);
    assertEquals(HTTPStatus.CREATED, response.getStatus());
    response = service("GET", getPathWS() + UTF_FILE, "", null, null);
    assertEquals(HTTPStatus.OK, response.getStatus());
    byte[] responseContent = TestUtils.stream2string((ByteArrayInputStream) response.getEntity(),
                                                     UTF_CHARSET).getBytes();
    assertTrue(Arrays.equals(UTF_CONTENT, responseContent));
//    System.out.println("Content in UTF-8 encoding:\t" + new String(responseContent, UTF_CHARSET));

    response = service("PUT", getPathWS() + WIN_FILE, "", null, WIN_CONTENT);
    assertEquals(HTTPStatus.CREATED, response.getStatus());
    response = service("GET", getPathWS() + WIN_FILE, "", null, null);
    assertEquals(HTTPStatus.OK, response.getStatus());
    responseContent = TestUtils.stream2string((ByteArrayInputStream) response.getEntity(),
                                              WIN_CHARSET).getBytes(WIN_CHARSET);
    assertTrue(Arrays.equals(WIN_CONTENT, responseContent));
//    System.out.println("Content in Cp1251 encoding:\t" + new String(responseContent, WIN_CHARSET));

    response = service("PUT", getPathWS() + ISO_FILE, "", null, ISO_CONTENT);
    assertEquals(HTTPStatus.CREATED, response.getStatus());
    response = service("GET", getPathWS() + ISO_FILE, "", null, null);
    assertEquals(HTTPStatus.OK, response.getStatus());
    responseContent = TestUtils.stream2string((ByteArrayInputStream) response.getEntity(),
                                              ISO_CHARSET).getBytes(ISO_CHARSET);
    assertTrue(Arrays.equals(ISO_CONTENT, responseContent));
//    System.out.println("Content in ISO-8859-5 encoding:\t"
//        + new String(responseContent, ISO_CHARSET));

  }

  public void testContentType() throws Exception {

//    System.out.println("\n\ttestContentType:");

    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.add(HttpHeaders.CONTENT_TYPE, UTF_CONTENT_TYPE);
    ContainerResponse response = service("PUT", getPathWS() + UTF_FILE, "", headers, UTF_CONTENT);
    assertEquals(HTTPStatus.CREATED, response.getStatus());
    response = service("GET", getPathWS() + UTF_FILE, "", null, null);
    assertEquals(HTTPStatus.OK, response.getStatus());
    byte[] responseContent = TestUtils.stream2string((ByteArrayInputStream) response.getEntity(),
                                                     UTF_CHARSET).getBytes(UTF_CHARSET);
    assertTrue(Arrays.equals(UTF_CONTENT, responseContent));
    assertEquals(UTF_CONTENT_TYPE, response.getContentType().toString());
//    System.out.println("Content in UTF-8 encoding:\t" + new String(responseContent, UTF_CHARSET));
    headers.clear();

    headers.add(HttpHeaders.CONTENT_TYPE, WIN_CONTENT_TYPE);
    response = service("PUT", getPathWS() + WIN_FILE, "", headers, WIN_CONTENT);
    assertEquals(HTTPStatus.CREATED, response.getStatus());
    response = service("GET", getPathWS() + WIN_FILE, "", null, null);
    assertEquals(HTTPStatus.OK, response.getStatus());
    responseContent = TestUtils.stream2string((ByteArrayInputStream) response.getEntity(),
                                              WIN_CHARSET).getBytes(WIN_CHARSET);
    assertTrue(Arrays.equals(WIN_CONTENT, responseContent));
    assertEquals(WIN_CONTENT_TYPE, response.getContentType().toString());
//    System.out.println("Content in Cp1251 encoding:\t" + new String(responseContent, WIN_CHARSET));
    headers.clear();

    headers.add(HttpHeaders.CONTENT_TYPE, ISO_CONTENT_TYPE);
    response = service("PUT", getPathWS() + ISO_FILE, "", headers, ISO_CONTENT);
    assertEquals(HTTPStatus.CREATED, response.getStatus());
    response = service("GET", getPathWS() + ISO_FILE, "", null, null);
    assertEquals(HTTPStatus.OK, response.getStatus());
    responseContent = TestUtils.stream2string((ByteArrayInputStream) response.getEntity(),
                                              ISO_CHARSET).getBytes(ISO_CHARSET);
    assertTrue(Arrays.equals(ISO_CONTENT, responseContent));
    assertEquals(ISO_CONTENT_TYPE, response.getContentType().toString());
//    System.out.println("Content in Cp1251 encoding:\t" + new String(responseContent, ISO_CHARSET));
    headers.clear();
  }

  public void testRewriteEncodedFile() throws Exception {

    System.out.println("\n\ttestRewriteEncodedFile:");

    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.add(HttpHeaders.CONTENT_TYPE, ISO_CONTENT_TYPE);
    ContainerResponse response = service("PUT", getPathWS() + ISO_FILE, "", headers, ISO_CONTENT);
    assertEquals(HTTPStatus.CREATED, response.getStatus());
    response = service("GET", getPathWS() + ISO_FILE, "", null, null);
    assertEquals(HTTPStatus.OK, response.getStatus());
    byte[] responseContent = TestUtils.stream2string((ByteArrayInputStream) response.getEntity(),
                                                     ISO_CHARSET).getBytes(ISO_CHARSET);
    assertTrue(Arrays.equals(ISO_CONTENT, responseContent));
    assertEquals(ISO_CONTENT_TYPE, response.getContentType().toString());
//    System.out.println("Content in ISO-8859-5 encoding:\t"
//        + new String(responseContent, ISO_CHARSET));
    headers.clear();

    headers.add(HttpHeaders.CONTENT_TYPE, WIN_CONTENT_TYPE);
    response = service("PUT", getPathWS() + ISO_FILE, "", headers, WIN_CONTENT);
    assertEquals(HTTPStatus.CREATED, response.getStatus());
    response = service("GET", getPathWS() + ISO_FILE, "", null, null);
    assertEquals(HTTPStatus.OK, response.getStatus());
    responseContent = TestUtils.stream2string((ByteArrayInputStream) response.getEntity(),
                                              WIN_CHARSET).getBytes(WIN_CHARSET);
    assertTrue(Arrays.equals(WIN_CONTENT, responseContent));
    assertEquals(WIN_CONTENT_TYPE, response.getContentType().toString());
//    System.out.println("Content in Cp1251 encoding:\t" + new String(responseContent, WIN_CHARSET));

  }

  @Override
  protected String getRepositoryName() {
    // TODO Auto-generated method stub
    return null;
  }

}
