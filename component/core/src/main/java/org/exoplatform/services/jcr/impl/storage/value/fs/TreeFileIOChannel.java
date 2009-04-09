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
package org.exoplatform.services.jcr.impl.storage.value.fs;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import org.exoplatform.services.jcr.impl.storage.value.ValueDataResourceHolder;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;

/**
 * Created by The eXo Platform SAS
 * 
 * Date: 22.07.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class TreeFileIOChannel extends FileIOChannel {

  private static Semaphore mkdirsLock = new Semaphore(1);

  TreeFileIOChannel(File rootDir,
                    FileCleaner cleaner,
                    String storageId,
                    ValueDataResourceHolder resources) {
    super(rootDir, cleaner, storageId, resources);
  }

  @Override
  protected String makeFilePath(final String propertyId, final int orderNumber) {
    return buildPath(propertyId) + File.separator + propertyId + orderNumber;
  }

  @Override
  protected File getFile(final String propertyId, final int orderNumber) throws IOException {
    final TreeFile tfile = new TreeFile(rootDir.getAbsolutePath()
        + makeFilePath(propertyId, orderNumber), cleaner, rootDir);
    mkdirs(tfile.getParentFile()); // make dirs on path
    return tfile;
  }

  @Override
  protected File[] getFiles(final String propertyId) throws IOException {
    final File dir = new File(rootDir.getAbsolutePath() + buildPath(propertyId));
    String[] fileNames = dir.list();
    File[] files = new File[fileNames.length];
    for (int i = 0; i < fileNames.length; i++) {
      files[i] = new TreeFile(dir.getAbsolutePath() + File.separator + fileNames[i],
                              cleaner,
                              rootDir);
    }
    return files;
  }

  protected String buildPath(String fileName) {
    return buildPathX8(fileName);
  }

  // not useful, as it slow in read/write
  protected String buildPathX(String fileName) {
    char[] chs = fileName.toCharArray();
    String path = "";
    for (char ch : chs) {
      path += File.separator + ch;
    }
    return path;
  }

  // best for now, 12.07.07
  protected String buildPathX8(String fileName) {
    final int xLength = 8;
    char[] chs = fileName.toCharArray();
    String path = "";
    for (int i = 0; i < xLength; i++) {
      path += File.separator + chs[i];
    }
    path += fileName.substring(xLength);
    return path;
  }

  protected String buildPathXX2X4(String fileName) {
    final int xxLength = 4;
    final int xLength = 8;
    boolean xxBlock = true;
    char[] chs = fileName.toCharArray();
    String path = "";
    for (int xxi = 0; xxi < xxLength; xxi++) {
      char ch = chs[xxi];
      path += xxBlock ? File.separator + ch : ch;
      xxBlock = !xxBlock;
    }
    for (int xi = xxLength; xi < xLength; xi++) {
      path += File.separator + chs[xi];
    }
    path += fileName.substring(xLength);
    return path;
  }

  protected String buildPathXX(String fileName) {
    char[] chs = fileName.toCharArray();
    String path = "";
    boolean block = true;
    for (char ch : chs) {
      path += block ? File.separator + ch : ch;
      block = !block;
    }
    return path;
  }

  protected String buildPathXX8(String fileName) {
    final int xxLength = 16; // length / 2 = xx length
    char[] chs = fileName.toCharArray();
    String path = "";
    boolean block = true;
    for (int i = 0; i < xxLength; i++) {
      char ch = chs[i];
      path += block ? File.separator + ch : ch;
      block = !block;
    }
    path += fileName.substring(xxLength);
    return path;
  }

  static private boolean mkdirs(final File dir) {
    // TODO issue on JIRA syncronized method removed and semaphore use
    try {
      mkdirsLock.acquire();
      return dir.mkdirs();
    } catch (InterruptedException e) {
      // chLog.error("mkdirs error on " + dir.getAbsolutePath() + ". " + e, e);
      // return false;
      throw new IllegalStateException("mkdirs error on " + dir.getAbsolutePath() + " due to " + e,
                                      e);
    } finally {
      mkdirsLock.release();
    }
  }
}
