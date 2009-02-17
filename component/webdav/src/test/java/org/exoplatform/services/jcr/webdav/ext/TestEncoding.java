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

import javax.servlet.http.HttpServlet;
import javax.ws.rs.core.HttpHeaders;

import org.apache.bcel.generic.NEW;
import org.apache.tools.ant.taskdefs.condition.Http;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.common.http.client.HTTPResponse;
import org.exoplatform.common.http.client.NVPair;
import org.exoplatform.services.jcr.webdav.BaseWebDavTest;
import org.exoplatform.services.jcr.webdav.utils.TestUtils;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 17 Feb 2009
 *
 * @author <a href="mailto:dmitry.kataev@exoplatform.com.ua">Dmytro Katayev</a>
 * @version $Id: TestEncoding.java 
 */
public class TestEncoding extends BaseWebDavTest {
  
  
  
  // UTF-8 content
  public static final String utfContent = new String("\u043f\u0440\u0438\u043a\u043b\u0430\u0434");
  public static final String utfFile = TestUtils.getFullWorkSpacePath() + "/utfFile.txt";
  public static final String utfContentType = "text/plain;charset=UTF-8";
  
  //windows-1251 content
  public static String winContent;
  public static final String winFile = TestUtils.getFullWorkSpacePath() + "/winFile.txt";
  public static final String winContentType = "text/plain;charset=windows-1251";
  
  // ISO 8859-5 content
  public static String isoContent;
  public static final String isoFile = TestUtils.getFullWorkSpacePath() + "/isoFile.txt";
  public static final String isoContentType = "text/plain;charset=iso-8859-5";
  
    
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    
    winContent = new String(utfContent.getBytes("Cp1251"));
    isoContent = new String(utfContent.getBytes("ISO-8859-5"));
            
  }

  
  public void testNoContentTypeHeader() throws Exception {

    HTTPResponse response = connection.Put(utfFile, utfContent);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());
    response = connection.Get(utfFile);
    assertEquals(HTTPStatus.OK, response.getStatusCode());
    assertEquals(utfContent, new String(response.getData()));

    response = connection.Put(winFile, winContent);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());
    response = connection.Get(winFile);
    assertEquals(HTTPStatus.OK, response.getStatusCode());
    assertEquals(winContent, new String(response.getData()));

    response = connection.Put(isoFile, isoContent);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());
    response = connection.Get(isoFile);
    assertEquals(HTTPStatus.OK, response.getStatusCode());
    assertEquals(isoContent, new String(response.getData()));
    
    response = connection.Delete(utfFile);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());
    
    response = connection.Delete(winFile);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());
    
    response = connection.Delete(isoFile);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());
    

  }
  
  public void testContentType() throws Exception {
    
    NVPair[] headers = new NVPair[]{new NVPair(HttpHeaders.CONTENT_TYPE, utfContentType)};
    HTTPResponse response = connection.Put(utfFile, utfContent, headers);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());
    response = connection.Get(utfFile);
    assertEquals(HTTPStatus.OK, response.getStatusCode());
    assertEquals(utfContent, new String(response.getData()));
    assertEquals(utfContentType, response.getHeader(HttpHeaders.CONTENT_TYPE));
    
    headers = new NVPair[]{new NVPair(HttpHeaders.CONTENT_TYPE, winContentType)};
    response = connection.Put(winFile, winContent, headers);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());
    response = connection.Get(winFile);
    assertEquals(HTTPStatus.OK, response.getStatusCode());
    assertEquals(winContent, new String(response.getData()));
    assertEquals(winContentType, response.getHeader(HttpHeaders.CONTENT_TYPE));
    
    headers = new NVPair[]{new NVPair(HttpHeaders.CONTENT_TYPE, isoContentType)};
    response = connection.Put(isoFile, isoContent, headers);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());
    response = connection.Get(isoFile);
    assertEquals(HTTPStatus.OK, response.getStatusCode());
    assertEquals(isoContent, new String(response.getData()));
    assertEquals(isoContentType, response.getHeader(HttpHeaders.CONTENT_TYPE));
    
    response = connection.Delete(utfFile);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());
    
    response = connection.Delete(winFile);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());
    
    response = connection.Delete(isoFile);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());
    
  }
  
  public void testRewriteEncodedFile() throws Exception {
    
    NVPair[] headers = new NVPair[]{new NVPair(HttpHeaders.CONTENT_TYPE, isoContentType)};
    HTTPResponse response = connection.Put(isoFile, isoContent, headers);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());
    response = connection.Get(isoFile);
    assertEquals(HTTPStatus.OK, response.getStatusCode());
    assertEquals(isoContent, new String(response.getData()));
    assertEquals(isoContentType, response.getHeader(HttpHeaders.CONTENT_TYPE));
    
    headers = new NVPair[]{new NVPair(HttpHeaders.CONTENT_TYPE, winContentType)};
    response = connection.Put(winFile, winContent, headers);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());
    response = connection.Get(winFile);
    assertEquals(HTTPStatus.OK, response.getStatusCode());
    assertEquals(winContent, new String(response.getData()));
    assertEquals(winContentType, response.getHeader(HttpHeaders.CONTENT_TYPE));
    
    response = connection.Delete(isoFile);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());
    
    response = connection.Delete(winFile);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());
    
  }

}
