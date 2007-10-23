/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS. All rights reserved.          *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.util.io;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Created by The eXo Platform SAS
 * Author : Peter Nedonosko
 *          peter.nedonosko@exoplatform.com.ua
 * 05.10.2007  
 *
 * For use in persistent layer, i.e. JDBCStorageConnection (ClenableFileValueData), 
 * will be shared across the Workspace cache with many Sessions . 
 * Swap files creation (like in JDBCStorageConnection.readValueData(String, int, int)) managed by SwapFile.get(File, String) method. 
 * There are no way to get swap file in another way.
 *  
 * A SwapFile extends the SpoolFile. But in runtime all swap files will be stored in global map used for 
 * prevent files rewriting in concurrent environment (case of issue JCR-329). 
 * Till the SwapFile spool operation in progress all other users who will attempt to get the file will wait the 
 * operation completion (java.util.concurrent.CountDownLatch used).
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class SwapFile extends SpoolFile {
  
  protected static Map<String, SwapFile> inShare = new HashMap<String, SwapFile>();  
  
  protected CountDownLatch spoolLatch = null;
  
  private SwapFile(File parent, String child) {
    super(parent, child);
  }
  
  /**
   * Obtain SwapFile by parent file and name.
   * 
   * If the file was swapped before and still in use it will be returned, 
   * i.e. same object of java.io.File will be returned.
   * 
   * if the file swapping (writing) now at this time the caller thread 
   * will wait till the swap process will be finished.    
   * 
   * @param parent - File 
   * @param child - String
   * @return SwapFile
   * @throws IOException
   */
  public static SwapFile get(final File parent, final String child) throws IOException {
    synchronized (inShare) {
      SwapFile newsf = new SwapFile(parent, child);
      String absPath = newsf.getAbsolutePath();
      
      SwapFile swapped = inShare.get(absPath);
      if (swapped != null) {
        CountDownLatch spoolLatch = swapped.spoolLatch;
        if (spoolLatch != null)
          try {
            spoolLatch.await(); // wait till the file will be done
          } catch (final InterruptedException e) {
            // thinking that is ok, i.e. this thread is interrupted 
            throw new IOException("Swap file read error " + swapped.getAbsolutePath() + ". " + e) {
              @Override
              public Throwable getCause() {
                return e;
              }
            };
          }  
        return swapped; 
      }
      
      newsf.spoolLatch = new CountDownLatch(1);
      inShare.put(absPath, newsf);
      return newsf;
    }
  }

  /** 
   * Tell if the file already spooled - ready for use.
   * @return boolean flag
   */
  public boolean isSpooled() {
    return spoolLatch == null;
  }
  
  /**
   * Mark the file ready for read. 
   */
  public void spoolDone() {
    final CountDownLatch sl = this.spoolLatch;
    this.spoolLatch = null;
    sl.countDown();
  }
  
  //------ java.io.File ------
  
  /**
   * Delete file if it was not used by any other thread.  
   */
  @Override
  public boolean delete() {
    synchronized (inShare) {
      if (super.delete()) {
        // remove from shared files list
        inShare.remove(getAbsolutePath());
                
        // make sure that the file doesn't make any other thread await in 'get' method
        // impossible case as 'delete' and 'get' which may waiting for, synchronized by inShare map. 
        //spoolDone();
        
        return true;
      }
      
      return false;
    }
  }
  
  /**
   * Not applicable. Call get(File, String) method instead
   * 
   * @throws IOException
   */
  public static SwapFile createTempFile(String prefix, String suffix,
      File directory) throws IOException {
    throw new IOException("Not applicable. Call get(File, String) method instead");
  }
}
 