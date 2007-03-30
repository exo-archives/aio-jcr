/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.maven.plugin.exo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.maven.archiver.ManifestConfiguration;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.archiver.war.WarArchiver;
import org.exoplatform.maven2.plugin.Utils;
import org.exoplatform.tools.xml.webapp.v23.ModifyWebXMLOperation;
/**
 * Created by The eXo Platform SARL Author : Phung Hai Nam phunghainam@gmail.com
 * Jan 5, 2006
 */
public class Command {

  // ===================== Extends PackagingCommand Class ======================
  /**
   * This class is used to copy webapp directory to the the target directory
   */
  static class CopyWebappDir extends PackagingCommand {
    public void execute(PackagingConfiguration pconfig) throws Exception {
      String outputDirectory = pconfig.getOutputDirectory();
      String outputFileName = pconfig.getOutputFileName();
      File webappDir = new File(outputDirectory + "/" + outputFileName);
      if (!webappDir.exists())   webappDir.mkdirs();
      ExoFileUtils.copyDirectoryStructure(new File(pconfig.getMavenProject().getBasedir() + 
                                      "/src/webapp"), webappDir, pconfig.getIgnoredFiles(), true);
      ProjectUtils.printMessage("info","Copy webapp directory to " + webappDir.getPath());
      File classSrc = new File(outputDirectory + "/classes");
      if (classSrc.exists()) {
        File webappClassDir = new File(outputDirectory + "/" + outputFileName + "/WEB-INF/classes");
        if (!webappClassDir.exists())  webappClassDir.mkdirs();
        ExoFileUtils.copyDirectoryStructure(classSrc, webappClassDir);
        ProjectUtils.printMessage("info","Copy class directory to " + webappClassDir.getPath());  
      }
    }
  }
  
  static class CopyWebappJarDependency extends PackagingCommand {
    public void execute(PackagingConfiguration pconfig) throws Exception {
      File libDir = 
        new File(pconfig.getOutputDirectory() + "/" + pconfig.getOutputFileName() +"/WEB-INF/lib");
      int counter = 0;
      MavenProject project= pconfig.getMavenProject(); 
      Collection artifacts = project.getArtifacts() ;
      List list = new ArrayList();
      list.addAll(artifacts);
      Collections.sort(list);
      Iterator i = list.iterator();
      while (i.hasNext()) {
        Artifact da = (Artifact) i.next();
        if ("jar".equals(da.getType())) {
          ProjectUtils.copyFileToDirectory(da.getFile(), libDir);
          ProjectUtils.printMessage("deploy", "  Deployed file '" + da.getArtifactId() + "' to " + libDir.getPath());
          counter++;
        }
      }
      if (counter > 0) 
        ProjectUtils.printMessage("deploy", "  DEPLOY : " + counter + " dependencies");
    }
  }

  /**
   * Use for ExoPortalPackaging. This class is used to copy share directory to
   * the target directory
   */
  static class CopyShareDir extends PackagingCommand {
    public void execute(PackagingConfiguration pconfig) throws Exception {
      String outputDirectory = pconfig.getOutputDirectory();
      String outputFileName = pconfig.getOutputFileName();
      File webappDir = new File(outputDirectory + "/" + outputFileName);
      List<String> shareDirectorys = pconfig.getSharePortalWebappDirs();
      if (shareDirectorys == null) {
        System.out.println("  You do not specify a shared webapp directory.............");
      } else {
        for (String shareDir : shareDirectorys ) {
          File shareDirectory = new File(shareDir);
          ExoFileUtils.copyDirectoryStructure(shareDirectory, webappDir, pconfig.getIgnoredFiles(), true);
          ProjectUtils.printMessage("info","Copy "+shareDirectory.getName()+" directory to " + webappDir.getPath());
        }
      }
      
    }
  }

  /**
   * Use for ExoPortalPackaging. This class is used to copy dependencies to the
   * WEB-INF/conf/bp for the bpar type
   */
  static class CopyBPARDependency extends PackagingCommand {
    public void execute(PackagingConfiguration pconfig) throws Exception {
      File bpDest = new File(pconfig.getOutputDirectory() + "/" + 
                             pconfig.getOutputFileName() + "/WEB-INF/conf/bp");
      ProjectUtils.printMessage("info","Copy bpar  projects to " + bpDest.getPath());
      if (!bpDest.exists()) bpDest.mkdirs();
      Collection artifacts = pconfig.getMavenProject().getDependencyArtifacts();
      for (Iterator i = artifacts.iterator(); i.hasNext();) {
        Artifact artifact = (Artifact) i.next();
        if ("bpar".equals(artifact.getType())) {
          ExoFileUtils.copyFileToDirectory(artifact.getFile(), bpDest);
        }
      }
    }
  }

  /**
   * Use for ExoPortalPackaging. This class is used to copy dependencies to the
   * applet/launcher or applet/distro for the exo-applet-launcher and
   * exo-applet-distro scopes.
   */
  static class CopyExoAppletDependencies extends PackagingCommand {
    public void execute(PackagingConfiguration pconfig) throws Exception {
      File appletDest = new File(pconfig.getOutputDirectory() + File.separator +
              pconfig.getOutputFileName() + File.separator + "applet");

      ProjectUtils.printMessage("info","Copy exo applet distribution to " +
              appletDest.getPath());
      
      Collection artifacts = pconfig.getMavenProject().getDependencyArtifacts();
      for (Iterator i = artifacts.iterator(); i.hasNext();) {
        Artifact artifact = (Artifact) i.next();
        String scope = artifact.getScope();
        // launcher applets
        if ("exo-applet-launcher".equals(scope)) {
            File launcherDir = new File(appletDest, "launcher");
            if (!launcherDir.exists()) launcherDir.mkdirs();

            String extension = Utils.extension(artifact.getFile().getName());
            File apDest = new File(launcherDir, artifact.getArtifactId() +
                    "." + extension);

            ExoFileUtils.copyFile(artifact.getFile(), apDest);
        }
        // real applet and its distribution
        else if ("exo-applet-distro".equals(scope)) {
            File distroDir = new File(appletDest, "distro" + File.separator +
                    artifact.getGroupId() + File.separator +
                    artifact.getArtifactId() + File.separator +
                    artifact.getVersion());
            if (!distroDir.exists()) distroDir.mkdirs();

            File distroFile = new File(distroDir, artifact.getArtifactId() +
                    "-" + artifact.getVersion() + "." + artifact.getType());
            ExoFileUtils.copyFile(artifact.getFile(), distroFile);

// TODO generate checksum files over the files copied
//            ExoFileUtils.generateFileDigest(distroFile, "SHA-1", new File(
//                    distroFile.getPath() + ".sha1"));
        }
      }
    }
  }
  /**
   * Use for ExoPortletPackaging. This class is used to copy velocity, groovy files
   * to target.
   */
  static class CopyFilesResource extends PackagingCommand {
    public void execute(PackagingConfiguration pconfig) throws Exception {
      MavenProject project = pconfig.getMavenProject();
      String outputDirectory = pconfig.getOutputDirectory();
      String outputFileName = pconfig.getOutputFileName();
      File webappDir = new File(outputDirectory + "/" + outputFileName);
      if (!webappDir.exists())   webappDir.mkdirs();
      File srcDirectory = new File(project.getBasedir() + "/src/java");
      if (!srcDirectory.exists()) return;
      File destVelocityDir = new File(webappDir.getPath() + "/velocity");
      File destGroovyDir = new File(webappDir.getPath() + "/groovy");
      int denum = ProjectUtils.deployResourceFiles(srcDirectory, destVelocityDir, pconfig.getIgnoredFiles(), ProjectUtils.velocityFiles());
      if (denum > 0)
        ProjectUtils.printMessage("info", "Copied " + denum + " file to directory " + destVelocityDir+ ".");
      int groovyCounter = ProjectUtils.deployResourceFiles(srcDirectory, destGroovyDir, pconfig.getIgnoredFiles(), ProjectUtils.groovyFiles());
      if (groovyCounter > 0)
        ProjectUtils.printMessage("info", "Copied " + groovyCounter + " file to directory " + destGroovyDir+ ".");
    }
  }

  /**
   * Use for ExoPorletPackaging.This class is used to modify web.xml file
   * AddExoPortletDeployer
   */
  static class AddExoPortletDeployer extends PackagingCommand {
    public void execute(PackagingConfiguration pconfig) throws Exception {
      if (pconfig.getMavenProject().getPackaging().equalsIgnoreCase("exo-portlet")) {
        String webxml = pconfig.getOutputDirectory() + "/" + pconfig.getOutputFileName() + "/WEB-INF/web.xml";
        String warName = pconfig.getOutputFileName();
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ModifyWebXMLOperation.class.getClassLoader());
        ModifyWebXMLOperation op = new ModifyWebXMLOperation(warName);
        File inputFile = new File(webxml);
        File outputFile = new File(webxml);
        try {
          op.modifyWebXML(inputFile, outputFile);
          ProjectUtils.printMessage("info","Modified web.xml");
        } finally {
          Thread.currentThread().setContextClassLoader(old);
        }
      } else return;
    }
  }

  /**
   * Use this command to create a  war  archive  file
   */
  static class WarArchive extends PackagingCommand {
    public void execute(PackagingConfiguration pconfig) throws Exception {
      
      MavenProject mproject = pconfig.getMavenProject();
      
      ProjectUtils.printMessage("info","Archive war file (" + mproject.getPackaging() + ")");
      String sourcePath = pconfig.getOutputDirectory() + "/" + pconfig.getOutputFileName() ;
      File sourceFile = new File(sourcePath);
      File warFile = new File(sourcePath + ".war");
      MavenArchiver archiver = new MavenArchiver();
      WarArchiver warArchiver = new WarArchiver();
      archiver.setArchiver(warArchiver);
      archiver.setOutputFile(warFile);
      warArchiver.addDirectory(sourceFile);
      warArchiver.setWebxml(new File(sourceFile, "WEB-INF/web.xml"));

      // Search for war-plugin conf
//      List plugins = mproject.getBuildPlugins();
//      // [PN] 19.02.07 TODO parse XML with SAX
//      boolean addClassPath = false; 
//      for (Object plugin: plugins) {
//        System.out.println("PLUGIN: " + plugin);
//        Plugin mplugin = (Plugin) plugin;
//        System.out.println("PLUGIN: " + plugin + " key " + mplugin.getArtifactId());
//        if (mplugin.getArtifactId().equals("maven-war-plugin")) {
//          System.out.println("PLUGIN: maven-war-plugin config " + mplugin.getConfiguration());
//          if (mplugin.getConfiguration().toString().indexOf("<addClasspath>true</addClasspath>")>0)
//            addClassPath = true;
//        }
//      }
      //manifest
      //final boolean manifestAddClassPath = addClassPath;
      MavenArchiveConfiguration warConf = new MavenArchiveConfiguration();
      warConf.setManifest(new ManifestConfiguration() {

        @Override
        public boolean isAddClasspath() {
          //return manifestAddClassPath;
          return true;
        }
        
      });
      
      archiver.createArchive(pconfig.getMavenProject(), warConf);
      pconfig.getMavenProject().getArtifact().setFile(warFile);
    }
  }
  /**
   * Use this command to create a  war  archive  file
   */
  static class JarArchive extends PackagingCommand {
    public void execute(PackagingConfiguration pconfig) throws Exception {
      ProjectUtils.printMessage("info","Archive jar file");
      String ftype = pconfig.getMavenProject().getPackaging() ;
      String sourcePath = pconfig.getOutputDirectory() + "/" + pconfig.getOutputFileName();
      File sourceDir =  new File(sourcePath);
      File destFile =   new File(sourcePath + "." + ftype);
      MavenArchiver archiver = new MavenArchiver();
      JarArchiver jarArchiver = new JarArchiver();
      archiver.setArchiver(jarArchiver);
      archiver.setOutputFile(destFile);
      jarArchiver.addDirectory(sourceDir);
      archiver.createArchive(pconfig.getMavenProject(), new MavenArchiveConfiguration());
      pconfig.getMavenProject().getArtifact().setFile(destFile);
    }
  }
  
  /**
   * This class is used to copy webapp directory to the the target directory
   */
  static class PrepareBPARDir extends PackagingCommand {
    public void execute(PackagingConfiguration pconfig) throws Exception {
      MavenProject project =  pconfig.getMavenProject() ;
      String templateLoc = pconfig.getOutputDirectory() + "/" + pconfig.getOutputFileName() ;
      File templateDir = new File(templateLoc) ;
      File confDir = new File(project.getBasedir() + "/src/conf") ;
      ExoFileUtils.copyDirectoryStructure(confDir, templateDir, pconfig.getIgnoredFiles());
      //copy classes 
      File classesSrc = new File(pconfig.getOutputDirectory() +  "/classes") ;
      if(classesSrc.exists()) {
        ExoFileUtils.copyDirectoryStructure(classesSrc, new File(templateLoc + "/classes"));
      }
    }
  }

  // ====================== Extends DeployCommand Class ========================
  /**
   * This class is used to deploy a module
   */
  static class DeployProject extends DeployCommand {
    public void execute(DeployConfiguration deployConfig) throws Exception {
      String scope = deployConfig.getDeployDependencyScope();
      try {
        if (scope != null && scope.equalsIgnoreCase("resources")) {
          deployResource(deployConfig) ;
          return ;
        }
        deployModule(deployConfig);
        if (scope != null) {
          if (scope.equalsIgnoreCase("all")) {
            deployDependencies(deployConfig,"all");
          } else if (scope.equalsIgnoreCase("compile")) {
            deployDependencies(deployConfig,"compile");
          } else if (scope.equalsIgnoreCase("runtime")) {
            deployDependencies(deployConfig,"runtime");
          }
        }
        return;
      } catch (Exception e) {}
      printInfo();
    }   
    
    public void deployModule(DeployConfiguration deployConfig) throws Exception {
      MavenProject project= deployConfig.getMavenProject();
      if (project.getPackaging().equals("exo-product")) {
        deployDependencies(deployConfig,"all");
        return;
      }
      System.out.println(
        "==========================================================================\n" +
        "                   DEPLOY MODULE  " + project.getArtifactId() + "         \n " +
        "==========================================================================\n"
      );
      ProjectUtils.deployProject(deployConfig);
    }  
    
    public void deployDependencies(DeployConfiguration deployConfig,String scope) throws Exception {
      HashSet<String> ignoreProjects = deployConfig.getIgnoredProjects();      
      ProjectUtils.deployDependencies(deployConfig,scope,ignoreProjects);
    }
    
    private boolean deployResource(DeployConfiguration deployConfig) throws IOException {
      MavenProject project = deployConfig.getMavenProject();
      String packaging = project.getPackaging();
      String serverWebappDir = deployConfig.getDeployWebappDir();
      if (packaging.equals("jar")) {
        File srcDirectory = new File(project.getBasedir() + "/src/java");
        if (!srcDirectory.exists()) return true;
        File portalDir = new File(deployConfig.getDefaultPortalDir());
        if (!portalDir.exists()) { 
          System.out.println("The directory" + portalDir + " is does not exists !");
          return false;
        }
        File destVelocityDir = new File(deployConfig.getDefaultPortalDir() + "/velocity");
        File destGroovyDir = new File(deployConfig.getDefaultPortalDir() + "/groovy");
        int denum = ProjectUtils.deployResourceFiles(srcDirectory, destVelocityDir, deployConfig.getIgnoredFiles(), ProjectUtils.velocityFiles());
        if (denum > 0)
          ProjectUtils.printMessage("copy", "copied " + denum + " file to directory " + destVelocityDir+ ".");
        int groovyCounter = ProjectUtils.deployResourceFiles(srcDirectory, destGroovyDir, deployConfig.getIgnoredFiles(), ProjectUtils.groovyFiles());
        if (groovyCounter > 0)
          ProjectUtils.printMessage("copy", "copied " + groovyCounter + " file to directory " + destGroovyDir+ ".");
      } else if (packaging.equals("exo-war") || packaging.equals("exo-portal") || packaging.equals("exo-portlet")) {
        File resourceDir = new File(serverWebappDir + "/" + project.getBuild().getFinalName());
        if (!resourceDir.exists()) {
          System.out.println("The directory" + resourceDir + " is does not exists !");
          return false;
        }
        File deployResourceDir = new File(project.getBasedir().toString() + "/src/webapp");
        if (deployResourceDir.exists()) {
          int counter = ExoFileUtils.copyDirectoryStructure(deployResourceDir, resourceDir, deployConfig.getIgnoredFiles(), false);
          if (counter > 0)
            ProjectUtils.printMessage("copy", "copied " + counter + " file to directory " + resourceDir + ".");
        } else {
          System.out.println("The directory" + deployResourceDir.toString() + " is does not exists !");
          return false;
        }
        if (packaging.equals("exo-portlet") ) {
          File srcDirectory = new File(project.getBasedir() + "/src/java");
          if (!srcDirectory.exists()) return true;
          if (!resourceDir.exists()) { 
            System.out.println("The directory" + resourceDir.getPath() + " is does not exists !");
            return false;
          }
          File destVelocityDir = new File(resourceDir.getPath() + "/velocity");
          File destGroovyDir = new File(resourceDir.getPath() + "/groovy");
          int denum = ProjectUtils.deployResourceFiles(srcDirectory, destVelocityDir, deployConfig.getIgnoredFiles(), ProjectUtils.velocityFiles());
          if (denum > 0)
            ProjectUtils.printMessage("copy", "copied " + denum + " file to directory " + destVelocityDir+ ".");
          int groovyCounter = ProjectUtils.deployResourceFiles(srcDirectory, destGroovyDir, deployConfig.getIgnoredFiles(), ProjectUtils.groovyFiles());
          if (groovyCounter > 0)
            ProjectUtils.printMessage("copy", "copied " + groovyCounter + " file to directory " + destGroovyDir+ ".");
        }
        return true;
      }
      return true;
    }
    private void printInfo() throws MojoExecutionException {
      String info = "The 'exo:module  maven plugin is used to deploy project and dependencies  to the working server.\n";
      System.out.println(info);
      throw new MojoExecutionException("");
    }
  }

  /**
   * This class create a new server to the working directory
   */
  static class NewServer extends DeployCommand {
    public void execute(DeployConfiguration dconfig) throws Exception {
      if (dconfig.isUseCleanServer()) {
        File deployServerDirectory = new File(dconfig.getDeployServerDir());
        File cleanServerDirectory = new File(dconfig.getCleanServerDir());
        if(deployServerDirectory.exists()) {
          ProjectUtils.printMessage("deleting", "  Deleting directory " + deployServerDirectory.getPath()) ;
          ExoFileUtils.deleteDirectory(deployServerDirectory);
        }
        ProjectUtils.printMessage("mkdir","  "+ deployServerDirectory.getPath() ) ;
        ProjectUtils.printMessage("copy","  Copy directory "+cleanServerDirectory.getName() +" to "+deployServerDirectory.getPath()) ;
        ExoFileUtils.copyDirectoryStructure(cleanServerDirectory,deployServerDirectory);
      }
    }
  }

  /**
   *  This classs is used to patch config to the deploy server dir .
   */
  static class PatchServerConfig extends DeployCommand {
    public void execute(DeployConfiguration dconfig) throws Exception {
      if (dconfig.isUseCleanServer()) {
        File deployServerDir = new File(dconfig.getDeployServerDir());    
        List<String> serverPatchs = dconfig.getServerPatchs();
        if (serverPatchs == null) {
          ProjectUtils.printMessage("info", " You do not specify custom server patch directories.............");
        } else {
          for (String customDir : serverPatchs ) {
            File customDirectory = new File(customDir);
            ExoFileUtils.copyDirectoryStructure(customDirectory, deployServerDir, dconfig.getIgnoredFiles(), true);
            ProjectUtils.printMessage("info"," Copy custom "+customDirectory.getName()+" directory to " + customDirectory.getPath());
          }
        }
      }
    }
  }
  
  /**
   *  Use for ExoDeployTomcat.This class is used portal.xml to ../conf/Catalina/localhost/
   */
  static class CreatePortalXml extends DeployCommand {
    public void execute(DeployConfiguration dconfig) throws Exception {
      MavenProject project = dconfig.getMavenProject();
      if (!dconfig.getMavenProject().getPackaging().equals("exo-product")) return ;
      Collection artifacts= project.getArtifacts();
      Iterator i = artifacts.iterator();
      while (i.hasNext()) {
        DefaultArtifact  da = (DefaultArtifact) i.next();
        String projectType = da.getType();
        if (projectType.equals("exo-portal")) {
          String deployTomcatDir = dconfig.getDeployServerDir();
          String contextName = da.getArtifactId();
          contextName = contextName.substring(da.getGroupId().length()+1,contextName.length()) ;
          if(contextName.indexOf(".") > 0) {
            contextName = contextName.substring(contextName.indexOf(".") + 1,contextName.length()) ;
          }
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
      }
    }
  }

  /**
   * Use for ExoDeployJboss.This classs is used to patch config to the deploy Jboss dir
   */
  static class PatchConfigJboss extends DeployCommand {
    public void execute(DeployConfiguration dconfig) throws Exception {
      File earDir = new File(dconfig.getDeployServerDir()+"/server/default/deploy/exoplatform.sar");
      String sharedResourcesDir = dconfig.getServerConfig();
      ExoFileUtils.copyDirectoryStructure(new File(sharedResourcesDir + "/exoplatform.sar"), earDir,dconfig.getIgnoredFiles()) ;
      ProjectUtils.printMessage("copy", "    Prepared sar configuration from " + earDir.getName());
    }
  }
  
  static class CreateManifest extends DeployCommand {
    public void execute(DeployConfiguration dconfig) throws Exception {
      StringBuilder b  = new StringBuilder() ;
      MavenProject project = dconfig.getMavenProject();
      File deployWebappDir = new File(dconfig.getDeployLibDir());
      File[] files = deployWebappDir.listFiles() ;
      for (File file : files) {
        if (file.getName().endsWith(".jar")) {
          b.append(file.getName()).append(' ');
        }
      }
      File warFile = new File(dconfig.getDeployWebappDir() + "/"  + 
                     project.getBuild().getFinalName() + ".war") ;
      String type = dconfig.getMavenProject().getPackaging() ;
      if ("exo-portal".equals(type) || "exo-war".equals(type) || "exo-portlet".equals(type)) {
        modifyManifest(warFile,b) ;
      } else if ("exo-product".equals(type)) {
        for(int i=0 ;i<files.length; i++) {
          if(files[i].getName().endsWith(".war")) {
            modifyManifest(files[i],b);
          }
        }
      }
    }
    
    private void modifyManifest(File warFile,StringBuilder b)  throws Exception {
      JarFile jar = new JarFile(warFile) ;
      Manifest mf = jar.getManifest() ;
      mf.getMainAttributes().putValue("Class-Path", b.toString()) ;
      File tmpFile = new File(warFile.getPath() + ".tmp") ;
      JarOutputStream jos = new JarOutputStream(new FileOutputStream(tmpFile), mf) ;
      Enumeration<JarEntry>  entries = jar.entries() ;
      while(entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement() ;
        byte[] buffer = new byte[1024];
        int bytesRead;
        InputStream entryStream = jar.getInputStream(entry);
        if(!entry.getName().endsWith("MANIFEST.MF")) {
          jos.putNextEntry(entry) ;
          while ((bytesRead = entryStream.read(buffer)) != -1) {
            jos.write(buffer, 0, bytesRead);
          }
        }
      }
      jar.close() ;
      jos.close() ;
      warFile.delete() ;
      tmpFile.renameTo(warFile) ;
    }
  }

  /**
   * Used for JOnAS or JBoss. This class is used to create aplication xml in a ear directory. 
   */ 
  static class CreateEarApplicationXml extends DeployCommand {
    public void execute(DeployConfiguration dconfig) throws Exception {
      if (dconfig.getMavenProject().getPackaging().equals("exo-product")) {
        File earDir = new File(dconfig.getDeployWebappDir());
        File metaInfDir = new File(earDir.getPath() + "/META-INF");
        if(!metaInfDir.exists())metaInfDir.mkdirs();
        StringBuilder b = new StringBuilder();
        b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        b.append("<!DOCTYPE application PUBLIC \"-//Sun Microsystems, Inc.//DTD J2EE Application 1.3//EN\" \"http://java.sun.com/dtd/application_1_3.dtd\">");
        b.append("\n<application>\n");
        b.append("  <display-name>exoplatform</display-name>\n");
        String[] file = earDir.list();
        for (int i = 0; i < file.length; i++) {
          if(file[i].endsWith("war")) {
            int idx = file[i].indexOf('.');
            String context = file[i].substring(0, idx);
            b.append("  <module>\n");
            b.append("    <web>\n");
            b.append("      <web-uri>").append(file[i]).append("</web-uri>\n");
            b.append("      <context-root>").append(context).append("</context-root>\n");
            b.append("    </web>\n");
            b.append("  </module>\n");
          }
          else if(file[i].endsWith("jar")) {
            b.append("  <module>\n");
            b.append("    <ejb>").append(file[i]).append("</ejb>\n");
            b.append("  </module>\n");
          }
          else if(file[i].endsWith("rar")) {
            b.append("  <module>\n");
            b.append("    <connector>").append(file[i]).append("</connector>\n");
            b.append("  </module>\n");
          }
        }
        b.append("</application>\n");
        FileOutputStream out = 
          new FileOutputStream(metaInfDir.getPath() + "/application.xml");
        out.write(b.toString().getBytes());
        out.close();
      }
    }
  }
  
  /**
   * Extracts the content of deployed web applications. This makes possible
   * to directly edit the configuration files, without extracting the archives.
   */
  static class ExplodeWars extends DeployCommand {
    public void execute(DeployConfiguration dconfig) throws Exception {
      Collection<Artifact> artifacts = 
        dconfig.getMavenProject().getArtifacts();
      
      for(Artifact artifact : artifacts) {
        String type = artifact.getType();
        // Select Portal archives. They contain customizable config files.
        if ("exo-portal".equals(type)) {
          String path =
            dconfig.getDeployWebappDir() + 
            '/' +
            ProjectUtils.getFinalName(artifact.getArtifactId(), "");
          String warPath = path + ".war";
          String tmpPath = path + ".tmp";
          ProjectUtils.printMessage("info",
                                    " Exploding war archive " + warPath);
          ExoFileUtils.mkdir(tmpPath);
          ExoFileUtils.extractZip(warPath, tmpPath);
          
          if(dconfig.getServerType().startsWith("tomcat")) {
            ExoFileUtils.rename(new File(tmpPath), new File(path));
          }
          else {
            ExoFileUtils.fileDelete(warPath);
            ExoFileUtils.rename(new File(tmpPath), new File(warPath));
          }
        }
      }
    }
  }
  
  // ====================== Extends ReleaseCommand Class ========================
  /**
   * This class is used to release eXo project 
   */
  static class ReleaseProject extends ReleaseCommand {
    public void execute(ReleaseConfiguration releaseConfig) throws Exception {
    File deployDirectory = new File(releaseConfig.getDeployDirectory());
    if (!deployDirectory.exists()) {
      System.out.println("   The directory" + deployDirectory.getPath() + " is does not exists !\n " +
                      "   You must deploy project before release it !");
      throw new MojoExecutionException("");
    }
    File releaseDirectory = new File(releaseConfig.getReleaseDirectory());
    if (!releaseDirectory.exists()) releaseDirectory.mkdirs();
    File releaseFile = new File(releaseConfig.getReleaseDirectory()+"/" +
                                releaseConfig.getReleaseFileName()+".zip");
    ExoFileUtils.addToArchive(deployDirectory, releaseFile);
    ProjectUtils.printMessage("archive", "  Created 1 file zip to " + releaseFile.getParent());
    }
  }
  /**
   *  This class is used to delete temp in tomcat server
   */
  static class DeleteTempInTomcat extends ReleaseCommand {
    public void execute(ReleaseConfiguration releaseConfig) throws Exception {
      File deployTomcatDir =new File(releaseConfig.getDeployDirectory());
      if (!deployTomcatDir.exists()) {
        System.out.println("    The directory" + deployTomcatDir.getPath() + " is does not exists !\n " +
                        "   You must deploy project before release it !");
        throw new MojoExecutionException("");
      }
      HashSet<File> dirContaintTemps = new HashSet<File>();
      dirContaintTemps.add(new File(deployTomcatDir.getPath() + "/work"));
      dirContaintTemps.add(new File(deployTomcatDir.getPath() + "/temp"));
      dirContaintTemps.add(new File(deployTomcatDir.getPath() + "/logs"));
      for (File dirContaintTemp : dirContaintTemps) {
        ExoFileUtils.deleteContentInDirectory(dirContaintTemp);
      }
      File webappDir = new File(deployTomcatDir+"/webapps");
      File[] files = webappDir.listFiles();
      for (int i=0; i<files.length; i++) {
        if (files[i].isDirectory() && ExoFileUtils.containFile(webappDir,files[i].getName()+".war")) 
          ExoFileUtils.deleteDirectory(files[i]);
      }
    }
  }
  /**
   * This class is used to release source code
   */
  static class ReleaseSrcCode extends ReleaseCommand {
    public void execute(ReleaseConfiguration releaseConfig) throws Exception {
      File sourceDirectory = new File(releaseConfig.getSourceDirectory());
      File releaseDirectory = new File(releaseConfig.getReleaseDirectory());
      if (!releaseDirectory.exists()) releaseDirectory.mkdirs();
      File releaseSrcFile = new File(releaseConfig.getReleaseDirectory()+"/" +
                                  releaseConfig.getReleaseSrcName()+".zip");
      ExoFileUtils.addToArchive(sourceDirectory, releaseSrcFile,releaseConfig.getIgnoredFiles());
      ProjectUtils.printMessage("archive", "  Created src file zip to " + releaseSrcFile.getParent()); 
    }
  }
}
