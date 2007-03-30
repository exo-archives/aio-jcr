/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.maven2.plugin.exo;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan08@users.sourceforge.net
 * Dec 30, 2005
 */
public class DeployConfiguration {
  private String deployLibDir ;
  private String deployWebappDir ;

  public String getDeployLibDir() {
    return deployLibDir;
  }

  public void setDeployLibDir(String deployLibDir) {
    this.deployLibDir = deployLibDir;
  }

  public String getDeployWebappDir() {
    return deployWebappDir;
  }

  public void setDeployWebappDir(String deployWebappDir) {
    this.deployWebappDir = deployWebappDir;
  }
}
