/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.maven.plugin.exo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * Created by The eXo Platform SARL Author : Phung Hai Nam phunghainam@gmail.com
 * Dec 6, 2005
 */
public class ProjectUtils extends FileUtils {
  
  public static void deployProject(File dirJar, File dirWar, MavenProject project) throws Exception {
    DeployConfiguration deployConfig = new DeployConfiguration();
    deployConfig.setDeployLibDir(dirJar.getPath());
    deployConfig.setDeployWebappDir(dirWar.getPath());
    deployConfig.setMavenProject(project);
    deployProject(deployConfig);
  }
  
  public static void deployProject(DeployConfiguration deployConfig) throws Exception {
    MavenProject project= deployConfig.getMavenProject();
    String deployLibDir = deployConfig.getDeployLibDir() ;
    String deployWebappDir = deployConfig.getDeployWebappDir();
    File directoryJar = new File(deployLibDir);
    File directoryWar = new File(deployWebappDir);
    if (!directoryJar.getParentFile().exists()) {
      System.out.println("The directory " + directoryJar.getPath() + " does not exists !");
      return;
    } else if (directoryJar.getParentFile().exists()) directoryJar.mkdirs(); 
    if (!directoryWar.getParentFile().exists()) {
      System.out.println("The directory " + directoryWar.getPath() + " does not exists !");
      return;
    } else if (directoryWar.getParentFile().exists())  directoryWar.mkdirs();
    int counter = 0;
    deleteFileOnExist(directoryWar, project.getBuild().getFinalName());
    if (project.getPackaging().equals("jar")) {
      counter++;
      File moduleFile = new File(project.getBasedir().toString() + "/target/" + project.getArtifactId() + "-" + project.getVersion() + "." + project.getPackaging());
      copyFileToDirectory(moduleFile, directoryJar);
      printMessage("deploy", "  Deployed file '" + project.getArtifactId() + "' to " + directoryJar.getPath());
    } else if (project.getPackaging().equals("exo-war") || project.getPackaging().equals("exo-jcr")
        || project.getPackaging().equals("exo-portal")
        || project.getPackaging().equals("exo-portlet")) {
      File deployWar = new File(project.getBasedir().toString() + "/target/" + project.getBuild().getFinalName() + ".war");
      copyFileToDirectory(deployWar, directoryWar);
      printMessage("deploy", "  Deployed file '" + project.getArtifactId() + "' to " + directoryWar.getPath());
      counter++;
    }
    if (counter > 0) printMessage("deploy", "  DEPLOY : " + counter + " project");
  }
  
  public static void deployProject(File dirJar, File dirWar, MavenProject project, 
      String scope, HashSet<String> ignoreProjects) throws Exception {
    DeployConfiguration deployConfig = new DeployConfiguration();
    deployConfig.setDeployLibDir(dirJar.getPath());
    deployConfig.setDeployWebappDir(dirWar.getPath());
    deployConfig.setMavenProject(project);
    deployProject(deployConfig);
    deployDependencies(deployConfig,scope,ignoreProjects);
  }
  
  public static void deployDependencies(DeployConfiguration deployConfig, String scope, 
      HashSet<String> ignoreProjects) throws IOException {
    int counter = 0;
    MavenProject project= deployConfig.getMavenProject();
    String deployLibDir = deployConfig.getDeployLibDir() ;
    String deployWebappDir = deployConfig.getDeployWebappDir();
    File directoryJar = new File(deployLibDir);
    File directoryWar = new File(deployWebappDir);
    Collection artifacts = project.getArtifacts();
    List list = new ArrayList();
    list.addAll(artifacts);
    Collections.sort(list);
    Iterator i = list.iterator();
    while (i.hasNext()) {
      Artifact da = (Artifact) i.next();
      if (!da.getScope().equalsIgnoreCase("test") && !ignoreProjects.contains(da.getArtifactId())) {
        String projectType = da.getType();
        if (scope.equalsIgnoreCase("all") ||
            (!scope.equalsIgnoreCase("all") &&  da.getScope().equalsIgnoreCase(scope))) {
          if ("jar".equals(projectType)) {
            copyFileToDirectory(da.getFile(), directoryJar);
            printMessage("deploy", "  Deployed file '" + da.getArtifactId() + "' to " + directoryJar.getPath());
            counter++;
          } else if ("ejb".equals(projectType)) {
            String finalName = getFinalName(da.getArtifactId(), ".jar");
            copyFile(da.getFile(), new File(deployConfig.getDeployEjbDir() + '/' + finalName));
            printMessage("deploy", "  Deployed file '" + da.getArtifactId() + "' to " + deployConfig.getDeployEjbDir());
            counter ++;
          } else if ("rar".equals(projectType)) {
            String finalName = getFinalName(da.getArtifactId(), ".rar");
            // TODO [PN] 07.07.06 Fix it for servers where rar not currently used
            if (deployConfig.getServerType() != null && deployConfig.getServerType().indexOf("tomcat") >= 0) {
              // rar not for Tomcat
              printMessage("info", "  Deploy of projects of '" + projectType
                  + "' type on a server type '" + deployConfig.getServerType()
                  + "' is not provided. Dependency name '" + finalName + "'");
              continue;
            }
            copyFile(da.getFile(), new File(deployConfig.getDeployRarDir() + '/' + finalName));
            printMessage("deploy", "  Deployed file '" + da.getArtifactId() + "' to " + deployConfig.getDeployRarDir());
            counter ++;
          } else if (projectType.equals("exo-war") || projectType.equals("exo-portal") || 
                     projectType.equals("exo-portlet") || directoryWar != null) {
            String finalName = getFinalName(da.getArtifactId(), ".war");
            deleteFileOnExist(directoryWar, finalName.substring(0, finalName.lastIndexOf(".")));
            copyFileToDirectory(da.getFile(), directoryWar);
            counter++;
            File[] fileChild = directoryWar.listFiles();
            for (int j = 0; j < fileChild.length; j++) {
              if (fileChild[j].getName().equals(da.getFile().getName())) {
                File reFile = new File(fileChild[j].getParent() + "/" + finalName);
                rename(fileChild[j], reFile);
                printMessage("deploy", "  Deployed file '" + da.getArtifactId() + "' to " + directoryWar.getPath());
              }
            }
          }
        }
      }
    }
    if (counter > 0) printMessage("deploy", "  DEPLOY : " + counter + " dependencies");
  }
  
  public static int deployResourceFiles(File srcDir, File desDir, HashSet<Pattern> ignoreFiles,
      HashSet<String> copyFiles) throws IOException {
    if (!srcDir.exists())
      throw new IOException("Source directory doesn't exists (" + srcDir.getAbsolutePath() + ").");
    int counter = 0;
    File[] files = srcDir.listFiles();
    String sourcePath = srcDir.getAbsolutePath();
    for (int i = 0; i < files.length; i++) {
      File file = files[i];
      String dest = file.getAbsolutePath();
      dest = dest.substring(sourcePath.length() + 1); 
      File destination = new File(desDir, dest);
      if (file.isFile()) {
        if (copyFiles.contains(file.getName().substring(file.getName().lastIndexOf(".")))) {
          if (!ExoFileUtils.isIgnoredFile(ignoreFiles,file.getName())) {
            if (!destination.getParentFile().exists() && !destination.getParentFile().mkdirs()) 
              throw new IOException("Could not create destination directory '" + destination.getAbsolutePath() + "'.");
            if (file.lastModified() > destination.lastModified()) {
              destination = destination.getParentFile();
              ExoFileUtils.copyFileToDirectory(file, destination);
              counter++;
            }
          }
        }
      } else if (file.isDirectory()) 
        if (!ExoFileUtils.isIgnoredFile(ignoreFiles,file.getName())) 
          counter += deployResourceFiles(file, destination, ignoreFiles, copyFiles);
    }
    return counter;
  }
  
  private static void deleteFileOnExist(File parentDir, String childName) throws IOException {
    if (ExoFileUtils.containFile(parentDir, childName)) {
      File fileOld = new File(parentDir.getPath() + "/" + childName);
      printMessage("delete", "  Deleted file " + fileOld);
      forceDelete(fileOld);
    }
    if (ExoFileUtils.containFile(parentDir, childName + ".war")) {
      File fileOld = new File(parentDir.getPath() + "/" + childName + ".war");
      printMessage("delete", "  Deleted file " + fileOld);
      forceDelete(fileOld);
    }
  }
  
  public static HashSet<String> velocityFiles() {
    HashSet<String> velocity = new HashSet<String>();
    velocity.add(".vm");
    return velocity;
  }
  
  public static HashSet<String> groovyFiles() {
    HashSet<String> groovy = new HashSet<String>();
    groovy.add(".groovy");
    groovy.add(".gtmpl");
    return groovy ;
  }
  
  public static String getFinalName(String name, String extension) {
    int in = name.lastIndexOf(".");
    if (in >= 0)  name = name.substring(in + 1) + extension;
    else name = name + extension;
    return name;
  }
  
  public static void printMessage(String category, String message) {
    System.out.println("  [" + category + "]  " + message);
  }
}
