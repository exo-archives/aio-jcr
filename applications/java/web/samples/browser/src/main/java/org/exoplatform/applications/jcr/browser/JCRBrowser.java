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
package org.exoplatform.applications.jcr.browser;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;

/**
 * Created by The eXo Platform SAS. <br/>
 * 
 * Date: 27.05.2008 <br/>
 * 
 * JavaBean for JCRBrowser sample application.<br/>
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: JCRBrowser.java 111 2008-11-11 11:11:11Z peterit $
 */
public class JCRBrowser {

  protected RepositoryService    repositoryService;

  protected ManageableRepository repository;

  protected Session              session;

  protected Node                 node;

  protected List<String>         errors = new ArrayList<String>();

  /**
   * Get browser repository.
   * 
   * @return the repository
   */
  public ManageableRepository getRepository() {
    return repository;
  }

  /**
   * Set browser repository.
   * 
   * @param repository
   *          the repository to set
   */
  public void setRepository(ManageableRepository repository) {
    this.repository = repository;
  }

  /**
   * Get browser JCR session.
   * 
   * @return the session
   */
  public Session getSession() {
    return session;
  }

  /**
   * Set browser JCR session.
   * 
   * @param session
   *          the session to set
   * @throws RepositoryException
   */
  public void setSession(Session session) throws RepositoryException {
    this.session = session;
    this.node = this.session.getRootNode();
  }

  /**
   * Get browser current node.
   * 
   * @return the node
   */
  public Node getNode() {
    return node;
  }

  /**
   * Set browser current node.
   * 
   * @param node
   *          the node to set
   */
  public void setNode(Node node) {
    this.node = node;
  }

  public void addError(Throwable error) {
    this.errors.add(error.toString());
  }

  public boolean isErrorsFound() {
    return this.errors.size() > 0;
  }

  public String[] getErrorsAndClean() {
    // StringBuilder msg = new StringBuilder();
    // msg.append("<div id='browserErrors' class='errors'>");
    // for (String e: this.errors) {
    // msg.append("&nbsp;&nbsp;&nbsp;&nbsp;");
    // msg.append(e);
    // msg.append("<br/>");
    // }
    // msg.append("</div>");
    // this.errors.clear();
    // return msg.toString();
    try {
      String[] errs = new String[this.errors.size()];
      this.errors.toArray(errs);
      return errs;
    } finally {
      this.errors.clear();
    }
  }

  /**
   * @return the repositoryService
   */
  public RepositoryService getRepositoryService() {
    return repositoryService;
  }

  /**
   * @param repositoryService
   *          the repositoryService to set
   */
  public void setRepositoryService(RepositoryService repositoryService) {
    this.repositoryService = repositoryService;
  }
}
