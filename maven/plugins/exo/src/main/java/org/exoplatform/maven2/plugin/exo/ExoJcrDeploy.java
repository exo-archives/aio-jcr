/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.maven2.plugin.exo;

import java.io.File;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.exoplatform.maven2.plugin.Utils;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.codehaus.plexus.archiver.ear.EarArchiver;
/** 
 * Created by The eXo Platform SARL
 * Author : Roman Pedchenko
 *          lautarul@gmail.com
 */
/**
 * @goal jcr
 * @requiresDependencyResolution runtime
 * @description mvn exo:jcr -Ddeploy=param for deploy JCR standalone
 */
public class ExoJcrDeploy extends AbstractMojo {   
  /**
   * @parameter expression="${exo.directory.working}"
   * @required
   */
  private String workingDir;
  /**
   * The directory for the generated EAR.
   *
   * @parameter expression="${project.build.directory}"
   * @required
   */
  protected String outputDir;
  /**
   * @parameter
   * @required
   */
  private String sharedResourcesDir;
  /**
   * @parameter expression="${deploy}"
   */
  private String deploy;
  /**
   * @parameter
   */
  private String excludeProjects;
  /**
   * @parameter
   * @required
   */
  private String includeToEar;
  /**
   * @parameter expression="${project}"
   * @required
   */
  protected MavenProject project;
  /**
   * @parameter
   * @required
   */
  private String deployJbossDir;
  /**
   * @parameter
   * @required
   */
  private String deployWLDir;
  /**
   * @parameter
   * @required
   */
  private String serverWLDir;
  /**
   * @parameter
   * @required
   */
  private String domainWLDir;
  /**
   * @parameter
   * @required
   */
  private String jarsWLToLib;
  /**
   * @parameter
   */
  private String provsWL;
  /**
   * The Ear archiver.
   *
   * @parameter expression="${component.org.codehaus.plexus.archiver.Archiver#ear}"
   * @required
   */
  private EarArchiver earArchiver;
  /**
   * The maven archive configuration to use.
   *
   * @parameter
   */
  private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();
  
  public void execute() throws MojoExecutionException{
    if(!"exo-jcr".equals(project.getPackaging()) && !"exo-jcr2".equals(project.getPackaging())) printInfo() ;
    try {  
      if (deploy != null) {
        execDeploy() ;
        return;
      }
    }catch (Exception e) {
      e.printStackTrace() ;
    }
    printInfo() ;
  }   
  
  protected void makeEar(File earFile, File webappDir, String outDir) throws Exception {
    getLog().info( "Generating ear " + earFile.getAbsolutePath() );
    MavenArchiver archiver = new MavenArchiver();    
    archiver.setArchiver( earArchiver );
    archiver.setOutputFile( earFile );
    earArchiver.addDirectory(webappDir);
    earArchiver.setAppxml( new File(webappDir, "/META-INF/application.xml" ) );
    File manifest = new File(outDir + "/manifest.txt") ;
    Utils.createManifest(manifest, project, getIgnoreProjects()) ;
    earArchiver.setManifest(manifest) ;
    archiver.createArchive(project, archive);
//    project.getArtifact().setFile(earFile);
  }

  protected HashSet<String> getSet(String hs) {
    HashSet<String> set = new HashSet<String>();
    if(hs != null) {
      String[] pro = hs.split(",");
      for(String s : pro) set.add(s.trim());
    }
    return set;
  }
  
  protected HashSet<String> getIgnoreProjects() {
    return getSet(excludeProjects);
  }
  
  protected HashSet<String> getIncludeToEar() {
    return getSet(includeToEar);
  }
  
  private void execDeploy() throws Exception {
    archive.setAddMavenDescriptor(false);
    if (deploy.equals("tomcat")) {
      File deployTomcatDirFile = new File(workingDir + "/exo-tomcat");
      deployTomcat(deployTomcatDirFile, getIgnoreProjects());
    } else if(deploy.equals("jonas")) {
      File deployJonasDirFile  = new File(workingDir + "/exo-jonas");
//      deployJonas(deployJonasDirFile, getIgnoreProjects());
      deployJonasEar(deployJonasDirFile, getIgnoreProjects());
    } else if(deploy.equals("jboss")) {
      File earDir = new File(deployJbossDir + "/jcr.sar");
//      deployJboss(earDir, getIgnoreProjects());
      deployJbossEar(earDir, getIgnoreProjects());
    } else if(deploy.equals("wl")) {
      File earDir = new File(deployWLDir);
      deployWLEar(earDir, getIgnoreProjects());
//      File patchDir = new File(patchWLDir);
//      deployWLEar(earDir, patchDir, getIgnoreProjects());
    } else if(deploy.equals("jboss2")) {
      deployJboss2(deployJbossDir, getIgnoreProjects());
    } else if(deploy.equals("ear")) {
      deployEar(outputDir, getIgnoreProjects());
    } else {
      Utils.printMessage("info","The task 'exo:jcr -Ddeploy=" + deploy+ "' is invalid !\n");
      printInfo();
    }
  }
  
  protected void deployTomcat(File deployTomcatDir, HashSet<String> ignoreProjects) throws Exception  {
    File directoryJar = new File(deployTomcatDir + "/common/lib");
    File directoryWar = new File(deployTomcatDir + "/webapps");
    directoryJar.mkdirs();
    directoryWar.mkdirs();
    Utils.deployedDependency2(directoryJar, null, null, project, ignoreProjects) ;
    Utils.deployProject(directoryJar, directoryWar, project, false, ignoreProjects) ;
    Utils.patchConfig2(new File(sharedResourcesDir + "/patch-tomcat-jcr"), deployTomcatDir) ;
  }
  
  protected void deployJonas(File deployDir, HashSet<String> ignoreProjects)  throws Exception {
    File warDir = new File(deployDir + "/webapps/autoload");
    File rarDir = new File(deployDir + "/rars/autoload");
    File libDir = new File(deployDir + "/lib/apps");
    warDir.mkdirs();
    rarDir.mkdirs();
    libDir.mkdirs();
    Utils.deployedDependency2(libDir, null, rarDir, project, ignoreProjects) ;
    Utils.deployProject(libDir, warDir, project, false,  ignoreProjects);
    Utils.patchConfig2(new File(sharedResourcesDir + "/patch-jonas-jcr"), deployDir);
    /*File[] files = warDir.listFiles();
    for(int i = 0; i < files.length; i++ ) {
      if(files[i].getName().endsWith("war")) {
        Utils.removeManifestFromJar(files[i]);
      }
    }*/
  }
  
  protected void deployJonasEar(File deployDir, HashSet<String> ignoreProjects)  throws Exception {
    File earDir = new File(deployDir + "/apps/autoload");
    earDir.mkdirs();
    Utils.patchConfig2(new File(sharedResourcesDir + "/patch-jonas-jcr"), deployDir);
    deployEar(earDir.getAbsolutePath(), ignoreProjects);
  }

  protected void deployJboss(File earDir, HashSet<String> ignoreProjects) throws Exception {
    earDir.mkdirs();
    Utils.deployedDependency2(earDir, null, earDir, project, ignoreProjects);
    Utils.deployProject(earDir, earDir, project, false, ignoreProjects);
    Utils.createApplicationXml(earDir);
    Utils.patchConfig2(new File(sharedResourcesDir + "/jcr.sar"), earDir);
  }
  
  protected void deployJbossEar(File earDir, HashSet<String> ignoreProjects) throws Exception {
    earDir.mkdirs();
    Utils.patchConfig2(new File(sharedResourcesDir + "/jcr.sar"), earDir);
    deployEar(earDir.getAbsolutePath(), ignoreProjects);
  }
  
  protected void copyArtifactsToDir(String sJars, File dir) throws Exception {
    if (sJars != null && sJars.length() > 0) {
      HashSet jars = getSet(sJars);
      Collection artifacts = project.getArtifacts();
      List list = new ArrayList();
      list.addAll(artifacts);
      Collections.sort(list);
      Iterator i = list.iterator();
      while (i.hasNext()) {
        Artifact da = (Artifact) i.next();
        if (jars.contains(da.getArtifactId())) {
          Utils.copyFileToDirectory(da.getFile(), dir);
          Utils.printMessage("deploy", "  Deployed file '" + da.getArtifactId() + "' to "
              + dir.getPath());
        }
      }
    }
  }
  
  protected void deployWLEar(File earDir, HashSet<String> ignoreProjects) throws Exception {
    File serverDir = new File(serverWLDir + "/server/lib/mbeantypes");
    File domainDir = new File(domainWLDir + "/lib");
    earDir.mkdirs();
//    Utils.patchConfig2(new File(sharedResourcesDir + "/patch-wl-jcr"), patchDir);
    deployEar(earDir.getAbsolutePath(), ignoreProjects);
    // copying jars
    getLog().info("Deploying jars to " + domainDir.getPath());
    copyArtifactsToDir(jarsWLToLib, domainDir);
    // copying providers
    getLog().info("Deploying providers to " + serverDir.getPath());
    copyArtifactsToDir(provsWL, serverDir);
  }
  
  protected void deployJboss2(String earDir, HashSet<String> ignoreProjects) throws Exception {
    File earFile =  new File(earDir + "/jcr.ear");
//    File earDir_ =  new File(earDir);
//    earDir_.mkdirs();
    File outDir = new File(outputDir + "/ear");
    outDir.mkdirs();
    Utils.deployedDependency2(outDir, null, outDir/*earDir_*/, project, ignoreProjects);
    Utils.deployProject(outDir, outDir, project, false, ignoreProjects);
    Utils.createApplicationXml2(outDir);
    Utils.patchConfig2(new File(sharedResourcesDir + "/jcr.sar"), outDir);
    makeEar(earFile, outDir, outputDir + "/ear");
  }
  
  protected void deployEar(String earDir, HashSet<String> ignoreProjects) throws Exception {
    File earFile =  new File(earDir + "/jcr.ear");
    File outDir = new File(outputDir + "/ear");
    outDir.mkdirs();
    Utils.deployedDependency2(/*outDir*/null, null, outDir/*earDir_*/, project, ignoreProjects);
    Utils.deployedDependency3(outDir, project, getIncludeToEar());
    Utils.deployProject(outDir, outDir, project, false, ignoreProjects);
    Utils.createApplicationXml2(outDir);
    Utils.patchConfig2(new File(sharedResourcesDir + "/ear"), outDir);
    makeEar(earFile, outDir, outputDir + "/ear");
  }
  
  private void printInfo() throws MojoExecutionException {
    String info =  
      "The 'exo:jcr' maven2 plugin is used to assemble many exo modules into an application \n" +
      "and deploy the application to a server. To run the command successfully, you need to\n" +
      "run the command in an exo-jcr module and all the dependencies modules must be built\n" +
      "before running the command mvn exo:jcr -Ddeploy=value\n\n" +
      "The valid syntax is:\n" +
      "  mvn exo:jcr -Ddeploy=tomcat\n" +
      "  This command will copy the jcr module and the dependency modules to the tomcat server.\n" +
      "  mvn exo:jcr -Ddeploy=jonas\n" +
      "  This command deploys jcr standalone module EAR to JOnAS and applies server configuration patches.\n" +
      "  mvn exo:jcr -Ddeploy=jboss\n" +
      "  This command deploys jcr standalone module EAR and server configuration files to a JBoss SAR directory.\n" +
      "  mvn exo:jcr -Ddeploy=jboss2\n" +
      "  I don't remember what this command does, probably something strange and irrelevant at the time.\n" +
      "  mvn exo:jcr -Ddeploy=wl\n" +
      "  This command deploys jcr standalone module EAR to WebLogic and applies server configuration patches.\n" +
      "  mvn exo:jcr -Ddeploy=ear\n" +
      "  This command compiles jcr standalone module EAR in 'target' directory.\n";
    System.out.println(info) ;
    throw new MojoExecutionException("") ;
  }
}
