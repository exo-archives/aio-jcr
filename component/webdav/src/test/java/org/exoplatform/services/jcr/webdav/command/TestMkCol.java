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

import junit.framework.TestCase;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.common.http.client.CookieModule;
import org.exoplatform.common.http.client.HTTPConnection;
import org.exoplatform.common.http.client.HTTPResponse;
import org.exoplatform.services.jcr.webdav.ContainerStarter;
import org.exoplatform.services.jcr.webdav.utils.TestUtils;

/**
 * Created by The eXo Platform SAS
 * Author : Dmytro Katayev
 *          work.visor.ck@gmail.com
 * Aug 13, 2008  
 */
public class TestMkCol extends TestCase {
  
  private final String destName = TestUtils.getFolderName();
  private HTTPConnection connection;
  
  private InstalledLocalContainer container;

  
  @Override
  protected void setUp() throws Exception {   
    
    container = ContainerStarter.cargoContainerStart("8088", null);
    assertTrue(container.getState().isStarted());
    
    connection = TestUtils.GetAuthConnection();
    
    CookieModule.setCookiePolicyHandler(null);    
    
    super.setUp();
  }
  
  @Override
  protected void tearDown() throws Exception {
    connection.Delete(TestUtils.getFullWorkSpacePath() + destName);
    
    ContainerStarter.cargoContainerStop(container);
    assertTrue(container.getState().isStopped());
      
    super.tearDown();
  }
  
  public void testSucceed() throws Exception {
    
    HTTPResponse response = connection.MkCol(TestUtils.getFullWorkSpacePath() + destName);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());
  }
  
  public void testForbidden() throws Exception {

    HTTPResponse response = connection.MkCol(TestUtils.SERVLET_PATH + TestUtils.INAVLID_WORKSPACE +
        destName);
    assertEquals(HTTPStatus.NOT_FOUND, response.getStatusCode());
    
  }
  
  public void testConflict() throws Exception {
    
    HTTPResponse response = connection.MkCol(TestUtils.getFullWorkSpacePath() + "/path" +
        destName);
    assertEquals(HTTPStatus.CONFLICT, response.getStatusCode());
    
  }

}
