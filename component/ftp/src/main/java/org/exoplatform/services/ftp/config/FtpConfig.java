/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.ftp.config;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public interface FtpConfig {

  int getCommandPort();
  
  int getDataMinPort();
  
  int getDataMaxPort();
  
  String getSystemType();
  
  String getClientSideEncoding();
  
  String getDefFolderNodeType();
  
  String getDefFileNodeType();
  
  String getDefFileMimeType();
  
  String getCacheFolderName();
  
  boolean isNeedSlowUpLoad();
  
  int getUpLoadSpeed();
  
  boolean isNeedSlowDownLoad();
  
  int getDownLoadSpeed();
  
  boolean isNeedTimeOut();
  
  int getTimeOut();
  
}
