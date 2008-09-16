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

import org.apache.commons.logging.Log;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.exoplatform.services.jcr.webdav.command.TestCommandsSuite;
import org.exoplatform.services.jcr.webdav.command.TestCopy;
import org.exoplatform.services.log.ExoLogger;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Created by The eXo Platform SAS
 * Author : Dmytro Katayev
 *          work.visor.ck@gmail.com
 * 15 Sep 2008  
 */
public class TestWebdavLauncher extends TestCase {
  
  private static Log log = ExoLogger.getLogger("org.exoplatform.services.jcr.webdav.TestWebdavLauncher");
  private static String port = "8088";
  private static InstalledLocalContainer container;
  
  @Override
  protected void setUp() throws Exception {

    if (System.getProperty("exo.test.cargo.port") != null){
      port = System.getProperty("exo.test.cargo.port");       
    }
    
    if (System.getProperty("exo.test.cargo.skip") == null
        || !System.getProperty("exo.test.cargo.skip").equalsIgnoreCase("true")){
      container = ContainerStarter.cargoContainerStart(port, null);
    }
    assertEquals(container.getState().isStarted(), true);
    
  }
  
  public static TestSuite suite() {
    
    log.info("Preparing...");
    System.out.println("TEST LOGGER: " + log);
    TestSuite suite = new TestSuite("webdav-service tests");
    
    if (System.getProperty("exo.test.cargo.port") != null){
      port = System.getProperty("exo.test.cargo.port");       
    }
    
    if (System.getProperty("exo.test.cargo.skip") == null
        || !System.getProperty("exo.test.cargo.skip").equalsIgnoreCase("true")){
      container = ContainerStarter.cargoContainerStart(port, null);
    }
    assertEquals(container.getState().isStarted(), true);
    
    suite.addTestSuite(TestCommandsSuite.class);
        
    return suite();
  }
  
  @Override
  protected void tearDown() throws Exception {
    assertTrue(container.getState().isStopped());
  }

}
