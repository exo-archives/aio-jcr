/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.maven2.plugin.exo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.maven.plugin.MojoExecutionException;
import org.exoplatform.maven2.plugin.Utils;

/**
 * Created by The eXo Platform SARL
 * Author : Phung Hai Nam
 *          phunghainam@gmail.com
 * Dec 6, 2005
 */
/**
 * @goal releaseyv
 * @requiresDependencyResolution runtime
 */
public class ExoRelease extends ExoPlatform {
  /**
   * @parameter
   * @required
   */
  private String workingDir;
  /**
   * @parameter
   * @required
   */
  private String releaseDir;
  /**
   * @parameter
   * @required
   */
  private String cleanTomcatDir;
  /**
   * @parameter
   * @required
   */
  private String sharedResourcesDir;
  /**
   * @parameter expression="${release}"
   */
  private String release ;
  
  public void execute() throws MojoExecutionException {
    if(!"exo-portal".equals(project.getPackaging())) {
      throw new MojoExecutionException("You need to run this command in a exo-portal type project") ;
    }
    try {
      File releaseFile = new File(releaseDir);
      if (!releaseFile.exists()) releaseFile.mkdirs();
      if (release != null) {
        if (release.equals("tomcat")) {
          execReleaseTomcat() ;
        } else if (release.equals("jonas")) {
          execReleaseJonas();
        } else if (release.equals("jboss")) {
          execReleaseJboss();
        }
      }
    }catch(Exception exe) {
      throw new MojoExecutionException("Error: " , exe) ;
    }
  }
  
  private void execReleaseTomcat() throws Exception {
    File deployTomcatDir = new File(workingDir+"/exo-tomcat");  
    File cleanTomcatDirFile = new File(cleanTomcatDir);
    if (!deployTomcatDir.exists())  Utils.copyDirectoryStructure(cleanTomcatDirFile,deployTomcatDir);
    
    String libDir = deployTomcatDir + "/common/lib";
    String webappDir = deployTomcatDir + "/webapps";
    File directoryJar = new File(libDir);
    File directoryWar = new File(webappDir);
    Utils.deployProject(directoryJar,directoryWar,project,true,getIgnoreProjects()) ;
    Utils.patchConfig(new File(sharedResourcesDir + "/patch-tomcat"), deployTomcatDir) ;
    File output = new File(releaseDir+"/exo-tomcat.zip");    
    addToArchive(deployTomcatDir,output) ;
    Utils.printMessage("archive",  "  Created 1 file zip  to " + releaseDir);
    Utils.deleteDirectory(deployTomcatDir);
  }

  private void execReleaseJonas() throws Exception {
    File deployJonasDir = new File(workingDir+"/exo-jonas");
    File output = new File(releaseDir+"/exo-jonas.zip");
    addToArchive(deployJonasDir,output) ;
    Utils.printMessage("archive" ,  "  Created 1  file zip to " + releaseDir);
  }
  
  private void execReleaseJboss() throws Exception {    
    File deployJbossDir = new File(workingDir+"/exo-jboss/server/default/deploy/exoplatform.sar");
    if (!deployJbossDir.exists()) deployJbossDir.mkdir();
    deployJboss(deployJbossDir,getIgnoreProjects()) ;
    Utils.patchConfig(new File(sharedResourcesDir + "/exoplatform.sar"), deployJbossDir) ;   
    File output = new File(releaseDir+"/exoplatform.sar.zip");
    addToArchive(deployJbossDir,output) ;
    Utils.printMessage("archive" ,  "  Created 1  file zip to " + releaseDir);
    Utils.deleteDirectory(deployJbossDir);
  }
  
  private void addToArchive( File input, File output) throws Exception {   
    String path = input.getAbsolutePath();               
    ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream( new FileOutputStream( output)));
    final int BUFFER = 2048;
    byte data[] = new byte[BUFFER];
    BufferedInputStream origin = null;
    java.util.List<File> files = getFiles( input);  
    if( files == null || files.size()<1) return;
    int index = 0;
    for (Iterator<File> iter = files.iterator(); iter.hasNext();) {
      File f = iter.next();
      index++;
      String filePath = f.getAbsolutePath();     
      if (filePath.startsWith( path))  filePath = filePath.substring( path.length());            
      FileInputStream fi = new FileInputStream(f);       
      origin = new BufferedInputStream(fi, BUFFER);
      ZipEntry entry = new ZipEntry(filePath);
      out.putNextEntry(entry);     
      int count;   
      while ((count = origin.read(data, 0, BUFFER)) != -1) out.write(data, 0, count);
      origin.close();      
    }
    out.close();  
  }
  
  private java.util.List<File> getFiles(File dir) {
    final java.util.List <File> files = new LinkedList<File>();
    dir.listFiles(new FileFilter() {
      public boolean accept(File f) {
        if (f.isDirectory())  return false;          
        files.add(f);
        return true;
      }
    });      
    dir.listFiles(new FileFilter() {
      public boolean accept(File f) {
        if (f.isFile())  return false;   
        files.addAll(getFiles(f));
        return false;
      }
    });
    return files;
  }
}
