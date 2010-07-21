/*
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
package org.exoplatform.services.jcr.webdav.ext;

import junit.framework.TestCase;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.common.http.client.CookieModule;
import org.exoplatform.common.http.client.HTTPConnection;
import org.exoplatform.common.http.client.HTTPResponse;
import org.exoplatform.common.http.client.NVPair;
import org.exoplatform.services.jcr.webdav.WebDavConst;
import org.exoplatform.services.jcr.webdav.WebDavHeaders;
import org.exoplatform.services.jcr.webdav.utils.TestUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by The eXo Platform SAS Author : Dmytro Katayev work.visor.ck@gmail.com Aug 13, 2008
 */
public class TestCaching extends TestCase {

  private final String   fileName    = TestUtils.getFullWorkSpacePath() + "/"
                                         + TestUtils.getFileName();

  private final String   fileContent = "TEST FILE CONTENT...";

  private HTTPConnection connection;

  @Override
  protected void setUp() throws Exception {

    CookieModule.setCookiePolicyHandler(null);

    connection = TestUtils.GetAuthConnection();

    HTTPResponse response = connection.Put(fileName, fileContent);  
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {

    HTTPResponse response = connection.Delete(fileName);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());

    super.tearDown();
  }

  public void testIfNotModifiedSince() throws Exception {

    List<String> props = new ArrayList<String>();
    props.add("getlastmodified");
    HTTPResponse response =  connection.Propfind(fileName, props);
    
    String responseText = response.getText();
    String lastModifiedProp = getLastModified(responseText);
    
    SimpleDateFormat dateFormat = new SimpleDateFormat(WebDavConst.DateFormat.IF_MODIFIED_SINCE_PATTERN);
    Date lastModifiedDate = dateFormat.parse(lastModifiedProp);
    lastModifiedDate.setTime(lastModifiedDate.getTime() - 60000);
    
    NVPair[] headers = {new NVPair(WebDavHeaders.IF_MODIFIED_SINCE, dateFormat.format(lastModifiedDate.getTime()))};
    
    response = connection.Get(fileName, new NVPair[]{}, headers);
    assertEquals(HTTPStatus.OK, response.getStatusCode());

  }
  
  public void testIfModifiedSince() throws Exception {

     List<String> props = new ArrayList<String>();
     props.add("getlastmodified");
     HTTPResponse response =  connection.Propfind(fileName, props);
     
     String responseText = response.getText();
     String lastModifiedProp = getLastModified(responseText);
     
     SimpleDateFormat dateFormat = new SimpleDateFormat(WebDavConst.DateFormat.IF_MODIFIED_SINCE_PATTERN);
     Date lastModifiedDate = dateFormat.parse(lastModifiedProp);
     lastModifiedDate.setTime(lastModifiedDate.getTime() + 60000);
     
     NVPair[] headers = {new NVPair(WebDavHeaders.IF_MODIFIED_SINCE, dateFormat.format(lastModifiedDate.getTime()))};
     
     response = connection.Get(fileName, new NVPair[]{}, headers);
     assertEquals(HTTPStatus.NOT_MODIFIED, response.getStatusCode());
     
   }
  
  private String getLastModified(String response){
     
     String patternString = ".+(getlastmodified).+\\>(.+)\\<.+(getlastmodified).+";

     Pattern pattern = Pattern.compile(patternString);
     Matcher matcher = pattern.matcher(response);
     matcher.matches();
     
     return matcher.group(2);
     
  }
  
  
  public class CacheControlType
  {
     private String contentType;
     private String cacheValue;
     
     /**
      * @return the cacheValue
      */
     public String getCacheValue()
     {
        return cacheValue;
     }

     /**
      * @return the contentType
      */
     public String getContentType()
     {
        return contentType;
     }
     
     public CacheControlType(String contentType, String cacheValue)
     {
        this.contentType = contentType;
        this.cacheValue = cacheValue;
     }
     

  }

}
