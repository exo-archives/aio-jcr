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
package org.exoplatform.services.jcr.ext.replication.async.storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.dataflow.ChangesLogIterator;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.dataflow.serialization.ObjectWriter;
import org.exoplatform.services.jcr.dataflow.serialization.UnknownClassIdException;
import org.exoplatform.services.jcr.ext.replication.async.SynchronizationLifeCycle;
import org.exoplatform.services.jcr.ext.replication.async.transport.MemberAddress;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.serialization.ObjectWriterImpl;
import org.exoplatform.services.jcr.impl.dataflow.serialization.ReaderSpoolFileHolder;
import org.exoplatform.services.jcr.impl.dataflow.serialization.TransactionChangesLogWriter;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: AbstractLocalStorage.java 111 2008-11-11 11:11:11Z $
 */
public abstract class AbstractLocalStorage extends SynchronizationLifeCycle implements LocalStorage {

  /**
   * Marked as internal files.
   */
  public static final String                                   INTERNAL_CHANGES_FILE_TAG  = "i";

  /**
   * The name of local storage sub-directory that contains changes logs.
   */
  protected static final String                                DIRECTORY_NAME             = "changes";

  protected static final String                                DIGESTFILE_EXTENTION       = ".md5";

  protected static final long                                  ERROR_TIMEOUT              = 10000;

  /**
   * Max ChangesLog file size in Kb.
   */
  protected static final long                                  MAX_FILE_SIZE              = 32 * 1024 * 1024;

  /**
   * Stuff for TransactionChangesLog.writeExternal.
   */
  protected static final String                                EXTERNALIZATION_SYSTEM_ID  = "".intern();

  /**
   * Stuff for PlainChangesLogImpl.writeExternal.
   */
  protected static final String                                EXTERNALIZATION_SESSION_ID = "".intern();

  /**
   * Error container file name.
   */
  protected static final String                                ERROR_FILENAME             = "errors";

  protected static final Log                                   LOG                        = ExoLogger.getLogger("jcr.LocalStorage");

  /**
   * Path to Local Storage.
   */
  protected String                                             storagePath;

  protected final ResourcesHolder                              resHolder                  = new ResourcesHolder();

  protected final ConcurrentLinkedQueue<TransactionChangesLog> changesQueue               = new ConcurrentLinkedQueue<TransactionChangesLog>();

  protected ChangesSpooler                                     changesSpooler             = null;

  protected File                                               currentDir                 = null;

  protected File                                               currentFile                = null;

  protected ObjectWriter                                       currentOut                 = null;

  protected MessageDigest                                      digest;

  protected final FileCleaner                                  fileCleaner;

  protected final int                                          maxBufferSize;

  protected final ReaderSpoolFileHolder                        holder;

  /**
   * This unique index used as name for ChangesFiles.
   */
  protected Long                                               index                      = new Long(0);

  class ChangesSpooler extends Thread {

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
      try {
        TransactionChangesLog chl = changesQueue.poll();
        while (chl != null) {
          writeLog(prepareChangesLog(chl));

          Thread.yield();

          chl = changesQueue.poll();
        }
      } catch (IOException e) {
        LOG.error("Cannot spool changes queue. I/O error " + e, e);
        reportException(e);
      } catch (Throwable e) {
        LOG.error("Cannot spool changes queue. Error " + e, e);
        reportException(e);
      } finally {
        changesSpooler = null; // reset self-reference
      }
    }

    /**
     * Set sessionId if it null for PlainChangesLog.
     * 
     * @param log
     *          local TransactionChangesLog
     * @return TransactionChangesLog with ValueData replaced.
     * @throws IOException
     *           if error occurs
     */
    private TransactionChangesLog prepareChangesLog(final TransactionChangesLog log) throws IOException {
      final ChangesLogIterator chIt = log.getLogIterator();

      final TransactionChangesLog result = new TransactionChangesLog();
      result.setSystemId(log.getSystemId() == null ? EXTERNALIZATION_SYSTEM_ID : log.getSystemId()); // for

      while (chIt.hasNextLog()) {
        PlainChangesLog plog = chIt.nextLog();

        // create new plain changes log
        result.addLog(new PlainChangesLogImpl(plog.getAllStates(), plog.getSessionId() == null
            ? EXTERNALIZATION_SESSION_ID
            : plog.getSessionId(), plog.getEventType()));
      }
      return result;
    }

    private void writeLog(TransactionChangesLog itemStates) throws IOException,
                                                           UnknownClassIdException {

      if (itemStates.getSystemId() == null
          || !itemStates.getSystemId()
                        .equals(Constants.JCR_CORE_RESTORE_WORKSPACE_INITIALIZER_SYSTEM_ID)) {
        if (currentFile == null) {
          // create new file
          long id = getNextFileId();
          currentFile = new File(currentDir, Long.toString(id));
          if (currentFile.exists()) {
            LOG.warn("Changes file :" + currentFile.getAbsolutePath()
                + " already exist and will be rewrited.");
          }
          currentOut = new ObjectWriterImpl(new DigestOutputStream(new FileOutputStream(currentFile),
                                                                   digest));
        }

        // write changes log
        TransactionChangesLogWriter writer = new TransactionChangesLogWriter();
        writer.write(currentOut, itemStates);

        // check changes file size
        if (currentFile.length() > MAX_FILE_SIZE) {
          // close stream
          closeCurrentOutput();
          currentFile = null;
        }

      } else {
        if (currentFile != null) {
          closeCurrentOutput();
          currentFile = null;
        }

        long id = getNextFileId();
        currentFile = new File(currentDir, Long.toString(id) + INTERNAL_CHANGES_FILE_TAG);
        currentOut = new ObjectWriterImpl(new DigestOutputStream(new FileOutputStream(currentFile),
                                                                 digest));

        TransactionChangesLogWriter writer = new TransactionChangesLogWriter();
        writer.write(currentOut, itemStates);

        closeCurrentOutput();
        currentFile = null;
      }

      // keep stream opened
    }

  }

  public AbstractLocalStorage(String storagePath,
                              FileCleaner fileCleaner,
                              int maxBufferSize,
                              ReaderSpoolFileHolder holder) throws NoSuchAlgorithmException,
      ChecksumNotFoundException {
    this.storagePath = storagePath;
    this.fileCleaner = fileCleaner;
    this.maxBufferSize = maxBufferSize;
    this.holder = holder;
    this.digest = MessageDigest.getInstance("MD5");

    // find last index of storage
    String[] dirs = getSubStorageNames(storagePath);

    // check local storage
    if (dirs.length > 1) {
      LOG.warn("Local storage contains more than one sub-directory!");
    }

    currentDir = new File(storagePath, DIRECTORY_NAME);

    if (!currentDir.exists()) {
      currentDir.mkdirs();
    }

    // set index to last file
    File[] files = currentDir.listFiles(new ChangesFilenameFilter(false));
    java.util.Arrays.sort(files, new ChangesFileComparator<File>());

    if (files.length > 0) {
      this.index = Long.parseLong(files[files.length - 1].getName()) + 1;
    } else {
      this.index = new Long(0);
    }

    java.lang.Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        flushChanges();
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public String[] getErrors() throws IOException {

    File err = new File(storagePath, ERROR_FILENAME);
    if (!err.exists()) {
      return new String[0];
    } else {
      List<String> list = new ArrayList<String>();

      // Open reader
      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(err),
                                                                   Constants.DEFAULT_ENCODING));
      String s;
      while ((s = br.readLine()) != null) {
        list.add(s);
      }
      br.close();
      return list.toArray(new String[list.size()]);
    }
  }

  protected long getNextFileId() {
    long fileId = 0;
    synchronized (index) {
      fileId = index++;
    }
    return fileId;
  }

  protected void closeCurrentOutput() throws IOException {
    if (currentOut != null) {

      // close stream
      currentOut.close();

      // flush digest
      File digestFile = new File(currentDir, currentFile.getName() + DIGESTFILE_EXTENTION);
      FileOutputStream foutDigest = new FileOutputStream(digestFile);
      byte[] crc = digest.digest();
      foutDigest.write(crc);
      foutDigest.close();
      digest.reset();

      currentOut = null;
    }
  }

  /**
   * Add exception in exception storage.
   * 
   * @param e
   *          Exception
   */
  protected void reportException(Throwable e) {
    try {
      BufferedWriter errorOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(storagePath,
                                                                                                        ERROR_FILENAME),
                                                                                               true),
                                                                          Constants.DEFAULT_ENCODING));

      errorOut.write(e.getMessage() + "\n");
      errorOut.flush();
      errorOut.close();

    } catch (IOException ex) {
      // do nothing
      LOG.warn("Exception on write to error storage file: ", ex);
    }
  }

  /**
   * Return all rootPath sub file names that has are numbers in ascending order.
   * 
   * @param rootPath
   *          Path of root directory
   * @return list of sub-files names
   */
  private String[] getSubStorageNames(String rootPath) {

    File storage = new File(rootPath);
    String[] dirNames = storage.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        File file = new File(dir, name);
        return file.isDirectory();
      }
    });

    return dirNames;
  }

  /**
   * {@inheritDoc}
   */
  public void onStop() {
    if (LOG.isDebugEnabled())
      LOG.debug("On STOP");

    if (isStarted()) {

      try {
        resHolder.close();
      } catch (IOException e) {
        LOG.error("Error of data streams close " + e, e);
      }

      flushChanges();

      // delete merged content
      File[] subfiles = currentDir.listFiles();

      for (File f : subfiles) {
        if (!f.delete()) {
          LOG.warn("Canot delete file " + f.getAbsolutePath());
          reportException(new Exception("Cannot delete file " + f.getAbsolutePath()));
        }
      }

      // reset files index
      index = new Long(0);

    } else
      LOG.warn("Not started or already stopped");

    doStop();
  }

  /**
   * {@inheritDoc}
   */
  public void onCancel() {
    if (LOG.isDebugEnabled())
      LOG.debug("On CANCEL");

    if (isStarted()) {
      doStop();
    } else
      LOG.warn("Not started or already stopped");
  }

  /**
   * {@inheritDoc}
   */
  public void onStart(List<MemberAddress> members) {
    if (LOG.isDebugEnabled())
      LOG.debug("On START");

    // check lastDir for any changes;
    String[] subfiles = currentDir.list(new ChangesFilenameFilter(false));
    if (subfiles.length == 0) {
      // write empty log to have at least one file to send/compare
      onSaveItems(new TransactionChangesLog());
    }

    flushChanges();

    doStart();
  }

  /**
   * FlushChanges.
   */
  private void flushChanges() {
    ChangesSpooler csp = changesSpooler;
    if (csp != null) {
      if (LOG.isDebugEnabled())
        LOG.debug("Waitig for the changes spooler done.");
      try {
        csp.join();
      } catch (InterruptedException e) {
        LOG.error("Waitig for the changes spooler fails. Data still can be not spooled to the file. Error "
                      + e,
                  e);
        try {
          Thread.sleep(ERROR_TIMEOUT);
        } catch (InterruptedException e1) {
          LOG.error("Sleep error " + e, e);
        }
      }
    }

    // close current file
    try {
      closeCurrentOutput();

    } catch (IOException e) {
      LOG.error("Can't close current output stream " + e, e);
      reportException(e);
    }

    currentFile = null;
  }

  /**
   * {@inheritDoc}
   */
  public void onDisconnectMembers(List<Member> member) {
    // not interested
  }

  /**
   * {@inheritDoc}
   */
  public void onMerge(MemberAddress member) {
    // not interested
  }
}
