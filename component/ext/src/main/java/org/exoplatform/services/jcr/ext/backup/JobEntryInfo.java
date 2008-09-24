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
package org.exoplatform.services.jcr.ext.backup;

import java.net.URL;
import java.util.Calendar;

/**
 * Created by The eXo Platform SAS Author : Alex Reshetnyak alex.reshetnyak@exoplatform.com.ua Nov
 * 28, 2007
 */
public class JobEntryInfo {
  private int      type;

  private int      state;

  private URL      url;

  private Integer  id;

  private Calendar calendar;

  public int getType() {
    return type;
  }

  public int getState() {
    return state;
  }

  public URL getURL() {
    return url;
  }

  public Calendar getDate() {
    return calendar;
  }

  public void setType(int type) {
    this.type = type;
  }

  public void setState(int state) {
    this.state = state;
  }

  public void setURL(URL url) {
    this.url = url;
  }

  public void setDate(Calendar calendar) {
    this.calendar = calendar;
  }

  public void setID(Integer id) {
    this.id = id;
  }

  public Integer getID() {
    return id;
  }
}
