/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.config;

/**
 * Created by The eXo Platform SARL Author : Alex Reshetnyak
 * alex.reshetnyak@exoplatform.org.ua reshetnyak.alex@gmail.com 19.12.2006
 * 16:43:38
 * 
 * @version $Id: ReplicationEntry.java 19.12.2006 16:43:38 rainf0x
 */
public class ReplicationEntry {
  
  private boolean enabled;
  
  private String channelConfig;
  
  private String mode;
  
  private boolean testMode;
  
  public ReplicationEntry() {
    super();
  }
  
  public boolean isEnabled(){
    return this.enabled;
  }
  
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
  
  public String getChannelConfig(){
    return this.channelConfig;
  }
  
  public void setChannelConfig(String channelConfig){
    this.channelConfig = channelConfig;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public boolean isTestMode(){
    return this.testMode;
  }
  
  public void setTestMode(boolean testMode) {
    this.testMode = testMode;
  }
}
