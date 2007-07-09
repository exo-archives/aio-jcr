/**
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail.
 **/

package org.exoplatform.services.jcr.impl.storage;


import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.BaseStandaloneTest;


/**
 * Created by The eXo Platform SARL        .
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: CacheTest.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class CacheTest extends BaseStandaloneTest {

  protected String getRepositoryName() {
    return "db1";
  }

  public void testIfCacheIsNotSharedBetweenWorkspaces() throws Exception {
  	
  	String[] wsNames = repository.getWorkspaceNames();
  	if(wsNames.length < 2)
  		fail("Too few number of ws for test should be > 1");
  			
  	Session s1 = repository.getSystemSession(wsNames[0]);
  	Session s2 = repository.getSystemSession(wsNames[1]);
  	
  	s1.getRootNode().addNode("test1","nt:unstructured");
  	
  	s1.save();
  	
  	
  	try {
		s2.getRootNode().getNode("test1");
		fail("PathNotFoundException should have been thrown");
	} catch (PathNotFoundException e) {
	} 
 
  	
  }

}
