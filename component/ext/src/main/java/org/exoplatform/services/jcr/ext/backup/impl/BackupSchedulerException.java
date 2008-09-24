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
package org.exoplatform.services.jcr.ext.backup.impl;

import org.exoplatform.services.jcr.ext.backup.BackupOperationException;

/**
 * Created by The eXo Platform SAS Author : Peter Nedonosko peter.nedonosko@exoplatform.com.ua
 * 06.12.2007
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: BackupSchedulerException.java 610 2008-01-11 17:01:49Z pnedonosko $
 */
public class BackupSchedulerException extends BackupOperationException {

  public BackupSchedulerException(String message) {
    super(message);
  }

  public BackupSchedulerException(String message, Throwable e) {
    super(message, e);
  }

  public BackupSchedulerException(Throwable e) {
    super(e.getMessage(), e);
  }
}
