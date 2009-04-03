/**
 * 
 */
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

import javax.jcr.InvalidItemStateException;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PersistentDataManager;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.LocalStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.ReplicableValueData;
import org.exoplatform.services.jcr.ext.replication.async.storage.StorageRuntimeException;
import org.exoplatform.services.jcr.ext.replication.async.storage.SynchronizationException;
import org.exoplatform.services.jcr.ext.replication.async.storage.SynchronizerChangesLog;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.io.WorkspaceFileCleanerHolder;
import org.exoplatform.services.jcr.storage.WorkspaceDataContainer;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/>Date: 12.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class WorkspaceSynchronizerImpl implements WorkspaceSynchronizer {

  private static final Log                   LOG = ExoLogger.getLogger("ext.WorkspaceSynchronizerImpl");

  protected final LocalStorage               storage;

  protected final PersistentDataManager     workspace;
  
  protected final PersistentDataManager systemWorkspace;

  protected final File                       tempDirectory;

  protected final int                        maxBufferSize;

  protected final WorkspaceFileCleanerHolder cleanerHolder;

  public WorkspaceSynchronizerImpl(PersistentDataManager workspace,
                                   PersistentDataManager systemWorkspace,
                                   LocalStorage storage,
                                   WorkspaceEntry workspaceConfig,
                                   WorkspaceFileCleanerHolder cleanerHolder) {
    this.storage = storage;
    this.workspace = workspace;
    this.systemWorkspace = systemWorkspace;
    
    this.maxBufferSize = workspaceConfig.getContainer()
                                        .getParameterInteger(WorkspaceDataContainer.MAXBUFFERSIZE,
                                                             WorkspaceDataContainer.DEF_MAXBUFFERSIZE);
    this.tempDirectory = new File(System.getProperty("java.io.tmpdir"));
    this.cleanerHolder = cleanerHolder;
  }

  /**
   * Return local changes.<br/> 1. to a merger<br/> 2. to a receiver
   * 
   * @return ChangesStorage
   */
  public ChangesStorage<ItemState> getLocalChanges() throws IOException {
    return storage.getLocalChanges(false);
  }

  /**
   * {@inheritDoc}
   */
  public void save(ChangesStorage<ItemState> synchronizedChanges) throws SynchronizationException,
                                                                 InvalidItemStateException,
                                                                 UnsupportedOperationException,
                                                                 RepositoryException {

    OnSynchronizationWorkspaceListenersFilter apiFilter = new OnSynchronizationWorkspaceListenersFilter();
    workspace.addItemPersistenceListenerFilter(apiFilter);
    systemWorkspace.addItemPersistenceListenerFilter(apiFilter);

    try {
      try {
        saveChangesLog(synchronizedChanges);
      } catch (ClassCastException e) {
        throw new SynchronizationException("Error of merge result save " + e, e);
      } catch (IOException e) {
        throw new SynchronizationException("Error of merge result save " + e, e);
      } catch (ClassNotFoundException e) {
        throw new SynchronizationException("Error of merge result save " + e, e);
      } catch (Throwable e) {
        throw new SynchronizationException("Error of merge result save " + e, e);
      }

      // TODO use it after TransactionChangesLog refactor in backup etc.
      // workspace.save(new SynchronizerChangesLog(synchronizedChanges));
    } catch (StorageRuntimeException e) {
      throw new SynchronizationException("Error of merge result read on save " + e, e);
    } finally {
      workspace.removeItemPersistenceListenerFilter(apiFilter);
      systemWorkspace.removeItemPersistenceListenerFilter(apiFilter);
    }
  }

  /**
   * ChangesStorage to TransactionChangesLog (SynchronizerChangesLog) conversion.
   * 
   * @param changes
   *          ChangesStorage
   * @return SynchronizerChangesLog
   * @throws IOException
   * @throws ClassCastException
   * @throws IllegalStateException
   * @throws ClassNotFoundException
   * @throws RepositoryException
   * @throws UnsupportedOperationException
   * @throws InvalidItemStateException
   */
  private void saveChangesLog(final ChangesStorage<ItemState> changes) throws IOException,
                                                                                        ClassCastException,
                                                                                        IllegalStateException,
                                                                                        ClassNotFoundException,
                                                                                        InvalidItemStateException,
                                                                                        UnsupportedOperationException,
                                                                                        RepositoryException {
    List<ItemState> states = new ArrayList<ItemState>();
    List<ItemState> sysStates = new ArrayList<ItemState>();

    for (Iterator<ItemState> iter = changes.getChanges(); iter.hasNext();) {
      ItemState state = iter.next();
      if (state.isNode()) {
        // use as is
        if (isSystemDescendant(state.getData().getQPath()))
          sysStates.add(state);
        else
          states.add(state);
      } else {
        // replace ReplicableValueData to Transient
        /*TransientPropertyData prop = (TransientPropertyData) state.getData();

        List<ValueData> nVals = new ArrayList<ValueData>();

        for (ValueData vd : prop.getValues()) {
          if (vd.isByteArray()) {
            nVals.add(vd);
          } else {
            ReplicableValueData rvd = (ReplicableValueData) vd;
            nVals.add(new TransientValueData(vd.getOrderNumber(),
                                             null,
                                             null,
                                             rvd.getSpoolFile(),
                                             cleanerHolder.getFileCleaner(),
                                             maxBufferSize,
                                             tempDirectory,
                                             true));
            rvd.getSpoolFile().release(rvd); // release file used by ReplicableValueData
          }
        }

        // rewrite values, TODO use values setter
        TransientPropertyData nProp = new TransientPropertyData(prop.getQPath(),
                                                                prop.getIdentifier(),
                                                                prop.getPersistedVersion(),
                                                                prop.getType(),
                                                                prop.getParentIdentifier(),
                                                                prop.isMultiValued());

        nProp.setValues(nVals);

        // create new ItemState
        ItemState newState = new ItemState(nProp,
                                           state.getState(),
                                           state.isEventFire(),
                                           state.getAncestorToSave(),
                                           state.isInternallyCreated(),
                                           state.isPersisted());*/

        if (isSystemDescendant(state.getData().getQPath()))
          sysStates.add(state);
        else
          states.add(state);
      }
    }

    // create new changes logs
    systemWorkspace.save(new SynchronizerChangesLog(new PlainChangesLogImpl(sysStates,IdGenerator.generate())));

    workspace.save(new SynchronizerChangesLog(new PlainChangesLogImpl(states,
                                                                      IdGenerator.generate())));
  }

  private boolean isSystemDescendant(QPath path) {
    return path.isDescendantOf(Constants.JCR_SYSTEM_PATH) || path.equals(Constants.JCR_SYSTEM_PATH);
  }
}
