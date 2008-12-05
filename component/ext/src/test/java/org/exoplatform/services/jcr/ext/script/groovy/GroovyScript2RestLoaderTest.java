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

package org.exoplatform.services.jcr.ext.script.groovy;

import java.net.URI;
import java.util.Calendar;

import javax.jcr.Node;
//import javax.jcr.query.Query;
//import javax.jcr.query.QueryResult;

import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.registry.RESTRegistryTest.DummyContainerResponseWriter;
import org.exoplatform.services.rest.RequestHandler;
import org.exoplatform.services.rest.impl.ContainerRequest;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.InputHeadersMap;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.rest.impl.ResourceBinder;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class GroovyScript2RestLoaderTest extends BaseStandaloneTest {

  private Node           testRoot;

  private ResourceBinder binder;

  private RequestHandler handler;

  private Node           scriptFile;

  private Node           script;

  /**
   * {@inheritDoc}
   */
  public void setUp() throws Exception {
    super.setUp();

    binder = (ResourceBinder) container.getComponentInstanceOfType(ResourceBinder.class);
    binder.clear();
    handler = (RequestHandler) container.getComponentInstanceOfType(RequestHandler.class);

    testRoot = root.addNode("testRoot", "nt:unstructured");
    scriptFile = testRoot.addNode("script", "nt:file");
    script = scriptFile.addNode("jcr:content", GroovyScript2RestLoader.DEFAULT_NODETYPE);
    script.setProperty("exo:autoload", true);
    script.setProperty("jcr:mimeType", "script/groovy");
    script.setProperty("jcr:lastModified", Calendar.getInstance());
    script.setProperty("jcr:data", Thread.currentThread()
                                         .getContextClassLoader()
                                         .getResourceAsStream("test1.groovy"));
    session.save();
  }
  
//  public void testStartQuery() throws Exception {
//    String xpath = "//element(*, " + GroovyScript2RestLoader.DEFAULT_NODETYPE
//        + ")[@exo:autoload='true']";
//    Query query = session.getWorkspace().getQueryManager().createQuery(xpath, Query.XPATH);
//    QueryResult result = query.execute();
//    assertEquals(1, result.getNodes().getSize());
//
//    script.setProperty("exo:autoload", false);
//    session.save();
//    result = query.execute();
//    assertEquals(0, result.getNodes().getSize());
//  }

  public void testBindScripts() throws Exception {
    // one script should be binded from start
    assertFalse(script.getProperty("exo:load").getBoolean());
    assertEquals(0, binder.getRootResources().size());

    script.setProperty("exo:load", true);
    session.save();
    assertEquals(1, binder.getRootResources().size());

    // bind script again
    script.setProperty("exo:load", false);
    session.save();
    assertEquals(0, binder.getRootResources().size());
  }

  public void testDispatchScript() throws Exception {
    script.setProperty("exo:load", true);
    session.save();
    ContainerRequest creq = new ContainerRequest("GET",
                                                 new URI("/groovy-test/groovy1/test"),
                                                 new URI(""),
                                                 null,
                                                 new InputHeadersMap(new MultivaluedMapImpl()));

    ContainerResponse cres = new ContainerResponse(new DummyContainerResponseWriter());

    handler.handleRequest(creq, cres);

    assertEquals(200, cres.getStatus());
    assertEquals("Hello from groovy to test", cres.getEntity());

    // change script source code
    script.setProperty("jcr:data", Thread.currentThread()
                                         .getContextClassLoader()
                                         .getResourceAsStream("test2.groovy"));
    session.save();

    // must be rebounded , not created other one
    assertEquals(1, binder.getRootResources().size());
    creq = new ContainerRequest("GET",
                                new URI("/groovy-test/groovy2/test"),
                                new URI(""),
                                null,
                                new InputHeadersMap(new MultivaluedMapImpl()));

    cres = new ContainerResponse(new DummyContainerResponseWriter());
    handler.handleRequest(creq, cres);
    assertEquals(200, cres.getStatus());
    assertEquals("Hello from groovy to >>>>> test", cres.getEntity());
  }

}
