/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.maven2.plugin.exo.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.exoplatform.maven2.plugin.exo.DeployConfiguration;
import org.exoplatform.maven2.plugin.exo.ExoWarPackaging;
import org.exoplatform.maven2.plugin.exo.PackagingConfiguration;

/**
 * Created by The eXo Platform SARL Author : Phung Hai Nam phunghainam@gmail.com
 * Jan 3, 2006
 */
public class TestExoWarPackaging extends TestCase {
  public TestExoWarPackaging(String testname) {
    super(testname);
  }

  public void testTest() throws Exception {
    DeployConfiguration dconfig = new DeployConfiguration();
    PackagingConfiguration pconfig = new MockPackagingConfiguration();
    ExoWarPackaging ewp = new ExoWarPackaging();
    String basedir = System.getProperty("basedir");

    List<String> command = new ArrayList<String>() ;
    command.add("org/exoplatform/maven2/plugin/exo/test/TestGroovyCommand.groovy");
    command.add("org.exoplatform.maven2.plugin.exo.test.JavaCommand.class");
    ewp.setCommands(command);

    // Set value for dconfig
    dconfig.setDeployLibDir(basedir + "/target/deployLibDir");
    dconfig.setDeployWebappDir(basedir + "/target/deployWebappDir");
    ewp.setDeployConfiguration(dconfig);

    // Set value for pconfig
    pconfig.setOutputDirectory(basedir + "/target/outputDirecttory");
    pconfig.setOutputFileName("outputFileName");
    ewp.setPackagingConfiguration(pconfig);

    // Test execute() method of ExoWarPackaging
    ewp.execute();
  }
}
