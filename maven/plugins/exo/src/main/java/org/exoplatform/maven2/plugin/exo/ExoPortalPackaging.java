/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.maven2.plugin.exo;

/**
 * Created by The eXo Platform SARL
 * Author : Phung Hai Nam
 *          phunghainam@gmail.com
 * Dec 2, 2005
 */
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.exoplatform.maven2.plugin.Utils;

/**
 * @goal portalWar
 * @requiresDependencyResolution runtime
 * @description 
 */
public class ExoPortalPackaging extends ExoPackaging {   
  /**
   * @parameter
   * @required
   */
  private String sharedDir;
  /**
   * Single directory for extra files to include in the WAR.
   *
   * @parameter expression="${basedir}/src/webapp"
   * @required
   */
  private File warSourceDirectory;
  /**
   * The name of the generated war.
   *
   * @parameter expression="${project.build.finalName}"
   * @required
   */
  private String warName;

  public void execute() throws MojoExecutionException {
    try {
      File webappDest = new File(outputDirectory + "/" + warName) ;
      //copy classes 
      if(sharedDir == null) {
        getLog().info( "You do not specify a shared webapp directory.........................");
      } else {
        File shareFile = new File(sharedDir);
        Utils.copyDirectoryStructure(shareFile,webappDest, Utils.getDefaultIgnoreFiles());
      }
      Utils.copyDirectoryStructure(warSourceDirectory, webappDest, Utils.getDefaultIgnoreFiles());
      
      Collection artifacts = project.getDependencyArtifacts();
      for (Iterator iter = artifacts.iterator(); iter.hasNext();) {
        Artifact artifact = (Artifact) iter.next();        
        if("bpar".equals(artifact.getType())) {
          File bpDest = new File(outputDirectory + "/" + warName + "/WEB-INF/conf/bp") ;
          if(!bpDest.exists()) bpDest.mkdir() ;
          Utils.copyFileToDirectory(artifact.getFile(), bpDest) ; 
        }        
      } 
      File warFile = new File( outputDirectory, warName + ".war");
      performPackaging(warFile, webappDest);
    } catch(Exception exe){
      throw new MojoExecutionException("Coppy Share directory to "+ project.getBuild().getFinalName() + " directory error :", exe) ;
    }
  }
}
