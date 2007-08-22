/**                                                                       *
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.  *
 */
package org.exoplatform.services.jcr.impl.storage.fs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.jcr.Node;
import javax.jcr.Property;

import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.core.ExtendedProperty;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class TestRandomValueIO extends JcrImplBaseTest {

  private Node testRoot; 
  
  private File testFile;
  
  @Override
  public void setUp() throws Exception {
    
    super.setUp();
    
    testRoot = root.addNode("binValueTest");
    session.save();
    
    if (testFile == null) {
      testFile = createBLOBTempFile(this.getClass().getSimpleName() + "_", 2 * 1024); // 2M
      testFile.deleteOnExit();
    }
  }

  @Override
  protected void tearDown() throws Exception {
    testRoot.remove();
    session.save();
    
    super.tearDown();
  }

  public void testRandomValueWrite_NewProperty() throws Exception {
   
    // create property
    String pname = "file@" + testFile.getName();
    Property p = testRoot.setProperty(pname, new FileInputStream(testFile));
    
    ExtendedProperty exp = (ExtendedProperty) p;
    
    String update1String = "update#1";
    
    long pos = 1024 * 1024;
    
    // update
    exp.updateValue(0, new ByteArrayInputStream(update1String.getBytes()), 
        update1String.length(), pos);
    
    // transient, before the save
    compareStream(
        new ByteArrayInputStream(update1String.getBytes()), 
        testRoot.getProperty(pname).getStream(), 0, pos, update1String.length());
    
    testRoot.save();
    
    // persisted, after the save
    compareStream(
        new ByteArrayInputStream(update1String.getBytes()), 
        testRoot.getProperty(pname).getStream(), 0, pos, update1String.length());
  }
  
  public void testRandomValueWrite_UpdateProperty() throws Exception {
    
    // create property
    String pname = "file@" + testFile.getName();
    Property p = testRoot.setProperty(pname, new FileInputStream(testFile));
    
    ExtendedProperty exp = (ExtendedProperty) p;
    
    String update1String = "update#1";
    
    long pos1 = 1024 * 1024;
    
    // update 1
    exp.updateValue(0, new ByteArrayInputStream(update1String.getBytes()), 
        update1String.length(), pos1);
    
    testRoot.save();
    
    String update2String = "UPDATE#2";
    
    long pos2 = (1024 * 1024) + 5;
    
    // update 2
    exp.updateValue(0, new ByteArrayInputStream(update2String.getBytes()), 
        update2String.length(), pos2);
    
    // check the content from the first updated char to the last char of second update 
    
    String updateString = update1String.substring(0, 5) + update2String;
    
    // transient, before the save
    compareStream(
        new ByteArrayInputStream(updateString.getBytes()), 
        testRoot.getProperty(pname).getStream(), 0, pos1, updateString.length());
    
    testRoot.save();
    
    // persisted, after the save
    compareStream(
        new ByteArrayInputStream(updateString.getBytes()), 
        testRoot.getProperty(pname).getStream(), 0, pos1, updateString.length());    
  }
}
