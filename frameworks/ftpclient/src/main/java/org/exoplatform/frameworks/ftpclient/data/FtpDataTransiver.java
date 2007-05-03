/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.ftpclient.data;

/**
* Created by The eXo Platform SARL        .
* @author Vitaly Guly
* @version $Id: $
*/

public interface FtpDataTransiver {

  public void OpenPassive(String host, int port);
  public boolean OpenActive(int port);
  
  public boolean isConnected();
  public void close();
  
  public byte []receive();
  public boolean send(byte []data);
  
}
