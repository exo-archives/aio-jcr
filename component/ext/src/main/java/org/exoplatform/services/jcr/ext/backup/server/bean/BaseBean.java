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
package org.exoplatform.services.jcr.ext.backup.server.bean;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 26.03.2009
 *
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a> 
 * @version $Id: BaseBeen.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public abstract class BaseBean {

  /**
   * The repository name.
   */
  private String  repositoryName;

  /**
   * The workspace name.
   */
  private String  workspaceName;
  
  /**
   * BaseBeen  constructor.
   * Empty constructor. 
   */
  public BaseBean() {
  }
  
  /**
   * BaseBeen  constructor.
   * 
   * @param repositoryName
   *          String, repository name
   * @param workspaceName
   *          String, workspace name
   */
  public BaseBean(String  repositoryName,
                  String  workspaceName) {
    this.repositoryName = repositoryName;
    this.workspaceName = workspaceName;
  }
  
  /**
   * getRepositoryName.
   *
   * @return String
   *           return the repository name
   */
  public String getRepositoryName() {
    return repositoryName;
  }

  /**
   * setRepositoryName.
   *
   * @param repositoryName
   *          String, repository name
   */
  public void setRepositoryName(String repositoryName) {
    this.repositoryName = repositoryName;
  }

  /**
   * getWorkspaceName.
   *
   * @return String
   *          return the workspace name
   */
  public String getWorkspaceName() {
    return workspaceName;
  }

  /**
   * setWorkspaceName.
   *
   * @param workspaceName
   *          String, the workspace name
   */
  public void setWorkspaceName(String workspaceName) {
    this.workspaceName = workspaceName;
  }
}
