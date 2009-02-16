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
package org.exoplatform.services.jcr.webdav;

//import org.codehaus.cargo.container.InstalledLocalContainer;
import org.exoplatform.common.http.client.CookieModule;
import org.exoplatform.common.http.client.HTTPConnection;
import org.exoplatform.services.jcr.webdav.WebDavConstants.WebDav;
import org.exoplatform.services.jcr.webdav.utils.TestUtils;
import org.exoplatform.services.log.ExoLogger;
import org.apache.commons.logging.Log;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS Author : Dmytro Katayev
 * work.visor.ck@gmail.com 22 Sep 2008
 */
public class BaseWebDavTest extends TestCase {

  protected static Log     log         = ExoLogger.getLogger("jcr.WebDavTest");

  // protected InstalledLocalContainer container;

  protected HTTPConnection connection;

  @Override
  protected void setUp() throws Exception {

    // container = ContainerStarter.cargoContainerStart(WebDav.PORT_STRING,
    // null);
    // assertTrue(container.getState().isStarted());

    super.setUp();

    CookieModule.setCookiePolicyHandler(null);
    connection = TestUtils.GetAuthConnection();

  }

  @Override
  protected void tearDown() throws Exception {

    // TODO Auto-generated method stub

    // ContainerStarter.cargoContainerStop(container);
    // assertTrue(container.getState().isStopped());

    super.tearDown();
  }

}
