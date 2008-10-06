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
package org.exoplatform.services.jcr.impl.storage.jdbc.init;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by The eXo Platform SAS
 * 
 * 26.03.2007
 * 
 * PgSQL convert all db object names to lower case, so respect it.
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class PgSQLDBInitializer extends DBInitializer {

  public PgSQLDBInitializer(String containerName,
                            Connection connection,
                            String scriptPath,
                            boolean multiDb) throws IOException {
    super(containerName, connection, scriptPath, multiDb);
  }

  @Override
  protected boolean isIndexExists(Connection conn, String tableName, String indexName) throws SQLException {
    return super.isIndexExists(conn, tableName.toUpperCase().toLowerCase(), indexName.toUpperCase()
                                                                                     .toLowerCase());
  }

  @Override
  protected boolean isTableExists(Connection conn, String tableName) throws SQLException {
    return super.isTableExists(conn, tableName.toUpperCase().toLowerCase());
  }

}
