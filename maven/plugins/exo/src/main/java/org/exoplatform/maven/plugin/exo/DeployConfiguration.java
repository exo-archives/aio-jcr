/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.maven.plugin.exo;

import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.maven.project.MavenProject;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan08@users.sourceforge.net
 * Dec 30, 2005
 */
public class DeployConfiguration {
  private MavenProject mavenProject;
  private String deployLibDir ;
  private String deployWebappDir ;
  private String deployEjbDir ;
  private String deployRarDir ;
  private String excludeProjects ;
  private List<String> scripts;
  private String serverType ;
  private String deployServerDir;
  private String cleanServerDir;
  private String serverConfig;
  private String deployDependencyScope;
  private String resource;
  private String ignoredFiles ;
  private String defaultPortalDir;
  private boolean useCleanServer;
  private List<String> serverPatchs;
  
  public String getServerType() {  return serverType ; }
  
  public void   setServerType(String s) {   serverType = s ;  }
  
  public String getDeployLibDir() {   return deployLibDir;  }
  
  public void setDeployLibDir(String dir) {  this.deployLibDir = dir; }

  public String getDeployWebappDir() {  return deployWebappDir;  }
  
  public void setDeployWebappDir(String dir) {  this.deployWebappDir = dir;  }

  public String getDeployEjbDir() {  return deployEjbDir;  }
  
  public void setDeployEjbDir(String dir) {  this.deployEjbDir = dir;  }

  public String getDeployRarDir() {  return deployRarDir;  }
  
  public void setDeployRarDir(String dir) {  this.deployRarDir = dir;  }
 
  public MavenProject getMavenProject() {  return mavenProject; }

  public void setMavenProject(MavenProject project) { this.mavenProject = project; }

  public List<String> getScripts() {  return scripts; }

  public void setScripts(List<String> scripts) {  this.scripts = scripts; }
  
  public String getDeployDependencyScope() {  return deployDependencyScope; }

  public void setDeployDependencyScope(String dep) {   this.deployDependencyScope = dep; }

  public String getResource() {  return resource; }

  public void setResource(String resource) {  this.resource = resource; }
  
  public boolean isUseCleanServer() {  return useCleanServer; }
  
  public void setUseCleanServer(boolean clean) {  this.useCleanServer = clean; }
  
  public String getDeployServerDir() {  return deployServerDir; }
  
  public void setDeployServerDir(String dir) {  this.deployServerDir = dir; }
  
  public String getCleanServerDir() {  return cleanServerDir; }
  
  public void setCleanServerDir(String dir) {   this.cleanServerDir = dir; }

  public String getServerConfig() { return serverConfig; }

  public void setServerConfig(String value) { this.serverConfig = value;}

  public String getDefaultPortalDir() { return defaultPortalDir; }

  public void setDefaultPortalDir(String value) { this.defaultPortalDir = value; }
  
  public void setExcludeProjects(String s) {  this.excludeProjects = s;  }

  public HashSet<String> getIgnoredProjects() {
    HashSet<String> ignoredProjects = new HashSet<String>();
    if (excludeProjects != null) {
      String[] project = excludeProjects.split(",");
      for (String s : project)  ignoredProjects.add(s.trim());
    }
    return ignoredProjects;
  }
  
  public void  setIgnoredFiles(String s) { ignoredFiles = s ; }
  
  public HashSet<Pattern>  getIgnoredFiles() {
    HashSet<Pattern>  set = new HashSet<Pattern>() ;
    if(ignoredFiles == null) ignoredFiles = "\\.*" ;
    String[] temp =  ignoredFiles.split(",") ;
    for(String exp : temp) set.add(Pattern.compile(exp.trim())) ; 
    return set ; 
  }

  public List<String> getServerPatchs() {
    return this.serverPatchs;
  }

  public void setServerPatchs(List<String> serverPatchs) {
    this.serverPatchs = serverPatchs;
  }
  
}
