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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;

/**
 * Created by The eXo Platform SAS. <br/>Date: 26.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class IncomeStorageImpl implements IncomeStorage {

  protected final String        storagePath;

  protected final static String MEMBER_INFO_FILE_NAME = "member_info";

  public IncomeStorageImpl(String storagePath) {
    this.storagePath = storagePath;
  }

  /**
   * {@inheritDoc}
   */
  public void addMemberChanges(Member member, ChangesFile changes) throws IOException {
    // get member directory
    File dir = new File(storagePath, Integer.toString(member.getPriority()));

    File memberInfo = new File(dir, MEMBER_INFO_FILE_NAME);
    if (!memberInfo.exists()) {
      // store member info
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(memberInfo));
      out.writeObject(member);
      out.close();
    }

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

        Integer.parseInt(childnames[i]); // also check - is member folder;

        File memberDir = new File(incomStorage, childnames[i]);
        File memberInfo = new File(memberDir, MEMBER_INFO_FILE_NAME);
        Member member = null;
        if (memberInfo.exists()) {
          // read member info
          ObjectInputStream in = new ObjectInputStream(new FileInputStream(memberInfo));
          try {
            member = (Member) in.readObject();
          } catch (ClassNotFoundException e) {
            // TODO
          } finally {
            in.close();
          }
        } else {
          // TODO
        }

        String[] fileNames = memberDir.list();
        // Sort names in ascending mode
        java.util.Arrays.sort(fileNames);

        List<ChangesFile> chFiles = new ArrayList<ChangesFile>();
        for (int j = 0; j < fileNames.length; j++) {
          File ch = new File(memberDir, fileNames[j]);
          chFiles.add(new ChangesFile(ch, "", Long.parseLong(fileNames[j])));
        }
        ItemStatesStorage<ItemState> storage = new ItemStatesStorage<ItemState>(chFiles, member);
        changeStorages.add(storage);
      } catch (NumberFormatException e) {
        // This is not int-named file. Skip it.
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
