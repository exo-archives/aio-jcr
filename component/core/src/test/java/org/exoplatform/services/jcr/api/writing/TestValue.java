/*
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail. 
 */

package org.exoplatform.services.jcr.api.writing;


import java.io.ByteArrayInputStream;
import java.util.GregorianCalendar;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.jcr.impl.core.SessionImpl;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: TestValue.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class TestValue extends JcrAPIBaseTest{

  public void initRepository() throws RepositoryException {
    Node root = session.getRootNode();
    Node propDef = root.addNode("propertyDefNode", "nt:unstructured");
    // Unknown Property Type. Should set something!
    propDef.setProperty("jcr:defaultValue", "testString");

    root.addNode("childNodeDefNode","nt:unstructured");
    session.save();
  }

  public void tearDown() throws Exception {
    session = (SessionImpl)repository.login(credentials, WORKSPACE);
    Node root = session.getRootNode();
    root.getNode("propertyDefNode").remove();
    root.getNode("childNodeDefNode").remove();
    session.save();
    
    super.tearDown();
  }

  public void testSetValue() throws RepositoryException {
    Node root = session.getRootNode();
    Property property = root.getNode("propertyDefNode").getProperty("jcr:defaultValue");

    property.setValue("haha");
    property.setValue("default");
    property.setValue(new ByteArrayInputStream(new String("default").getBytes()));
    property.setValue(true);
    property.setValue(new GregorianCalendar());
    property.setValue(20D);
    property.setValue(20L); 
    assertFalse("Property '" + property.getPath() + "' must be single-valued", property.getDefinition().isMultiple());
  }
  
  public void testSetMuliValue() throws RepositoryException {
    Node root = session.getRootNode();
    Node propertyDefNode = root.getNode("propertyDefNode");
    Property singleValuedProperty = propertyDefNode.getProperty("jcr:defaultValue");
    // remove initially created property
    singleValuedProperty.setValue((String) null);
    propertyDefNode.save();
    
    Property multiValuedProperty = propertyDefNode.setProperty("jcr:defaultValue", new String[] {null, null});
    
    Value[] values = {session.getValueFactory().createValue("not"), session.getValueFactory().createValue("in")};
    multiValuedProperty.setValue(values);
    
    assertTrue("Property '" + multiValuedProperty.getPath() + "' must be multi-valued", 
        multiValuedProperty.getDefinition().isMultiple());
  }

}
