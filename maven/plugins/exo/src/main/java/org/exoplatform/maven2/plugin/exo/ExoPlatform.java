/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.maven2.plugin.exo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashSet;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.exoplatform.maven2.plugin.Utils;
/** 
 * Created by The eXo Platform SARL
 * Author : Phung Hai Nam
 *          phunghainam@gmail.com
 * Nov 18, 2005
 */
/**
 * @goal platform
 * @requiresDependencyResolution runtime
 * @description mvn exo:platform -Drelease=param for release and deploy and create new tomcat
 */
public class ExoPlatform extends AbstractMojo {   
  /**
   * @parameter
   * @required
   */
  private String workingDir;
  /**
   * @parameter
   * @required
   */
  private String sharedResourcesDir;
  /**
   * @parameter expression="${deploy}"
   */
  private String deploy ;
  /**
   * @parameter
   */
  private String excludeProjects ;
  /**
   * @parameter expression="${project}"
   * @required
   */
  protected MavenProject project;
  /**
   * @parameter
   */
  private String cleanTomcatDir;
  /**
   * @parameter
   */
  private String cleanJonasDir;
  /**
   * @parameter
   */
  private String deployJonasDir;  
  /**
   * @parameter
   */
  private String deployJbossDir;
  
  public void execute() throws MojoExecutionException{
    if(!"exo-portal".equals(project.getPackaging())) printInfo() ;
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
  
  protected HashSet<String> getIgnoreProjects() {
    HashSet<String> ignoreProjects = new HashSet<String>() ;
    if(excludeProjects != null) {
      String[] pro =  excludeProjects.split(",") ;
      for(String s : pro) ignoreProjects.add(s.trim()) ;
    }
    return ignoreProjects ;
  }
  
  private void execDeploy() throws Exception {
    if (deploy.equals("newTomcat")) {
      File deployTomcatDirFile = new File(workingDir+"/exo-tomcat");
      File cleanTomcatDirFile = new File(cleanTomcatDir);
      if(deployTomcatDirFile.exists()) {
        Utils.printMessage("deleting", "  Deleting directory " + deployTomcatDirFile.getPath()) ;
        Utils.deleteDirectory(deployTomcatDirFile);
      }
      Utils.printMessage("mkdir","  "+ deployTomcatDirFile.getPath() ) ;
      Utils.printMessage("copy","  Copy directory "+cleanTomcatDirFile.getName() +" to "+deployTomcatDirFile.getPath()) ;
      Utils.copyDirectoryStructure(cleanTomcatDirFile,deployTomcatDirFile);
    } else if (deploy.equals("tomcat")) {
      File deployTomcatDirFile = new File(workingDir+"/exo-tomcat");
      deployTomcat(deployTomcatDirFile, getIgnoreProjects());
    } else if(deploy.equals("newJonas")) {
      File deployJonasDirFile  = new File(deployJonasDir);
      File cleanJonasDirFile = new File(cleanJonasDir);
      if(deployJonasDirFile.exists()) {
        Utils.printMessage("deleting","  Deleting directory " +deployJonasDirFile.getPath()) ;
        Utils.deleteDirectory(deployJonasDirFile);
      }
      Utils.printMessage("mkdir","  "+ deployJonasDirFile.getPath() ) ;
      Utils.printMessage("copy","  Copy directory "+cleanJonasDirFile.getName() +" to "+deployJonasDirFile.getPath()) ;
      Utils.copyDirectoryStructure(cleanJonasDirFile,deployJonasDirFile);      
    } else if(deploy.equals("jonas")) {
      File deployJonasDirFile  = new File(deployJonasDir);
      deployJonas(deployJonasDirFile, getIgnoreProjects());
    } else if(deploy.equals("jboss")) {
      File earDir = new File(deployJbossDir+"/exoplatform.sar");
      deployJboss(earDir,getIgnoreProjects());
    } else {
      Utils.printMessage("info","The task 'exo:platform -Ddeploy=" + deploy+ "' is invalid !\n");
      printInfo();
    }
  }
  
  protected void deployTomcat(File deployTomcatDir, HashSet<String> ignoreProjects) throws Exception  {
    File directoryJar = new File(deployTomcatDir + "/common/lib");
    File directoryWar = new File(deployTomcatDir + "/webapps");
    directoryJar.mkdirs();
    directoryWar.mkdirs();
    Utils.deployProject(directoryJar, directoryWar, project, true, ignoreProjects) ;
    Utils.patchConfig(new File(sharedResourcesDir + "/patch-tomcat"), deployTomcatDir) ;
    String contextName = project.getBuild().getFinalName() ;
    FileInputStream is = 
      new FileInputStream(deployTomcatDir + "/conf/Catalina/localhost/portal.template") ;
    byte[] buf  = new  byte[is.available()] ;
    is.read(buf) ;
    is.close() ;
    String s = new String(buf).replace("@context@", contextName) ;
    FileOutputStream os = 
      new FileOutputStream(deployTomcatDir + "/conf/Catalina/localhost/" + contextName +".xml") ;
    os.write(s.getBytes()) ;
    os.close() ;
  }
  
  protected void deployJonas(File deployDir, HashSet<String> ignoreProjects)  throws Exception {
    /*
     * There are two different ways to distribute applications in JOnAS :
     * - The first one consists in providing a "root", that is to say the
     *   entire distribution including binaries,
     * - The second one consists in providing a "base", that is to say a
     *   package containing only configuration and deployed components.
     * Although the second one seems preferrable, the first one is selected
     * as it allows to patch startup scripts and JOnAS libraries. 
     */
    File earDir = new File(deployDir + "/apps/autoload/exoplatform.ear");
    File libDir = new File(deployDir + "/lib/apps");
    earDir.mkdirs();
    libDir.mkdirs();
    /*
     * Create the ear file. Jars are excluded so that they can be loaded by
     * a common class loader. That way, it is possible to deploy portlet
     * applications bundled as simple wars outside of the ear.
     */
    Utils.deployProject(libDir, earDir, project, true,  ignoreProjects);
    Utils.createApplicationXml(earDir);
    Utils.patchConfig(new File(sharedResourcesDir + "/patch-jonas"), deployDir);
    /*
     * Change MANIFEST.MF from web applications because JOnAS recursively
     * resolves Class-Path libraries. Some libraries, such as jakarta commons,
     * have dependencies not bundled in the ear, but found in the application
     * server. This makes JOnAS fail to start. Removing MANIFEST.MF has no
     * other consequence than allowing JOnAS to start correctly.
     */
    File[] files = earDir.listFiles();
    for(int i = 0; i < files.length; i++ ) {
      if(files[i].getName().endsWith("war")) {
        Utils.removeManifestFromJar(files[i]);
      }
    }
  }
  
  protected void deployJboss(File earDir, HashSet<String> ignoreProjects) throws Exception {
    earDir.mkdirs();
    Utils.deployProject(earDir, earDir, project, true, ignoreProjects);
    Utils.createApplicationXml(earDir);
    Utils.patchConfig(new File(sharedResourcesDir + "/exoplatform.sar"), earDir) ;
  }
  
  private void printInfo() throws MojoExecutionException {
    String info =  
      "The 'exo:platform' maven2 plugin is used to assemble many exo modules into an application \n" +
      "and deploy the application to a server. To run the command successfully, you need to\n" +
      "run the command in an exo-portal type module and all the dependencies modules must be built\n" +
      "before running the command mvn exo:platform -Dparam=value\n\n" +
      "The valid syntax is:\n" +
      "  mvn exo:platform -Ddeploy=newTomcat\n" +
      "  This command will create a new clean tomcat in the exo-working directory.\n" +
      "  mvn exo:platform -Ddeploy=tomcat\n" +
      "  This command will copy will copy the portal module and the dependency module to the tomcat server.\n" +
      "  mvn exo:platform -Ddeploy=newJonas\n" +
      "  This command will create a new clean jonas in the exo-working directory.\n" +
      "  mvn exo:platform -Ddeploy=jonas\n" +
      "  This command will copy will copy the portal module and the dependency module to the jonas server.\n" +
      "  mvn exo:platform -Ddeploy=jboss\n" +
      "  This command will copy will copy the portal module and the dependency module to the jboss server.\n" ;
    System.out.println(info) ;
    throw new MojoExecutionException("") ;
  }
}
