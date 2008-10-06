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
package org.exoplatform.services.jcr.impl.storage;

import java.util.Calendar;
import java.util.Date;

import org.exoplatform.services.jcr.storage.WorkspaceDataContainer;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: WorkspaceDataContainerBase.java 11907 2008-03-13 15:36:21Z ksm $
 */
abstract public class WorkspaceDataContainerBase implements WorkspaceDataContainer {

  /**
   * Read-only status.
   */
  protected boolean readOnly = false;

  /**
   * {@inheritDoc}
   */
  public Calendar getCurrentTime() {
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    return cal;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isReadOnly() {
    return readOnly;
  }

  /**
   * {@inheritDoc}
   */
  public void setReadOnly(boolean status) {
    this.readOnly = status;
  }
}
