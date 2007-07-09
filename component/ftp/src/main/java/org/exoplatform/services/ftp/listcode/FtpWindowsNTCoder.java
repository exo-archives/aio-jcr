/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.ftp.listcode;

import org.exoplatform.services.ftp.FtpTextUtils;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class FtpWindowsNTCoder implements FtpSystemCoder {

  public String serializeFileInfo(FtpFileInfo fileInfo) {
    String date = FtpTextUtils.getStrached("07-11-06", 10);
    String time = FtpTextUtils.getStrached("12:03PM", 14);
    String dir;
    String size;
    if (fileInfo.isCollection()) {
      dir = "<DIR>";
      size = FtpTextUtils.getStrached("", 9);
    } else {
      dir = FtpTextUtils.getStrached("", 5);
      size = FtpTextUtils.getStrachedAtStart(String.format("%s", fileInfo.getSize()), 9);
    }
    
    return date + time + dir + size + " " + fileInfo.getName();
  }
  
}
