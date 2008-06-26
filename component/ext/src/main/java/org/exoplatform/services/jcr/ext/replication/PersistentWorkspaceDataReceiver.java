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
package org.exoplatform.services.jcr.ext.replication;

import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspacePersistentDataManager;

/**
 * Created by The eXo Platform SAS
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a> 
 * @version $Id: PersistentWorkspaceDataReceiver.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class PersistentWorkspaceDataReceiver extends AbstractWorkspaceDataReceiver {

  public PersistentWorkspaceDataReceiver(WorkspacePersistentDataManager dataManager)
      throws RepositoryConfigurationException {
    super();
    dataKeeper = dataManager;
  }
}
