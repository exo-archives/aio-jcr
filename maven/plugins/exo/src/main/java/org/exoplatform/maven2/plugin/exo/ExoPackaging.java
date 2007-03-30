/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.maven2.plugin.exo;

import java.io.File;
import java.util.HashSet;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.war.WarArchiver;
import org.exoplatform.maven2.plugin.Utils;
/** 
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 * Nov 18, 2005
 */
abstract public class ExoPackaging extends AbstractMojo {
  /**
   * The directory for the generated WAR.
   *
   * @parameter expression="${project.build.directory}"
   * @required
   */
  protected String outputDirectory;
  /**
   * The Jar archiver.
   *
   * @parameter expression="${component.org.codehaus.plexus.archiver.Archiver#war}"
   * @required
   */
  private WarArchiver warArchiver;
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
  
  
  protected void performPackaging(File warFile, File webappDir) throws Exception {
    getLog().info( "Generating war " + warFile.getAbsolutePath() );
    MavenArchiver archiver = new MavenArchiver();    
    archiver.setArchiver( warArchiver );
    archiver.setOutputFile( warFile );
    warArchiver.addDirectory(webappDir);
    warArchiver.setWebxml( new File(webappDir, "WEB-INF/web.xml" ) );
    File manifest = new File(outputDirectory + "/manifest.txt") ;
    Utils.createManifest(manifest, project, getIgnoreProjects()) ;
    warArchiver.setManifest(manifest) ;
    // create archive
    archiver.createArchive(project, archive );
    project.getArtifact().setFile( warFile );
  }
  
  protected HashSet<String> getIgnoreProjects() {
    HashSet<String> ignoreProjects = new HashSet<String>() ;
    if(excludeProjects != null) {
      String[] pro =  excludeProjects.split(",") ;
      for(String s : pro) ignoreProjects.add(s.trim()) ;
    }
    return ignoreProjects ;
  }
}
