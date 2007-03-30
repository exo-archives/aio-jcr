/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.maven2.plugin.exo;

import org.exoplatform.maven2.plugin.Utils;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

/**
 * Created by The eXo Platform SARL
 * Author : Phung Hai Nam
 *          phunghainam@gmail.com
 * Jan 3, 2006
 */
public class GroovyScriptCommand extends Command {
  private String script_ ;
  
  public GroovyScriptCommand(String script) throws Exception {
    script_ = script ;
  }
  
  public void execute(PackagingConfiguration pconfig, DeployConfiguration dconfig) throws Exception {
    Binding binding = new Binding();
    binding.setVariable("packagingConfig", pconfig);
    binding.setVariable("deployConfig", dconfig);
    binding.setVariable("FileUtils", Utils.class);
    GroovyShell shell = new GroovyShell(binding);
    shell.evaluate(script_);
  }
}
