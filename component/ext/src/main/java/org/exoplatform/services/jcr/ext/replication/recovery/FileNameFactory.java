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

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id$
 */

public class FileNameFactory {
  private DateFormat       datefName        = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");

  private static final int SUB_PATH_LENGTH  = 7;

  private static final int PATTERN_10       = 10;

  private static final int PATTERN_100      = 100;

  private static final int PATTERN_1000     = 1000;

  private final String     pathCharSequence = "0123456789abcdef";

  public String getStrDate(Calendar c) {
    // Returns as a String (YYYYMMDD) a Calendar date
    int m = c.get(Calendar.MONTH) + 1;
    int d = c.get(Calendar.DATE);
    return "" + c.get(Calendar.YEAR) + (m < PATTERN_10 ? "0" + m : m)
        + (d < PATTERN_10 ? "0" + d : d);
  }

  public String getStrTime(Calendar c) {
    // Returns as a String (YYYYMMDD_MS) a Calendar date
    int h = c.get(Calendar.HOUR_OF_DAY);
    int m = c.get(Calendar.MINUTE);
    int s = c.get(Calendar.SECOND);
    int ms = c.get(Calendar.MILLISECOND);

    return "" + (h < PATTERN_10 ? "0" + h : h) + (m < PATTERN_10 ? "0" + m : m)
        + (s < PATTERN_10 ? "0" + s : s) + "_"
        + (ms < PATTERN_100 ? (ms < PATTERN_10 ? "00" + ms : "0" + ms) : ms);
  }

  public String getTimeStampName(Calendar c) {
    return (getStrDate(c) + "_" + getStrTime(c));
  }

  public String getRandomSubPath() {
    String subPath = new String();

    for (int i = 0; i < SUB_PATH_LENGTH; i++) {
      int index = (int) (Math.random() * PATTERN_1000) % pathCharSequence.length();

      if (i != SUB_PATH_LENGTH - 1)
        subPath += (pathCharSequence.charAt(index) + File.separator);
      else
        subPath += pathCharSequence.charAt(index);
    }

    return subPath;
  }

  public Calendar getDateFromFileName(String fName) throws ParseException {
    Calendar c = Calendar.getInstance();

    String[] sArray = fName.split("_");

    Date d = datefName.parse(sArray[0] + "_" + sArray[1] + "_" + sArray[2]);
    c.setTime(d);

    return c;
  }
}
