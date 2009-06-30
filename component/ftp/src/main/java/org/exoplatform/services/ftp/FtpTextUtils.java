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
package org.exoplatform.services.ftp;

import org.exoplatform.services.log.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * 
 * @version $Id: $
 */

public class FtpTextUtils {

  private static Log log = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "FtpTextUtils");

  public static String getStrached(String strVal, int reqLen) {
    try {
      String datka = "";
      for (int i = 0; i < reqLen; i++) {
        if (i >= strVal.length()) {
          datka += " ";
        } else {
          datka += strVal.charAt(i);
        }
      }
      return datka;
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage());
      exc.printStackTrace();
    }
    String resStr = "";
    for (int i = 0; i < reqLen; i++) {
      resStr += " ";
    }
    return resStr;
  }

  public static String getStrachedAtStart(String strVal, int reqLen) {
    String result = strVal;
    while (result.length() < reqLen) {
      result = " " + result;
    }
    return result;
  }

}
