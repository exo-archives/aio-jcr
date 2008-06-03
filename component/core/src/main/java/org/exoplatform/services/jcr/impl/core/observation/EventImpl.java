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
package org.exoplatform.services.jcr.impl.core.observation;

import javax.jcr.observation.Event;

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: EventImpl.java 11907 2008-03-13 15:36:21Z ksm $
 */

public class EventImpl implements Event {

  private int type;

  private String path;

  private String userId;

  public EventImpl(int type, String path, String userId) {

    this.type = type;
    this.userId = userId;
    this.path = path;
  }
  
  /**
   * @see javax.jcr.observation.Event#getType
   */
  public int getType() {
    return this.type;
  }

  /**
   * @see javax.jcr.observation.Event#getPath
   */
  public String getPath() {
    return this.path;
  }

  /**
   * @see javax.jcr.observation.Event#getUserId
   */
  public String getUserID() {
    return this.userId;
  }

}
