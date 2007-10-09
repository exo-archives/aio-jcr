/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS. All rights reserved.          *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.util.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS
 * Author : Peter Nedonosko
 *          peter.nedonosko@exoplatform.com.ua
 * 05.10.2007  
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class SpoolFile extends File {

  private static Log log = ExoLogger.getLogger("jcr.SpoolFile");
  
  protected Map<Object, Long> users = new WeakHashMap<Object, Long>();
  
  public SpoolFile(File parent, String child) {
    super(parent, child);
  }
  
  public SpoolFile(String absPath) {
    super(absPath);
  }
  
  public static SpoolFile createTempFile(String prefix, String suffix, File directory) throws IOException {
    return new SpoolFile(File.createTempFile(prefix, suffix, directory).getAbsolutePath());
  }
  
  public synchronized void acquire(Object holder) throws FileNotFoundException {
    if (users == null)
      throw new FileNotFoundException("File was deleted " + getAbsolutePath());
    
    users.put(holder, System.currentTimeMillis());
  }
  
  public synchronized void release(Object holder) throws FileNotFoundException {
    if (users == null)
      throw new FileNotFoundException("File was deleted " + getAbsolutePath());
    
    users.remove(holder);
  }

  public synchronized boolean inUse() throws FileNotFoundException {
    if (users == null)
      throw new FileNotFoundException("File was deleted " + getAbsolutePath());
    
    return users.size() > 0; 
  }
  
  // ------- java.io.File -------
  
  @Override
  public synchronized boolean delete() {
    if (users.size() <= 0) {
      // make unusable
      users.clear();
      users = null;
      return super.delete();
    }
    
    return false;
  }
  
}
 