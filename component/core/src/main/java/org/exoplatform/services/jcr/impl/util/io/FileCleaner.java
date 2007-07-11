/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.util.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.impl.proccess.WorkerThread;
import org.exoplatform.services.log.ExoLogger;
/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: FileCleaner.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class FileCleaner extends WorkerThread {
  
  protected static final long DEFAULT_TIMEOUT = 10000;
  
  protected static Log log = ExoLogger.getLogger("jcr.FileCleaner");

  protected List <File> files = new ArrayList <File> ();
  
  public FileCleaner() {
    this(DEFAULT_TIMEOUT);
  }
  
  public FileCleaner(long timeout) {
    super(timeout);
    setName("FileCleaner "+getId());
    setPriority(Thread.MIN_PRIORITY);
    start();
    registerShutdownHook();
    log.info("FileCleaner instantiated name= "+getName()+" timeout= "+timeout);
  }
  
  /**
   * @param file
   */
  public synchronized void addFile(File file) {
    if (file.exists()) { 
      files.add(file);
    }
  }

  public void halt() {
    try {
      callPeriodically();
    } catch (Exception e) {}
    if(files.size() > 0)
      log.warn("There are uncleared files: "+files.size());
      
    super.halt();
  }
  
  /**
   * @see org.exoplatform.services.jcr.impl.proccess.WorkerThread#callPeriodically()
   */
  protected void callPeriodically() throws Exception {
    if (files != null && files.size() > 0) {
      List<File> oldFiles = files;
      files = new ArrayList<File>();
      for (File file : oldFiles) {
        if (file.exists()) {
          String ftype = file.isDirectory() ? "Directory" : "File";
          if(!file.delete()) {
            log.warn("Could not delete " + ftype.toLowerCase() + ". Will try next time: "
              + file.getAbsolutePath());
            files.add(new File(file.getAbsolutePath()));
          } else if (log.isDebugEnabled()) {
            log.debug(ftype + " deleted : " + file.getAbsolutePath());
          }
        }
      }
    }
  }

  private void registerShutdownHook() {
    // register shutdownhook for final cleaning up
    try {
      Runtime.getRuntime().addShutdownHook(new Thread() {
        public void run() {
          List<File> oldFiles = files;
          files = null; 
          // synchronize on the list before iterating over it in order
          // to avoid ConcurrentModificationException (JCR-549)
          // @see java.lang.util.Collections.synchronizedList(java.util.List)
          synchronized (oldFiles) {
            for (File file : oldFiles) {
              file.delete();
            }
          }
        }
      });
    } catch (IllegalStateException e) {
      // can't register shutdownhook because
      // jvm shutdown sequence has already begun,
      // silently ignore...
    }
  }
}
