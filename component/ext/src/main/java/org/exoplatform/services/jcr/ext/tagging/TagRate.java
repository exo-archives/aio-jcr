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

package org.exoplatform.services.jcr.ext.tagging;

import java.util.Calendar;

/**
 * Created by The eXo Platform SAS        .
 * @author eXo Platform
 * @version $Id: $
 */

public class TagRate {
  
  private final String tagName;
  
  private final int count;
  
  private final int rate;
  
  private final Calendar updated;

  public TagRate(String tagName, int count, int rate, Calendar updated) {
    this.tagName = tagName;
    this.count = count;
    this.rate = rate;
    this.updated = updated;
  }

  public String getTagName() {
    return tagName;
  }

  public int getCount() {
    return count;
  }

  public int getRate() {
    return rate;
  }

  public Calendar getUpdated() {
    return updated;
  }  
  
}

