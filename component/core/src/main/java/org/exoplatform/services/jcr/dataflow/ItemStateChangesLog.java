/**
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.dataflow;

import java.util.List;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public interface ItemStateChangesLog {

  /**
   * @return all states
   */
  List<ItemState> getAllStates();

  /**
   * @return number of stored states
   */
  int getSize();

  /**
   * @return info about this log
   */
  String dump();
  
}