/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import junit.framework.TestCase;
import org.exoplatform.services.rest.data.XMLValidator;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;

import org.exoplatform.container.StandaloneContainer;


public class XMLValidatorTest extends TestCase {

  private StandaloneContainer container;
  
  public void setUp() throws Exception {
    StandaloneContainer.setConfigurationPath("src/test/java/conf/standalone/test-configuration.xml");
    container = StandaloneContainer.getInstance();
  }
  
  public void testValidateXMLValidator() throws Exception {
  	XMLValidator validator = (XMLValidator)container.getComponentInstanceOfType(XMLValidator.class);
  	InputStream in = new FileInputStream(new File("src/test/java/conf/standalone/test.xml"));
  	assertEquals("schema1", validator.validate(in));
  }
  
}
