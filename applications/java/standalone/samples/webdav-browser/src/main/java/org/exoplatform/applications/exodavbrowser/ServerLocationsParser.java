/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.applications.exodavbrowser;

/**
 * Created by The eXo Platform SARL
 * Author : Alex Reshetnyak
 *          alex_reshetnyak@yahoo.com
 * ${date}  
 */

public class ServerLocationsParser{
  private String sHost;
  private int iPort;
  private String sServerPath;
  
  public ServerLocationsParser(String ServerLocations){
    String sl = ServerLocations;
    
    sHost = sl.replaceAll("([hH][tT][tT][pP]://)|(/[-0-9a-zA-Z/]+)|(:[0-9]+)","");
    
    String temp =  sl.replaceAll("([hH][tT][tT][pP]://)|(/[-0-9a-zA-Z/]+)|(" + sHost + ")","");
    temp = temp.replaceAll(":","");
    iPort = Integer.valueOf(temp).intValue();
    
    sServerPath = sl.replaceAll("([hH][tT][tT][pP]://)|(" + sHost + ")|(:[0-9]+)","");    
  }
  
  public String getHost(){
    return sHost;
  }
  
  public String getServerPath(){
    return sServerPath;
  }
  
  public int getPort(){
    return iPort;
  }
}