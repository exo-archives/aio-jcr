/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.ext.audit;

import java.util.Calendar;

import org.exoplatform.services.jcr.datamodel.InternalQName;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class AuditRecord implements Comparable {
  
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
    return "TODO "+eventType;
  }

  public InternalQName getPropertyName() {
    return propertyName;
  }

  public int compareTo(Object o) {
    AuditRecord rec = (AuditRecord)o;
    return date.compareTo(rec.getDate());
  }

//  public static class RecordsComparator implements Comparable {
//
//    
//  }


}
