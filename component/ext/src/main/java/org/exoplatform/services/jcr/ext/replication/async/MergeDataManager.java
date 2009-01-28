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
package org.exoplatform.services.jcr.ext.replication.async;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.ext.replication.async.merge.AddMerger;
import org.exoplatform.services.jcr.ext.replication.async.merge.DeleteMerger;
import org.exoplatform.services.jcr.ext.replication.async.merge.MixinMerger;
import org.exoplatform.services.jcr.ext.replication.async.merge.RenameMerger;
import org.exoplatform.services.jcr.ext.replication.async.merge.UpdateMerger;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogReadException;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.EditableChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.Member;
import org.exoplatform.services.jcr.ext.replication.async.storage.MemberChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.SolidEditableItemStatesStorage;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/> Merge manager per Workspace.
 * 
 * <br/>Date: 10.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class MergeDataManager {

  protected final RemoteExporter      exporter;

  protected final DataManager         dataManager;

  protected final NodeTypeDataManager ntManager;

  /**
   * Flag allowing run of merge.
   */
  private volatile boolean            run         = true;

  private final String                storageDir;

  private Member                      localMember = null;

  /**
   * Log.
   */
  protected static final Log          LOG         = ExoLogger.getLogger("jcr.MergerDataManager");

  MergeDataManager(RemoteExporter exporter,
                   DataManager dataManager,
                   NodeTypeDataManager ntManager,
                   String storageDir) {

    this.exporter = exporter;

    this.dataManager = dataManager;

    this.ntManager = ntManager;

    this.storageDir = storageDir;
  }

  /**
   * @param localMember
   *          the localMember to set
   */
  public void setLocalMember(Member localMember) {
    this.localMember = localMember;
  }

  private File makePath(Member first, Member second) {
    File dir = new File(storageDir, first.getPriority() + "-" + second.getPriority());
    dir.mkdirs();
    return dir;
  }

  private File makePath(String dirName) {
    File dir = new File(storageDir, dirName);
    dir.mkdirs();
    return dir;
  }

  /**
   * Start merge process.
   * 
   * @param membersChanges
   *          List of members changes
   * @throws RepositoryException
   * @throws RemoteExportException
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws ClassCastException
   * @throws MergeDataManagerException
   */
  public ChangesStorage<ItemState> merge(Iterator<MemberChangesStorage<ItemState>> membersChanges) throws RepositoryException,
                                                                                                  RemoteExportException,
                                                                                                  IOException,
                                                                                                  ClassCastException,
                                                                                                  ClassNotFoundException,
                                                                                                  MergeDataManagerException,
                                                                                                  ChangesLogReadException {

    try {

      // TODO init
      EditableChangesStorage<ItemState> interChanges = null;
      EditableChangesStorage<ItemState> iteratorChanges = new SolidEditableItemStatesStorage<ItemState>(makePath("iterator"),
                                                                                                        null);
      EditableChangesStorage<ItemState> resultChanges = new SolidEditableItemStatesStorage<ItemState>(makePath("result"),
                                                                                                      localMember);

      MemberChangesStorage<ItemState> local;
      MemberChangesStorage<ItemState> income;

      MemberChangesStorage<ItemState> first = membersChanges.next();

      // prepare
      if (localMember.getPriority() == first.getMember().getPriority()) {
        Iterator<ItemState> st = first.getChanges();
        while (st.hasNext())
          iteratorChanges.add(st.next());
      }

      while (membersChanges.hasNext() && run) {
        MemberChangesStorage<ItemState> second = membersChanges.next();

        List<QPath> skippedList = new ArrayList<QPath>();

        // TODO if we merging two remote members, how localMember can be used for isLocalPriority
        // value?
        boolean isLocalPriority = localMember.getPriority() >= second.getMember().getPriority();
        if (isLocalPriority) {
          income = first;
          local = second;
        } else {
          income = second;
          local = first;
        }

        LOG.info("Merge changes (local=" + isLocalPriority + ") from "
            + first.getMember().getPriority() + " (" + first.getMember().getAddress() + ") and "
            + second.getMember().getPriority() + " (" + second.getMember().getAddress()
            + ") members");

        // synchronizedChanges always with higher priority (income)
        interChanges = new SolidEditableItemStatesStorage<ItemState>(makePath(first.getMember(),
                                                                              second.getMember()),
                                                                     second.getMember());

        exporter.setRemoteMember(second.getMember().getAddress());
        // TODO NT reregistration
        AddMerger addMerger = new AddMerger(isLocalPriority, exporter, dataManager, ntManager);
        DeleteMerger deleteMerger = new DeleteMerger(isLocalPriority,
                                                     exporter,
                                                     dataManager,
                                                     ntManager);
        UpdateMerger udpateMerger = new UpdateMerger(isLocalPriority,
                                                     exporter,
                                                     dataManager,
                                                     ntManager);
        RenameMerger renameMerger = new RenameMerger(isLocalPriority,
                                                     exporter,
                                                     dataManager,
                                                     ntManager);
        MixinMerger mixinMerger = new MixinMerger(isLocalPriority, exporter, dataManager, ntManager);

        if (run) {
          Iterator<ItemState> changes = income.getChanges();
          if (changes.hasNext()) {
            outer: while (changes.hasNext() && run) {
              ItemState incomeChange = changes.next();

              LOG.info("\t\tMerging income item "
                  + ItemState.nameFromValue(incomeChange.getState()) + " "
                  + incomeChange.getData().getQPath().getAsString());

              // skip already processed itemstate
              if (interChanges.hasState(incomeChange)) {
                LOG.info("\t\tSkip income item " + ItemState.nameFromValue(incomeChange.getState())
                    + " " + incomeChange.getData().getQPath().getAsString());
                continue;
              }

              // skip subtree changes
              for (int i = 0; i < skippedList.size(); i++) {
                if (incomeChange.getData().getQPath().equals(skippedList.get(i))
                    || incomeChange.getData().getQPath().isDescendantOf(skippedList.get(i))) {
                  LOG.info("\t\tMerging income item "
                      + ItemState.nameFromValue(incomeChange.getState()) + " "
                      + incomeChange.getData().getQPath().getAsString());
                  continue outer;
                }
              }

              // skip lock properties
              if (!incomeChange.getData().isNode()) {
                if (incomeChange.getData().getQPath().getName().equals(Constants.JCR_LOCKISDEEP)
                    || incomeChange.getData().getQPath().getName().equals(Constants.JCR_LOCKOWNER)) {
                  continue;
                }
              }

              switch (incomeChange.getState()) {
              case ItemState.ADDED:
                interChanges.addAll(addMerger.merge(incomeChange,
                                                    income,
                                                    local,
                                                    storageDir,
                                                    skippedList));
                break;
              case ItemState.DELETED:
                // DELETE
                if (incomeChange.isPersisted()) {
                  interChanges.addAll(deleteMerger.merge(incomeChange,
                                                         income,
                                                         local,
                                                         storageDir,
                                                         skippedList));
                } else {
                  ItemState nextIncomeChange = income.findNextState(incomeChange,
                                                                    incomeChange.getData()
                                                                                .getIdentifier());

                  // RENAME
                  if (nextIncomeChange != null && nextIncomeChange.getState() == ItemState.RENAMED) {

                    // skip processed itemstates
                    if (interChanges.hasState(nextIncomeChange.getData().getIdentifier(),
                                              nextIncomeChange.getData().getQPath(),
                                              ItemState.ADDED)) {
                      continue;
                    }

                    interChanges.addAll(renameMerger.merge(incomeChange,
                                                           income,
                                                           local,
                                                           storageDir,
                                                           skippedList));

                    // UPDATE node
                  } else if (nextIncomeChange != null
                      && nextIncomeChange.getState() == ItemState.UPDATED) {
                    interChanges.addAll(udpateMerger.merge(incomeChange,
                                                           income,
                                                           local,
                                                           storageDir,
                                                           skippedList));
                  } else {
                    LOG.info("Income changes log: " + income.dump());
                    LOG.info("Local changes log: " + local.dump());

                    throw new MergeDataManagerException("Can not resolve merge. Unknown DELETE sequence."
                        + "[path="
                        + incomeChange.getData().getQPath().getAsString()
                        + "][identifier="
                        + incomeChange.getData().getIdentifier()
                        + "][parentIdentifier="
                        + incomeChange.getData().getParentIdentifier()
                        + "]");
                  }
                }
                break;
              case ItemState.UPDATED:
                // UPDATE property
                if (!incomeChange.getData().isNode()) {
                  interChanges.addAll(udpateMerger.merge(incomeChange,
                                                         income,
                                                         local,
                                                         storageDir,
                                                         skippedList));
                }
                break;
              case ItemState.MIXIN_CHANGED:
                interChanges.addAll(mixinMerger.merge(incomeChange,
                                                      income,
                                                      local,
                                                      storageDir,
                                                      skippedList));
                break;
              }
            }
          }

          // add changes to resulted changes and prepare changes for next merge iteration
          if (!isLocalPriority) {
            Iterator<ItemState> st = interChanges.getChanges();
            while (st.hasNext()) {
              ItemState item = st.next();

              iteratorChanges.add(item);
              resultChanges.add(item);
            }

          } else {
            iteratorChanges.delete();

            Iterator<ItemState> st = second.getChanges();
            while (st.hasNext())
              iteratorChanges.add(st.next());

            st = interChanges.getChanges();
            while (st.hasNext()) {
              ItemState item = st.next();

              iteratorChanges.add(item);
              if (localMember.getPriority() == second.getMember().getPriority())
                resultChanges.add(item);
            }
          }

          first = iteratorChanges;
        }
      }

      // if success
      return resultChanges;

    } finally {
      run = true;
    }
  }

  /**
   * Cancel current merge process.
   * 
   * @throws RepositoryException
   * @throws RemoteExportException
   */
  public void cancel() throws RepositoryException, RemoteExportException {
    run = false;
  }
}
