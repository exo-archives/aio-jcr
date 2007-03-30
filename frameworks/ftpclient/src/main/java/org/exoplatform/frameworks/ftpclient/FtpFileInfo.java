/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.ftpclient;

/**
* Created by The eXo Platform SARL        .
* @author Vitaly Guly
* @version $Id: $
*/

public interface FtpFileInfo {

  public void setName(String name);
  public String getName();
  
  public void setSize(long size);
  public long getSize();
  
  public void setType(boolean collection);
  public boolean isCollection();
  
  public void setDate(String date);
  public String getDate();
  
  public void setTime(String time);
  public String getTime();
  
  public boolean parseDir(String fileLine, String systemType);
  
}
