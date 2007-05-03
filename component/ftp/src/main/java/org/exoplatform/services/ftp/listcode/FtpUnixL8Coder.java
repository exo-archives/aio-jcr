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

public class FtpUnixL8Coder implements FtpSystemCoder {

  public String serializeFileInfo(FtpFileInfo fileInfo) {
    String attr = String.format("%srw-------", fileInfo.isCollection() ? "d" : "-");
    String subFolders = "  1";
    String ftpDescr = "eXo      eXo    ";
    String size = FtpTextUtils.getStrachedAtStart(String.format("%s", fileInfo.getSize()), 9);
    String month = fileInfo.getMonth();
    String day = String.format("%d", fileInfo.getDay());
    day = FtpTextUtils.getStrachedAtStart(day, 2);
    String time = fileInfo.getTime();
    String name = fileInfo.getName();
    
    return String.format("%s %s %s %s %s %s %s %s",
        attr, subFolders, ftpDescr,
        size, month, day, time, name);
  }
  
}
