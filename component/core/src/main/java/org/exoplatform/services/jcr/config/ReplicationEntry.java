/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.config;

/**
 * Created by The eXo Platform SAS Author : Alex Reshetnyak
 * alex.reshetnyak@exoplatform.org.ua reshetnyak.alex@gmail.com 19.12.2006
 * 16:43:38
 * 
 * @version $Id: ReplicationEntry.java 19.12.2006 16:43:38 rainf0x
 */
public class ReplicationEntry {
  
  private boolean enabled;
  
  private String channelConfig;
  
  private String mode;
  
  private String bindIPAddress;
  
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
  
  public String getBindIPAddress() {
    return bindIPAddress;
  }
  
  public void setBindIPAddress(String ipAddress) {
    this.bindIPAddress = ipAddress;
  }

  public boolean isTestMode(){
    return this.testMode;
  }
  
  public void setTestMode(boolean testMode) {
    this.testMode = testMode;
  }
}
