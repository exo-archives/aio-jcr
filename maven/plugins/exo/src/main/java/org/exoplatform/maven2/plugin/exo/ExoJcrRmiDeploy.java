/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.maven2.plugin.exo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.util.HashSet;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.exoplatform.maven2.plugin.Utils;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.codehaus.plexus.archiver.ear.EarArchiver;
import org.codehaus.plexus.archiver.jar.JarArchiver;
/**
 * Created by The eXo Platform SARL
 * Author : Roman Pedchenko
 *          lautarul@gmail.com
 */
/**
 * @goal jcrrmi
 * @requiresDependencyResolution runtime
 * @description mvn exo:jcrrmi -Ddest=param to create JCR RMI standalone server
 */
public class ExoJcrRmiDeploy extends AbstractMojo {
  /**
   * @parameter
   * @required
   */
  private String runDir;
  /**
   * @parameter
   * @required
   */
  private String resDir;
  /**
   * @parameter expression="${dest}"
   */
  private String dest;
  /**
   * @parameter
   */
  private String excludeProjects;
  /**
   * @parameter
   */
  private String codebase;
  /**
   * @parameter expression="${project}"
   * @required
   */
  protected MavenProject project;

  public void execute() throws MojoExecutionException{
    if(!"exo-jcrrmi".equals(project.getPackaging())) printInfo();
    try {
      if (dest != null) {
        execDeploy();
        return;
      }
    }catch (Exception e) {
      e.printStackTrace();
    }
    printInfo();
  }

  protected HashSet<String> getIgnoreProjects() {
    HashSet<String> ignoreProjects = new HashSet<String>();
    if(excludeProjects != null) {
      String[] pro =  excludeProjects.split(",");
      for(String s : pro) ignoreProjects.add(s.trim());
    }
    return ignoreProjects;
  }

  protected HashSet<String> getCodeBase() {
    HashSet<String> codeBase = new HashSet<String>();
    if(codeBase != null) {
      String[] pro =  codebase.split(",");
      for(String s : pro) codeBase.add(s.trim());
    }
    return codeBase;
  }

/*  protected void makeRmiJar(HashSet<String> ignoreProjects) throws Exception {
    File utilsDir = new File(sharedResourcesDir);
    File destDir = new File(sharedDir);
    File jarFile = new File(project.getBasedir().toString() + "/target/"
            + project.getBuild().getFinalName() + ".jar");
    File manFile = new File(project.getBasedir().toString() + "/target/"
            + "MANIFEST.MF");

    jarArchiver.setDestFile(jarFile);

    Utils.createManifest(manFile, project, ignoreProjects);
    jarArchiver.setManifest(manFile);

    Collection artifacts = project.getArtifacts();
    List list = new ArrayList();
    list.addAll(artifacts);
    Collections.sort(list);
    Iterator i = list.iterator();
    while (i.hasNext()) {
      Artifact da = (Artifact) i.next();
      if (!da.getScope().equalsIgnoreCase("test") && !ignoreProjects.contains(da.getArtifactId())) {
        String projectType = da.getType();
        if ("jar".equals(projectType)) {
          jarArchiver.addFile(da.getFile(), da.getFile().getName());
          Utils.printMessage("add", "  Added file '" + da.getFile().getName() + "' to jar");
        }
      }
    }
    jarArchiver.createArchive();

    Utils.printMessage("copy", "  Copying jar '" + jarFile.getAbsolutePath() + "' to '" + destDir.getAbsolutePath() + "'");
    Utils.copyFileToDirectory(jarFile, destDir);
    Utils.patchConfig2(utilsDir, destDir);
  }
*/

  public void createBatch(File dir, String cp) throws IOException {
    StringBuilder b = new StringBuilder();
    b.append("@echo off\n");
    b.append("set dbin=%~dp0\n");
    b.append("if \"%OS%\"==\"Windows_NT\" GOTO setcodebase\n");
    b.append("if \"%OS%\"==\"WINNT\" GOTO setcodebase\n");
	b.append("echo [WARN] Can not set codebase variable. Try to set codebase variable = absolute path to run/cb folder\n");
	b.append(":setcodebase\n");
	b.append("set dbin=%~dp0\n");
	b.append("set codebase=%dbin%cb\\\n");
    b.append(cp + "\n");
    b.append("java -Djava.security.auth.login.config=jaas.conf -Djava.rmi.server.codebase=file:/%codebase% -Djava.security.policy=java.policy org.exoplatform.connectors.jcr.RMIStarter bind configuration.xml\n");
    FileOutputStream out = new FileOutputStream(dir.getAbsolutePath() + "/run.bat");
    out.write(b.toString().getBytes());
    out.close();
  }

  public void extractJar(File dir, File file) throws IOException {
    Utils.printMessage("extract", "  Extracting jar '" + file.getName() + "'");
    File tempJarFile = new File(file.getAbsolutePath() + ".tmp");
    JarFile jar = new JarFile(file);

    byte[] buffer = new byte[1024];
    int bytesRead;

    for (Enumeration entries = jar.entries(); entries.hasMoreElements();) {
      JarEntry entry = (JarEntry) entries.nextElement();
      if (!entry.getName().startsWith("META-INF/") && !entry.isDirectory()) {
        InputStream entryStream = jar.getInputStream(entry);
        File outFile = new File(dir.getAbsolutePath() + "/" + entry.getName());
        outFile.getParentFile().mkdirs();
        FileOutputStream out = new FileOutputStream(outFile);
        while ((bytesRead = entryStream.read(buffer)) != -1) {
          out.write(buffer, 0, bytesRead);
        }
        out.close();
      }
    }

    jar.close();
  }

  public void createCodebase(File dir, List cbas) throws IOException {
    int j = 0;
    Iterator i = cbas.iterator();
    while (i.hasNext()) {
      File jar = (File) i.next();
      extractJar(dir, jar);
      j++;
    }
    Utils.printMessage("info", "  " + j + " jars extracted");
  }

  protected void makeRmiDir() throws Exception {
    File _runDir = new File(runDir);
    File _resDir = new File(resDir);
    File _libDir = new File(runDir + "/lib");
    File _cbDir = new File(runDir + "/cb");
    _libDir.mkdirs();
    _cbDir.mkdirs();
    HashSet<String> ignoreProjects = getIgnoreProjects();
    HashSet<String> codeBase = getCodeBase();

    String classpath = "set CLASSPATH=%CLASSPATH%;.";
    ArrayList cbas = new ArrayList();

    Collection artifacts = project.getArtifacts();
    List list = new ArrayList();
    list.addAll(artifacts);
    Collections.sort(list);
    Iterator i = list.iterator();
    while (i.hasNext()) {
      Artifact da = (Artifact) i.next();
      if (!da.getScope().equalsIgnoreCase("test") && !ignoreProjects.contains(da.getArtifactId())) {
        String projectType = da.getType();
        if ("jar".equals(projectType)) {
          classpath = classpath + ";lib/" + da.getFile().getName();
          Utils.copyFileToDirectory(da.getFile(), _libDir);
          Utils.printMessage("copy", "  Copied file '" + da.getFile().getName() + "' to lib dir");
          if (codeBase.contains(da.getArtifactId()))
            cbas.add(da.getFile());
        }
      }
    }

    Utils.patchConfig2(_resDir, _runDir);
    Utils.printMessage("create", "  Creating batch file 'run.bat' in '" + _runDir.getAbsolutePath() + "'");
    createBatch(_runDir, classpath);
    Utils.printMessage("create", "  Creating codebase in '" + _cbDir.getAbsolutePath() + "'");
    createCodebase(_cbDir, cbas);
  }

  private void execDeploy() throws Exception {
    if(dest.equals("rmisrv")) {
//      makeRmiJar(getIgnoreProjects());
      makeRmiDir();
    } else {
      Utils.printMessage("info","The task 'exo:jcrrmi -Ddest=" + dest + "' is invalid !\n");
      printInfo();
    }
  }

  private void printInfo() throws MojoExecutionException {
    String info =
      "The valid syntax is:\n" +
      "  mvn exo:jcrrmi -Ddest=rmisrv\n";
    System.out.println(info);
    throw new MojoExecutionException("");
  }
}
