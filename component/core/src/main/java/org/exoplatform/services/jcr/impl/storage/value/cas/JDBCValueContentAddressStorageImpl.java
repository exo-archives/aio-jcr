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
package org.exoplatform.services.jcr.impl.storage.value.cas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS .<br/>
 * 
 * Stored CAS table in JDBC database.<br/>
 * 
 * NOTE! To make SQL commands compatible with possible ALL RDBMS we use objects names in <strong>!lowercase!</strong>.<br/>
 * 
 * Date: 18.07.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class JDBCValueContentAddressStorageImpl implements ValueContentAddressStorage {

  /**
   * JDBC DataSource name for lookup in JNDI.
   */
  public static final String JDBC_SOURCE_NAME_PARAM = "jdbc-source-name";

  /**
   * It's possible reassign VCAS table name with this parameter. For development purpose!
   */
  public static final String TABLE_NAME_PARAM       = "jdbc-table-name";

  /**
   * Default VCAS table name.
   */
  public static final String DEFAULT_TABLE_NAME     = "jcr_vcas";

  private static Log         LOG                    = ExoLogger.getLogger("jcr.JDBCValueContentAddressStorageImpl");

  protected DataSource       dataSource;

  protected String           tableName;

  protected String           sqlAddRecord;

  protected String           sqlDeleteRecord;

  protected String           sqlSelectRecord;

  protected String           sqlSelectRecords;

  protected String           sqlSelectOwnRecords;

  protected String           sqlSelectSharingProps;

  protected String           sqlConstraintPK;

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.storage.value.cas.ValueContentAddressStorage#init(java.util.Properties)
   */
  public void init(Properties props) throws RepositoryConfigurationException, VCASException {
    // init database metadata
    String tn = props.getProperty(TABLE_NAME_PARAM);
    if (tn != null)
      tableName = tn.toUpperCase().toLowerCase(); // lower case for postgres, for other seems not matter
    else
      tableName = DEFAULT_TABLE_NAME;

    sqlAddRecord = "INSERT INTO " + tableName + " (property_id, order_num, cas_id) VALUES(?,?,?)";
    sqlDeleteRecord = "DELETE FROM " + tableName + " WHERE property_id=?";
    sqlSelectRecord = "SELECT cas_id FROM " + tableName + " WHERE property_id=? AND order_num=?";
    sqlSelectRecords = "SELECT cas_id, order_num FROM " + tableName + " WHERE property_id=? ORDER BY order_num";

    // this script owrks ok if shared exists only
    //    sqlSelectOwnRecords =
    //        "SELECT DISTINCT OWN.cas_id, OWN.order_num FROM jcr_vcas_test OWN, jcr_vcas_test S, jcr_vcas_test P "
    //            + "WHERE OWN.property_id=P.property_id AND OWN.cas_id<>S.cas_id AND S.cas_id=P.cas_id AND S.property_id<>P.property_id AND P.property_id=? "
    //            + "ORDER BY OWN.order_num";
    sqlSelectOwnRecords =
        "SELECT P.cas_id, P.order_num, S.cas_id as shared_id "
            + "FROM jcr_vcas_test P LEFT JOIN jcr_vcas_test S ON P.property_id<>S.property_id AND P.cas_id=S.cas_id "
            + "WHERE P.property_id=? GROUP BY P.cas_id, P.order_num, S.cas_id ORDER BY P.order_num";

    sqlSelectSharingProps =
        "SELECT DISTINCT C.property_id AS property_id FROM " + tableName + " C, " + tableName + " P "
            + "WHERE C.cas_id=P.cas_id AND C.property_id<>P.property_id AND P.property_id=?";
    sqlConstraintPK = tableName + "_pk";

    // init database objects
    String sn = props.getProperty(JDBC_SOURCE_NAME_PARAM);
    if (sn != null) {
      try {
        dataSource = (DataSource) new InitialContext().lookup(sn);
        try {
          Connection con = dataSource.getConnection();
          try {
            ResultSet trs = con.getMetaData().getTables(null, null, tableName, null);
            // check if table already exists
            if (!trs.next()) {
              // create table
              con.createStatement().executeUpdate("CREATE TABLE " + tableName
                  + " (property_id VARCHAR(96) NOT NULL, order_num INTEGER NOT NULL, cas_id VARCHAR(512) NOT NULL, "
                  + "CONSTRAINT " + sqlConstraintPK + " PRIMARY KEY(property_id, order_num))");

              // create index on hash (CAS_ID)
              con.createStatement().executeUpdate("CREATE INDEX " + tableName + "_idx ON " + tableName
                  + "(cas_id, property_id, order_num)");

              LOG.info("JDBC Value Content Address Storage initialized in database " + sn);
            } else
              LOG.info("JDBC Value Content Address Storage already initialized in database " + sn);
          } finally {
            con.close();
          }
        } catch (SQLException e) {
          throw new VCASException("VCAS INIT database error: " + e, e);
        }
      } catch (final NamingException e) {
        throw new RepositoryConfigurationException("JDBC data source is not available in JNDI with name '" + sn + "'. Error: "
            + e);
      }
    } else
      throw new RepositoryConfigurationException(JDBC_SOURCE_NAME_PARAM + " parameter should be set");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.storage.value.cas.ValueContentAddressStorage#add(java.lang.String, int, java.lang.String)
   */
  public void add(String propertyId, int orderNum, String identifier) throws RecordAlreadyExistsException, VCASException {

    try {
      Connection con = dataSource.getConnection();
      try {
        PreparedStatement ps = con.prepareStatement(sqlAddRecord);
        ps.setString(1, propertyId);
        ps.setInt(2, orderNum);
        ps.setString(3, identifier);
        ps.executeUpdate();
        ps.close();
      } finally {
        con.close();
      }
    } catch (SQLException e) {
      if (e.toString().toUpperCase().toLowerCase().indexOf(sqlConstraintPK) >= 0)
        throw new RecordAlreadyExistsException("Record already exists. propertyId=" + propertyId + " orderNum=" + orderNum
            + ". Error: " + e, e);

      throw new VCASException("VCAS ADD database error: " + e, e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.storage.value.cas.ValueContentAddressStorage#delete(java.lang.String)
   */
  public void delete(String propertyId) throws RecordNotFoundException, VCASException {
    try {
      Connection con = dataSource.getConnection();
      try {
        PreparedStatement ps = con.prepareStatement(sqlDeleteRecord);
        ps.setString(1, propertyId);
        ps.executeUpdate();
        ps.close();
      } finally {
        con.close();
      }
    } catch (SQLException e) {
      throw new VCASException("VCAS DELETE database error: " + e, e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.storage.value.cas.ValueContentAddressStorage#getIdentifier(java.lang.String, int)
   */
  public String getIdentifier(String propertyId, int orderNum) throws RecordNotFoundException, VCASException {
    try {
      Connection con = dataSource.getConnection();
      try {
        PreparedStatement ps = con.prepareStatement(sqlSelectRecord);
        ps.setString(1, propertyId);
        ps.setInt(2, orderNum);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
          return rs.getString("cas_id");
        } else
          throw new RecordNotFoundException("No record found with propertyId=" + propertyId + " orderNum=" + orderNum);
      } finally {
        con.close();
      }
    } catch (SQLException e) {
      throw new VCASException("VCAS GET ID database error: " + e, e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.storage.value.cas.ValueContentAddressStorage#getIdentifiers(java.lang.String)
   */
  public List<String> getIdentifiers(String propertyId, boolean ownOnly) throws RecordNotFoundException, VCASException {
    try {
      Connection con = dataSource.getConnection();
      try {
        List<String> ids = new ArrayList<String>();
        PreparedStatement ps;
        
        if (ownOnly) {
          ps = con.prepareStatement(sqlSelectOwnRecords);
          ps.setString(1, propertyId);
          ResultSet rs = ps.executeQuery();
          if (rs.next()) {
            do {
              rs.getString("shared_id");
              if (rs.wasNull())
                ids.add(rs.getString("cas_id"));
            } while (rs.next());
            return ids;
          } else
            throw new RecordNotFoundException("No records found with propertyId=" + propertyId);
        } else {
          // TODO unused externaly feature (except tests)
          ps = con.prepareStatement(sqlSelectRecords);
          ps.setString(1, propertyId);
          ResultSet rs = ps.executeQuery();
          if (rs.next()) {
            do {
              ids.add(rs.getString("cas_id"));
            } while (rs.next());
            return ids;
          } else
            throw new RecordNotFoundException("No records found with propertyId=" + propertyId);
        }
      } finally {
        con.close();
      }
    } catch (SQLException e) {
      throw new VCASException("VCAS GET IDs database error: " + e, e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.storage.value.cas.ValueContentAddressStorage#hasSharedContent(java.lang.String)
   */
  public boolean hasSharedContent(String propertyId) throws RecordNotFoundException, VCASException {
    try {
      Connection con = dataSource.getConnection();
      try {
        PreparedStatement ps = con.prepareStatement(sqlSelectSharingProps);
        ps.setString(1, propertyId);
        return ps.executeQuery().next();
      } finally {
        con.close();
      }
    } catch (SQLException e) {
      throw new VCASException("VCAS HAS SHARED IDs database error: " + e, e);
    }
  }

}
