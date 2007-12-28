/**
 * Copyright 2001-2007 The eXo Platform SAS All rights reserved.
 * Please look at license.txt in info directory for more license detail.
 **/

package org.exoplatform.services.jcr.impl.name;

import junit.framework.TestCase;

import org.exoplatform.services.jcr.datamodel.IllegalNameException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.Constants;


/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: TestPath.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class TestPath extends TestCase {
	
  public void testParseQName() throws Exception {
  	InternalQName qname = InternalQName.parse(Constants.PRIMARY_TYPE_URI);
  	assertEquals(Constants.PRIMARY_TYPE_URI, qname.getAsString());
  	assertEquals("primaryType", qname.getName());
  	assertEquals("http://www.jcp.org/jcr/1.0", qname.getNamespace());
  	
  	qname = InternalQName.parse("[]test");
  	assertEquals("[]test", qname.getAsString());
  	assertEquals("test", qname.getName());
  	assertEquals("", qname.getNamespace());
  	InternalQName qname1 = InternalQName.parse("[]test");
  	InternalQName qname2 = InternalQName.parse("[]test1");

  	assertTrue(qname.equals(qname1));
  	assertFalse(qname.equals(qname2));
  	
	try {
      qname = InternalQName.parse("test");
      fail("Exception should have been thrown");
    } catch (IllegalNameException e) {
    }
 
  }
  
  public void testParseQPath() throws Exception {
  	QPath path = QPath.parse(Constants.ROOT_URI);
  	assertEquals("", path.getName().getName()); 
  	assertEquals("", path.getName().getNamespace()); 
  	assertEquals(1, path.getIndex());
  	assertEquals(1, path.getDepth() + 1);
  	assertEquals(Constants.ROOT_URI, path.getAsString());
  	
  	path = QPath.parse("[]:1[]root:2[]node1:1[http://www.exoplatform.com/jcr/exo/1.0]node2:3");
  	assertEquals("[]:1[]root:2[]node1:1[http://www.exoplatform.com/jcr/exo/1.0]node2:3", path.getAsString());
  	assertEquals(3, path.getIndex());
  	assertEquals(3, path.getDepth());
  	QPath path1 = QPath.parse("[]:1[]root:2[]node1:1[http://www.exoplatform.com/jcr/exo/1.0]node2:3");
  	assertTrue(path.equals(path1));

  	path = QPath.parse("[]:1[]root:1[]node1:1[http://www.exoplatform.com/jcr/exo/1.0]node2");
  	assertEquals("[]:1[]root:1[]node1:1[http://www.exoplatform.com/jcr/exo/1.0]node2:1", path.getAsString());
  	assertEquals(1, path.getIndex());
  	assertEquals("[http://www.exoplatform.com/jcr/exo/1.0]node2", path.getName().getAsString());
  	assertFalse(path.equals(path1));
 	
  }
  
  public void testMakeQPath() throws Exception {
  	QPath root = QPath.parse(Constants.ROOT_URI);
  	QPath path = QPath.makeChildPath(root, "[http://www.exoplatform.com/jcr/exo/1.0]test");
  	assertEquals(1, path.getIndex());
  	assertEquals("test", path.getName().getName());
  	assertTrue(path.isDescendantOf(root, true));
  	assertEquals(root, path.makeParentPath());
  }

}
