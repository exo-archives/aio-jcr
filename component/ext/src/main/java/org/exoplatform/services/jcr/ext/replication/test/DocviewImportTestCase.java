/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.test;

import java.io.File;
import java.io.FileInputStream;

import javax.jcr.ImportUUIDBehavior;

import org.exoplatform.services.jcr.RepositoryService;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 2010
 *
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a> 
 * @version $Id: DocviewImportTestCase.java 48669 2010-06-17 14:02:45Z rainf0x $
 */
public class DocviewImportTestCase extends BaseReplicationTestCase {

   /**
    * DeleteTestCase constructor.
    * 
    * @param repositoryService
    *          the RepositoryService.
    * @param reposytoryName
    *          the repository name
    * @param workspaceName
    *          the workspace name
    * @param userName
    *          the user name
    * @param password
    *          the password
    */
   public DocviewImportTestCase(RepositoryService repositoryService, 
                                String reposytoryName, 
                                String workspaceName,
                                String userName, 
                                String password) {
      super(repositoryService, reposytoryName, workspaceName, userName, password);
      log.info("DocviewImportTestCase inited");
   }

   /**
    * Import from xml.
    * 
    * @param repoPath
    *          repository path
    * @param nodeName
    *          node name
    * @return StringBuffer return the responds {'ok', 'fail'}
    */
   public StringBuffer docviewImport(String repoPath, String docviewPath) {
      StringBuffer sb = new StringBuffer();
      
      try {
         File f = new File(getNormalizePath(docviewPath));
                  
         session.importXML(getNormalizePath(repoPath), new FileInputStream(f), ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
         session.save();
                  
         sb.append("ok");
      } catch (Exception e) {
        log.error("Can't import data from : " + getNormalizePath(docviewPath), e);
        sb.append("fail");
      }

      return sb;
   }

}
