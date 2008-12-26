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
package org.exoplatform.services.jcr.ext.replication.async.merge;

import java.io.IOException;
import java.util.List;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.IllegalPathException;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExportException;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 10.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public interface ChangesMerger {

  /**
   * Tell if local changes has high priority.
   * 
   * @return boolean
   */
  boolean isLocalPriority();

  /**
   * 
   * Merge income changes with local and return result list of item states.
   * 
   * @param itemChange
   *          TODO
   * @param income
   *          CompositeChangesLog with income changes
   * @param local
   *          CompositeChangesLog with local changes
   * 
   * @return List of item states with resulting changes
   * @throws IllegalPathException
   * @throws RepositoryException
   * @throws RemoteExportException
   *           TODO
   * @throws IOException
   */
  List<ItemState> merge(ItemState itemChange,
                        ChangesStorage income,
                        ChangesStorage local) throws IllegalPathException,
                                                    RepositoryException,
                                                    RemoteExportException;

}
