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

import java.io.IOException;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 
 *
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a> 
 * @version $Id: ClientTransport.java 111 2008-11-11 11:11:11Z serg $
 */
public interface ClientTransport {
  
  /**
   * Execute assigned sURL using current transport and return result as byte array.
   * 
   * @param sURL String form of URL to execute.
   * @param postData data for post request.
   * @return BackupAgentResponce result.
   * @throws IOException any transport exception.
   * @throws BackupExecuteException other internal exception.
   */
  BackupAgentResponse executePOST(String sURL, String postData) throws IOException, BackupExecuteException;
  
  /**
   * Execute assigned sURL using current transport and return result as byte array.
   * 
   * @param sURL String form of URL to execute, GET method.
   * @return BackupAgentResponce result.
   * @throws IOException any transport exception.
   * @throws BackupExecuteException other internal exception.
   */
  BackupAgentResponse executeGET(String sURL) throws IOException, BackupExecuteException;
}
