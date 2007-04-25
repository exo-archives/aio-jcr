/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.storage.jdbc;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class ValueReference {
  
  private final int    orderNumber;

  private final String uuid;

  public ValueReference(String uuid, int orderNumber) {
    this.orderNumber = orderNumber;
    this.uuid = uuid;
  }

  public int getOrderNumber() {
    return orderNumber;
  }

  public String getUuid() {
    return uuid;
  }
}
