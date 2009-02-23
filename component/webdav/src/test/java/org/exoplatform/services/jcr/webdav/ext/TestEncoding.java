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

import java.util.Arrays;

import javax.ws.rs.core.HttpHeaders;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.common.http.client.HTTPResponse;
import org.exoplatform.common.http.client.NVPair;
import org.exoplatform.services.jcr.webdav.BaseWebDavTest;
import org.exoplatform.services.jcr.webdav.utils.TestUtils;

/**
 * Created by The eXo Platform SAS. <br/>
 * Date: 17 Feb 2009
 * 
 * @author <a href="mailto:dmitry.kataev@exoplatform.com.ua">Dmytro Katayev</a>
 * @version $Id: TestEncoding.java
 */
public class TestEncoding extends BaseWebDavTest {

  public static final String CONTENT          = "\u043f\u0440\u0438\u043a\u043b\u0430\u0434";

  // UTF-8 content
  public static final String UTF_FILE         = TestUtils.getFullWorkSpacePath() + "/utfFile.txt";

  public static final String UTF_CONTENT_TYPE = "text/plain;charset=UTF-8";

  public static final String UTF_CHARSET      = "UTF8";

  public static byte[]       UTF_CONTENT;

  // windows-1251 content
  public static final String WIN_FILE         = TestUtils.getFullWorkSpacePath() + "/winFile.txt";

  public static final String WIN_CONTENT_TYPE = "text/plain;charset=windows-1251";

  public static final String WIN_CHARSET      = "Cp1251";

  public static byte[]       WIN_CONTENT;

  // ISO 8859-5 content
  public static final String ISO_FILE         = TestUtils.getFullWorkSpacePath() + "/isoFile.txt";

  public static final String ISO_CONTENT_TYPE = "text/plain;charset=iso-8859-5";

  public static final String ISO_CHARSET      = "ISO-8859-5";

  public static byte[]       ISO_CONTENT;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    UTF_CONTENT = CONTENT.getBytes(UTF_CHARSET);

    WIN_CONTENT = CONTENT.getBytes(WIN_CHARSET);

    ISO_CONTENT = CONTENT.getBytes(ISO_CHARSET);

  }

  public void testNoContentTypeHeader() throws Exception {

    System.out.println("\ttestNoContentTypeHeader:");

    HTTPResponse response = connection.Put(UTF_FILE, UTF_CONTENT);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());
    response = connection.Get(UTF_FILE);
    assertEquals(HTTPStatus.OK, response.getStatusCode());
    assertTrue(Arrays.equals(UTF_CONTENT, response.getData()));
    System.out.println("Content in UTF-8 encoding:\t" + new String(response.getData(), UTF_CHARSET));

    response = connection.Put(WIN_FILE, WIN_CONTENT);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());
    response = connection.Get(WIN_FILE);
    assertEquals(HTTPStatus.OK, response.getStatusCode());
    assertTrue(Arrays.equals(WIN_CONTENT, response.getData()));
    System.out.println("Content in Cp1251 encoding:\t" + new String(response.getData(), WIN_CHARSET));

    response = connection.Put(ISO_FILE, ISO_CONTENT);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());
    response = connection.Get(ISO_FILE);
    assertEquals(HTTPStatus.OK, response.getStatusCode());
    assertTrue(Arrays.equals(ISO_CONTENT, response.getData()));
    System.out.println("Content in ISO-8859-5 encoding:\t" + new String(response.getData(), ISO_CHARSET));

    response = connection.Delete(UTF_FILE);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());

    response = connection.Delete(WIN_FILE);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());

    response = connection.Delete(ISO_FILE);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());

  }

  public void testContentType() throws Exception {

    System.out.println("\n\ttestContentType:");

    NVPair[] headers = new NVPair[] { new NVPair(HttpHeaders.CONTENT_TYPE, UTF_CONTENT_TYPE) };
    HTTPResponse response = connection.Put(UTF_FILE, UTF_CONTENT, headers);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());
    response = connection.Get(UTF_FILE);
    assertEquals(HTTPStatus.OK, response.getStatusCode());
    assertTrue(Arrays.equals(UTF_CONTENT, response.getData()));
    assertEquals(UTF_CONTENT_TYPE, response.getHeader(HttpHeaders.CONTENT_TYPE));
    System.out.println("Content in UTF-8 encoding:\t" + new String(response.getData(), UTF_CHARSET));

    headers = new NVPair[] { new NVPair(HttpHeaders.CONTENT_TYPE, WIN_CONTENT_TYPE) };
    response = connection.Put(WIN_FILE, WIN_CONTENT, headers);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());
    response = connection.Get(WIN_FILE);
    assertEquals(HTTPStatus.OK, response.getStatusCode());
    assertTrue(Arrays.equals(WIN_CONTENT, response.getData()));
    assertEquals(WIN_CONTENT_TYPE, response.getHeader(HttpHeaders.CONTENT_TYPE));
    System.out.println("Content in Cp1251 encoding:\t" + new String(response.getData(), WIN_CHARSET));

    headers = new NVPair[] { new NVPair(HttpHeaders.CONTENT_TYPE, ISO_CONTENT_TYPE) };
    response = connection.Put(ISO_FILE, ISO_CONTENT, headers);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());
    response = connection.Get(ISO_FILE);
    assertEquals(HTTPStatus.OK, response.getStatusCode());
    assertTrue(Arrays.equals(ISO_CONTENT, response.getData()));
    assertEquals(ISO_CONTENT_TYPE, response.getHeader(HttpHeaders.CONTENT_TYPE));
    System.out.println("Content in ISO-8859-5 encoding:\t" + new String(response.getData(), ISO_CHARSET));

    response = connection.Delete(UTF_FILE);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());

    response = connection.Delete(WIN_FILE);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());

    response = connection.Delete(ISO_FILE);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());

  }

  public void testRewriteEncodedFile() throws Exception {

    System.out.println("\n\ttestRewriteEncodedFile:");
    
    NVPair[] headers = new NVPair[] { new NVPair(HttpHeaders.CONTENT_TYPE, ISO_CONTENT_TYPE) };
    HTTPResponse response = connection.Put(ISO_FILE, ISO_CONTENT, headers);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());
    response = connection.Get(ISO_FILE);
    assertEquals(HTTPStatus.OK, response.getStatusCode());
    assertTrue(Arrays.equals(ISO_CONTENT, response.getData()));
    assertEquals(ISO_CONTENT_TYPE, response.getHeader(HttpHeaders.CONTENT_TYPE));
    System.out.println("Content in Cp1251 encoding:\t" + new String(response.getData(), ISO_CHARSET));

    headers = new NVPair[] { new NVPair(HttpHeaders.CONTENT_TYPE, WIN_CONTENT_TYPE) };
    response = connection.Put(ISO_FILE, WIN_CONTENT, headers);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());
    response = connection.Get(ISO_FILE);
    assertEquals(HTTPStatus.OK, response.getStatusCode());
    assertTrue(Arrays.equals(WIN_CONTENT, response.getData()));
    assertEquals(WIN_CONTENT_TYPE, response.getHeader(HttpHeaders.CONTENT_TYPE));
    System.out.println("Content in ISO-8859-5 encoding:\t" + new String(response.getData(), WIN_CHARSET));

    response = connection.Delete(ISO_FILE);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());

  }

}
