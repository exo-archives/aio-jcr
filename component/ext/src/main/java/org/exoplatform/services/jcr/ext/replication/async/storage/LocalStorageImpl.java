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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ChangesLogIterator;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.ext.replication.async.LocalEventListener;
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
public class LocalStorageImpl implements LocalStorage, LocalEventListener {

  protected static final Log    LOG                       = ExoLogger.getLogger("jcr.LocalStorageImpl");

  /**
   * Stuff for PlainChangesLogImpl.writeExternal.
   */
  private static final String   EXTERNALIZATION_SYSTEM_ID = "".intern();

  protected static final String ERROR_FILENAME            = "errors";

  protected final String        storagePath;

  private BufferedWriter        errorOut                  = null;

  public LocalStorageImpl(String storagePath) {
    this.storagePath = storagePath;
  }

  /**
   * {@inheritDoc}
   */
  public ChangesStorage<ItemState> getLocalChanges() throws IOException {
    File incomStorage = new File(storagePath);

    String[] fileNames = incomStorage.list(ChangesFile.getFilenameFilter());

    // TODO Sort names in ascending mode
    java.util.Arrays.sort(fileNames);

    List<ChangesFile> chFiles = new ArrayList<ChangesFile>();
    for (int j = 0; j < fileNames.length; j++) {
      try {
        File ch = new File(incomStorage, fileNames[j]);
        chFiles.add(new ChangesFile(ch, "", Long.parseLong(fileNames[j])));
      } catch (NumberFormatException e) {
        throw new IOException(e.getMessage());
      }
    }

    ChangesLogStorage<ItemState> changeStorage = new ChangesLogStorage<ItemState>(chFiles,
                                                                                  new Member(null,
                                                                                             0));
    return changeStorage;
  }

  /**
   * {@inheritDoc}
   */
  public void onSaveItems(ItemStateChangesLog itemStates) {
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

  /**
   * Creates ChangesFile in storage directory.
   * 
   * @return ChangesFile object
   * @throws IOException
   */
  private ChangesFile createChangesFile() throws IOException {
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      // do nothing
    }
    return new ChangesFile("", System.currentTimeMillis(), storagePath);
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
          Iterator<ValueData> valIt = prop.getValues().iterator();
          List<ValueData> destVals = new ArrayList<ValueData>();

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
          // rewrite values
          prop.setValues(destVals);

          // create new ItemState
          ItemState nItem = new ItemState(prop,
                                          item.getState(),
                                          false,
                                          item.getAncestorToSave(),
                                          true);
          destlist.add(nItem);
        }
      }
      // create new plain changeslog
      result.addLog(new PlainChangesLogImpl(destlist, plog.getSessionId(), plog.getEventType()));
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
      if (this.errorOut == null) {
        errorOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(storagePath,
                                                                                           ERROR_FILENAME),
                                                                                  true),
                                                             Constants.DEFAULT_ENCODING));
      }
      errorOut.write(e.getMessage() + "\n");
      errorOut.flush();
    } catch (IOException ex) {
      // TODO do nothing?
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

      // Close writer
      if (this.errorOut != null)
        errorOut.close();

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
    // TODO rotate storage (archive detached)
  }

  /**
   * {@inheritDoc}
   */
  public void onCancel() {
    // TODO merge detached and current storages in one (rename detached to a current now, till we
    // use READ-ONLY)
  }

  /**
   * {@inheritDoc}
   */
  public void onStart(List<Member> members) {
    // TODO detach current storage and create new current
  }

}
