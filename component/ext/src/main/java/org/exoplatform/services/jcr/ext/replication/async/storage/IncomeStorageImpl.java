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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.ext.replication.async.LocalEventListener;
import org.exoplatform.services.jcr.ext.replication.async.RemoteEventListener;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/>Date: 26.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class IncomeStorageImpl implements IncomeStorage, LocalEventListener, RemoteEventListener {

  protected static final Log LOG = ExoLogger.getLogger("jcr.IncomeStorageImpl");

  protected final String     storagePath;

  public IncomeStorageImpl(String storagePath) {
    this.storagePath = storagePath;
  }

  /**
   * {@inheritDoc}
   */
  public void addMemberChanges(Member member, ChangesFile changes) throws IOException {
    // TODO check if CRC is valid for received file

    // get member directory
    // File dir = new File(storagePath, Integer.toString(member.getPriority()));

    // dir.mkdirs();

    // move changes file to member directory
    // if(!changes.moveTo(dir)) throw new IOException("Can't move file.");
  }

  /**
   * {@inheritDoc}
   */
  public ChangesFile createChangesFile(String crc, long timeStamp, Member member) throws IOException {
    File dir = new File(storagePath, Integer.toString(member.getPriority()));
    dir.mkdirs();
    return new ChangesFile(crc, timeStamp, dir.getAbsolutePath());
  }

  /**
   * {@inheritDoc}
   */
  public List<ChangesStorage<ItemState>> getChanges() throws IOException {

    File incomStorage = new File(storagePath);
    File[] memberDirs = incomStorage.listFiles(new FilenameFilter() {

      public boolean accept(File dir, String name) {
        File fdir = new File(dir, name);
        if (fdir.isDirectory()) {
          return true;
        }
        return false;
      }
    });

    List<ChangesStorage<ItemState>> changeStorages = new ArrayList<ChangesStorage<ItemState>>();
    for (File memberDir : memberDirs) {
      try {
        int memberPriority = Integer.parseInt(memberDir.getName()); // also check - is
        // member folder;

        String[] fileNames = memberDir.list(ChangesFile.getFilenameFilter());

        java.util.Arrays.sort(fileNames,ChangesFile.getFilenameComparator());

        List<ChangesFile> chFiles = new ArrayList<ChangesFile>();
        for (int j = 0; j < fileNames.length; j++) {
          File ch = new File(memberDir, fileNames[j]);
          chFiles.add(new ChangesFile(ch, "", Long.parseLong(fileNames[j])));
        }
        ChangesLogStorage<ItemState> storage = new ChangesLogStorage<ItemState>(chFiles,
                                                                                new Member(memberPriority));
        changeStorages.add(storage);
      } catch (final NumberFormatException e) {
        // This is not int-named file. Fatal.
        throw new IOException("Cannot read file name: " + e.getMessage()) {

          /**
           * {@inheritDoc}
           */
          @Override
          public Throwable getCause() {
            return e;
          }

        };
      }
    }
    return changeStorages;
  }

  public void clean() throws IOException {
    // delete all data in storage
    File storage = new File(this.storagePath);
    for (File file : storage.listFiles()) {
      file.delete();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void onDisconnectMembers(List<Member> members) {
    // Get members directory
    
    Iterator<Member> memIt = members.iterator();
    while(memIt.hasNext()){
      File dir = new File(storagePath, Integer.toString(memIt.next().getPriority()));
      deleteStorage(dir);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void onCancel() {
    // clean storage
    File dir = new File(storagePath);
    if (dir.exists()) {
      deleteStorage(dir);
    }

  }

  /**
   * {@inheritDoc}
   */
  public void onStart(List<Member> members) {
    // prepare storage (clean)
    File dir = new File(storagePath);
    if (dir.exists()) {
      deleteStorage(dir);
    }

  }

  /**
   * {@inheritDoc}
   */
  public void onStop() {
    // clean storage
    File dir = new File(storagePath);
    if (dir.exists()) {
      deleteStorage(dir);
    }

  }

  private void deleteStorage(File file) {
    if (file != null) {
      if (file.isDirectory()) {
        File[] files = file.listFiles();
        for (File f : files) {
          deleteStorage(f);
        }
      }
      file.delete();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void onMerge(Member member) {
    // not interested
  }

}
