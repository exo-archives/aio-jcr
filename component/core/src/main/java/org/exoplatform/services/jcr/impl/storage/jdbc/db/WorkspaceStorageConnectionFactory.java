package org.exoplatform.services.jcr.impl.storage.jdbc.db;

import java.sql.Connection;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;

/**
 * WorkspaceStorageConnectionFactory interface.
 * 
 * Describe methods contract of Workspace Connections Factory.
 * 
 */
public interface WorkspaceStorageConnectionFactory {

  /**
   * Open connection to Workspace storage.
   * 
   * @return WorkspaceStorageConnection connection
   * @throws RepositoryException
   *           if error occurs
   */
  WorkspaceStorageConnection openConnection() throws RepositoryException;

  /**
   * Open connection to Workspace storage.
   * 
   * @param readOnly
   *          boolean, if true the Connection will be marked as read-only
   * 
   * @return WorkspaceStorageConnection connection
   * @throws RepositoryException
   *           if error occurs
   */
  WorkspaceStorageConnection openConnection(boolean readOnly) throws RepositoryException;

  /**
   * Return native JDBC Connection to workspace storage (JDBC specific).
   * 
   * @return java.sql.Connection connection
   * @throws RepositoryException
   *           if error occurs
   */
  Connection getJdbcConnection() throws RepositoryException;

  /**
   * Return native JDBC Connection to workspace storage (JDBC specific).
   * 
   * @param readOnly
   *          boolean, if true the JDBC Connection will be marked as read-only, see
   *          {@link java.sql.Connection#setReadOnly(boolean)}
   * 
   * @return java.sql.Connection connection
   * @throws RepositoryException
   *           if error occurs
   */
  Connection getJdbcConnection(boolean readOnly) throws RepositoryException;
}
