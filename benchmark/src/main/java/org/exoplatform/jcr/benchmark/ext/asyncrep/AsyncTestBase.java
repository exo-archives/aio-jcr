/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.jcr.benchmark.ext.asyncrep;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

import com.sun.japex.TestCase;


/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 
 *
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a> 
 * @version $Id: AsynctestBase.java 111 2008-11-11 11:11:11Z serg $
 */
public abstract class AsyncTestBase {
  protected static Log log = ExoLogger.getLogger("jcr.benchmark.async");

  public final int COUNT_I = 50;

  public final int COUNT_J = 5;

  public File              content;

  public int       sc;

  public int       fc;
  
  /**
   * @param tc
   * @param context
   * @throws Exception
   */
  public void doPrepare(final TestCase tc, AsyncTestContext context) throws Exception {
    String size = tc.getParam("ext.fileSizeInKb");

    int fileSize = (size != null) ? Integer.parseInt(size) : 100*1024;

    content = this.createBLOBfile(fileSize);

    String si = tc.getParam("ext.savesCount");
    sc = (si != null) ? Integer.parseInt(si) : COUNT_I;
    
    String sj = tc.getParam("ext.filePerSaveCount");
    fc = (sj != null) ? Integer.parseInt(sj) : COUNT_J;
  }

  /**
   * @param tc
   * @param context
   * @throws Exception
   */
  public void doFinish(final TestCase tc, AsyncTestContext context) throws Exception {
    content.delete();
  }

  /**
   * @param tc
   * @param context
   * @throws Exception
   */
  public abstract void doRun(final TestCase tc, AsyncTestContext context) throws Exception;

  public static File createBLOBfile(int sizeKb) throws IOException{
    // create test file
    byte[] data = new byte[1024]; // 1Kb

    File testFile = File.createTempFile("asyncBenchmark", ".tmp");
    FileOutputStream tempOut = new FileOutputStream(testFile);
    Random random = new Random();

    for (int i = 0; i < sizeKb; i++) {
      random.nextBytes(data);
      tempOut.write(data);
    }
    tempOut.close();
    testFile.deleteOnExit(); // delete on test exit
    log.info("Temp file created: " + testFile.getAbsolutePath() + " size: " + testFile.length());
    return testFile;
  }
  
}
