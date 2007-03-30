/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.maven2.plugin.exo;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.exoplatform.maven2.plugin.Utils;

/**
 * Created by The eXo Platform SARL Author : Phung Hai Nam phunghainam@gmail.com
 * Nov 23, 2005
 */
/**
 * @goal module
 * @requiresDependencyResolution runtime
 * @description deploy module or deploy resources
 */
public class ExoModule extends AbstractMojo {
  /**
   * @parameter
   * @required
   */
  private String       serverLibDir;

  /**
   * @parameter
   * @required
   */
  private String       serverWebappDir;

  /**
   * @parameter
   */
  private String       excludeProjects;

  /**
   * @parameter expression="${project}"
   * @required
   */
  private MavenProject project;

  /**
   * @parameter expression="${deploy}"
   */
  private String       deploy;

  public void execute() throws MojoExecutionException {
    try {
      if (deploy != null) {
        if (deploy.equals("all")) {
          deployModule(serverLibDir, serverWebappDir, project, true);
          return;
        } else if (deploy.equals("nodep")) {
          deployModule(serverLibDir, serverWebappDir, project, false);
          return;
        } else if (deploy.equals("resource")) {
          if (deployResource())
            return;
        } else if (!deploy.equals("resource") && !deploy.equals("all") && !deploy.equals("nodep"))
          getLog().info(
              "Invalid task 'exo:module'  The parameter -Ddeploy=" + deploy + " is invalid !\n"
                  + "      Goal exo:module -Ddeploy=" + deploy
                  + " does not exist in this project \n\n");
      }
    } catch (Exception e) {
    }
    ;
    printInfo();
  }

  public void deployModule(String libDir, String webappDir, MavenProject project, boolean deployAll)
      throws Exception {
    File directoryJar = new File(libDir);
    File directoryWar = new File(webappDir);
    HashSet<String> ignoreProjects = getIgnoreProjects();
    System.out
        .println("  ==========================================================================\n"
            + "                    DEPLOY MODULE  " + project.getArtifactId() + "              \n "
            + " ==========================================================================\n");

    Utils.deployProject(directoryJar, directoryWar, project, deployAll, ignoreProjects);
  }

  private boolean deployResource() throws IOException {
    String packaging = project.getPackaging();
    if (packaging.equals("jar")) {
      // nothing to do
    } else if (packaging.equals("war") || packaging.equals("exo-portal")
        || packaging.equals("exo-portlet")) {
      File resourceDir = new File(serverWebappDir + "/" + project.getBuild().getFinalName());
      if (!resourceDir.exists()) {
        getLog().info("The directory" + resourceDir + " is does not exists !");
        return false;
      }
      File deployResourceDir = new File(project.getBasedir().toString() + "/src/webapp");
      if (deployResourceDir.exists()) {
        int counter = Utils.copyDirectoryStructure(deployResourceDir, resourceDir, Utils
            .getDefaultIgnoreFiles(), true);
        if (counter > 0)
          Utils.printMessage("copy", "copied " + counter + " file to directory " + resourceDir
              + ".");
      } else {
        getLog().info("The directory" + deployResourceDir.toString() + " is does not exists !");
        return false;
      }
      if (packaging.equals("exo-portlet")) {
        File srcVelocityDir = new File(project.getBasedir() + "/src/java");
        File destVelocityDir = new File(resourceDir.getPath() + "/velocity");
        if (!srcVelocityDir.exists())
          return true;
        if (!destVelocityDir.exists()) {
          destVelocityDir.mkdirs();
        }
        int denum = deployResourceFiles(srcVelocityDir, destVelocityDir, Utils
            .getDefaultIgnoreFiles(), copyFiles());
        if (denum > 0)
          Utils.printMessage("copy", "copied " + denum + " file to directory " + destVelocityDir
              + ".");
      }
      return true;
    }
    return true;
  }

  private int deployResourceFiles(File srcDir, File desDir, HashSet<String> ignoreFiles,
      HashSet<String> copyFiles) throws IOException {
    if (!srcDir.exists()) {
      throw new IOException("Source directory doesn't exists (" + srcDir.getAbsolutePath() + ").");
    }
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
          if (!ignoreFiles.contains(file.getName())) {
            if (!destination.getParentFile().exists() && !destination.getParentFile().mkdirs()) {
              throw new IOException("Could not create destination directory '"
                  + destination.getAbsolutePath() + "'.");
            }
            if (file.lastModified() > destination.lastModified()) {
              destination = destination.getParentFile();
              Utils.copyFileToDirectory(file, destination);
              counter++;
            }
          }
        }
      } else if (file.isDirectory()) {
        if (!ignoreFiles.contains(file.getName())) {
          counter += deployResourceFiles(file, destination, ignoreFiles, copyFiles);
        }
      }
    }
    return counter;
  }

  private HashSet<String> copyFiles() {
    HashSet<String> copyFiles = new HashSet<String>();
    copyFiles.add(".vm");
    copyFiles.add(".gtmpl");
    return copyFiles;
  }

  protected HashSet<String> getIgnoreProjects() {
    HashSet<String> ignoreProjects = new HashSet<String>();
    if (excludeProjects != null) {
      String[] pro = excludeProjects.split(",");
      for (String s : pro)
        ignoreProjects.add(s.trim());
    }
    return ignoreProjects;
  }

  private void printInfo() throws MojoExecutionException {
    String info = "The 'exo:module  maven2 plugin is used to deploy a single module to the working server.\n"
        + "To run the command successfully you need to run mvn install  before running the command"
        + "mvn exo:module -Dparam=value\n\n"
        + "The valid syntax are:\n"
        + "*  mvn exo:module -Ddeploy=all'\n"
        + "   This command  copies  your module  and the dependency modules to the working server\n"
        + "*  mvn exo:module -Ddeploy=nodep'\n"
        + "   This command  copies  only your module to the working server\n"
        + "*  mvn exo:module -Ddeploy=resource'\n"
        + "   This command  copies  only the static resources of  your  module to the running server.\n"
        + "   It is useful  when  you modify a vm template  or css and want to depploy it to\n"
        + "   the running server  without  restarting it";
    System.out.println(info);
    throw new MojoExecutionException("");
  }
}
