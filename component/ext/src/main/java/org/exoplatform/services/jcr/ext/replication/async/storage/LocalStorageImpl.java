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
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ChangesLogIterator;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.ext.replication.async.LocalEventListener;
import org.exoplatform.services.jcr.ext.replication.async.RemoteEventListener;
import org.exoplatform.services.jcr.ext.replication.async.SynchronizationLifeCycle;
import org.exoplatform.services.jcr.ext.replication.async.transport.MemberAddress;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: SolidLocalStorageImpl.java 111 2008-11-11 11:11:11Z serg $
 */
public class LocalStorageImpl extends SynchronizationLifeCycle implements LocalStorage,
    LocalEventListener, RemoteEventListener {

  protected static final Log                                 LOG                        = ExoLogger.getLogger("jcr.LocalStorageImpl");

  /**
   * Stuff for TransactionChangesLog.writeExternal.
   */
  private static final String                                EXTERNALIZATION_SYSTEM_ID  = "".intern();

  /**
   * Stuff for PlainChangesLogImpl.writeExternal.
   */
  private static final String                                EXTERNALIZATION_SESSION_ID = "".intern();

  /**
   * Error container file name.
   */
  private static final String                                ERROR_FILENAME             = "errors";

  /**
   * The name of local storage sub-directory that contains changes logs.
   */
  private static final String                                DIRECTORY_NAME             = "changes";

  private static final long                                  ERROR_TIMEOUT              = 10000;

  /**
   * Max ChangesLog file size in Kb.
   */
  private static final long                                  MAX_FILE_SIZE              = 32 * 1024 * 1024;

  /**
   * Path to Local Storage.
   */
  private final String                                       storagePath;

  private final ConcurrentLinkedQueue<TransactionChangesLog> changesQueue               = new ConcurrentLinkedQueue<TransactionChangesLog>();

  private ChangesSpooler                                     changesSpooler             = null;

  private File                                               currentDir                 = null;

  private File                                               currentFile                = null;

  private ObjectOutputStream                                 currentOut                 = null;

  /**
   * This unique index used as name for ChangesFiles.
   */
  private Long                                               index                      = new Long(0);

  // private Long dirIndex = new Long(0);

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
     * Change all TransientValueData to ReplicableValueData.
     * 
     * @param log local TransactionChangesLog
     * @return TransactionChangesLog with ValueData replaced.
     * @throws IOException if error occurs
     */
    private TransactionChangesLog prepareChangesLog(final TransactionChangesLog log) throws IOException {
      final ChangesLogIterator chIt = log.getLogIterator();

      final TransactionChangesLog result = new TransactionChangesLog();
      result.setSystemId(EXTERNALIZATION_SYSTEM_ID); // for
      // PlainChangesLogImpl.writeExternal

      while (chIt.hasNextLog()) {
        PlainChangesLog plog = chIt.nextLog();

        List<ItemState> destlist = new ArrayList<ItemState>();

        for (ItemState item : plog.getAllStates()) {
          if (item.isNode()) {
            // use nodes states as is
            destlist.add(item);
          } else {
            TransientPropertyData prop = (TransientPropertyData) item.getData();

            List<ValueData> srcVals = prop.getValues();
            List<ValueData> nVals = new ArrayList<ValueData>();

            if (srcVals != null) { // TODO we don't need it actually
              for (ValueData val : srcVals) {
                ReplicableValueData dest;
                if (val instanceof ReplicableValueData) {
                  dest = (ReplicableValueData) val;
                } else {
                  if (val.isByteArray()) {
                    dest = new ReplicableValueData(val.getAsByteArray(), val.getOrderNumber());
                  } else {
                    if (val instanceof TransientValueData) {
                      dest = new ReplicableValueData(((TransientValueData) val).getSpoolFile(),
                                                     val.getOrderNumber());
                    } else {
                      // create new dataFile
                      dest = new ReplicableValueData(val.getAsStream(), val.getOrderNumber());
                    }
                  }
                }
                nVals.add(dest);
              }
            }
            // rewrite values
            TransientPropertyData nProp = new TransientPropertyData(prop.getQPath(),
                                                                    prop.getIdentifier(),
                                                                    prop.getPersistedVersion(),
                                                                    prop.getType(),
                                                                    prop.getParentIdentifier(),
                                                                    prop.isMultiValued());

            nProp.setValues(nVals);

            // create new ItemState
            ItemState nItem = new ItemState(nProp,
                                            item.getState(),
                                            item.isEventFire(),
                                            item.getAncestorToSave(),
                                            item.isInternallyCreated(),
                                            item.isPersisted());
            destlist.add(nItem);
          }
        }
        // create new plain changes log
        result.addLog(new PlainChangesLogImpl(destlist,
                                              plog.getSessionId() == null ? EXTERNALIZATION_SESSION_ID
                                                                         : plog.getSessionId(),
                                              plog.getEventType()));
      }
      return result;
    }

    private void writeLog(ItemStateChangesLog itemStates) throws IOException {

      if (currentFile == null) {
        long id = getNextFileId();
        currentFile = new File(currentDir, Long.toString(id));
        currentOut = new ObjectOutputStream(new FileOutputStream(currentFile));
      } else if (currentFile.length() > MAX_FILE_SIZE) {
        // close stream
        currentOut.close();

        // create new file
        long id = getNextFileId();
        currentFile = new File(currentDir, Long.toString(id));
        if (currentFile.exists()) {
          LOG.warn("Changes file :" + currentFile.getAbsolutePath()
              + " already exist and will be rewrited.");
        }

        currentOut = new ObjectOutputStream(new FileOutputStream(currentFile));
      }

      currentOut.writeObject(itemStates);
      // keep stream opened

      LOG.info("Write done: \r\n" + itemStates.dump());
    }
  }

  public LocalStorageImpl(String storagePath) {
    this.storagePath = storagePath;

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

    // synchronization is not started for default
    doStop();
  }

  /**
   * {@inheritDoc}
   */
  public ChangesStorage<ItemState> getLocalChanges() throws IOException {

    if (isStopped())
      throw new IOException("Local storage already stopped.");

    if (currentDir != null) {
      List<ChangesFile> chFiles = new ArrayList<ChangesFile>();

      File[] files = currentDir.listFiles(new ChangesFilenameFilter());

      java.util.Arrays.sort(files, new ChangesFileComparator<File>());

      for (int j = 0; j < files.length; j++) {
        try {
          chFiles.add(new SimpleChangesFile(files[j], "", Long.parseLong(files[j].getName())));
        } catch (NumberFormatException e) {
          throw new IOException(e.getMessage());
        }
      }

      return new ChangesLogStorage<ItemState>(chFiles);
    } else {
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  public void onSaveItems(ItemStateChangesLog itemStates) {
    if (!(itemStates instanceof SynchronizerChangesLog)) {
      LOG.info("onSave \n\r" + itemStates.dump()); // TODO

      changesQueue.add((TransactionChangesLog) itemStates);

      if (changesSpooler == null) {
        // changesSpooler var can be nulled from ChangesSpooler.run()
        ChangesSpooler csp = changesSpooler = new ChangesSpooler();
        csp.start();
      }
    }
  }

  /**
   * Return all rootPath sub file names that has are numbers in ascending order.
   * 
   * @param rootPath Path of root directory
   * @return list of sub-files names
   */
  private String[] getSubStorageNames(String rootPath) {

    File storage = new File(rootPath);
    String[] dirNames = storage.list(new FilenameFilter() {
      // private final static String FILENAME_REGEX = "[0-9]+";

      // private final Pattern PATTERN = Pattern.compile(FILENAME_REGEX);

      public boolean accept(File dir, String name) {
        File file = new File(dir, name);
        // Matcher m = PATTERN.matcher(name);
        // if (!m.matches())
        // return false;
        return file.isDirectory();
      }
    });

    /*
     * java.util.Arrays.sort(dirNames, new Comparator<String>() { public int
     * compare(String o1, String o2) { long first = Long.parseLong(o1); long
     * second = Long.parseLong(o2); if (first < second) { return -1; } else if
     * (first == second) { return 0; } else { return 1; } } });
     */

    return dirNames;
  }

  /**
   * Add exception in exception storage.
   * 
   * @param e Exception
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

  /**
   * {@inheritDoc}
   */
  public void onStop() {
    LOG.info("On STOP");

    if (isStarted()) {
      // delete merged content
      File[] subfiles = currentDir.listFiles();

      for (File f : subfiles) {
        if (!f.delete()) {
          LOG.warn("Canot delete file " + f.getAbsolutePath());
          reportException(new Exception("Canot delete file " + f.getAbsolutePath()));
        }
      }

      // leave current directory

     /* if (!currentDir.mkdirs()) {
        LOG.error("Can't create Local strage subfolder: " + currentDir.getAbsolutePath());
        this.reportException(new IOException("LocalStorage subfolder create fails : "
            + currentDir.getAbsolutePath()));
      }*/
    } else
      LOG.warn("Not started or already stopped");

    doStop();
  }

  /**
   * {@inheritDoc}
   */
  public void onCancel() {
    LOG.info("On CANCEL");

    if (isStarted()) {
      doStop();
    } else
      LOG.warn("Not started or already stopped");
  }

  /**
   * {@inheritDoc}
   */
  public void onStart(List<MemberAddress> members) {
    LOG.info("On START");

    // check lastDir for any changes;
    String[] subfiles = currentDir.list(new ChangesFilenameFilter());
    if (subfiles.length == 0) {
      // write empty log to have at least one file to send/compare
      onSaveItems(new TransactionChangesLog());
    }

    ChangesSpooler csp = changesSpooler;
    if (csp != null) {
      LOG.info("Waitig for the changes spooler done.");
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
      if (currentOut != null)
        currentOut.close();

    } catch (IOException e) {
      LOG.error("Can't close current output stream " + e, e);
      reportException(e);
    }

    currentFile = null;

    doStart();
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

  private long getNextFileId() {
    long fileId = 0;
    synchronized (index) {
      fileId = index++;
    }
    return fileId;
  }

}
