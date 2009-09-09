/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.impl.dataflow.persistent;

import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.impl.util.io.SwapFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.jcr.RepositoryException;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date: 29.07.2009
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class TestCleanableFileStreamValueData extends JcrImplBaseTest {

  private static final int             CLEANER_TIMEOUT = 4000;                          // 4sec
  
  private static final int             WAIT_TIMEOUT = 1000;                          // 1sec

  private static final String          FILE_NAME       = "testFileCleaned";

  private File                         parentDir       = new File("./target");

  private File                         testFile        = new File(parentDir, FILE_NAME);

  private FileCleaner                  testCleaner;

  private CleanableFileStreamValueData cleanableValueData;

  private static class TestSwapFile extends SwapFile {
    /**
     * Dummy constructor.
     * 
     * @param parent
     *          Fiel
     * @param child
     *          String
     */
    protected TestSwapFile(File parent, String child) {
      super(parent, child);
    }

    /**
     * Clean inShare for tearDown.
     * 
     */
    static void cleanShare() {
      inShare.clear();
    }
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();

    testCleaner = new FileCleaner(CLEANER_TIMEOUT);

    SwapFile sf = SwapFile.get(parentDir, FILE_NAME);
    FileOutputStream fout = new FileOutputStream(sf);
    fout.write("testFileCleaned".getBytes());
    fout.close();
    sf.spoolDone();

    cleanableValueData = new CleanableFileStreamValueData(sf, 1, testCleaner);
  }

  @Override
  protected void tearDown() throws Exception {
    cleanableValueData = null;

    testCleaner.halt();
    testCleaner = null;

    if (testFile.exists())
      testFile.delete();

    TestSwapFile.cleanShare();

    System.gc();
    Thread.yield();
    System.gc();

    super.tearDown();
  }

  public void testFileCleaned() throws InterruptedException {

    assertTrue(testFile.exists());

    cleanableValueData = null; // CleanableVD dies

    // allows GC to call finalize on vd
    System.gc();
    Thread.sleep(CLEANER_TIMEOUT + WAIT_TIMEOUT);
    Thread.yield();
    System.gc();

    assertFalse(testFile.exists()); // file released and deleted
  }

  public void testSharedFileNotCleaned() throws InterruptedException, IOException {

    assertTrue(testFile.exists());

    System.gc();
    Thread.sleep(CLEANER_TIMEOUT / 2);

    CleanableFileStreamValueData cfvd2 = new CleanableFileStreamValueData(SwapFile.get(parentDir,
                                                                                       FILE_NAME),
                                                                          1,
                                                                          testCleaner);
    assertTrue(testFile.exists());

    cleanableValueData = null; // CleanableVD dies but another instance points swapped file

    // allows GC to call finalize on vd
    System.gc();
    Thread.sleep(CLEANER_TIMEOUT + WAIT_TIMEOUT);
    Thread.yield();
    System.gc();

    assertTrue(testFile.exists());
  }

  public void testTransientFileNotCleaned() throws InterruptedException,
                                           IOException,
                                           RepositoryException {

    assertTrue(testFile.exists());

    System.gc();
    Thread.sleep(CLEANER_TIMEOUT / 2);

    TransientValueData trvd = cleanableValueData.createTransientCopy();
    assertTrue(testFile.exists());

    trvd = null; // TransientVD dies

    // allows GC to call finalize on vd
    System.gc();
    Thread.sleep(CLEANER_TIMEOUT + WAIT_TIMEOUT);
    Thread.yield();
    System.gc();

    assertTrue(testFile.exists()); // but Swapped CleanableVD lives and uses the file
  }

  public void testTransientFileCleaned() throws InterruptedException,
                                        IOException,
                                        RepositoryException {

    assertTrue(testFile.exists());

    System.gc();
    Thread.sleep(CLEANER_TIMEOUT / 2);

    TransientValueData trvd = cleanableValueData.createTransientCopy();
    assertTrue(testFile.exists());

    cleanableValueData = null; // CleanableVD dies but TransientVD still uses swapped file

    // allows GC to work
    System.gc();
    Thread.sleep(CLEANER_TIMEOUT + WAIT_TIMEOUT);
    Thread.yield();
    System.gc();

    assertTrue(testFile.exists());

    trvd = null; // TransientVD dies

    // allows GC to call finalize on vd
    System.gc();
    Thread.sleep(CLEANER_TIMEOUT + WAIT_TIMEOUT);
    Thread.yield();
    System.gc();

    assertFalse(testFile.exists()); // swapped file deleted
  }

  public void testTransientSharedFileCleaned() throws InterruptedException,
                                              IOException,
                                              RepositoryException {

    assertTrue(testFile.exists());

    System.gc();
    Thread.sleep(CLEANER_TIMEOUT / 2);

    // file shared with TransientVD
    TransientValueData trvd = cleanableValueData.createTransientCopy();

    assertTrue(testFile.exists());

    // 1st CleanableVD die
    cleanableValueData = null;

    // allows GC to work
    System.gc();
    Thread.sleep(CLEANER_TIMEOUT + WAIT_TIMEOUT);
    Thread.yield();
    System.gc();

    // file shared with third CleanableVD, i.e. file still exists (aquired by TransientVD)
    CleanableFileStreamValueData cfvd2 = new CleanableFileStreamValueData(SwapFile.get(parentDir,
                                                                                       FILE_NAME),
                                                                          1,
                                                                          testCleaner);
    assertTrue(testFile.exists());

    trvd = null; // TransientVD dies

    // allows GC to work
    System.gc();
    Thread.sleep(CLEANER_TIMEOUT + WAIT_TIMEOUT);
    Thread.yield();
    System.gc();

    assertTrue(testFile.exists()); // still exists, aquired by 2nd CleanableVD

    cfvd2 = null; // 2nd CleanableVD dies

    // allows GC to work
    System.gc();
    Thread.sleep(CLEANER_TIMEOUT + WAIT_TIMEOUT);
    Thread.yield();
    System.gc();

    assertFalse(testFile.exists()); // file should be deleted
  }

}
