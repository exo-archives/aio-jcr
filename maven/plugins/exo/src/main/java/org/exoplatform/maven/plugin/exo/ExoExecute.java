/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.maven.plugin.exo;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Created by The eXo Platform SARL Author : Tuan Nguyen Nov 18, 2005
 * 
 * @goal execute
 * @requiresDependencyResolution runtime
 * @description mvn exo:execute
 */
public class ExoExecute extends AbstractMojo {
  /**
   * @parameter expression="${project}"
   * @required
   */
  protected MavenProject         project;
  /**
   * @parameter
   */
  private List<ExecuteConfig>  executeConfigs ;
  
  public void execute() throws MojoExecutionException {
    ExecuteConfig select  = null ;
    String selectId = System.getProperty("executeId") ;
    if(selectId == null) selectId = "default" ;
    for(ExecuteConfig config : executeConfigs) {
      if(config.getExecuteId().equals(selectId)) {
        select = config ;
        break ;
      }
    }
    if(select == null) {
      System.out.println("You need to specify the id  of  the execution or have an execution with 'default' as id") ;
      return ;
    }
    execute(select) ;
  }
  
  private void execute(ExecuteConfig config) throws MojoExecutionException {
    ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
    List<String> classpaths = config.getClasspaths() ;
    Map<String,String> sysproperties = config.getSysproperties() ;
    try {
      Set set = project.getArtifacts() ;
      Iterator itr = set.iterator() ;
      List<URL> urls = new ArrayList<URL>() ; 
      StringBuilder sysclasspath = new StringBuilder() ;
      String pathSeparator = System.getProperty("path.separator") ;
      sysclasspath.append(System.getProperty("java.class.path")) ;
      while(itr.hasNext()) {
        Artifact art = (Artifact) itr.next() ; 
        urls.add(new URL("file:" + art.getFile().getPath())) ;
        sysclasspath.append(pathSeparator).append(art.getFile().getPath()) ;
      }
      if(classpaths != null) {
        for(String path : classpaths)  {
          urls.add(new URL("file:" + path)) ;
          sysclasspath.append(pathSeparator).append(path) ;
        }
      }
      System.setProperty("java.class.path", sysclasspath.toString()) ;
      ClassLoader newCl = new URLClassLoader(urls.toArray(new URL[urls.size()]),oldCl) ;
      Thread.currentThread().setContextClassLoader(newCl) ;
      if(sysproperties != null && sysproperties.size() > 0) {
        for(Map.Entry<String,String> entry : sysproperties.entrySet()) {
          System.setProperty(entry.getKey(), entry.getValue()) ;
        }
      }
      for (String script : config.getScripts()) {
        if (script.endsWith(".class")) {
          String className = script.substring(0, script.indexOf(".class"));
          Object obj =  newCl.loadClass(className).newInstance();
          if(obj instanceof Runnable) ((Runnable)obj).run() ;
          else throw new Exception("Cannot execute : " + obj) ;
        } else if(script.endsWith(".groovy")) {
          File file = new File(config.getScriptLocation() + "/" + script) ;
          FileInputStream is = new FileInputStream(file) ;
          byte[] buf = new byte[is.available()] ;
          is.read(buf) ;
          Binding binding = new Binding();
          binding.setVariable("FileUtil", ExoFileUtils.class);
          binding.setVariable("project", project);
          GroovyShell shell = new GroovyShell(binding);
          shell.evaluate(new String(buf));
        } else {
          Binding binding = new Binding();
          binding.setVariable("FileUtil", ExoFileUtils.class);
          binding.setVariable("project", project);
          GroovyShell shell = new GroovyShell(binding);
          shell.evaluate(script);
        }
      }
    } catch (Exception ex) {
      throw new MojoExecutionException("FALSE", ex);
    } finally {
      Thread.currentThread().setContextClassLoader(oldCl) ;
    }
  }
}
