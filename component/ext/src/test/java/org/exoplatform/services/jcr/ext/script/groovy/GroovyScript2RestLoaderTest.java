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

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.rest.MultivaluedMetadata;
import org.exoplatform.services.rest.Request;
import org.exoplatform.services.rest.ResourceBinder;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.ResourceIdentifier;
import org.exoplatform.services.rest.Response;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class GroovyScript2RestLoaderTest extends BaseStandaloneTest {

  private Node               testRoot;

  private ResourceBinder     binder;

  private ResourceDispatcher dispatcher;

  private Node               scriptFile;

  private Node               script;

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.ext.BaseStandaloneTest#setUp()
   */
  public void setUp() throws Exception {
    super.setUp();

    binder = (ResourceBinder) container.getComponentInstanceOfType(ResourceBinder.class);
    binder.clear();
    dispatcher = (ResourceDispatcher) container.getComponentInstanceOfType(ResourceDispatcher.class);

    testRoot = root.addNode("testRoot", "nt:unstructured");
    scriptFile = testRoot.addNode("script", "nt:file");
    script = scriptFile.addNode("jcr:content", GroovyScript2RestLoader.DEFAULT_NODETYPE);
    script.setProperty("exo:autoload", true);
    script.setProperty("jcr:mimeType", "text/groovy");
    script.setProperty("jcr:lastModified", Calendar.getInstance());
    script.setProperty("jcr:data", Thread.currentThread()
                                         .getContextClassLoader()
                                         .getResourceAsStream("test1.groovy"));

    session.save();
  }

  public void testStartQuery() throws Exception {
    String xpath = "//element(*, " + GroovyScript2RestLoader.DEFAULT_NODETYPE
        + ")[@exo:autoload='true']";
    Query query = session.getWorkspace().getQueryManager().createQuery(xpath, Query.XPATH);
    QueryResult result = query.execute();
    assertEquals(1, result.getNodes().getSize());

    script.setProperty("exo:autoload", false);
    session.save();
    result = query.execute();
    assertEquals(0, result.getNodes().getSize());
  }

  public void testBindScripts() throws Exception {
    // one script should be binded from start
    assertEquals(1, binder.getAllDescriptors().size());

    script.setProperty("exo:autoload", false);
    session.save();
    assertEquals(0, binder.getAllDescriptors().size());

    // bind script again
    script.setProperty("exo:autoload", true);
    session.save();
    assertEquals(1, binder.getAllDescriptors().size());
  }

  public void testDispatchScript() throws Exception {
    assertEquals(1, binder.getAllDescriptors().size());
    Request request = new Request(null,
                                  new ResourceIdentifier("/test/groovy1/test/"),
                                  "GET",
                                  new MultivaluedMetadata(),
                                  new MultivaluedMetadata());
    Response response = dispatcher.dispatch(request);
    assertEquals(200, response.getStatus());
    assertEquals("Hello from groovy to test!", response.getEntity());

    // change script source code
    script.setProperty("jcr:data", Thread.currentThread()
                                         .getContextClassLoader()
                                         .getResourceAsStream("test2.groovy"));
    session.save();

    // must be rebounded , not created other one
    assertEquals(1, binder.getAllDescriptors().size());
    // relative URI changed
    request = new Request(null,
                          new ResourceIdentifier("/test/groovy2/test/"),
                          "GET",
                          new MultivaluedMetadata(),
                          new MultivaluedMetadata());
    response = dispatcher.dispatch(request);
    assertEquals(200, response.getStatus());
    assertEquals("Hello from groovy to >>>>> test!", response.getEntity());
  }

}
