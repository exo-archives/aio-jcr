/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.ftp.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public interface FtpDataTransiver {

  int getDataPort();
  
  boolean isConnected();
  
  void close();
  
  InputStream getInputStream() throws IOException;
  
  OutputStream getOutputStream() throws IOException;
  
}
