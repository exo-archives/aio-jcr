/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.backup;

import java.io.File;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.ext.backup.impl.BackupMessage;
import org.exoplatform.services.jcr.ext.backup.impl.BackupScheduler;

/**
 * Created by The eXo Platform SARL .<br/>
 * 
 * @author Gennady Azarenkov
 * @version $Id$
 */

public interface BackupManager {

  final static int FULL_BACKUP_ONLY     = 0;

  final static int FULL_AND_INCREMENTAL = 1;

  Set<BackupChain> getCurrentBackups();

  BackupChainLog[] getBackupsLogs();

  BackupChain startBackup(BackupConfig config) throws BackupOperationException,
                                              BackupConfigurationException,
                                              RepositoryException,
                                              RepositoryConfigurationException;

  void stopBackup(BackupChain backup);

  BackupChain findBackup(String reposytore, String workspace);

  void restore(BackupChainLog log, RepositoryEntry repository, WorkspaceEntry workspaceEntry) throws BackupOperationException,
                                                                                             BackupConfigurationException,
                                                                                             RepositoryException,
                                                                                             RepositoryConfigurationException;

  BackupScheduler getScheduler();

  BackupMessage[] getMessages();

  File getBackupDirectory();

  String getFullBackupType();

  String getIncrementalBackupType();
}
