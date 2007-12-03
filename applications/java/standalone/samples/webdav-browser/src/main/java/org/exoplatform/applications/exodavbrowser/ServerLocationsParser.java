/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.applications.exodavbrowser;

/**
 * Created by The eXo Platform SAS
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