/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.ext.registry;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SARL
 * Author : Tung Pham
 *          thanhtungty@gmail.com
 * Nov 14, 2007  
 */
public class NewRegistryTest extends BaseStandaloneTest {

  private ThreadLocalSessionProviderService sessionProviderService;
  
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
  
  public void testCreateRegistryEntry() throws Exception {
    RegistryService regService = (RegistryService) container
    .getComponentInstanceOfType(RegistryService.class);
    String path = "src/test/java/org/exoplatform/services/jcr/ext/registry/navigation.xml";
    if (!new File(path).exists()){
      path = "component/core/" + path;
    }
    Document doc = getDocument(path) ;
    RegistryEntry newEntry = new RegistryEntry(doc) ;
    regService.createEntry(sessionProviderService.getSessionProvider(null),
        RegistryService.EXO_APPLICATIONS + "/MainPortal" + "/site", newEntry) ;    
  }
  
  private Document getDocument(String fileName) throws Exception {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance() ;
    DocumentBuilder db = dbf.newDocumentBuilder() ;
    Document document = db.parse(new File(fileName)) ;
    return document ;
  }



}
