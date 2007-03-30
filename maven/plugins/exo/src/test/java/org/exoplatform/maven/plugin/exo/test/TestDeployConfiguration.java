/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.maven.plugin.exo.test;

import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.exoplatform.maven.plugin.exo.DeployConfiguration;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SARL Author : Phung Hai Nam phunghainam@gmail.com
 * Jan 3, 2006
 */
public class TestDeployConfiguration extends TestCase {
  public TestDeployConfiguration(String testname) {
    super(testname);
  }

  public void testGetIgnoreFiles() throws IOException {
    String ignoreFiles = "\\..*, .*target.*, .*\\.class, .*\\.svn.*" ;
    String svnDir = "path/to/a/.svn/cbdsfvc" ;
    String targetPattern = "path/to/target/some" ;
    String classPattern = "path/to/cdmsvnjfu/some.class" ;
    String acceptPattern = "path/to/cdnsc/some.java" ;
    String ignoreDotPatern = ".settings" ;
     
    DeployConfiguration dconfig = new DeployConfiguration();
    dconfig.setIgnoredFiles(ignoreFiles);
    assertTrue("Should ignore pattern: " + svnDir, isIgnore(dconfig.getIgnoredFiles(), svnDir)) ;
    assertTrue("Should ignore pattern: " + targetPattern, isIgnore(dconfig.getIgnoredFiles(), targetPattern)) ;
    assertTrue("Should ignore pattern: " + classPattern, isIgnore(dconfig.getIgnoredFiles(), classPattern)) ;
    assertTrue("Should accept pattern: " + classPattern, !isIgnore(dconfig.getIgnoredFiles(), acceptPattern)) ;
    assertTrue("Should ignore pattern: " + ignoreDotPatern, isIgnore(dconfig.getIgnoredFiles(), ignoreDotPatern)) ;
  }
  
  private boolean isIgnore(HashSet<Pattern> set, String input) {
    for(Pattern p : set) {
      Matcher m = p.matcher(input) ;
      if(m.matches()) return true;
    }  
    return false ;
  }
}
