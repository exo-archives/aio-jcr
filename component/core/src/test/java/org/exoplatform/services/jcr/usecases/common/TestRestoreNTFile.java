/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.usecases.common;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.version.Version;

import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

/**
 * Created by The eXo Platform SARL
 * Author : Hoa Pham
 *					hoa.pham@exoplatform.com
 *          phamvuxuanhoa@yahoo.com
 * Jul 3, 2006
 */
public class TestRestoreNTFile extends BaseUsecasesTest {  
  
  public void testRestoreNTFile() throws Exception {
    Session session = repository.getSystemSession(repository.getSystemWorkspaceName()) ;
    Node ntFile = session.getRootNode().addNode("test","nt:file") ;    
    ntFile.addNode("jcr:content","nt:folder") ;    
    session.save() ;
    
    Node testNTFile = session.getRootNode().getNode("test") ;
    assertTrue("nt:file".equals(testNTFile.getPrimaryNodeType().getName())) ;
    assertTrue(testNTFile.hasProperty("jcr:created")) ;
    assertNotNull(testNTFile.getProperty("jcr:created").getValue()) ;
    ntFile.addMixin("mix:versionable") ;
    session.save() ;
    Version ver1 = ntFile.checkin() ;
    ntFile.checkout() ;
    Version ver2 = ntFile.checkin() ;
    ntFile.checkout() ;
    Version ver3 = ntFile.checkin() ;
    ntFile.checkout() ;
    session.save() ;
    Version baseVersion = ntFile.getBaseVersion() ;
    assertEquals(ver3,baseVersion) ;
    try {
      ntFile.restore(ver2,false) ;
      baseVersion = ntFile.getBaseVersion() ;
      assertEquals(ver2,baseVersion) ;
    }catch (Exception e) {
      log.error("exception when restore version of nt:file", e);
      fail("========> exception when restore version of nt:file:\n\n" + e.getMessage()) ;
    }    
    testNTFile.remove() ;
    session.save() ;
  }
}
