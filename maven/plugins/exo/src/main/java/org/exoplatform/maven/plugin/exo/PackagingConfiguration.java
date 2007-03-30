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
public class PackagingConfiguration {
  private MavenProject mavenProject ;
  private String outputFileName ;
  private String outputDirectory ;
  private List<String> sharePortalWebappDirs ;
  private List<String> scripts ;
  private String type;
  private String ignoredFiles;
  
  public String getOutputDirectory() {  return outputDirectory; }

  public void setOutputDirectory(String dir) {  outputDirectory = dir; }

  public MavenProject getMavenProject() {  return mavenProject; }

  public void setMavenProject(MavenProject project) {  mavenProject = project; }
  
  public String getOutputFileName() {  return outputFileName; }
  
  public void setOutputFileName(String name) {  outputFileName = name; }

  public List<String> getSharePortalWebappDirs() {  return sharePortalWebappDirs; }

  public void setSharePortalWebappDirs(List<String> dir) { sharePortalWebappDirs = dir; }

  public List<String> getScripts() {  return scripts; }

  public void setScripts(List<String> scripts) {  this.scripts = scripts; }

  public String getType() {  return type; }

  public void setType(String type) {  this.type = type; }

  public void   setIgnoreFiles(String s) { ignoredFiles = s ; }
  
  public HashSet<Pattern>  getIgnoredFiles() {
    HashSet<Pattern>  set = new HashSet<Pattern>() ;
    if(ignoredFiles == null) ignoredFiles = "\\.*" ;
    String[] temp =  ignoredFiles.split(",") ;
    for(String exp : temp) set.add(Pattern.compile(exp.trim())) ; 
    return set ; 
  }
}
