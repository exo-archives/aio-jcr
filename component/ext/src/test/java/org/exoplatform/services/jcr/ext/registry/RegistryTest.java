/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.ext.registry;

import java.io.File;
import java.io.FileInputStream;

import javax.jcr.ItemNotFoundException;

import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.rest.transformer.PassthroughOutputTransformer;


public class RegistryTest extends BaseStandaloneTest{
  
	private ThreadLocalSessionProviderService sessionProviderService;
	
	@Override
  public void setUp() throws Exception {

    super.setUp();
    this.sessionProviderService = (ThreadLocalSessionProviderService)
    		container.getComponentInstanceOfType(ThreadLocalSessionProviderService.class);
    sessionProviderService.setSessionProvider(null, new SessionProvider(credentials));
  }
	
	

  public void testInit() throws Exception {
    RegistryService regService = (RegistryService) container
    .getComponentInstanceOfType(RegistryService.class);
    assertNotNull(regService);
      
    assertNotNull(regService.getRegistry(sessionProviderService.getSessionProvider(null)).getNode());
    assertTrue(regService.getRegistry(sessionProviderService.getSessionProvider(null)).getNode().hasNode(RegistryService.EXO_SERVICES));
    assertTrue(regService.getRegistry(sessionProviderService.getSessionProvider(null)).getNode().hasNode(RegistryService.EXO_APPLICATIONS));
    assertTrue(regService.getRegistry(sessionProviderService.getSessionProvider(null)).getNode().hasNode(RegistryService.EXO_USERS));
    
    session.getWorkspace().getNodeTypeManager().getNodeType("exo:registry");
    session.getWorkspace().getNodeTypeManager().getNodeType("exo:registryEntry");
    session.getWorkspace().getNodeTypeManager().getNodeType("exo:registryGroup");

  }
   
  public void testRegister() throws Exception {
    RegistryService regService = (RegistryService) container
    .getComponentInstanceOfType(RegistryService.class);

    try {
      regService.getEntry(sessionProviderService.getSessionProvider(null),
      		RegistryService.EXO_USERS + "/exo_user");
      fail("ItemNotFoundException should have been thrown");
    } catch (ItemNotFoundException e) {}
    
    String path = "src/test/java/org/exoplatform/services/jcr/ext/registry/exo_user.xml";
      if (!new File(path).exists()){
        path = "component/ext/" + path;
      }
    File entryFile = new File(path);
    
    regService.createEntry(sessionProviderService.getSessionProvider(null),
    		RegistryService.EXO_USERS, RegistryEntry.parse(new FileInputStream(entryFile)));
    
    RegistryEntry entry = regService.getEntry(sessionProviderService.getSessionProvider(null),
    		RegistryService.EXO_USERS + "/exo_user");
    PassthroughOutputTransformer transformer = new PassthroughOutputTransformer();
    transformer.writeTo(entry.getAsInputStream(), System.out);

    regService.recreateEntry(sessionProviderService.getSessionProvider(null), RegistryService.EXO_USERS, 
        RegistryEntry.parse(new FileInputStream(entryFile)));

    regService.removeEntry(sessionProviderService.getSessionProvider(null),
    		RegistryService.EXO_USERS + "/exo_user");
    
    try {
      regService.getEntry(sessionProviderService.getSessionProvider(null),
      		RegistryService.EXO_USERS + "/exo_user");
      fail("ItemNotFoundException should have been thrown");
    } catch (ItemNotFoundException e) {
    }

  }
  
  public void testRegisterToNonExistedGroup() throws Exception {
    RegistryService regService = (RegistryService) container
    .getComponentInstanceOfType(RegistryService.class);
    
    String groupPath = RegistryService.EXO_USERS+"/newGroup1/newGroup2";
    String entryName = "testEntry";
    
    try {
			regService.getEntry(sessionProviderService.getSessionProvider(null),
					groupPath + "/" + entryName);
			fail("ItemNotFoundException should have been thrown");
		} catch (ItemNotFoundException e) {
			// OK
		}
		
    // group path should have been created along with entry
    regService.createEntry(sessionProviderService.getSessionProvider(null),
    		groupPath, new RegistryEntry(entryName));
    

    RegistryEntry entry = regService.getEntry(sessionProviderService.getSessionProvider(null),
    		groupPath + "/" + entryName);
    
    assertNotNull(entry);
    assertEquals(entryName, entry.getName());

  }


  public void testLocation() throws Exception {
    RegistryService regService = (RegistryService) container
    .getComponentInstanceOfType(RegistryService.class);
    
    regService.addRegistryLocation("wrong", "wrong");
  }
  
}
