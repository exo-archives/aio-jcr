/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.exoplatform.services.jcr.rmi.api.client.ClientRepositoryFactory;

/**
 * Created by The eXo Platform SARL
 * 
 * @Author : Sergey Kabashnyuk
 */
public class RMITestHelper {
  public static final String STUB_IMPL_PROPS = "repositoryStubImpl.properties";

  public static final String PROP_PREFIX = "javax.jcr.tck";

  public static final String STUB_IMPL_SYS_PROPS = PROP_PREFIX + ".properties";

  public static final String PROP_STUB_IMPL_CLASS = PROP_PREFIX + ".repository_stub_impl";

  public static final String PROP_SUPERUSER_PWD = "superuser.pwd";

  public static final String PROP_SUPERUSER_NAME = "superuser.name";

  public static final String PROP_READONLY_PWD = "readonly.pwd";

  public static final String PROP_READONLY_NAME = "readonly.name";

  public static final String PROP_READWRITE_PWD = "readwrite.pwd";

  public static final String PROP_READWRITE_NAME = "readwrite.name";

  public static final String PROP_NODETYPE = "nodetype";

  public static final String PROP_TESTROOT = "testroot";

  public static final String PROP_NODE_NAME1 = "nodename1";

  public static final String PROP_NODE_NAME2 = "nodename2";

  public static final String PROP_NODE_NAME3 = "nodename3";

  public static final String PROP_NODE_NAME4 = "nodename4";

  public static final String PROP_PROP_NAME1 = "propertyname1";

  public static final String PROP_PROP_NAME2 = "propertyname2";

  public static final String PROP_WORKSPACE_NAME = "workspacename";

  public static final String PROP_NAMESPACES = "namespaces";

  /**
   * Client repository factory
   */
  private ClientRepositoryFactory factory;

  /**
   * The repository instance
   */
  private Repository              repository;

  /**
   * Repository session
   */
  private Session                 session;

  /**
   * workspace name
   */
  private String                  workspace;

  /**
   * url to remote repository
   */
  private String                  url;

  /**
   * root node;
   */
  protected Node                  rootNode;

  /**
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * @param url the url to set
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * @return the factory
   */
  public ClientRepositoryFactory getFactory() {
    return factory;
  }

  /**
   * @return the repository
   */
  public Repository getRepository() {
    return repository;
  }

  /**
   * @return the session
   */
  public Session getSession() {
    return session;
  }

  /**
   * @return the workspace
   */
  public String getWorkspace() {
    return workspace;
  }

  /**
   * @param workspace the workspace to set
   */
  public void setWorkspace(String workspace) {
    this.workspace = workspace;
  }

  /**
   * @param factory the factory to set
   */
  public void setFactory(ClientRepositoryFactory factory) {
    this.factory = factory;
  }

  /**
   * @param repository the repository to set
   */
  public void setRepository(Repository repository) {
    this.repository = repository;
  }

  /**
   * @param session the session to set
   */
  public void setSession(Session session) {
    this.session = session;
  }

  public void login(String login, String password) throws ClassCastException,
      MalformedURLException, RemoteException, NotBoundException, LoginException,
      NoSuchWorkspaceException, RepositoryException {
    // Creating factory
    factory = new ClientRepositoryFactory();
    // geting repository
    repository = factory.getRepository(getUrl());

    Credentials credentials = new SimpleCredentials(login, password.toCharArray());
    // login to remote repository
    session = repository.login(credentials, getWorkspace());

  }

  /**
   * @return the rootNode
   * @throws RepositoryException 
   */
  public Node getRootNode() throws RepositoryException {
    return session.getRootNode();
  }
}
