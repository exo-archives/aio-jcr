/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.storage.jdbc.init;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by The eXo Platform SARL
 *
 * 26.03.2007
 *
 * PgSQL convert all db object names to lower case, so respect it.
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: PgSQLDBInitializer.java 13869 2007-03-28 13:50:50Z peterit $
 */
public class PgSQLDBInitializer extends DBInitializer {

  public PgSQLDBInitializer(String containerName, Connection connection, String scriptPath, boolean multiDb) throws IOException {
    super(containerName, connection, scriptPath, multiDb);
  }

  @Override
  protected boolean isIndexExists(Connection conn, String tableName, String indexName)
      throws SQLException {
    return super.isIndexExists(conn, tableName.toUpperCase().toLowerCase(), indexName.toUpperCase().toLowerCase());
  }

  @Override
  protected boolean isTableExists(Connection conn, String tableName) throws SQLException {
    return super.isTableExists(conn, tableName.toUpperCase().toLowerCase());
  }
  
  
}
