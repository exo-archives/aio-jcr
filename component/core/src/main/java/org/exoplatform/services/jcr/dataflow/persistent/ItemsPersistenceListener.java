/**
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.dataflow.persistent;

import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;



/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: ItemsPersistenceListener.java 13421 2007-03-15 10:46:47Z geaz $
 */
public interface ItemsPersistenceListener {
  /**
   * called wnen data is permanently saved
   * @param itemStates
   */
  void onSaveItems(ItemStateChangesLog itemStates);
}
