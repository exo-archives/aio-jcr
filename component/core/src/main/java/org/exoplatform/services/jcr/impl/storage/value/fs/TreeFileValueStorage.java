/**                                                                       *
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.  *
 */

package org.exoplatform.services.jcr.impl.storage.value.fs;

import java.io.File;
import java.io.IOException;

import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.storage.value.ValueIOChannel;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class TreeFileValueStorage extends FileValueStorage {
  
  protected class TreeFile extends File {
    
    private static final long serialVersionUID = 5125295927077006487L;

    TreeFile(String fileName) {
      super(fileName);
    }
    
    @Override
    public boolean delete() {
      boolean res = super.delete();
      if (res)
        deleteParent(new File(getParent()));
      
      return res;
    }
    
    protected boolean deleteParent(File fp) {
      boolean res = false;
      String fpPath = fp.getAbsolutePath();
      String rootPath = rootDir.getAbsolutePath();
      if (fpPath.startsWith(rootPath) && fpPath.length() > rootPath.length())
        if (fp.isDirectory()) {
          String[] ls = fp.list();
          if (ls.length<=0) {
            if (res = fp.delete()) {
              res = deleteParent(new File(fp.getParent()));
            } else {
              log.warn("Parent directory can not be deleted now. " + fp.getAbsolutePath());
              cleaner.addFile(new TreeFile(fp.getAbsolutePath()));
            }
          }
        } else
          log.warn("Parent can not be a file but found " + fp.getAbsolutePath());
      return res;
    }
  }
  
  protected class TreeFileCleaner extends FileCleaner {
    @Override
    public synchronized void addFile(File file) {
      super.addFile(new TreeFile(file.getAbsolutePath()));
    }
  }
  
  protected class TreeFileIOChannel extends FileIOChannel {
    
    TreeFileIOChannel(File rootDir, FileCleaner cleaner) {
      super(rootDir, cleaner);
    }
    
    @Override
    protected File getFile(String propertyId, int orderNumber) {
      File dir = new File(rootDir.getAbsolutePath() + buildPathXX(propertyId));
      dir.mkdirs();
      return new TreeFile(dir.getAbsolutePath() + File.separator + propertyId + orderNumber);
    }

    @Override
    protected File[] getFiles(String propertyId) {
      String[] fileNames = rootDir.list();
      File[] files = new File[fileNames.length];
      for (int i=0; i<fileNames.length; i++) {
        files[i] = new TreeFile(rootDir.getAbsolutePath() + File.separator + fileNames[i]);
      }
      return files;
    }
    
    protected String buildPathXX(String fileName) {
      char[] chs = fileName.toCharArray();
      String path = "";
      boolean block = true;
      for (char ch: chs) {
        path += block ? File.separator + ch : ch;
        block = !block;
      }
      return path;
    }
    
    protected String buildPathXX3(String fileName) {
      char[] chs = fileName.toCharArray();
      String path = "";
      boolean block = true;
      for (int i=0; i<6; i++) {
        char ch = chs[i];
        path += block ? File.separator + ch : ch;
        block = !block;
      }
      path += fileName.substring(6);
      return path;
    }
  }
  
  public TreeFileValueStorage() {
    this.cleaner = new TreeFileCleaner();
  }
  
  @Override
  public ValueIOChannel openIOChannel() throws IOException {
    return new TreeFileIOChannel(rootDir, cleaner);
  }
}
