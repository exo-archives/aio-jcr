/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.ftp.listcode;

import javax.jcr.Node;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public interface FtpFileInfo {

  void initFromNode(Node node) throws Exception; 
  
  void setName(String name);
  
  String getName();
  
  void setType(boolean collection);
  
  boolean isCollection();
  
  void setSize(long size);

  long getSize();
  
  void setDateTime(String dateTime);
  
  String getDateTime();
  
  String getMonth();
  
  int getDay();
  
  String getTime();  
  
}
