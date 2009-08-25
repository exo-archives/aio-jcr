package org.exoplatform.services.jcr.impl.storage.jdbc.db;

import java.io.File;
import java.sql.SQLException;

import javax.jcr.RepositoryException;
import javax.sql.DataSource;

import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;
import org.exoplatform.services.jcr.storage.value.ValueStoragePluginProvider;

public class HSQLConnectionFactory extends GenericConnectionFactory {

	public HSQLConnectionFactory(DataSource dataSource, String containerName,
			boolean multiDb, ValueStoragePluginProvider valueStorageProvider,
			int maxBufferSize, File swapDirectory, FileCleaner swapCleaner) {

		super(dataSource, containerName, multiDb, valueStorageProvider,
				maxBufferSize, swapDirectory, swapCleaner);
	}

	public HSQLConnectionFactory(String dbDriver, String dbUrl,
			String dbUserName, String dbPassword, String containerName,
			boolean multiDb, ValueStoragePluginProvider valueStorageProvider,
			int maxBufferSize, File swapDirectory, FileCleaner swapCleaner)
			throws RepositoryException {

		super(dbDriver, dbUrl, dbUserName, dbPassword, containerName, multiDb,
				valueStorageProvider, maxBufferSize, swapDirectory, swapCleaner);
	}

	@Override
	public WorkspaceStorageConnection openConnection()
			throws RepositoryException {
		try {

			if (multiDb) {
				return new HSQLMultiDbJDBCConnection(getJdbcConnection(),
						containerName, valueStorageProvider, maxBufferSize,
						swapDirectory, swapCleaner);
			} 

			return new HSQLSingleDbJDBCConnection(getJdbcConnection(),
					containerName, valueStorageProvider, maxBufferSize,
					swapDirectory, swapCleaner);

		} catch (SQLException e) {
			throw new RepositoryException(e);
		}
	}
}
