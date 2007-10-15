/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.dataflow;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.log.ExoLogger;


/**
 * 
 * Created by The eXo Platform SARL        .
 * 
 * @author Gennady Azarenkov
 * @version $Id:AbstractValueData.java 12534 2007-02-02 15:30:52Z peterit $
 */

public abstract class AbstractValueData implements ValueData {

  protected final static Log log = ExoLogger.getLogger("jcr.AbstractValueData");
  
  protected int orderNumber;
  
  protected AbstractValueData(int orderNumber) {
    this.orderNumber = orderNumber;
  }

  public final int getOrderNumber() {
    return orderNumber;
  }

  public final void setOrderNumber(int orderNumber) {
    this.orderNumber = orderNumber;
  }
  
  public abstract TransientValueData createTransientCopy();
}
