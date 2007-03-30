/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.maven.plugin.exo;

import java.util.List;
import java.util.Map;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan08@users.sourceforge.net
 * Jan 22, 2006
 */
public class ExecuteConfig {
  private String executeId ;
  private String scriptLocation 
    ;
  private Map<String, String> sysproperties ;
  private List<String>  classpaths ;
  private List<String>  scripts ;
  
  public String getExecuteId() {   return executeId; }
  public void setExecuteId(String excuteId) {  this.executeId = excuteId; }
  
  public List<String> getClasspaths() {  return classpaths; }
  public void setClasspaths(List<String> classpaths) {  this.classpaths = classpaths; }
  
  public String getScriptLocation() { return scriptLocation ; }
  public void   setScriptLocation(String s) { scriptLocation = s; }
  
  public List<String> getScripts() {  return scripts; }
  public void setScripts(List<String> scripts) {  this.scripts = scripts; }
  
  public Map<String, String> getSysproperties() {   return sysproperties; }
  public void setSysproperties(Map<String, String> props) {  this.sysproperties = props; }
}
