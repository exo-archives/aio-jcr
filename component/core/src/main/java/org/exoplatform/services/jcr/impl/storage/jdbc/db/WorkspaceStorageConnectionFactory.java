package org.exoplatform.services.jcr.impl.storage.jdbc.db;

import java.sql.Connection;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;

public interface WorkspaceStorageConnectionFactory {

  WorkspaceStorageConnection openConnection() throws RepositoryException;
  
  Connection getJdbcConnection() throws RepositoryException;
}
