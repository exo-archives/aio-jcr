/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.rest.data.StringEntityTransformer;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class TransformerTest extends TestCase {

  private StandaloneContainer container;
  
  public void setUp() throws Exception {
    StandaloneContainer.setConfigurationPath("src/test/java/conf/standalone/test-configuration.xml");
    container = StandaloneContainer.getInstance();
  }

  public void testTransformerBind() {
    TransformerBinder tbinder = 
      (TransformerBinder)container.getComponentInstanceOfType(TransformerBinder.class);
    assertNotNull(tbinder);
    StringEntityTransformer transf = new StringEntityTransformer();
    tbinder.bind(transf);
    assertEquals(1, tbinder.transformersNumber());
    tbinder.unbind(transf);
    assertEquals(0, tbinder.transformersNumber());
  }
  
  public void testStringEntityTransformer() throws Exception {
    StringEntityTransformer transf = new StringEntityTransformer();
    assertTrue(transf.support(String.class));
    ByteArrayInputStream in = new ByteArrayInputStream("hello".getBytes());
    assertEquals("hello", transf.readFrom(in));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    transf.writeTo("hello", out);
    assertEquals("hello", out.toString());
  }
}
