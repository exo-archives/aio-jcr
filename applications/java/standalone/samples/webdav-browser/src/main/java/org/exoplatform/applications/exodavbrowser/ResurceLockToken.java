/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.applications.exodavbrowser;

/**
 * Created by The eXo Platform SARL
 * Author : Alex Reshetnyak
 *          alex_reshetnyak@yahoo.com
 * ${date}  
 */

public class ResurceLockToken {
  private String sResurcePath;
  private String sLockToken;
  
  public ResurceLockToken(String ResurcePath, String LockToken) {
    sResurcePath = new String(ResurcePath);
    sLockToken = new String(LockToken);
  }
  
  public String getResurcePath(){
    return sResurcePath;
  } 
  
  public String getLockToken(){
    return sLockToken;
  }
}