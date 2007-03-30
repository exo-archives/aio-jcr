/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.name;

import junit.framework.TestCase;

import org.exoplatform.services.jcr.datamodel.InternalQPath;
import org.exoplatform.services.jcr.impl.core.JCRName;
import org.exoplatform.services.jcr.impl.core.JCRPath;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.NamespaceRegistryImpl;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: TestJCRPath.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class TestJCRPath extends TestCase {

	private LocationFactory factory;
	private NamespaceRegistryImpl namespaceRegistry;
	
    public void setUp() throws Exception {
  	  if(factory == null) {
  	  	 namespaceRegistry = new NamespaceRegistryImpl();
  	  	 factory = new LocationFactory(namespaceRegistry);
  	  } 
    }
    
    public void testCreateRoot() throws Exception {
    	JCRPath path = factory.createRootLocation();
    	assertEquals("/", path.getAsString(false));
    	assertEquals("/", path.getAsString(true));
    	assertEquals(1, path.getIndex());
    	assertEquals("", path.getName().getName());
    }

    public void testCreateName() throws Exception {
    	JCRName name = factory.parseJCRName("jcr:test");
    	assertEquals("jcr:test", name.getAsString());
    	assertEquals("test", name.getName());
    	assertEquals(namespaceRegistry.getNamespaceURIByPrefix("jcr"), 
    			name.getNamespace());
    	assertEquals("jcr", name.getPrefix());
    	assertEquals("["+namespaceRegistry.getNamespaceURIByPrefix("jcr")+"]test", 
    			name.getInternalName().getAsString());
    	
    	JCRName name1 = factory.createJCRName(name.getInternalName());
    	assertTrue(name.equals(name1));
    }
    
    public void testParsePath() throws Exception {
    	
    	JCRPath path = factory.parseAbsPath("/jcr:node/node1[2]/exo:node2");
    	assertEquals("node2", path.getName().getName());
    	assertEquals(1, path.getIndex());
    	assertEquals("node2", path.getInternalPath().getName().getName());
    	assertEquals(3, path.getDepth());
    	assertEquals("/jcr:node/node1[2]/exo:node2", path.getAsString(false));
    	assertEquals("/jcr:node[1]/node1[2]/exo:node2[1]", path.getAsString(true));
    	
    	// with index
    	assertTrue(path.equals(factory.parseAbsPath("/jcr:node/node1[2]/exo:node2[1]")));
    	assertFalse(path.equals(factory.parseAbsPath("/jcr:node/node1[1]/exo:node2[1]")));

    	JCRPath path1 = factory.parseAbsPath("/jcr:node[3]");
    	assertEquals(3, path1.getIndex());
    	
    }

    public void testCreatePath() throws Exception {
    	JCRPath path = factory.parseAbsPath("/jcr:node/node1[2]/exo:node2");
    	JCRPath parent = path.makeParentPath();
    	assertEquals("/jcr:node/node1[2]", parent.getAsString(false));
    	assertTrue(path.isDescendantOf(parent, true));
    	assertTrue(path.isDescendantOf(parent.makeParentPath(), false));
    	
    	assertEquals("/jcr:node/node1[2]/exo:node2", 
    			factory.createJCRPath(parent, "exo:node2").getAsString(false));
    	assertEquals("/jcr:node/node1[2]/exo:node2/node3", 
    			factory.createJCRPath(parent, "exo:node2/node3").getAsString(false));

    	assertTrue(path.equals(factory.createJCRPath(parent, "exo:node2")));
    	InternalQPath qpath = path.getInternalPath();
    	assertTrue(path.equals(factory.createJCRPath(qpath)));
    	
    	JCRPath sibs = factory.parseAbsPath("/jcr:node/node1[2]/exo:node2[2]");
    	assertTrue(path.isSameNameSibling(sibs));
    	
    	path = factory.parseAbsPath("/jcr:node/node1[2]/exo:node2");
    	assertEquals("/jcr:node",path.makeAncestorPath(2).getAsString(false));

    }
}
