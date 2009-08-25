package org.exoplatform.services.jcr.impl.storage.jdbc.db;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.storage.value.ValueStoragePluginProvider;

public class HSQLDBMultiDbJDBCConnection extends MultiDbJDBCConnection{
	
	public HSQLDBMultiDbJDBCConnection(Connection dbConnection,
			String containerName,
			ValueStoragePluginProvider valueStorageProvider, 
			int maxBufferSize,
			File swapDirectory, 
			FileCleaner swapCleaner) throws SQLException {
		super(dbConnection, containerName, valueStorageProvider, maxBufferSize,
				swapDirectory, swapCleaner);
	}
	
	@Override
	  protected void prepareQueries() throws SQLException {
		
		super.prepareQueries();
		FIND_PROPERTY_BY_NAME = "select V.DATA"
	        + " from JCR_MITEM I, JCR_MVALUE V"
	        + " where I.PARENT_ID=? and I.I_CLASS=2 and I.NAME=? and I.ID=V.PROPERTY_ID order by V.ORDER_NUM";
		FIND_NODES_BY_PARENTID = "select * from JCR_MITEM" + " where PARENT_ID=? and I_CLASS=1"
        + " order by N_ORDER_NUM";
		FIND_PROPERTIES_BY_PARENTID = "select * from JCR_MITEM" + " where PARENT_ID=? and I_CLASS=2"
        + " order by ID";
	}
}
