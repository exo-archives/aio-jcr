/**
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.dataflow;

import java.util.List;

/**
 * Created by The eXo Platform SARL        .<br/>
 * Plain changes log implementation (i.e. no nested logs inside)
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public interface PlainChangesLog extends ItemStateChangesLog {
  
  /**
   * @return sessionId of a session produced this changes log
   */
  String getSessionId();
  
  /**
   * @return event type produced this log
   * @see ExtendedEventType
   */
  int getEventType();

  /**
   * adds an item state object to the bottom of this log
   * @param state
   */
  PlainChangesLog add(ItemState state);
 
  /**
   * adds list of states object to the bottom of this log
   * @param states
   */
  PlainChangesLog addAll(List <ItemState> states);

  
  /**
   * @deprecated
   */
  void clear();
  
//  /**
//   * clones this changed log
//   * @return new changes log filled with this changes log data
//   */
//  PlainChangesLog copy();
}
