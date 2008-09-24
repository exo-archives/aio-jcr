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
package org.exoplatform.services.jcr.dataflow;

import java.util.List;

import org.exoplatform.services.jcr.observation.ExtendedEventType;
import org.exoplatform.services.jcr.impl.core.observation.EventImpl;

/**
 * Created by The eXo Platform SAS.<br/> Plain changes log implementation (i.e. no nested logs
 * inside)
 * 
 * @author Gennady Azarenkov
 * @version $Id: PlainChangesLog.java 11907 2008-03-13 15:36:21Z ksm $
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
   * 
   * @param state
   */
  PlainChangesLog add(ItemState state);

  /**
   * adds list of states object to the bottom of this log
   * 
   * @param states
   */
  PlainChangesLog addAll(List<ItemState> states);

  /**
   * @deprecated
   */
  void clear();
}
