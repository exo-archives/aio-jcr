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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.ext.replication.async.LocalEventListener;
import org.exoplatform.services.jcr.ext.replication.async.RemoteEventListener;
import org.exoplatform.services.jcr.ext.replication.async.SynchronizationLifeCycle;
import org.exoplatform.services.jcr.ext.replication.async.transport.MemberAddress;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/>Date: 26.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class IncomeStorageImpl extends SynchronizationLifeCycle implements IncomeStorage,
    LocalEventListener, RemoteEventListener {

  protected static final Log                  LOG       = ExoLogger.getLogger("jcr.IncomeStorageImpl");

  protected final String                      storagePath;

  protected final Map<Integer, MemberChanges> changes   = new HashMap<Integer, MemberChanges>();

  protected final ResourcesHolder             resHolder = new ResourcesHolder();

  class MemberChanges {

    final Member            member;

    final List<ChangesFile> changes;

    MemberChanges(Member member, List<ChangesFile> changes) {
      this.member = member;
      this.changes = changes;
    }
  }

  public IncomeStorageImpl(String storagePath) {
    this.storagePath = storagePath;
  }

  /**
   * {@inheritDoc}
   */
  public synchronized void addMemberChanges(Member member, ChangesFile changesFile) throws IOException {
    changesFile.validate();

    MemberChanges mch = this.changes.get(member.getPriority());
    if (mch == null) {
      mch = new MemberChanges(member, new ArrayList<ChangesFile>());
      this.changes.put(member.getPriority(), mch);
    }

    mch.changes.add(changesFile);
  }

  /**
   * {@inheritDoc}
   */
  public synchronized RandomChangesFile createChangesFile(byte[] crc, long id, Member member) throws IOException {
    File dir = new File(storagePath, Integer.toString(member.getPriority()));
    dir.mkdirs();
    File cf = new File(dir, Long.toString(id));

    try {
      return new RandomChangesFile(cf, crc, id, resHolder);
    } catch (NoSuchAlgorithmException e) {
      throw new IOException(e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  public List<MemberChangesStorage<ItemState>> getChanges() throws IOException {
    if (isStopped())
      throw new IOException("Incom storage already stopped.");

    return getChangesFromMap();
  }

  /**
   * Get changes from runtime Map.
   * 
   * @return List of ChangesStorage
   */
  private List<MemberChangesStorage<ItemState>> getChangesFromMap() {

    List<MemberChangesStorage<ItemState>> result = new ArrayList<MemberChangesStorage<ItemState>>();
    for (Map.Entry<Integer, MemberChanges> entry : changes.entrySet()) {
      result.add(new IncomeChangesStorage<ItemState>(new ChangesLogStorage<ItemState>(entry.getValue().changes),
                                                     entry.getValue().member));
    }

    Collections.sort(result, new Comparator<MemberChangesStorage<ItemState>>() {
      /**
       * {@inheritDoc}
       */
      public int compare(MemberChangesStorage<ItemState> m1, MemberChangesStorage<ItemState> m2) {
        return m1.getMember().getPriority() - m2.getMember().getPriority();
      }
    });

    return result;
  }

  /**
   * Get changes from FS. Usefull when we wants load changes from persistence - file system.
   * 
   * @return List of ChangesStorage
   * @throws IOException
   *           on FS error
   */
  @Deprecated
  private List<ChangesStorage<ItemState>> getChangesFromFS() throws IOException {

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
        int memberPriority = Integer.parseInt(memberDir.getName()); // also
        // check -
        // is
        // member folder;

        File[] files = memberDir.listFiles(new ChangesFilenameFilter());

        java.util.Arrays.sort(files, new ChangesFileComparator<File>());

        List<ChangesFile> chFiles = new ArrayList<ChangesFile>();
        for (int j = 0; j < files.length; j++) {
          File ch = new File(memberDir, files[j].getName());
          try {
            chFiles.add(new RandomChangesFile(ch,
                                              new byte[] {},
                                              Long.parseLong(files[j].getName()),
                                              resHolder));
          } catch (NoSuchAlgorithmException e) {
            throw new IOException(e.getMessage());
          }
        }

        if (LOG.isDebugEnabled())
          LOG.debug("The ChangesFiles in IncomeStorage = " + chFiles.size());

        IncomeChangesStorage<ItemState> storage = new IncomeChangesStorage<ItemState>(new ChangesLogStorage<ItemState>(chFiles),
                                                                                      null); // TODO
        // NPE
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

  public void clean() {
    try {
      resHolder.close();
    } catch (IOException e) {
      LOG.error("Error of data streams close " + e, e);
    }

    // delete all data in storage
    File dir = new File(storagePath);
    if (dir.exists()) {
      deleteStorage(dir);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void onDisconnectMembers(List<Member> members) {
    if (LOG.isDebugEnabled())
      LOG.debug("On DisconnectMembers " + members);

    for (Member member : members)
      changes.remove(member.getPriority());

    // Get members directory

    Iterator<Member> memIt = members.iterator();
    while (memIt.hasNext()) {
      File dir = new File(storagePath, Integer.toString(memIt.next().getPriority()));
      deleteStorage(dir);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void onCancel() {
    if (LOG.isDebugEnabled())
      LOG.debug("On CANCEL");

    if (isStarted())
      doStop();
    else
      LOG.warn("Not started or already stopped");
  }

  /**
   * {@inheritDoc}
   */
  public void onStart(List<MemberAddress> members) {
    if (LOG.isDebugEnabled())
      LOG.debug("On START");

    // prepare storage (clean)
    clean();

    doStart();
  }

  /**
   * {@inheritDoc}
   */
  public void onStop() {
    if (LOG.isDebugEnabled())
      LOG.debug("On STOP");

    if (isStarted()) {
      doStop();
    } else
      LOG.warn("Not started or already stopped");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doStop() {
    super.doStop();

    // clean map
    changes.clear();

    // clean storage
    clean();
  }

  private void deleteStorage(File file) {
    if (file.isDirectory()) {
      File[] files = file.listFiles();
      for (File f : files) {
        deleteStorage(f);
      }
    }
    if (!file.delete())
      LOG.warn("Cannot delete file " + file.getAbsolutePath());
  }

  /**
   * {@inheritDoc}
   */
  public void onMerge(MemberAddress member) {
    // not interested
  }

}
