/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.recovery;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id$
 */

public class PendingConfirmationChengesLog {
  /**
   * The list of members who has been saved successfully.
   */
  private List<String>        confirmationList;

  /**
   * The list of members who has not been saved successfully.
   */
  private List<String>        notConfirmationList;

  /**
   * Pending the ChangesLog.
   */
  private ItemStateChangesLog changesLog;

  /**
   * The date of save the ChchgesLog.
   */
  private Calendar            timeStamp;

  /**
   * The identification string to PendingConfirmationChengesLog.
   */
  private String              identifier;

  /**
   * PendingConfirmationChengesLog constructor.
   * 
   * @param changesLog
   *          the ChangesLog with data
   * @param timeStamp
   *          the save date
   * @param identifier
   *          the identifier string
   */
  public PendingConfirmationChengesLog(ItemStateChangesLog changesLog,
                                       Calendar timeStamp,
                                       String identifier) {
    this.confirmationList = new ArrayList<String>();
    this.changesLog = changesLog;
    this.timeStamp = timeStamp;
    this.identifier = identifier;
  }

  /**
   * getConfirmationList.
   * 
   * @return List return the list of members who has not been saved successfully
   */
  public List<String> getConfirmationList() {
    return confirmationList;
  }

  /**
   * setConfirmationList.
   * 
   * @param confirmationList
   *          the list of members who has been saved successfully
   */
  public void setConfirmationList(List<String> confirmationList) {
    this.confirmationList = confirmationList;
  }

  /**
   * getChangesLog.
   * 
   * @return ItemStateChangesLog return the ChangesLog
   */
  public ItemStateChangesLog getChangesLog() {
    return changesLog;
  }

  /**
   * setChangesLog.
   * 
   * @param changesLog
   *          the ChangesLog
   */
  public void setChangesLog(ItemStateChangesLog changesLog) {
    this.changesLog = changesLog;
  }

  /**
   * getTimeStamp.
   * 
   * @return Calendar return the date of ChangesLog
   */
  public Calendar getTimeStamp() {
    return timeStamp;
  }

  /**
   * setTimeStamp.
   * 
   * @param timeStamp
   *          the Calendar
   */
  public void setTimeStamp(Calendar timeStamp) {
    this.timeStamp = timeStamp;
  }

  /**
   * getIdentifier.
   * 
   * @return String return the identification string
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * setIdentifier.
   * 
   * @param identifier
   *          the identification string
   */
  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  /**
   * getNotConfirmationList.
   * 
   * @return List return the list of members who has not been saved successfully
   */
  public List<String> getNotConfirmationList() {
    return notConfirmationList;
  }

  /**
   * setNotConfirmationList.
   * 
   * @param notConfirmationList
   *          the list of members who has not been saved successfully
   */
  public void setNotConfirmationList(List<String> notConfirmationList) {
    this.notConfirmationList = notConfirmationList;
  }
}
