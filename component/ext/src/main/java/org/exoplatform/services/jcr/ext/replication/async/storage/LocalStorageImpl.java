/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/>Date: 26.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class LocalStorageImpl extends SynchronizationLifeCycle implements LocalStorage,
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

  // protected static final String MAIN_DIRNAME = "primary";

  // protected static final String BACK_DIRNAME = "back";

  protected final String        storagePath;

  private final int             priority;

  // private File primeDir;

  // private File secondDir;

  /**
   * This unique index used as name for ChangesFiles.
   */
  private volatile long         index                      = 0;

  private volatile long         dirIndex                   = 0;

  public LocalStorageImpl(String storagePath, int priority) {
    this.storagePath = storagePath;
    this.priority = priority;

    // find last index of storage
    String[] dirs = getSubStorageNames(this.storagePath);

    if (dirs.length != 0) {
      dirIndex = Long.parseLong(dirs[dirs.length - 1]) + 1;
      // TODO check is last directory archived. If true create new directory.

      File lastDir = new File(storagePath, dirs[dirs.length - 1]);
      // get last filename as index
      String[] fileNames = lastDir.list(new ChangesFileNameFilter());
      java.util.Arrays.sort(fileNames, new ChangesFileComparator());
      if (fileNames.length != 0) {
        index = Long.parseLong(fileNames[fileNames.length - 1] + 1);
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
  public ChangesStorage<ItemState> getLocalChanges() throws IOException {
    List<ChangesFile> chFiles = new ArrayList<ChangesFile>();

    String[] dirNames = getSubStorageNames(storagePath);

    // get previous directory
    File prevDir = new File(storagePath, dirNames[dirNames.length - 2]);

    String[] fileNames = prevDir.list(new ChangesFileNameFilter());

    java.util.Arrays.sort(fileNames, new ChangesFileComparator());

    for (int j = 0; j < fileNames.length; j++) {
      try {
        File ch = new File(prevDir, fileNames[j]);
        chFiles.add(new ChangesFile(ch, "", Long.parseLong(fileNames[j])));
      } catch (NumberFormatException e) {
        throw new IOException(e.getMessage());
      }
    }

    ChangesLogStorage<ItemState> changeStorage = new ChangesLogStorage<ItemState>(chFiles,
                                                                                  new Member(priority));
    return changeStorage;
  }

  /**
   * {@inheritDoc}
   */
  public void onSaveItems(ItemStateChangesLog itemStates) {

    if (!(itemStates instanceof SynchronizerChangesLog)) {
      try {
        ChangesFile file = createChangesFile();

        ObjectOutputStream out = new ObjectOutputStream(file.getOutputStream());

        TransactionChangesLog log = prepareChangesLog((TransactionChangesLog) itemStates);

        out.writeObject(log);
        out.close();
        file.finishWrite();
      } catch (IOException e) {
        LOG.error("On save items error " + e, e);
        this.reportException(e);
      }
    }
  }

  /**
   * Creates ChangesFile in storage directory.
   * 
   * @return ChangesFile object
   * @throws IOException
   */
  private ChangesFile createChangesFile() throws IOException {

    String[] dirs = getSubStorageNames(storagePath);

    File lastDir = new File(storagePath, dirs[dirs.length - 1]);

    return new ChangesFile("", index++, lastDir.getAbsolutePath());

  }

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
    // TODO archive primary dir content
    // delete files in primary dir
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
    String[] dirs = getSubStorageNames(this.storagePath);

    File lastDir = new File(storagePath, dirs[dirs.length - 1]);
    lastDir.delete();

  }

  /**
   * {@inheritDoc}
   */
  public void onStart(List<Member> members) {
    LOG.info("On START");
   
    //check previous dir
    String dirs[] = getSubStorageNames(this.storagePath);
    File prevDir = new File(storagePath, dirs[dirs.length-1]);
    String[] subfiles = prevDir.list(new ChangesFileNameFilter());
    if(subfiles.length==0){
        // write empty log to have at least one file to send/compare
        onSaveItems(new TransactionChangesLog());
    }
    File subdir = new File(storagePath, Long.toString(dirIndex++));
    subdir.mkdirs();
    
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

}
