/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.maven.plugin.exo;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Created by The eXo Platform SARL Author : Tuan Nguyen Nov 18, 2005
 * 
 * @goal package
 * @requiresDependencyResolution runtime
 * @description mvn exo:package
 */
public class ExoPackaging extends AbstractMojo {
  /**
   * @parameter expression="${project}"
   * @required
   */
  protected MavenProject         project;

  /**
   * @parameter
   */
  private List<PackagingConfiguration> packagingConfigs ;
  
  public void execute() throws MojoExecutionException {
    PackagingConfiguration packagingConfig  = null; 
    for(PackagingConfiguration config : packagingConfigs) {
      if(config.getType().equals(project.getPackaging())) {
        packagingConfig = config ;
        break ;
      }
    }
    setDefaultPConfig(packagingConfig);
    List<String> scripts = packagingConfig.getScripts() ;
    try {
      for (String command : scripts) {
        if (command.endsWith(".class")) {
          String className = command.substring(0, command.indexOf(".class"));
          ClassLoader cl = Thread.currentThread().getContextClassLoader();
          PackagingCommand commandObject = 
            (PackagingCommand) cl.loadClass(className).newInstance();
          commandObject.execute(packagingConfig);
        } else {
          Binding binding = new Binding();
          binding.setVariable("packagingConfig", packagingConfig);
          binding.setVariable("FileUtil", ExoFileUtils.class);
          GroovyShell shell = new GroovyShell(binding);
          shell.evaluate(command);
        }
      }
    } catch (Exception ex) {
      throw new MojoExecutionException("FALSE", ex);
    }
  }

  private void setDefaultPConfig(PackagingConfiguration packagingConfig) {
    String outputDirectory = packagingConfig.getOutputDirectory();
    String outputFileName = packagingConfig.getOutputFileName();
    if (packagingConfig.getMavenProject() == null)
      packagingConfig.setMavenProject(project);
    if (outputDirectory == null || outputDirectory.equalsIgnoreCase(""))
      packagingConfig.setOutputDirectory(packagingConfig.getMavenProject().getBuild().getDirectory());
    if (outputFileName == null || outputFileName.equalsIgnoreCase(""))
      packagingConfig.setOutputFileName(packagingConfig.getMavenProject().getBuild().getFinalName());
    if (packagingConfig.getScripts().isEmpty()) {
      throw new RuntimeException("You need to define the chained commands") ;
    }
  }
}
