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
package org.exoplatform.services.jcr.webdav.command;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;

import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.common.http.client.HTTPResponse;
import org.exoplatform.common.http.client.NVPair;
import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.services.jcr.webdav.BaseStandaloneTest;
import org.exoplatform.services.jcr.webdav.BaseWebDavTest;
import org.exoplatform.services.jcr.webdav.Range;
import org.exoplatform.services.jcr.webdav.WebDavServiceImpl;
import org.exoplatform.services.jcr.webdav.lock.NullResourceLocksHolder;
import org.exoplatform.services.jcr.webdav.utils.TestUtils;
import org.exoplatform.services.rest.ExtHttpHeaders;
import org.exoplatform.services.rest.impl.ContainerResponse;

/**
 * Created by The eXo Platform SAS Author : Dmytro Katayev
 * work.visor.ck@gmail.com Aug 13, 2008
 */
public class TestPut extends BaseStandaloneTest {

   
  
   
  public void testPut() throws Exception{
    String content = TestUtils.getFileContent();
    ContainerResponse containerResponse = service("PUT",getPathWS() + TestUtils.getFileName() , "", null, content.getBytes());
    assertEquals(HTTPStatus.CREATED, containerResponse.getStatus());
  }
    
  
  public void testPutNotFound() throws Exception{
    String content = TestUtils.getFileContent();
    ContainerResponse containerResponse = service("PUT",getPathWS() + "/not-found"+TestUtils.getFileName() , "", null, content.getBytes());
    assertEquals(HTTPStatus.CONFLICT, containerResponse.getStatus());
  }
  
  
  
  @Override
  protected String getRepositoryName() {
    return null;
  }

}
