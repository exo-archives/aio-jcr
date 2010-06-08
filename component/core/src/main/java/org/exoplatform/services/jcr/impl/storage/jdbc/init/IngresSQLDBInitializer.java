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
package org.exoplatform.services.jcr.impl.storage.jdbc.init;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by The eXo Platform SAS
 * 
 * 26.03.2007
 * 
 * Ingres convert all db object names to lower case, so respect it. Same as
 * PgSQL initializer.
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter
 *         Nedonosko</a>
 * @version $Id: IngresSQLDBInitializer.java 42817 2010-01-22 08:01:06Z tolusha
 *          $
 */
public class IngresSQLDBInitializer extends DBInitializer {

  public IngresSQLDBInitializer(String containerName,
                                Connection connection,
                                String scriptPath,
                                boolean multiDb) throws IOException {
    super(containerName, connection, scriptPath, multiDb);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean isIndexExists(Connection conn, String tableName, String indexName) throws SQLException {
    return super.isIndexExists(conn, tableName.toUpperCase().toLowerCase(), indexName.toUpperCase()
                                                                                     .toLowerCase());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean isTableExists(Connection conn, String tableName) throws SQLException {
    return super.isTableExists(conn, tableName.toUpperCase().toLowerCase());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean isSequenceExists(Connection conn, String sequenceName) throws SQLException {
    String seqName = sequenceName.toUpperCase().toLowerCase();
    Statement st = conn.createStatement();
    ResultSet srs = null;
    try {
      srs = st.executeQuery("SELECT NEXT VALUE FOR " + seqName);
      if (srs.next()) {
        return true;
      }
      return false;
    } catch (final SQLException e) {
      // check if sequence does not exist
      if (e.getMessage().indexOf("DEFINE CURSOR") >= 0 && e.getMessage().indexOf("Sequence") >= 0)
        return false;
      throw new SQLException(e.getMessage()) {

        /**
         * {@inheritDoc}
         */
        @Override
        public Throwable getCause() {
          return e;
        }
      };
    } finally {
      if (srs != null) {
        try {
          srs.close();
        } catch (SQLException e) {
          LOG.error("Can't close the ResultSet: " + e);
        }
      }

      try {
        st.close();
      } catch (SQLException e) {
        LOG.error("Can't close the Statement: " + e);
      }

    }
  }

}
