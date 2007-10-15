/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.api.nodetypes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;

/**
 * Created by The eXo Platform SARL
 * Author : Alex Reshetnyak
 *          alex.reshetnyak@exoplatform.com.ua
 *          reshetnyak.alex@gmail.com		
 * 13.03.2007 18:00:03 
 * @version $Id: TestAutoCreatedProperty.java 13.03.2007 18:00:03 rainfox 
 */
public class TestAutoCreatedProperty  extends JcrImplBaseTest{
  
  private NodeTypeManagerImpl ntManager = null;
  
  public void setUp() throws Exception {
    super.setUp();
    
    byte[] xmlData = readXmlContent("/org/exoplatform/services/jcr/api/nodetypes/nodetypes-api-test.xml");
    ByteArrayInputStream xmlInput = new ByteArrayInputStream(xmlData);
    ntManager = (NodeTypeManagerImpl) session.getWorkspace().getNodeTypeManager();
    ntManager.registerNodeTypes(xmlInput, 0);
    assertNotNull(ntManager.getNodeType("exo:autoCreate"));
    assertNotNull(ntManager.getNodeType("exo:refRoot"));
    assertNotNull(ntManager.getNodeType("exo:autoCreate2"));
  }
  
  public void testAutoCreated() throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException{
    Node autoCreated = root.addNode("NODE","exo:autoCreate");
    autoCreated.setProperty("jcr:data", "123123123");
    session.save();
    
    Node dest = root.getNode("NODE");
    
    String prop = null;
    
    try {
      prop = dest.getProperty("jcr:autoCreateProperty").getString();
      fail("Error: 'jcr:autoCreateProperty' ...");
    } catch (PathNotFoundException e) {
      //ok
      assertNull(prop);
    }
    
    String data = dest.getProperty("jcr:data").getString();
    assertEquals(data, "123123123");
  }
  
  public void testAutoCreated2() throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException{
    Node autoCreated = root.addNode("NODE2","exo:autoCreate2");
    autoCreated.setProperty("jcr:data", "123123123");
    session.save();
    
    Node dest = root.getNode("NODE2");
    
    String prop = null;
    
    try {
      prop = dest.getProperty("jcr:autoCreateProperty").getString();
      fail("Error: 'jcr:autoCreateProperty2' ...");
    } catch (PathNotFoundException e) {
      //ok
      assertNull(prop);
    }
    
    String data = dest.getProperty("jcr:data").getString();
    assertEquals(data, "123123123");
  }
  
  private byte[] readXmlContent(String fileName) {
    try {
      InputStream is = TestValueConstraints.class.getResourceAsStream(fileName);
      ByteArrayOutputStream output = new ByteArrayOutputStream();
        int r = is.available();
      byte[] bs = new byte[r];
      while (r > 0) {
        r = is.read(bs);
        if (r > 0) {
          output.write(bs, 0, r);
        }
        r = is.available();
      }
      is.close();
      return output.toByteArray();
    } catch (Exception e) {
      log.error("Error read file '" + fileName + "' with NodeTypes. Error:" + e);
      return null;
    }
  }
 
}
