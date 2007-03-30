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

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.util.FileUtils;
import org.exoplatform.maven2.plugin.Utils;

/**
 * @goal pbar
 * @requiresDependencyResolution runtime
 * @description 
 */
public class ExoProcessPackaging extends AbstractMojo {
  
  /**
   * The directory for the generated PBAR.
   *
   * @parameter expression="${project.build.directory}"
   * @required
   */
  protected String outputDirectory;
  
  /**
   * The Jar archiver.
   *
   * parameter expression="${component.org.codehaus.plexus.archiver.Archiver#jar}"
   * required
   */
  private JarArchiver jarArchiver = new JarArchiver();
  
  /**
   * The maven archive configuration to use.
   *
   * @parameter
   */
  private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();
  
  /**
   * @parameter expression="${project}"
   * @required
   */
  protected MavenProject project;
  
  /**
   * @parameter
   */
  private String excludeProjects ;
  
  /**
   * Process definition files
   *
   * @parameter expression="${basedir}/src/conf"
   * @required
   */
  private File processConfDirectory;  
  
  /**
   * The name of the generated pbar.
   *
   * @parameter expression="${project.build.finalName}"
   * @required
   */
  private String pbarName;  
  
  public void execute() throws MojoExecutionException {
    try {
      File pbarDest = new File(outputDirectory + "/" + pbarName) ;
      Utils.copyDirectoryStructure(processConfDirectory, pbarDest, Utils.getDefaultIgnoreFiles());
      //copy classes 
      File classesSrc = new File(outputDirectory +  "/classes") ;
      if(classesSrc.exists()) {
        File pbarClassDest = new File(outputDirectory + "/" + pbarName + "/classes") ;
        if(!pbarClassDest.exists()) pbarClassDest.mkdir() ;
        FileUtils.copyDirectoryStructure(classesSrc, pbarClassDest);
      }
      File pbarFile = new File( outputDirectory, pbarName + ".pbar" );
      performPackaging(pbarFile, pbarDest);
    } catch(Exception ex) {
      throw new MojoExecutionException("Error", ex) ;
    }
  }    
  
  protected void performPackaging(File pbarFile, File pbarDest) throws Exception {
    getLog().info( "Generating pbar " + pbarFile.getAbsolutePath() );
    MavenArchiver archiver = new MavenArchiver();    
    archiver.setArchiver( jarArchiver );
    archiver.setOutputFile( pbarFile );
    jarArchiver.addDirectory(pbarDest);
    
    // create archive
    archiver.createArchive(project, archive );
    project.getArtifact().setFile( pbarFile );
  }
  
}
