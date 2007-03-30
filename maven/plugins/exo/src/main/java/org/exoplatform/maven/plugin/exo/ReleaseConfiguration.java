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
 * Created by The eXo Platform SARL Author : Phung Hai Nam phunghainam@gmail.com
 * Jan 25, 2006
 */
public class ReleaseConfiguration {
  private MavenProject mavenProject;
 
  private String deployDirectory;
  
  private String releaseDirectory;
  
  private String releaseFileName;
  
  private String serverType;
  
  private String releaseSrcName;
  
  private String sourceDirectory;
  
  private String ignoredFiles ;
  
  private List<String> scripts;
  
  public MavenProject getMavenProject() { return mavenProject; }

  public void setMavenProject(MavenProject value) { this.mavenProject = value; } 
  
  public String getDeployDirectory() { return deployDirectory; }

  public void setDeployDirectory(String value) { this.deployDirectory = value; }

  public String getReleaseDirectory() { return releaseDirectory; }

  public void setReleaseDirectory(String value) {this.releaseDirectory = value; }

  public String getReleaseFileName() { return releaseFileName; }

  public void setReleaseFileName(String value) { this.releaseFileName = value; }

  public String getServerType() { return serverType; }

  public void setServerType(String serverType) {this.serverType = serverType; }

  public List<String> getScripts() {return scripts; }

  public void setScripts(List<String> scripts) { this.scripts = scripts; }

  public String getReleaseSrcName() { return releaseSrcName; }

  public void setReleaseSrcName(String value) { this.releaseSrcName = value; }

  public String getSourceDirectory() { return sourceDirectory; }

  public void setSourceDirectory(String value) { this.sourceDirectory = value; }

  public void setIgnoredFiles(String value) { this.ignoredFiles = value; }
  
  public HashSet<Pattern>  getIgnoredFiles() {
    HashSet<Pattern>  set = new HashSet<Pattern>() ;
    if(ignoredFiles == null) ignoredFiles = "\\.*" ;
    String[] temp =  ignoredFiles.split(",") ;
    for(String exp : temp) set.add(Pattern.compile(exp.trim())) ; 
    return set ; 
  }
}
