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
package org.exoplatform.services.jcr.ext.audit;

import java.util.Calendar;

import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.observation.ExtendedEventType;

/**
 * Created by The eXo Platform SAS        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class AuditRecord implements Comparable<AuditRecord> {
  
  private final String userId;
  private final int eventType;
  private final Calendar date;
  private final InternalQName propertyName;
  
  public AuditRecord(final String userId, final int eventType, final Calendar date
      ,InternalQName propertyName) {
    this.userId = userId;
    this.eventType = eventType;
    this.date = date;
    this.propertyName = propertyName;
  }

  public Calendar getDate() {
    return date;
  }

  public int getEventType() {
    return eventType;
  }

  public String getUserId() {
    return userId;
  }
  
  public String getEventTypeName() {
    return ExtendedEventType.nameFromValue(eventType);
  }

  public InternalQName getPropertyName() {
    return propertyName;
  }

  public int compareTo(AuditRecord otherRecord) {
    return date.compareTo(otherRecord.getDate());
  }


}
