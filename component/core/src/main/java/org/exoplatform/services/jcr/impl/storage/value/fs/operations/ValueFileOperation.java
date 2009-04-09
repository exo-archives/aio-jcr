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
package org.exoplatform.services.jcr.impl.storage.value.fs.operations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.impl.storage.value.ValueDataResourceHolder;
import org.exoplatform.services.jcr.impl.storage.value.ValueOperation;
import org.exoplatform.services.jcr.impl.storage.value.fs.FileLockException;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date: 03.04.2009
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: ValueFileOperation.java 111 2008-11-11 11:11:11Z pnedonosko $
 */
public abstract class ValueFileOperation extends ValueFileIOHelper implements ValueOperation {

  public static final String              TEMP_FILE_EXTENSION = ".temp";

  public static final String              LOCK_FILE_EXTENSION = ".lock";

  protected static Log                    LOG                 = ExoLogger.getLogger("jcr.ValueFileOperation");

  protected final FileCleaner             cleaner;

  protected final ValueDataResourceHolder resources;

  protected final String                  operationInfo;

  protected final File                    tempDir;

  /**
   * Performed state flag. Optional for use.
   */
  private boolean                         performed           = false;

  class ValueFileLockHolder implements ValueLockSupport {

    private final File       targetFile;

    private File             lockFile;

    private FileOutputStream lockFileStream;

    ValueFileLockHolder(File file) {
      this.targetFile = file;
    }

    /**
     * {@inheritDoc}
     */
    public void lock() throws IOException {
      // lock file in temp directory
      lockFile = new File(tempDir, targetFile.getName() + LOCK_FILE_EXTENSION);

      FileOutputStream lout = new FileOutputStream(lockFile, true);
      lout.write(operationInfo.getBytes()); // TODO write info
      lout.getChannel().lock(); // wait for unlock (on Windows will wait for this JVM too)

      lockFileStream = lout;
    }

    /**
     * {@inheritDoc}
     */
    public void share(ValueLockSupport anotherLock) throws IOException {
      if (anotherLock instanceof ValueFileLockHolder) {
        ValueFileLockHolder al = (ValueFileLockHolder) anotherLock;
        lockFile = al.lockFile;
        lockFileStream = al.lockFileStream;
      } else
        throw new IOException("Cannot share lock with " + anotherLock.getClass());
    }

    /**
     * {@inheritDoc}
     */
    public void unlock() throws IOException {
      if (lockFileStream != null)
        lockFileStream.close();

      if (!lockFile.delete()) { // TODO don't use FileCleaner, delete should be enough
        LOG.warn("Cannot delete lock file " + lockFile.getAbsolutePath()
            + ". Add to the FileCleaner");
        cleaner.addFile(lockFile);
      }
    }

  }

  class ValueFileLock {

    private final File          file;

    //private File                fileLock;

    //private ValueFileLockHolder fileLockHolder;

    ValueFileLock(File file) {
      this.file = file;
    }

    /**
     * Lock File location (place on file-system) for this JVM and external changes.
     * 
     * @return boolean, true - if locked, false - if already locked by this Thread
     * @throws IOException
     *           if error occurs
     */
    public boolean lock() throws IOException {
//      if (fileLockHolder != null)
//        throw new IOException("File already locked " + file.getAbsolutePath());

      // lock in JVM (wait for unlock if required)
      try {
        return resources.aquire(file.getAbsolutePath(), new ValueFileLockHolder(file));
//        if (resources.aquire(file.getAbsolutePath())) {
//          // locked in JVM,
//          // lock on FS (locking fileLock via NIO, wait for unlock if required)
//          fileLockHolder = new ValueFileLockHolder(file);
//          return true;
//        } else {
//          // already locked in JVM by me
//          fileLockHolder = null;
//          return false;
//        }
      } catch (InterruptedException e) {
        throw new FileLockException("Lock error on " + file.getAbsolutePath(), e);
      }
    }

    /**
     * Unlock File location (place on file-system) for this JVM and external changes.
     * 
     * @return boolean, true - if unlocked, false - if still locked by this Thread
     * @throws IOException
     *           if error occurs
     */
    public boolean unlock() throws IOException {
//      if (fileLock == null)
//        throw new IOException("File not locked " + file.getAbsolutePath());

      return resources.release(file.getAbsolutePath());
      
//      try {
//        // unlock in JVM
//        if (resources.release(file.getAbsolutePath())) {
//          // unlock FS lock
//          if (fileLockHolder != null) {
//            fileLockHolder.close();
//
//            if (!fileLock.delete()) { // TODO don't use FileCleaner, delete should be enough
//              LOG.warn("Cannot delete lock file " + fileLock.getAbsolutePath()
//                  + ". Add to the FileCleaner");
//              cleaner.addFile(fileLock);
//            }
//          }
//          return true;
//        } else
//          return false;
//      } finally {
//        fileLockHolder = null;
//        fileLock = null;
//      }
    }
  }

  /**
   * ValueFileOperation constructor.
   * 
   * @param resources
   *          ValueDataResourceHolder
   * @param cleaner
   *          FileCleaner
   * @param tempDir
   *          temp dir for locking and other I/O operations
   */
  ValueFileOperation(ValueDataResourceHolder resources, FileCleaner cleaner, File tempDir) {

    this.cleaner = cleaner;
    this.resources = resources;

    this.tempDir = tempDir;

    // TODO this info may be is not neccesary
    String localAddr;
    try {
      // get the inet address
      InetAddress local = InetAddress.getLocalHost();
      localAddr = local.getHostAddress() + " (" + local.getHostName() + ")";
    } catch (UnknownHostException e) {
      LOG.warn("Cannot read host address " + e);
      localAddr = "no address, " + e;
    }
    operationInfo = System.currentTimeMillis() + " " + localAddr;
  }

  protected boolean isPerformed() throws IOException {
    return performed;
  }

  protected void makePerformed() throws IOException {
    if (performed)
      throw new IOException("Operation cannot be performed twice");

    performed = true;
  }

  /**
   * Copy file.
   * 
   * @param src
   *          File
   * @param dest
   *          File
   * @throws IOException
   *           if error occurs
   */
  protected void copyFile(File src, File dest) throws IOException {
    FileInputStream is = new FileInputStream(src);
    FileOutputStream os = new FileOutputStream(dest);

    try {
      try {
        // use NIO
        FileChannel in = is.getChannel();
        FileChannel out = os.getChannel();
        long r = 0;
        long fsize = src.length();
        do {
          r += out.transferFrom(in, r, fsize - r);
          out.position(r);
        } while (r < fsize);

        // int r = -1;
        // byte[] buff = new byte[2048];
        // while ((r = is.read(buff)) >= 0) {
        // os.write(buff, 0, r);
        // }
      } finally {
        is.close();
      }
    } finally {
      os.close();
    }
  }

  /**
   * Move file.
   * 
   * @param src
   *          File
   * @param dest
   *          File
   * @param failOnExists
   *          if true and dest file exists an IOException will be thrown
   * @throws IOException
   */
  protected void moveFile(File src, File dest, boolean failOnExists) throws IOException {
    if (dest.exists())
      throw new IOException("FATAL Cannot rename file " + src.getAbsolutePath()
          + ". Destination file already exists " + dest.getAbsolutePath());

    src.renameTo(dest);

    // check does file exists
    if (!dest.exists()) {
      if (src.exists()) {
        LOG.warn("Value operation. File rename fails " + src.getAbsolutePath()
            + ". Will try bytes copy.");

        // so, we'll try a copy of the file
        copyFile(src, dest);

        // try delete src file
        if (src.exists())
          if (!src.delete())
            cleaner.addFile(src);
      } else
        throw new IOException("FATAL Cannot move. Source file not exists after the File.renameTo(dest). Source "
            + src.getAbsolutePath() + ". Destination " + dest.getAbsolutePath());
    }
  }

}
