/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.ext.registry;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.registry.RegistryService.RegistryEntryNode;

public class RegistryTest extends BaseStandaloneTest{
  
  @Override
  public void setUp() throws Exception {

    super.setUp();
  }

  public void testInit() throws Exception {
    RegistryService regService = (RegistryService) container
    .getComponentInstanceOfType(RegistryService.class);
    
    SessionProvider sp = new SessionProvider(repository, credentials);
    assertNotNull(regService.getRegistry(sp).getNode());
    assertTrue(regService.getRegistry(sp).getNode().hasNode(RegistryService.EXO_SERVICES));
    assertTrue(regService.getRegistry(sp).getNode().hasNode(RegistryService.EXO_APPLICATIONS));
    assertTrue(regService.getRegistry(sp).getNode().hasNode(RegistryService.EXO_USERS));
    
    session.getWorkspace().getNodeTypeManager().getNodeType("exo:registry");
    session.getWorkspace().getNodeTypeManager().getNodeType("exo:registryEntry");
    session.getWorkspace().getNodeTypeManager().getNodeType("exo:registryGroup");

  }
  
  public void testRegister() throws Exception {
    RegistryService regService = (RegistryService) container
    .getComponentInstanceOfType(RegistryService.class);
    
    SessionProvider sp = new SessionProvider(repository, credentials);
    
    RegistryEntryNode ren = regService.getRegistryEntry(sp, RegistryService.EXO_SERVICES, "testService");
    
    Node node = ren.getNode(); 
    assertTrue(node.isNew());
    
    node.setProperty("test", "test");
    regService.register(ren);
    
    RegistryEntryNode ren1 = regService.getRegistryEntry(sp, RegistryService.EXO_SERVICES, "testService");
    node = ren1.getNode();
    assertFalse(node.isNew());
    assertTrue(node.hasProperty("test"));

    // unregister
    regService.unregister(sp, RegistryService.EXO_SERVICES, "testService");
    RegistryEntryNode ren2 = regService.getRegistryEntry(sp, RegistryService.EXO_SERVICES, "testService");
    node = ren2.getNode();
    assertTrue(node.isNew());

  }

  public void testLocation() throws Exception {
    RegistryService regService = (RegistryService) container
    .getComponentInstanceOfType(RegistryService.class);
    
    regService.addRegistryLocation("wrong", "wrong");
  }
}
