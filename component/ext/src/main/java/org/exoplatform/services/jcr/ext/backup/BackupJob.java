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

import java.net.URL;
import java.util.Calendar;

import org.exoplatform.services.jcr.core.ManageableRepository;

/**
 * Created by The eXo Platform SARL .<br/>
 * 
 * @author Gennady Azarenkov
 * @version $Id$
 */

public interface BackupJob extends Runnable {

  public static final int FULL        = 1;

  public static final int INCREMENTAL = 2;

  public static final int STARTING    = 0;

  public static final int WAITING     = 1;

  public static final int WORKING     = 2;

  public static final int FINISHED    = 4;

  /**
   * FULL or INCREMENTAL
   * 
   * @return
   */
  int getType();

  int getState();

  int getId();

  URL getStorageURL() throws BackupOperationException;

  void stop();

  void init(ManageableRepository repository,
            String workspaceName,
            BackupConfig config,
            Calendar timeStamp);

  void addListener(BackupJobListener listener);

  void removeListener(BackupJobListener listener);

}
