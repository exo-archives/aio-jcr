/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.jcr.backupconsole;


/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 
 *
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a> 
 * @version $Id: BackupClientImpl.java 111 2008-11-11 11:11:11Z serg $
 */
public class BackupClientImpl implements BackupClient{
  
  private ClientTransport transport; 
  
  public BackupClientImpl(ClientTransport transport){
    this.transport = transport;
  }
  
  public void restore(String pathToWS, String pathToBackup) {
    // TODO Auto-generated method stub
    
  }

  public void startBackUp(String pathToWS) {
    // TODO Auto-generated method stub
    
  }

  public void startIncrementalBackUp(String pathToWS, long incr, int jobnumber) {
    // TODO Auto-generated method stub
    
  }

  public void status(String pathToWS) {
    // TODO Auto-generated method stub
    
  }
  
  public void stop(String pathToWS) {
    // TODO Auto-generated method stub
    
  }

}
