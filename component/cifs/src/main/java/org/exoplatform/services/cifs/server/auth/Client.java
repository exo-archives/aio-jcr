/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.cifs.server.auth;

/**
 * This class contain info about auhenticated user
 * 
 * Created by The eXo Platform SAS Author : Sergey Karpenko
 * <sergey.karpenko@exoplatform.com.ua>
 * 
 * @version $Id: $
 */

public class Client {

  public static final String GUEST_NAME = "__anonim";
  
  // Logon types

  public final static int LOGON_NORMAL = 0;
  public final static int LOGON_GUEST = 1;
  public final static int LOGON_NULL_SESSION = 2;
  
  private String username;

  private String plainpassword;
  
  private int logonType;

  public Client(String name, String pass) {
    username = name;
    plainpassword = pass;
  }

  public String getUsername(){
    return username;
  }
  
  public String getPassword(){
    return plainpassword;
  }
  
  public boolean isGuest(){
    return (logonType == LOGON_GUEST);
  }
  
  public void setGuest(boolean isguest){
    setLogonType(isguest == true ? LOGON_GUEST : LOGON_NORMAL);
  }
  

  public final void setLogonType(int logonType)
  {
      this.logonType = logonType;
  }
  
  public boolean isNullSession(){
    return logonType==LOGON_NULL_SESSION;
  }
  
}
