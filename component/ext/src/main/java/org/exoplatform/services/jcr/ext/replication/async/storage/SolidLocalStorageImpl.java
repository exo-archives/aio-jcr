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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: SolidLocalStorageImpl.java 111 2008-11-11 11:11:11Z serg $
 */
public class SolidLocalStorageImpl extends SynchronizationLifeCycle implements LocalStorage,
    LocalEventListener, RemoteEventListener {

  protected static final Log    LOG                        = ExoLogger.getLogger("jcr.LocalStorageImpl");

  /**
   * Stuff for TransactionChangesLog.writeExternal.
   */
  private static final String   EXTERNALIZATION_SYSTEM_ID  = "".intern();

  /**
   * Stuff for PlainChangesLogImpl.writeExternal.
   */
  private static final String   EXTERNALIZATION_SESSION_ID = "".intern();

  protected static final String ERROR_FILENAME             = "errors";

  private FileCleaner           cleaner                    = new FileCleaner();

  /**
   * Max ChangesLog file size in Kb.
   */
  private static final long     MAX_FILE_SIZE              = 32 * 1024 * 1024;

  protected final String        storagePath;

  private File                  currentDir                 = null;

  private File                  currentFile                = null;

  /**
   * This unique index used as name for ChangesFiles.
   */
  private Long                  index                      = new Long(0);

  private Long                  dirIndex                   = new Long(0);

  public SolidLocalStorageImpl(String storagePath) {
    this.storagePath = storagePath;

    // find last index of storage

    String[] dirs = getSubStorageNames(this.storagePath);
    // check local storage
    if (dirs.length > 1) {
      // TODO previous dir wasn't removed. So OnStop or OnCancel wasn't
      // called. Its incorrect service close. Fix it
    }

    if (dirs.length != 0) {
      dirIndex = Long.parseLong(dirs[dirs.length - 1]) + 1;

      currentDir = new File(storagePath, dirs[dirs.length - 1]);

      // get last filename as index
      File[] files = currentDir.listFiles(new ChangesFilenameFilter());
      java.util.Arrays.sort(files, new ChangesFileComparator<File>());
      if (files.length != 0) {
        index = Long.parseLong(files[files.length - 1].getName()) + 1;
      }
    } else {
      currentDir = new File(storagePath, Long.toString(dirIndex++));
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
          // chFiles.add(new RandomChangesFile("", Long.parseLong(fileNames[j]), currentDir));
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
  public synchronized void onSaveItems(ItemStateChangesLog itemStates) {
    // if (isStarted()) {
    // reportException(new IOException("Local storage already stared."));
    // } else {
    if (!(itemStates instanceof SynchronizerChangesLog)) {
      try {
        LOG.info("onSave \n\r" + itemStates.dump());

        TransactionChangesLog log = prepareChangesLog((TransactionChangesLog) itemStates);
        writeLog(log);
      } catch (IOException e) {
        LOG.error("On save items error " + e, e);
        reportException(e);
      }
    }
    // }
  }

  protected void writeLog(ItemStateChangesLog itemStates) throws IOException {

    ObjectOutputStream currentOut;

    // Create file if not exist or file length more than acceptable
    if (currentFile != null && currentFile.length() < MAX_FILE_SIZE) {
      currentOut = new ChangesOutputStream(new FileOutputStream(currentFile, true));
    } else {
      // currentFile = new RandomChangesFile("", getNextFileId(), currentDir); // TODO

      long id = getNextFileId();
      currentFile = new File(currentDir, Long.toString(id));
      currentOut = new ChangesOutputStream(new FileOutputStream(currentFile));
    }

    currentOut.writeObject(itemStates);
    currentOut.close();

    // currentFile.finishWrite(); // TODO
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
      private final static String FILENAME_REGEX = "[0-9]+";

      private final Pattern       PATTERN        = Pattern.compile(FILENAME_REGEX);

      public boolean accept(File dir, String name) {
        File file = new File(dir, name);
        Matcher m = PATTERN.matcher(name);
        if (!m.matches())
          return false;
        return file.isDirectory();
      }
    });

    java.util.Arrays.sort(dirNames, new Comparator<String>() {
      public int compare(String o1, String o2) {
        long first = Long.parseLong(o1);
        long second = Long.parseLong(o2);
        if (first < second) {
          return -1;
        } else if (first == second) {
          return 0;
        } else {
          return 1;
        }
      }
    });

    return dirNames;
  }

  /**
   * Change all TransientValueData to ReplicableValueData.
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
    result.setSystemId(EXTERNALIZATION_SYSTEM_ID); // for
    // PlainChangesLogImpl.writeExternal

    while (chIt.hasNextLog()) {
      PlainChangesLog plog = chIt.nextLog();

      Iterator<ItemState> srcIt = plog.getAllStates().iterator();
      List<ItemState> destlist = new ArrayList<ItemState>();
      while (srcIt.hasNext()) {

        ItemState item = srcIt.next();

        if (item.isNode()) {
          // use nodes states as is
          destlist.add(item);
        } else {
          TransientPropertyData prop = (TransientPropertyData) item.getData();

          List<ValueData> srcVals = prop.getValues();
          List<ValueData> destVals = new ArrayList<ValueData>();

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
              destVals.add(dest);
            }
          }
          // rewrite values
          prop.setValues(destVals);

          // create new ItemState
          ItemState nItem = new ItemState(prop,
                                          item.getState(),
                                          item.isEventFire(),
                                          item.getAncestorToSave(),
                                          item.isInternallyCreated(),
                                          item.isPersisted());
          destlist.add(nItem);
        }
      }
      // create new plain changes log
      result.addLog(new PlainChangesLogImpl(destlist, plog.getSessionId() == null
          ? EXTERNALIZATION_SESSION_ID
          : plog.getSessionId(), plog.getEventType()));
    }
    return result;
  }

  /**
   * Add exception in exception storage.
   * 
   * @param e
   *          Exception
   */
  protected void reportException(Exception e) {
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
      deleteDir(currentDir);

      // TODO don't INCREMENT dirIndex - just use one name
      // create folder for new changes logs
      currentDir = new File(storagePath, Long.toString(dirIndex++));

      if (!currentDir.mkdirs()) {
        this.reportException(new IOException("LocalStorage subfolder create fails : "
            + currentDir.getAbsolutePath()));
      }
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

    // close current file
    currentFile = null;

    doStart();
  }

  /**
   * {@inheritDoc}
   */
  public void onDisconnectMembers(List<Member> member) {
    // TODO not interested
  }

  /**
   * {@inheritDoc}
   */
  public void onMerge(MemberAddress member) {
    // TODO not interested
  }

  private long getNextFileId() {
    long fileId = 0;
    synchronized (index) {
      fileId = index++;
    }
    return fileId;
  }

  public void deleteDir(File dir) {
    File[] subfiles = dir.listFiles();

    for (File f : subfiles) {
      if (!f.delete()) {
        cleaner.addFile(f);
      }
    }

    if (!dir.delete()) {
      cleaner.addFile(dir);
    }
  }

}
