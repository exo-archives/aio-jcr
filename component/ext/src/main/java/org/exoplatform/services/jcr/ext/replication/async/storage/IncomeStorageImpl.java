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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/>Date: 26.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class IncomeStorageImpl implements IncomeStorage {

  protected static final Log LOG = ExoLogger.getLogger("jcr.IncomeStorageImpl");

  protected final String     storagePath;

  public IncomeStorageImpl(String storagePath) {
    this.storagePath = storagePath;
  }

  /**
   * {@inheritDoc}
   */
  public void addMemberChanges(Member member, ChangesFile changes) throws IOException {
    // get member directory
    File dir = new File(storagePath, Integer.toString(member.getPriority()));

    dir.mkdirs();

    // move changes file to member directory
    changes.moveTo(dir);
  }

  /**
   * {@inheritDoc}
   */
  public ChangesFile createChangesFile(String crc, long timeStamp) throws IOException {
    return new ChangesFile(crc, timeStamp, storagePath);
  }

  /**
   * {@inheritDoc}
   */
  public List<ChangesStorage<ItemState>> getChanges() throws IOException {

    File incomStorage = new File(storagePath);
    String[] childnames = incomStorage.list();

    List<ChangesStorage<ItemState>> changeStorages = new ArrayList<ChangesStorage<ItemState>>();
    for (int i = 0; i < childnames.length; i++) {
      try {

        int memberPriority = Integer.parseInt(childnames[i]); // also check - is
                                                              // member folder;
        File memberDir = new File(incomStorage, childnames[i]);

        String[] fileNames = memberDir.list();
        //TODO Sort names in ascending mode
        java.util.Arrays.sort(fileNames);

        List<ChangesFile> chFiles = new ArrayList<ChangesFile>();
        for (int j = 0; j < fileNames.length; j++) {
          File ch = new File(memberDir, fileNames[j]);
          chFiles.add(new ChangesFile(ch, "", Long.parseLong(fileNames[j])));
        }
        ChangesLogStorage<ItemState> storage = new ChangesLogStorage<ItemState>(chFiles,
                                                                                new Member(null,
                                                                                           memberPriority));
        changeStorages.add(storage);
      } catch (NumberFormatException e) {
        // This is not int-named file. Skip it.
        LOG.warn("Illegal named file in storage - " + childnames[i]); 
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

}
