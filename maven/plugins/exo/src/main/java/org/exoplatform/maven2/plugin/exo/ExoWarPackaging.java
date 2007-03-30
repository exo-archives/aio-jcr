/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.maven2.plugin.exo;


import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
/** 
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 * Nov 18, 2005
 * 
 * @goal war
 * @requiresDependencyResolution runtime
 * @description mvn war
 */
public class ExoWarPackaging extends AbstractMojo {
  /**
   * @parameter
   */
  private List<String> commands ;
  /**
   * @parameter
   */
  private PackagingConfiguration packagingConfig ;
  /**
   * @parameter
   */
  private DeployConfiguration deployConfig ;
  
  public void execute() throws MojoExecutionException {
    try {
      for(String script : commands) {
        Command command = new GroovyScriptCommand(script) ;
        command.execute(packagingConfig, deployConfig) ;
      }
    } catch (Exception ex) {
      throw new MojoExecutionException("FAIL", ex) ;
    }
  }
  
  //use  for testing  purpose
  public void setPackagingConfiguration(PackagingConfiguration pconfig) {
    packagingConfig = pconfig ;
  }
  
  //use  for testing  purpose
  public void setDeployConfiguration(DeployConfiguration dconfig) {
    deployConfig = dconfig ;
  }
  
  //use  for testing  purpose
  public void setCommands(List list) {   commands =  list ; }
  
  public void checkPackagingConfiguration() throws Exception {
    System.out.println("===> " + packagingConfig) ;
    System.out.println("===> " + packagingConfig.getOutputFileName()) ;
    System.out.println("===> " + packagingConfig.getOutputDirectory()) ;
    System.out.println("===> " + packagingConfig.getMavenProject()) ;
  }
}
