/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.maven2.plugin.exo;

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
  
  public String getOutputDirectory() {
    return outputDirectory;
  }

  public void setOutputDirectory(String outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public MavenProject getMavenProject() {
    return mavenProject;
  }

  public void setMavenProject(MavenProject mavenProject) {
    this.mavenProject = mavenProject;
  }
  
  public String getOutputFileName() {
    return outputFileName;
  }
  
  public void setOutputFileName(String outputFileName) {
    this.outputFileName = outputFileName;
  }
}
