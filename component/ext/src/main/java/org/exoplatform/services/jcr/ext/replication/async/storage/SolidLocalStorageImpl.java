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
  private static final long     MAX_FILE_SIZE_KB           = 1024 * 1024;

  protected final String        storagePath;

  private File                  lastDir                    = null;

  private File                  previousDir                = null;

  private ChangesFile           currentFile                = null;

  private ObjectOutputStream    currentOut                 = null;                                       // TODO

  /**
   * This unique index used as name for ChangesFiles.
   */
  private Long                  index                      = new Long(0);

  private long                  dirIndex                   = 0;

  public SolidLocalStorageImpl(String storagePath) {
    this.storagePath = storagePath;

    // find last index of storage

    String[] dirs = getSubStorageNames(this.storagePath);

    if (dirs.length != 0) {
      dirIndex = Long.parseLong(dirs[dirs.length - 1]) + 1;

      // TODO check is last directory archived. If true create new directory.

      lastDir = new File(storagePath, dirs[dirs.length - 1]);

      // get last filename as index
      String[] fileNames = lastDir.list(new ChangesFileNameFilter());
      java.util.Arrays.sort(fileNames, new ChangesFileComparator());
      if (fileNames.length != 0) {
        index = Long.parseLong(fileNames[fileNames.length - 1] + 1);
      }

      // find previousDir
      if (dirs.length > 1) {
        // TODO previous dir wasn't removed. So OnStop or OnCancel wasn't
        // called. Its incorrect service close. Fix it
        // previousDir = new File(storagePath, dirs[dirs.length - 2]);
      }
    } else {
      File subdir = new File(storagePath, Long.toString(dirIndex++));
      subdir.mkdirs();
    }

    // started everytime
    doStart();
  }

  /**
   * {@inheritDoc}
   */
  public ChangesStorage<ItemState> getLocalChanges(Member localMember) throws IOException {

    if (previousDir != null) {
      List<ChangesFile> chFiles = new ArrayList<ChangesFile>();

      String[] fileNames = previousDir.list(new ChangesFileNameFilter());

      java.util.Arrays.sort(fileNames, new ChangesFileComparator());

      for (int j = 0; j < fileNames.length; j++) {
        try {
          File ch = new File(previousDir, fileNames[j]);
          chFiles.add(new ChangesFile(ch, "", Long.parseLong(fileNames[j])));
        } catch (NumberFormatException e) {
          throw new IOException(e.getMessage());
        }
      }

      SolidChangesLogStorage<ItemState> changeStorage = new SolidChangesLogStorage<ItemState>(chFiles,
                                                                                    localMember);
      return changeStorage;
    } else {
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  public void onSaveItems(ItemStateChangesLog itemStates) {

    if (!(itemStates instanceof SynchronizerChangesLog)) {
      try {
        addChangesLog(itemStates);
      } catch (IOException e) {
        LOG.error("On save items error " + e, e);
        this.reportException(e);
      }
    }
  }

  protected void addChangesLog(ItemStateChangesLog itemStates) throws IOException {

    // TODO make addition Log to list and writer Thread
    writeLog(itemStates);
  }

  protected void writeLog(ItemStateChangesLog itemStates) throws IOException {

    // Check is file already exists
    if (currentFile == null) {
      currentFile = new ChangesFile("", getNextFileId(), lastDir.getAbsolutePath());
    }

    // Check file size
    if (currentFile.length() > MAX_FILE_SIZE_KB * 1024) {

      closeCurrentFile();
      // create new file
      currentFile = new ChangesFile("", getNextFileId(), lastDir.getAbsolutePath());
    }

    if (currentOut == null) {
      currentOut = new ObjectOutputStream(currentFile.getOutputStream());
    }

    TransactionChangesLog log = prepareChangesLog((TransactionChangesLog) itemStates);
    currentOut.writeObject(log);
    // TODO register timer to close output Stream if file isn't changes too long

  }

  /**
   * Close current Changes file and release any resources associated with it.
   * 
   * @throws IOException
   */
  private void closeCurrentFile() throws IOException {
    // TODO stop any timers

    if (currentOut != null) {
      currentOut.close();
    }

    if (currentFile != null) {
      currentFile.finishWrite();
      currentFile = null;
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
   * @param log local TransactionChangesLog
   * @return TransactionChangesLog with ValueData replaced.
   * @throws IOException if error occurs
   */
  private TransactionChangesLog prepareChangesLog(TransactionChangesLog log) throws IOException {
    ChangesLogIterator chIt = log.getLogIterator();

    TransactionChangesLog result = new TransactionChangesLog();
    result.setSystemId(EXTERNALIZATION_SYSTEM_ID); // for
    // PlainChangesLogImpl.writeExternal

    while (chIt.hasNextLog()) {
      PlainChangesLog plog = chIt.nextLog();

      Iterator<ItemState> srcIt = plog.getAllStates().iterator();
      List<ItemState> destlist = new ArrayList<ItemState>();
      while (srcIt.hasNext()) {

        ItemState item = srcIt.next();

        if (item.isNode()) {
          // skip nodes
          destlist.add(item);
        } else {
          TransientPropertyData prop = (TransientPropertyData) item.getData();

          List<ValueData> srcVals = prop.getValues();
          List<ValueData> destVals = new ArrayList<ValueData>();

          if (srcVals != null) {
            Iterator<ValueData> valIt = prop.getValues().iterator();
            while (valIt.hasNext()) {

              ValueData val = valIt.next();
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
      result.addLog(new PlainChangesLogImpl(destlist,
                                            plog.getSessionId() == null ? EXTERNALIZATION_SESSION_ID
                                                                       : plog.getSessionId(),
                                            plog.getEventType()));
    }
    return result;
  }

  /**
   * Add exception in exception storage.
   * 
   * @param e Exception
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

    deleteDir(previousDir);
    LOG.info("On STOP");

  }

  /**
   * {@inheritDoc}
   */
  public void onCancel() {
    // TODO merge detached and current storages in one (rename detached to a
    // current now, till we use READ-ONLY)

    LOG.info("On CANCEL");
    // get last directory in storage and delete
    // TODO is it correct?

    deleteDir(lastDir);

  }

  /**
   * {@inheritDoc}
   */
  public void onStart(Member localMember, List<Member> members) {
    LOG.info("On START");

    // check lastDir for any changes;
    String[] subfiles = lastDir.list(new ChangesFileNameFilter());
    if (subfiles.length == 0) {
      // write empty log to have at least one file to send/compare
      onSaveItems(new TransactionChangesLog());
    }
    // close current file
    try {
      closeCurrentFile();
    } catch (IOException e) {
      this.reportException(e);
    }

    File subdir = new File(storagePath, Long.toString(dirIndex++));
    if (!subdir.mkdirs()) {
      this.reportException(new IOException("LocalStorage subfolder create fails."));
    }
    
    previousDir = lastDir;
    lastDir = subdir;
    
    LOG.info("LocalStorageImpl:onStart()");
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
  public void onMerge(Member member) {
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
