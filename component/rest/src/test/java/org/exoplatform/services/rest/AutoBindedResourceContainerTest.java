/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import junit.framework.TestCase;
import java.util.List;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.rest.container.ResourceDescriptor;
import org.exoplatform.services.rest.data.StringRepresentation;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */

public class AutoBindedResourceContainerTest extends TestCase {
  
  private StandaloneContainer container;
  
  public void setUp() throws Exception {
    StandaloneContainer.setConfigurationPath("src/test/java/conf/standalone/test-configuration.xml");
    container = StandaloneContainer.getInstance();
  }

  public void testIfResourceContainerPresent() throws Exception {
    ResourceBinder binder =
      (ResourceBinder)container.getComponentInstanceOfType(ResourceBinder.class);
    assertNotNull(binder);
    ResourceDispatcher disp = 
      (ResourceDispatcher)container.getComponentInstanceOfType(ResourceDispatcher.class);
    assertNotNull(disp);
    List<ResourceDescriptor> list = binder.getAllDescriptors();
    assertEquals(1, list.size());
    assertEquals("GET", list.get(0).getAcceptableMethod());
    assertEquals("method_", list.get(0).getServer().getName());

    Request request = new Request(new ResourceIdentifier("/level1/test/level2/"),
        "GET", new StringRepresentation("text/plain"));
    Response resp = disp.dispatch(request);
    assertEquals("method_", resp.getEntity().getString());
    System.out.println("##### " + resp.getEntity().getString());
    binder.clear();
  }

}
