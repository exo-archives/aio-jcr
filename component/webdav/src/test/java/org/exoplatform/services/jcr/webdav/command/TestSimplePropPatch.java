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
package org.exoplatform.services.jcr.webdav.command;

import org.exoplatform.common.http.client.CookieModule;
import org.exoplatform.common.http.client.HTTPConnection;
import org.exoplatform.services.jcr.webdav.BaseStandaloneTest;
import org.exoplatform.services.jcr.webdav.utils.TestUtils;

/**
 * Created by The eXo Platform SAS. <br/>
 * Date: 16 Dec 2008
 * 
 * @author <a href="dkatayev@gmail.com">Dmytro Katayev</a>
 * @version $Id: TestSetPropPatch.java
 */
public class TestSimplePropPatch extends BaseStandaloneTest {

  private static final String   REPOSITORY  = "repository";

  protected HTTPConnection      connection;

  protected String              fileName;

  protected static final String fileContent = "TEST FILE CONTENT...";

  @Override
  protected String getRepositoryName() {
    return REPOSITORY;
  }

  @Override
  public void setUp() throws Exception {
    // TODO Auto-generated method stub
    super.setUp();

    CookieModule.setCookiePolicyHandler(null);
    connection = TestUtils.GetAuthConnection();

    fileName = TestUtils.getFileName();

  }

  @Override
  protected void tearDown() throws Exception {
    // TODO Auto-generated method stub
    super.tearDown();
  }

  public void testSiplePropPatch() throws Exception {
    fail("Not yet implemented");

  }

}
