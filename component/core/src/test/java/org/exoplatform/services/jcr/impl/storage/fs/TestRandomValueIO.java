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
import org.exoplatform.services.jcr.core.value.ExtendedBinaryValue;
import org.exoplatform.services.jcr.impl.core.value.BinaryValue;

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

  public void testNewProperty() throws Exception {
   
    // create property
    String pname = "file@" + testFile.getName();
    Property p = testRoot.setProperty(pname, new FileInputStream(testFile));
    
    ExtendedBinaryValue exv = (ExtendedBinaryValue) p.getValue();
    
    String update1String = "update#1";
    
    long pos = 1024 * 1024;
    
    // update
    exv.update(new ByteArrayInputStream(update1String.getBytes()), 
        update1String.length(), pos);
    
    // transient, before the save
    try {
      
      // the value obtained by getXXX must be same as on setProperty()
      compareStream(new FileInputStream(testFile), testRoot.getProperty(pname).getStream()); 
      
      // apply edited ExtendetValue to the Property
      p.setValue(exv);
      
      compareStream(
          new ByteArrayInputStream(update1String.getBytes()), 
          testRoot.getProperty(pname).getStream(), 0, pos, update1String.length());
      
      testRoot.save();
      
      // persisted, after the save
      compareStream(
          new ByteArrayInputStream(update1String.getBytes()), 
          testRoot.getProperty(pname).getStream(), 0, pos, update1String.length());
      
    } catch(CompareStreamException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }
  
  public void testExistedProperty() throws Exception {
    
    // create property
    String pname = "file@" + testFile.getName();
    Property p = testRoot.setProperty(pname, new FileInputStream(testFile));
    
    testRoot.save();
    
    ExtendedBinaryValue exv = (ExtendedBinaryValue) p.getValue();
    
    String update1String = "update#1";
    
    long pos = 1024 * 1024;
    
    // update
    exv.update(new ByteArrayInputStream(update1String.getBytes()), 
        update1String.length(), pos);
    
    // transient, before the save
    try {
      
      // the value obtained by getXXX must be same as on setProperty()
      compareStream(new FileInputStream(testFile), testRoot.getProperty(pname).getStream()); 
      
      // apply edited ExtendetValue to the Property
      p.setValue(exv);
      
      compareStream(
          new ByteArrayInputStream(update1String.getBytes()), 
          testRoot.getProperty(pname).getStream(), 0, pos, update1String.length());
      
      testRoot.save();
      
      // persisted, after the save
      compareStream(
          new ByteArrayInputStream(update1String.getBytes()), 
          testRoot.getProperty(pname).getStream(), 0, pos, update1String.length());
      
    } catch(CompareStreamException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }  
  
  public void testUpdateProperty_SameValueObject() throws Exception {
    
    // create property
    String pname = "file@" + testFile.getName();
    Property p = testRoot.setProperty(pname, new FileInputStream(testFile));
    
    ExtendedBinaryValue exv = (ExtendedBinaryValue) p.getValue();
    
    String update1String = "update#1";
    
    long pos1 = 1024 * 1024;
    
    // update 1
    exv.update(new ByteArrayInputStream(update1String.getBytes()), 
        update1String.length(), pos1);
    
    // apply to the Property and save
    p.setValue(exv);
    testRoot.save();
    
    String update2String = "UPDATE#2";
    
    long pos2 = (1024 * 1024) + 5;
    
    // update 2
    exv.update(new ByteArrayInputStream(update2String.getBytes()), 
        update2String.length(), pos2);
    // apply to the Property
    p.setValue(exv);
    
    // check the content from the first updated char to the last char of second update 
    
    String updateString = update1String.substring(0, 5) + update2String;
    
    // transient, before the save
    try {
      compareStream(
          new ByteArrayInputStream(updateString.getBytes()), 
          testRoot.getProperty(pname).getStream(), 0, pos1, updateString.length());
      
      testRoot.save();
      
      // persisted, after the save
      compareStream(
          new ByteArrayInputStream(updateString.getBytes()), 
          testRoot.getProperty(pname).getStream(), 0, pos1, updateString.length());
    
    } catch(CompareStreamException e) {
      fail(e.getMessage());
    } 
  }
  
  public void testRollbackProperty() throws Exception {
    
    // create property
    String pname = "file@" + testFile.getName();
    Property p = testRoot.setProperty(pname, new FileInputStream(testFile));
    
    ExtendedBinaryValue exv = (ExtendedBinaryValue) p.getValue();
    
    String update1String = "update#1";
    
    long pos = 1024 * 1024;
    
    // update
    exv.update(new ByteArrayInputStream(update1String.getBytes()), 
        update1String.length(), pos);
    // apply to the Property and save
    p.setValue(exv);
    testRoot.save();
    
    // test if the rollbacked value isn't saved
    
    String update2String = "UPDATE#2";
    
    long pos2 = (1024 * 1024) + 5;
    
    // update 2
    exv.update(new ByteArrayInputStream(update2String.getBytes()), 
        update2String.length(), pos2);
    // apply to the Property
    p.setValue(exv);
    
    testRoot.refresh(false); // rollback
    
    // check the content
    // transient, before the save
    
    try {
      compareStream(
          new ByteArrayInputStream(update1String.getBytes()), 
          testRoot.getProperty(pname).getStream(), 0, pos, update1String.length());
      
      testRoot.save();
      
      // persisted, after the save
      compareStream(
          new ByteArrayInputStream(update1String.getBytes()), 
          testRoot.getProperty(pname).getStream(), 0, pos, update1String.length());
      
    } catch(CompareStreamException e) {
      fail(e.getMessage());
    }
  }
}
