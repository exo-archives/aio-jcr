/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.maven.plugin.exo;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.util.List;

import org.apache.maven.model.Profile;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Created by The eXo Platform SARL Author : Phung Hai Nam phunghainam@gmail.com
 * Nov 23, 2005
 */
/**
 * @goal deploy
 * @requiresDependencyResolution runtime
 * @description deploy module or deploy resources
 */
public class ExoDeploy extends AbstractMojo {
  /**
   * @parameter expression="${project}"
   * @required
   */
  protected MavenProject         project;
  /**
   * @parameter
   */
  private List<DeployConfiguration> deployConfigurations ;

  public void execute() throws MojoExecutionException {
    List profiles = project.getActiveProfiles() ;
    Profile serverProfile = null ;
    for(int i=0; i < profiles.size(); i++) {
      Profile profile = (Profile) profiles.get(i) ;
      if(profile.getId().endsWith("-server")||profile.getId().endsWith("-perftest")) {
        serverProfile = profile ;
      }
    }
    String selectServer = "tomcat-server" ;
    if(serverProfile != null)  selectServer = serverProfile.getId() ;
    DeployConfiguration selectedConfig = null ;
    for(DeployConfiguration dconfig : deployConfigurations) {
      if(selectServer.equals(dconfig.getServerType())) {
        selectedConfig = dconfig ;
        break ;
      }
    }
    selectedConfig.setMavenProject(project) ;
    List<String> scripts = selectedConfig.getScripts() ;
    try {
      for (String command : scripts) {
        if (command.endsWith(".class")) {
          String className = command.substring(0, command.indexOf(".class"));
          ClassLoader cl = Thread.currentThread().getContextClassLoader();
          DeployCommand commandObject = (DeployCommand) cl.loadClass(className).newInstance();
          commandObject.execute(selectedConfig);
        } else {
          Binding binding = new Binding();
          binding.setVariable("deployConfig", selectedConfig);
          binding.setVariable("FileUtil", ExoFileUtils.class);
          GroovyShell shell = new GroovyShell(binding);
          shell.evaluate(command);
        }
      }
    } catch (Exception ex) {
      throw new MojoExecutionException("FALSE", ex);
    }
  }
}