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
 * Created by The eXo Platform SAS
 * Author : Alex Reshetnyak
 *          alex.reshetnyak@exoplatform.com.ua
 * 26.03.2008  
 */
public class PendingConfirmationChengesLog {
  private List<String> confirmationList;
  private List<String> notConfirmationList;
  private ItemStateChangesLog changesLog;
  private Calendar timeStamp;
  private String identifier;
  
  public PendingConfirmationChengesLog(ItemStateChangesLog changesLog, Calendar timeStamp, String identifier) {
    this.confirmationList = new ArrayList<String>();
    this.changesLog = changesLog;
    this.timeStamp = timeStamp;
    this.identifier = identifier;
  }

  public List<String> getConfirmationList() {
    return confirmationList;
  }

  public void setConfirmationList(List<String> confirmationList) {
    this.confirmationList = confirmationList;
  }

  public ItemStateChangesLog getChangesLog() {
    return changesLog;
  }

  public void setChangesLog(ItemStateChangesLog changesLog) {
    this.changesLog = changesLog;
  }

  public Calendar getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(Calendar timeStamp) {
    this.timeStamp = timeStamp;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public List<String> getNotConfirmationList() {
    return notConfirmationList;
  }

  public void setNotConfirmationList(List<String> notConfirmationList) {
    this.notConfirmationList = notConfirmationList;
  }
  
}