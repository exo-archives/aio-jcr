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
 * Dec 6, 2005
 */
/**
 * @goal release
 * @requiresDependencyResolution runtime
 */
public class ExoRelease extends AbstractMojo {
  /**
   * @parameter expression="${project}"
   * @required
   */
  protected MavenProject         project;
  /**
   * @parameter
   */
  private List<ReleaseConfiguration> releaseConfigurations ;
  
  public void execute() throws MojoExecutionException {
    List profiles = project.getActiveProfiles() ;
    Profile serverProfile = null ;
    for(int i=0; i < profiles.size(); i++) {
      Profile profile = (Profile) profiles.get(i) ;
      if(profile.getId().endsWith("-server")) {
        serverProfile = profile ;
      }
    }
    String selectServer = "tomcat-server" ;
    if(serverProfile != null)  selectServer = serverProfile.getId() ;
    ReleaseConfiguration selectedConfig = null ;
    for(ReleaseConfiguration releaseConfig : releaseConfigurations) {
      if(selectServer.equals(releaseConfig.getServerType())) {
        selectedConfig = releaseConfig ;
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
          ReleaseCommand commandObject = (ReleaseCommand) cl.loadClass(className).newInstance();
          commandObject.execute(selectedConfig);
        } else {
          Binding binding = new Binding();
          binding.setVariable("releaseConfig", selectedConfig);
          GroovyShell shell = new GroovyShell(binding);
          shell.evaluate(command);
        }
      }
    } catch (Exception ex) {
      throw new MojoExecutionException("FALSE", ex);
    }
  }
}
