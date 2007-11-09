/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.ftp.listcode;

import org.apache.commons.logging.Log;
import org.exoplatform.services.ftp.FtpConst;
import org.exoplatform.services.ftp.config.FtpConfig;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class FtpSystemCoderManager {

  private static Log log = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "FtpSystemCoderManager");
  
  private static final String [][]availableSysemCoders = {
    {FtpConst.Encoding.WINDOWS_NT, FtpWindowsNTCoder.class.getCanonicalName()},
    {FtpConst.Encoding.UNIX_L8, FtpUnixL8Coder.class.getCanonicalName()}
  };
  
  public static FtpSystemCoder getSystemCoder(FtpConfig configuration) {
    String systemType = configuration.getSystemType();
    for (int i = 0; i < availableSysemCoders.length; i++) {
      if (systemType.equals(availableSysemCoders[i][0])) {
        try {
          FtpSystemCoder coder = (FtpSystemCoder)Class.forName(availableSysemCoders[i][1]).newInstance();           
          return coder;            
        } catch (Exception exc) {
          log.info("Unhandled exception. " + exc.getMessage(), exc);
        }
      }
    }
    return null;
  }
  
}
