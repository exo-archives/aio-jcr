/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.ftp;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
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
