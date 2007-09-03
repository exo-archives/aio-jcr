/**                                                                       *
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.  *
 */
package org.exoplatform.services.jcr.impl.storage.fs;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.core.value.ExtendedBinaryValue;

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
      testFile = createBLOBTempFile(this.getClass().getSimpleName() + "_",
          2 * 1024); // 2M
      testFile.deleteOnExit();
    }
  }

  @Override
  protected void tearDown() throws Exception {
    testRoot.remove();
    session.save();

    super.tearDown();
  }

  public void testNew() throws Exception {

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
      compareStream(new FileInputStream(testFile), testRoot.getProperty(pname)
          .getStream());

      // apply edited ExtendetValue to the Property
      p.setValue(exv);

      compareStream(new ByteArrayInputStream(update1String.getBytes()),
          testRoot.getProperty(pname).getStream(), 0, pos, update1String
              .length());

      testRoot.save();

      // persisted, after the save
      compareStream(new ByteArrayInputStream(update1String.getBytes()),
          testRoot.getProperty(pname).getStream(), 0, pos, update1String
              .length());

    } catch (CompareStreamException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  public void testExisted() throws Exception {

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
      compareStream(new FileInputStream(testFile), testRoot.getProperty(pname)
          .getStream());

      // apply edited ExtendetValue to the Property
      p.setValue(exv);

      compareStream(new ByteArrayInputStream(update1String.getBytes()),
          testRoot.getProperty(pname).getStream(), 0, pos, update1String
              .length());

      testRoot.save();

      // persisted, after the save
      compareStream(new ByteArrayInputStream(update1String.getBytes()),
          testRoot.getProperty(pname).getStream(), 0, pos, update1String
              .length());

    } catch (CompareStreamException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  public void testUpdate_SameObject() throws Exception {

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

    // check the content from the first updated char to the last char of second
    // update

    String updateString = update1String.substring(0, 5) + update2String;

    // transient, before the save
    try {
      compareStream(new ByteArrayInputStream(updateString.getBytes()), testRoot
          .getProperty(pname).getStream(), 0, pos1, updateString.length());

      testRoot.save();

      // persisted, after the save
      compareStream(new ByteArrayInputStream(updateString.getBytes()), testRoot
          .getProperty(pname).getStream(), 0, pos1, updateString.length());

    } catch (CompareStreamException e) {
      fail(e.getMessage());
    }
  }

  public void _testUpdate_SameObjectAcrossSessions() throws Exception {

    // create property
    String pname = "file@" + testFile.getName();
    Property p = testRoot.setProperty(pname, new FileInputStream(testFile));

    ExtendedBinaryValue exv = (ExtendedBinaryValue) p.getValue();

    String update1String = "update#1";

    long pos1 = 1024 * 1024;

    // update 1
    exv.update(new ByteArrayInputStream(update1String.getBytes()),
        update1String.length(), pos1);

    // apply to the Property in another session and save
    Session s1 = repository.login(credentials);

    try {
      Node troot = s1.getRootNode().getNode(testRoot.getName());

      byte[] s1Content = "__string_stream__".getBytes();
      p = troot.setProperty(pname, new ByteArrayInputStream(s1Content));

      BufferedInputStream exvStream = new BufferedInputStream(exv.getStream());
      // fill the buffer from exv
      exvStream.mark((int) exv.getLength() + 1);
      while ((exvStream.read(new byte[2048])) >= 0) {
      }
      exvStream.reset();

      p.setValue(exv);
      troot.save();

      // check if we has the exv value
      compareStream(exvStream, troot.getProperty(pname).getStream(), 0, pos1,
          update1String.length());

      String update2String = "UPDATE#2";

      long pos2 = (1024 * 1024) + 5;

      // update 2
      exv.update(new ByteArrayInputStream(update2String.getBytes()),
          update2String.length(), pos2);
      // apply to the Property
      p.setValue(exv);

      // check the content from the first updated char to the last char of
      // second update

      String updateString = update1String.substring(0, 5) + update2String;

      // transient, before the save
      compareStream(new ByteArrayInputStream(updateString.getBytes()), troot
          .getProperty(pname).getStream(), 0, pos1, updateString.length());

      troot.save();

      // persisted, after the save
      compareStream(new ByteArrayInputStream(updateString.getBytes()), troot
          .getProperty(pname).getStream(), 0, pos1, updateString.length());

      // from first session
      compareStream(new ByteArrayInputStream(updateString.getBytes()), testRoot
          .getProperty(pname).getStream(), 0, pos1, updateString.length());

    } catch (CompareStreamException e) {
      e.printStackTrace();
      fail(e.getMessage());
    } finally {
      s1.logout();
    }
  }

  public void testRollback() throws Exception {

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
      compareStream(new ByteArrayInputStream(update1String.getBytes()),
          testRoot.getProperty(pname).getStream(), 0, pos, update1String
              .length());

      testRoot.save();

      // persisted, after the save
      compareStream(new ByteArrayInputStream(update1String.getBytes()),
          testRoot.getProperty(pname).getStream(), 0, pos, update1String
              .length());

    } catch (CompareStreamException e) {
      fail(e.getMessage());
    }
  }

  public void testUpdate_AddLength() throws Exception {

    // create property
    String pname = "file@" + testFile.getName();
    Property p = testRoot.setProperty(pname, new FileInputStream(testFile));

    ExtendedBinaryValue exv = (ExtendedBinaryValue) p.getValue();
    String update1String = "update#1";
    long pos = 3 * 1024 * 1024;

    // update
    try {

      exv.setLength(pos + 1);
      assertEquals("Value data length must be increased ", pos + 1, exv
          .getLength());

      exv.update(new ByteArrayInputStream(update1String.getBytes()),
          update1String.length(), pos);

      // test the value for correct stream
      BufferedInputStream vstream = new BufferedInputStream(exv.getStream());
      vstream.mark((int) exv.getLength() + 1);

      // first 2M of stream data must be same as on setProperty()
      compareStream(new FileInputStream(testFile), vstream, 0, 0, testFile
          .length());

      vstream.reset();
      compareStream(new ByteArrayInputStream(update1String.getBytes()),
          vstream, 0, pos, update1String.length());

      // apply to the Property and save
      p.setValue(exv);
      testRoot.save();

      // test after save
      // first 2M of stream data must be same as on setProperty()
      compareStream(new FileInputStream(testFile), testRoot.getProperty(pname)
          .getStream(), 0, 0, testFile.length());

      compareStream(new ByteArrayInputStream(update1String.getBytes()),
          testRoot.getProperty(pname).getStream(), 0, pos, update1String
              .length());
    } catch (CompareStreamException e) {
      fail(e.getMessage());
    }
  }

  /**
   * Here is test of small value (byte[]) increase truncate case.
   * 
   * @throws Exception
   */
  public void _testTruncateIncreaseSmall() throws Exception {
    // create property
    String pname = "file@" + testFile.getName();
    Property p = testRoot.setProperty(pname, new ByteArrayInputStream(
        new byte[] { 0 }));

    ExtendedBinaryValue exv = (ExtendedBinaryValue) p.getValue();

    long size = 40;

    exv.setLength(size);

    // apply to the Property and save
    p.setValue(exv);
    testRoot.save();

    assertEquals(size, p.getLength());

  }

  public void _testTruncateIncreaseBig() throws Exception {
    // create property
    String pname = "file@" + testFile.getName();
    Property p = testRoot.setProperty(pname, new FileInputStream(testFile));

    ExtendedBinaryValue exv = (ExtendedBinaryValue) p.getValue();

    long size = p.getLength() + 200;

    exv.setLength(size);

    // apply to the Property and save
    p.setValue(exv);
    testRoot.save();

    assertEquals(size, p.getLength());

  }

  public void testAddLength_BigValue() throws Exception {

    // create property
    String pname = "file@" + testFile.getName();
    Property p = testRoot.setProperty(pname, new FileInputStream(testFile));

    ExtendedBinaryValue exv = (ExtendedBinaryValue) p.getValue();
    long pos = 3 * 1024 * 1024;

    exv.setLength(pos);

    assertEquals("Value data length must be increased ", pos, exv.getLength());

    // apply to the Property and save
    p.setValue(exv);
    testRoot.save();

    ExtendedBinaryValue newexv = (ExtendedBinaryValue) testRoot.getProperty(
        pname).getValue();
    assertEquals("Value data length must be increased ", pos, newexv
        .getLength());
  }

  public void testTruncateLength_BigValue() throws Exception {

    // create property
    String pname = "file@" + testFile.getName();
    Property p = testRoot.setProperty(pname, new FileInputStream(testFile));

    ExtendedBinaryValue exv = (ExtendedBinaryValue) p.getValue();
    long pos = 1024 * 1024;

    exv.setLength(pos);

    assertEquals("Value data length must be decreased ", pos, exv.getLength());

    // apply to the Property and save
    p.setValue(exv);
    testRoot.save();

    ExtendedBinaryValue newexv = (ExtendedBinaryValue) testRoot.getProperty(
        pname).getValue();
    assertEquals("Value data length must be decreased ", pos, newexv.getLength());
  }

  public void testAddLength_SmallValue() throws Exception {

    // create property
    String pname = "file@" + testFile.getName();
    Property p = testRoot.setProperty(pname, new ByteArrayInputStream(
        "short message".getBytes()));

    ExtendedBinaryValue exv = (ExtendedBinaryValue) p.getValue();
    long pos = exv.getLength() + 20;

    exv.setLength(pos);

    assertEquals("Value data length must be increased ", pos, exv.getLength());

    // apply to the Property and save
    p.setValue(exv);
    testRoot.save();

    ExtendedBinaryValue newexv = (ExtendedBinaryValue) testRoot.getProperty(
        pname).getValue();
    assertEquals("Value data length must be increased ", pos, newexv.getLength());
  }

  public void testAddLength_SmallToBigValue() throws Exception {

    // create property
    String pname = "file@" + testFile.getName();
    Property p = testRoot.setProperty(pname, new ByteArrayInputStream(
        "short message".getBytes()));

    ExtendedBinaryValue exv = (ExtendedBinaryValue) p.getValue();
    long pos = exv.getLength() + 1024 * 1024 * 25;
    
    long fmem = Runtime.getRuntime().freeMemory();

    exv.setLength(pos);

    long fmemAfter = Runtime.getRuntime().freeMemory();

    if ((fmemAfter - fmem) >= pos)
      log.warn("Free memory must not be increased on value of the new Value size but does. Was " + fmem + " current " + fmemAfter);
    
    assertEquals("Value data length must be increased ", pos, exv.getLength());

    // apply to the Property and save
    p.setValue(exv);
    testRoot.save();

    ExtendedBinaryValue newexv = (ExtendedBinaryValue) testRoot.getProperty(
        pname).getValue();
    assertEquals("Value data length must be increased ", pos, newexv.getLength());
  }
  
  public void testAddLength_SmallToBigValue_Persistent() throws Exception {

    // create property
    String pname = "file@" + testFile.getName();
    testRoot.setProperty(pname, new ByteArrayInputStream(new byte[] {}));

    testRoot.save();
    
    // change after save
    ExtendedBinaryValue newexv = (ExtendedBinaryValue) testRoot.getProperty(pname).getValue();
    long pos = newexv.getLength() + 1024 * 1024 * 25;
    
    long tmem = Runtime.getRuntime().totalMemory();
    
    newexv.setLength(pos);
    
    // apply to the Property and save
    testRoot.getProperty(pname).setValue(newexv);
    
    long tmemAfter = Runtime.getRuntime().totalMemory();

    if ((tmemAfter - tmem) >= pos)
      log.warn("JVM total memory should not be increased on size of the new Value but does. Was " + tmem + " current " + tmemAfter);
    
    assertEquals("Value data length must be increased ", pos, newexv.getLength());

    newexv = (ExtendedBinaryValue) testRoot.getProperty(pname).getValue();
    assertEquals("Value data length must be increased ", pos, newexv.getLength());
    
    // save new size
    testRoot.save();
    newexv = (ExtendedBinaryValue) testRoot.getProperty(pname).getValue();
    assertEquals("Value data length must be increased ", pos, newexv.getLength());
  }

  public void testTruncateLength_BigToSmallValue() throws Exception {

    // create property
    String pname = "file@" + testFile.getName();
    Property p = testRoot.setProperty(pname, new FileInputStream(testFile));

    ExtendedBinaryValue exv = (ExtendedBinaryValue) p.getValue();
    long pos = exv.getLength() - (testFile.length() - 20);

    exv.setLength(pos);

    assertEquals("Value data length must be decreased ", pos, exv.getLength());

    // apply to the Property and save
    p.setValue(exv);
    testRoot.save();

    ExtendedBinaryValue newexv = (ExtendedBinaryValue) testRoot.getProperty(
        pname).getValue();
    assertEquals("Value data length must be decreased ", pos, newexv.getLength());
  }

  public void testTruncateLength_SmallValue() throws Exception {

    // create property
    String pname = "file@" + testFile.getName();
    Property p = testRoot.setProperty(pname, new ByteArrayInputStream(
        "short message".getBytes()));

    ExtendedBinaryValue exv = (ExtendedBinaryValue) p.getValue();
    long pos = exv.getLength() - 5;

    exv.setLength(pos);

    assertEquals("Value data length must be decreased ", pos, exv.getLength());

    // apply to the Property and save
    p.setValue(exv);
    testRoot.save();

    ExtendedBinaryValue newexv = (ExtendedBinaryValue) testRoot.getProperty(
        pname).getValue();
    assertEquals("Value data length must be decreased ", pos, newexv.getLength());
  }

  public void _testSetLengthLargeProp() throws Exception {
    // create property
    String pname = "file@" + testFile.getName();
    Property p = testRoot.setProperty(pname, new ByteArrayInputStream(
        new byte[] {}));

    ExtendedBinaryValue exv = (ExtendedBinaryValue) p.getValue();
    long pos = 1024 * 1024 * 100;

    exv.setLength(pos);

    assertEquals("Value data length must be increased ", pos, exv.getLength());

    // apply to the Property and save
    p.setValue(exv);
    testRoot.save();

    ExtendedBinaryValue newexv = (ExtendedBinaryValue) testRoot.getProperty(
        pname).getValue();
    assertEquals("Value data length must be increased ", pos, newexv.getLength());

  }
  
  public void testAddLength_SmallToBigValue_NTFile() throws Exception {
    // create property

    String type = "nt:file";
    String name = "foo.doc";

    Node createdNodeRef = testRoot.addNode(name, type);
    Node dataNode = createdNodeRef.addNode("jcr:content", "nt:resource");

    MimeTypeResolver mimetypeResolver = new MimeTypeResolver();
    mimetypeResolver.setDefaultMimeType("application/zip");
    String mimeType = mimetypeResolver.getMimeType(name);

    dataNode.setProperty("jcr:mimeType", mimeType);
    dataNode.setProperty("jcr:lastModified", Calendar.getInstance());
    dataNode.setProperty("jcr:data", new ByteArrayInputStream(new byte[] {}));

    testRoot.save();

    ExtendedBinaryValue exv = (ExtendedBinaryValue) testRoot.getNode(name)
        .getNode("jcr:content").getProperty("jcr:data").getValue();
    long pos = 1024 * 1024 * 25;

    exv.setLength(pos);

    assertEquals("Value data length must be increased ", pos, exv.getLength());

    // apply to the Property and save
    testRoot.getNode(name).getNode("jcr:content").getProperty("jcr:data").setValue(exv);
    testRoot.save();
  }

 
}
