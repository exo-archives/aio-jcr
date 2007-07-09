/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.storage.fs;

import java.io.File;

import junit.framework.TestCase;

import org.exoplatform.services.jcr.util.SIDGenerator;

/**
 * Created by The eXo Platform SARL
 * 10.07.2007
 *  
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: TestFSReadWrite.java 13869 2007-03-28 13:50:50Z peterit $
 */
public class TestFSReadWrite extends TestCase {

  public static final int FILES_COUNT = 2000;
  
  protected File testRoot = null; 
  
  protected void setUp() throws Exception {
    super.setUp();
    
    testRoot = new File("target/fstest");
    testRoot.mkdirs();
    testRoot.deleteOnExit();
  }

  protected void tearDown() throws Exception {
    testRoot.delete();
    
    super.tearDown();
  }
  
  protected void createOneLevelCase() {
    for (int i=0; i<FILES_COUNT; i++) {
      File f = new File(testRoot.getAbsolutePath() + "/" + SIDGenerator.generate());
    }
  }
  
  public void testOneLevelRead() {
    createOneLevelCase();
    
  }

}
