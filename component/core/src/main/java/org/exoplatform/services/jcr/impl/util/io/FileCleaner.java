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
package org.exoplatform.services.jcr.impl.util.io;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.impl.proccess.WorkerThread;
import org.exoplatform.services.log.ExoLogger;
/**
 * Created by The eXo Platform SAS.
 * @author Gennady Azarenkov
 * @version $Id: FileCleaner.java 11907 2008-03-13 15:36:21Z ksm $
 */

public class FileCleaner extends WorkerThread {
  
  protected static final long DEFAULT_TIMEOUT = 30000;
  
  protected static Log log = ExoLogger.getLogger("jcr.FileCleaner");

  protected Set<File> files = new LinkedHashSet<File> ();
  
  public FileCleaner() {
    this(DEFAULT_TIMEOUT);
  }
  
  public FileCleaner(long timeout) {
    this(timeout, true);
  }
  
  public FileCleaner(boolean start) {
    this(DEFAULT_TIMEOUT, start);
  }
  
  public FileCleaner(long timeout, boolean start) {
    super(timeout);
    setName("FileCleaner " + getId());
    setDaemon(true);
    setPriority(Thread.MIN_PRIORITY);
    
    if (start)
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
    
    if (files != null && files.size() > 0)
      log.warn("There are uncleared files: "+files.size());
      
    super.halt();
  }
  
  /**
   * @see org.exoplatform.services.jcr.impl.proccess.WorkerThread#callPeriodically()
   */
  protected void callPeriodically() throws Exception {
    if (files != null && files.size() > 0) {
      Set<File> oldFiles = files;
      files = new LinkedHashSet<File>();
      for (File file : oldFiles) {
        if (file.exists()) {
          if(!file.delete()) {
            log.warn("Could not delete " + (file.isDirectory() ? "directory" : "file") + 
                ". Will try next time: " + file.getAbsolutePath());
            
            // [PN] 08.10.07 should use same file (i.e. SpoolFile instance)
            //files.add(new File(file.getAbsolutePath())); 
            files.add(file);
          } else if (log.isDebugEnabled()) {
            log.debug((file.isDirectory() ? "Directory" : "File") + " deleted : " + file.getAbsolutePath());
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
          Set<File> oldFiles = files;
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
