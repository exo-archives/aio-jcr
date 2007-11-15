/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.ext.registry;

import java.io.ByteArrayInputStream;

import javax.jcr.ItemNotFoundException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.rest.transformer.PassthroughOutputTransformer;
import org.w3c.dom.Document;


public class RegistryTest extends BaseStandaloneTest{
  
	private ThreadLocalSessionProviderService sessionProviderService;
	
	private static final String SERVICE_XML="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
   "<exo_service xmlns:jcr=\"http://www.jcp.org/jcr/1.0\" jcr:primaryType=\"exo:registryEntry\"/>";
	
	private static final String NAV_XML="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
	"<node-navigation><owner-type>portal</owner-type><owner-id>portalone</owner-id>"+
	"<access-permissions>*:/guest</access-permissions><page-nodes><node>"+
	"<uri>portalone::home</uri><name>home</name><label>Home</label>"+
	"<page-reference>portal::portalone::content</page-reference></node>"+ 
	"<node><uri>portalone::register</uri><name>register</name><label>Register</label>"+
	"<page-reference>portal::portalone::register</page-reference></node>"+
	"</page-nodes></node-navigation>";

	
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
      		RegistryService.EXO_USERS + "/exo_service");
      fail("ItemNotFoundException should have been thrown");
    } catch (ItemNotFoundException e) {}
/*    
    String path = "exo_user.xml";
      if (!new File(path).exists()){
        path = "component/ext/" + path;
      }
    File entryFile = new File(path);
*/    
    regService.createEntry(sessionProviderService.getSessionProvider(null),
    		RegistryService.EXO_USERS, RegistryEntry.parse(new ByteArrayInputStream(SERVICE_XML.getBytes())));
    
    RegistryEntry entry = regService.getEntry(sessionProviderService.getSessionProvider(null),
    		RegistryService.EXO_USERS + "/exo_service");
    PassthroughOutputTransformer transformer = new PassthroughOutputTransformer();
    transformer.writeTo(entry.getAsInputStream(), System.out);

    regService.recreateEntry(sessionProviderService.getSessionProvider(null), RegistryService.EXO_USERS, 
        RegistryEntry.parse(new ByteArrayInputStream(SERVICE_XML.getBytes())));

    regService.removeEntry(sessionProviderService.getSessionProvider(null),
    		RegistryService.EXO_USERS + "/exo_service");
    
    try {
      regService.getEntry(sessionProviderService.getSessionProvider(null),
      		RegistryService.EXO_USERS + "/exo_service");
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

  public void testRegisterFromXMLStream() throws Exception {
  	
    RegistryService regService = (RegistryService) container
    .getComponentInstanceOfType(RegistryService.class);

    String groupPath = RegistryService.EXO_USERS+"/testRegisterFromXMLStream";
    
		regService.createEntry(sessionProviderService.getSessionProvider(null), 
				groupPath, 
				RegistryEntry.parse(NAV_XML.getBytes()));
		
		RegistryEntry entry = regService.getEntry(sessionProviderService.getSessionProvider(null),
				groupPath + "/node-navigation");

		assertEquals("node-navigation",entry.getName());

  }  


  public void testRegisterFromDOM() throws Exception {
  	
    RegistryService regService = (RegistryService) container
    .getComponentInstanceOfType(RegistryService.class);

    String groupPath = RegistryService.EXO_USERS+"/testRegisterFromDOM";
    
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance() ;
    DocumentBuilder db = dbf.newDocumentBuilder() ;
    Document document = db.parse(new ByteArrayInputStream(NAV_XML.getBytes())) ;
    
		regService.createEntry(sessionProviderService.getSessionProvider(null), 
				groupPath, 
				new RegistryEntry(document));
		
		RegistryEntry entry = regService.getEntry(sessionProviderService.getSessionProvider(null),
				groupPath + "/node-navigation");

		assertEquals("node-navigation",entry.getName());

  }  

  
  public void testLocation() throws Exception {
    RegistryService regService = (RegistryService) container
    .getComponentInstanceOfType(RegistryService.class);
    
    regService.addRegistryLocation("wrong", "wrong");
  }
  
}
